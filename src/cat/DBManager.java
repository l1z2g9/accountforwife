package cat;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.table.TableModel;

import cat.model.Item;
import cat.vo.StatItem;

public class DBManager {
	static Logger log = Logger.getLogger("Util");

	static DecimalFormat df = new DecimalFormat("##.##");

	static Connection conn = null;
	static {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:Account.db");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 统计页使用,读取当日的所有收支情况.
	 * 
	 * @param date
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Vector<Vector> getItemsByDate(String date) {
		Vector<Vector> result = new Vector<Vector>();
		try {
			String sql = "SELECT ID, Type, Date, Item, Money, Remark, Color FROM Account WHERE Date = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, date);
			ResultSet rs = ps.executeQuery();
			assemble(result, rs);
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static void assemble(Vector<Vector> result, ResultSet rs) {
		try {
			while (rs.next()) {
				Vector vo = new Vector();
				vo.addElement(rs.getInt(1));
				vo.addElement(rs.getString(2));
				vo.addElement(rs.getString(3));
				vo.addElement(rs.getString(4));
				vo.addElement(df.format(rs.getFloat(5)));
				vo.addElement(rs.getString(6));
				vo.addElement(rs.getString(7));
				result.add(vo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ---------- start 统计 ------------
	@SuppressWarnings("unchecked")
	public static Vector<Vector> getItemsBetweenDates(String fromDate,
			String toDate) {
		Vector<Vector> result = new Vector<Vector>();
		try {
			String sql = "SELECT ID, Type, Date, Item, Money, Remark, Color FROM Account WHERE Date BETWEEN ? AND ? ORDER BY Date";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, fromDate);
			ps.setString(2, toDate);
			ResultSet rs = ps.executeQuery();
			assemble(result, rs);
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// ---------- end 统计 ------------

	@SuppressWarnings("unchecked")
	public static Vector<Vector> getCategory(String type) {
		String sql = "SELECT ID, Name Item FROM Category WHERE Type = ?";

		Vector<Vector> result = new Vector<Vector>();
		try {

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, type);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Vector vo = new Vector();
				vo.addElement(rs.getInt(1));
				vo.addElement(rs.getString(2));
				result.add(vo);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static Vector<String> getSubCategory(int id) {
		String sql = "SELECT Name Item FROM Category WHERE ParentID = ?";

		Vector<String> result = new Vector<String>();
		try {

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, 1);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				result.addElement(rs.getString(1));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 某一天的金额发生变化，可能增加，可能减少，这样可能导致当月超支，或者超支的地方变成没有超支，所以需要同步更新当月的所有预算背景色。
	 * 
	 * @param date
	 */
	private static void refreshBudgetColor(String date) {
		String fromDate = date.substring(0, date.length() - 2) + "01";
		String year = date.substring(0, 4);
		String month = date.substring(5, 7);
		// log.info("month: " + month);
		String lastDay = "30";

		String toDate = date.substring(0, date.length() - 2) + lastDay;
		// float payout = queryPayoutTotal(fromDate, toDate, "支出");
		Vector<Vector> items = getItemsBetweenDates(fromDate, toDate);

		int[] budgetColor = getBudgetColor();

		int percent75color = budgetColor[0];
		int percent90color = budgetColor[1];

		//    
		Map<String, Float> itemSum = new HashMap<String, Float>();
		Map<Integer, Integer> normal = new HashMap<Integer, Integer>();
		Map<Integer, Integer> percent75 = new HashMap<Integer, Integer>();
		Map<Integer, Integer> percent90 = new HashMap<Integer, Integer>();
		Map<Integer, Integer> over_budget = new HashMap<Integer, Integer>();
		for (Vector item : items) {
			String i = (String) item.get(1);
			if (!"支出".equalsIgnoreCase(i)) {
				continue;
			}

			int id = (Integer) item.get(0);
			String _item = (String) item.get(3);
			int budget = getBudget(year, month);
			if (budget == 0) {
				continue;
			}

			float sum = Float.valueOf(item.get(4).toString());
			if (itemSum.containsKey(_item)) {
				sum = itemSum.get(_item) + sum;
			}
			itemSum.put(_item, sum);

			if (budget * 0.75 > sum) {
				normal.put(id, Color.WHITE.getRGB());
			} else if (budget * 0.75 < sum && budget * 0.9 >= sum) {
				percent75.put(id, percent75color);
			} else if (budget * 0.9 < sum && budget >= sum) {
				percent90.put(id, percent90color);
			} else {
				over_budget.put(id, Color.RED.getRGB());
			}
		}
		try {
			conn.setAutoCommit(false);
			PreparedStatement ps = conn
					.prepareStatement("UPDATE Account SET Color = ? WHERE ID = ?");
			for (int id : normal.keySet()) {
				ps.setInt(1, normal.get(id));
				ps.setInt(2, id);
				ps.executeUpdate();
			}

			for (int id : percent75.keySet()) {
				ps.setInt(1, percent75.get(id));
				ps.setInt(2, id);
				ps.executeUpdate();
			}

			for (int id : percent90.keySet()) {
				ps.setInt(1, percent90.get(id));
				ps.setInt(2, id);
				ps.executeUpdate();
			}

			for (int id : over_budget.keySet()) {
				ps.setInt(1, over_budget.get(id));
				ps.setInt(2, id);
				ps.executeUpdate();
			}

			conn.commit();
			ps.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertItem(Item item) {
		try {
			String sql = "insert into Item(Title, Date, Money, CategoryID, Remark, User, Address) values(?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, item.getTitle());
			ps.setDate(2, item.getDate());
			ps.setFloat(3, item.getMoney());
			ps.setFloat(4, item.getCategoryID());
			ps.setString(5, item.getRemark());
			ps.setString(6, item.getUser());
			ps.setString(7, item.getAddress());

			ps.executeUpdate();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateItem(Item item) {
		String sql = "update Item set Title =?, Date = ?, Money = ?, CategoryID = ?, Remark = ?, User = ?, Address = ? where ID = ?";
		log.info(sql);
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, item.getTitle());
			ps.setDate(2, item.getDate());
			ps.setFloat(3, item.getMoney());
			ps.setInt(4, item.getCategoryID());
			ps.setString(5, item.getRemark());
			ps.setString(6, item.getUser());
			ps.setString(7, item.getAddress());
			ps.setInt(7, item.getId());

			ps.executeUpdate();
			ps.close();

			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteItem(int id) {
		String sql = "delete from Item where id = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Vector<String> getPayoutItems() {
		return getItems("支出");
	}

	public static Vector<String> getIncomeItems() {
		return getItems("收入");
	}

	private static Vector<String> getItems(String type) {
		Vector<String> v = new Vector<String>();
		try {
			String sql = "select Name from Category where type = ?";

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, type);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				v.addElement(rs.getString(1));
			}
			rs.close();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return v;
	}

	private static void addList(String date, String type, float money,
			Map<String, StatItem> dateMap) {
		StatItem stat = null;
		if (dateMap.containsKey(date)) {
			stat = dateMap.get(date);
			if ("支出".equalsIgnoreCase(type)) {
				stat.setPayout(money);
			} else {
				stat.setIncome(money);
			}
		} else {
			stat = new StatItem();
			dateMap.put(date, stat);
			stat.setDate(date);
			if ("支出".equalsIgnoreCase(type)) {
				stat.setPayout(money);
			} else {
				stat.setIncome(money);
			}
		}
	}

	public static float queryPayoutTotal(String fromDate, String toDate,
			String type) {
		String sql = "SELECT sum(Money) FROM Account WHERE date BETWEEN ? AND ? and Type = ?";
		float total = 0;
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, fromDate);
			ps.setString(2, toDate);
			ps.setString(3, type);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				total = rs.getFloat(1);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return total;
	}

	public static Vector<Vector<String>> query(String fromDate, String toDate) {
		Map<String, StatItem> dateMap = new TreeMap<String, StatItem>();
		try {
			String sql = "SELECT Date, Type, sum(Money) FROM Account WHERE date BETWEEN ? AND ? and Type = ? GROUP BY Date";

			PreparedStatement ps = conn.prepareStatement(sql);

			ps.setString(1, fromDate);
			ps.setString(2, toDate);
			ps.setString(3, "支出");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String date = rs.getString(1);
				String type = rs.getString(2);
				float money = rs.getFloat(3);
				addList(date, type, money, dateMap);
			}
			rs.close();

			ps.setString(3, "收入");
			rs = ps.executeQuery();
			while (rs.next()) {
				String date = rs.getString(1);
				String type = rs.getString(2);
				float money = rs.getFloat(3);
				addList(date, type, money, dateMap);
			}

			rs.close();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		Vector<Vector<String>> list = new Vector<Vector<String>>();

		for (StatItem stat : dateMap.values()) {
			Vector<String> v = new Vector<String>();
			v.addElement(stat.getDate());
			v.addElement(df.format(stat.getPayout()));
			v.addElement(df.format(stat.getIncome()));
			list.add(v);
		}
		return list;
	}

	public static void releaseConnection() {
		try {
			log.info("释放数据库连接");
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void saveSource(String type, String item) {
		String sql = "insert into Source(Type, Item) values(?, ?) ";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, type);
			ps.setString(2, item);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 统计页使用,获取各项统计结果
	 * 
	 * @param type
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public static Vector<Vector<String>> getItemStat(String type,
			String fromDate, String toDate) {
		Vector<Vector<String>> result = new Vector<Vector<String>>();
		String sumsql = "SELECT Item, SUM(money) FROM Account WHERE Date BETWEEN ? and ? and Type = ? ";
		String sql = "SELECT Item, SUM(Money) FROM Account WHERE Date BETWEEN ? and ? and Type = ? group by Item";

		try {
			PreparedStatement ps = conn.prepareStatement(sumsql);
			ps.setString(1, fromDate);
			ps.setString(2, toDate);
			ps.setString(3, type);

			ResultSet rs = ps.executeQuery();
			float total = 0;
			while (rs.next()) {
				total = rs.getFloat(2);
			}
			rs.close();

			ps = conn.prepareStatement(sql);
			ps.setString(1, fromDate);
			ps.setString(2, toDate);
			ps.setString(3, type);

			rs = ps.executeQuery();
			while (rs.next()) {
				Vector<String> v = new Vector<String>();
				v.addElement(rs.getString(1));
				v.addElement(df.format(rs.getFloat(2)));
				v.addElement(df.format(rs.getFloat(2) / total * 100));
				result.addElement(v);
			}

			rs.close();
			ps.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static int getTotalBudget(String year, String month) {
		return 0;
	}

	public static int getBudget(String year, String month) {
		int budget = 0;
		String userSql = "SELECT Money FROM Budget b, Category c WHERE b.CategoryID = c.ID and ParentID is not null and Type = 'Expenditure' and year = ? and month = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(userSql);
			ps.setString(1, year);
			ps.setString(2, month);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				budget = rs.getInt(1);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return budget;
	}

	public static int[] getBudgetColor() {
		int[] color = new int[2];
		Preferences pref = Preferences.userNodeForPackage(DBManager.class);
		color[0] = pref.getInt("Percent75", Color.GRAY.getRGB());
		color[1] = pref.getInt("Percent90", Color.MAGENTA.getRGB());
		return color;
	}

	public static void saveBudgetColor(int percent75, int percent90) {
		Preferences pref = Preferences.userNodeForPackage(DBManager.class);
		pref.putInt("Percent75", percent75);
		pref.putInt("Percent90", percent90);
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		refreshBudgetColor(df.format(new Date()));
	}

	public static void saveTotalBudget(int year, int month, int budget) {
		saveBudget(year, month, "Total", budget);
	}

	private static void saveBudget(int year, int month, String item, int budget) {
		String userSql = "SELECT ID FROM Budget WHERE USER = ? AND Year = ? AND Month = ? AND Item = ?";
		String insertSql = "INSERT INTO Budget(User, Year, Month, Item, Budget) VALUES(?, ?, ?, ?, ?)";
		String updateSql = "UPDATE Budget set Budget = ? WHERE ID = ?";

		int id = -1;
		try {
			PreparedStatement ps = conn.prepareStatement(userSql);
			ps.setString(1, Constance.LOGIN_USER);
			ps.setInt(2, year);
			ps.setInt(3, month);
			ps.setString(4, item);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				id = rs.getInt(1);
			}
			rs.close();

			if (id != -1) {
				ps = conn.prepareStatement(updateSql);
				ps.setFloat(1, budget);
				ps.setInt(2, id);
			} else {
				ps = conn.prepareStatement(insertSql);
				ps.setString(1, Constance.LOGIN_USER);
				ps.setInt(2, year);
				ps.setInt(3, month);
				ps.setString(4, item);
				ps.setFloat(5, budget);
			}
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void updateColor(TableModel model) {
		String sql = "SELECT ID, Color FROM Account WHERE ID = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			for (int i = 0; i < model.getRowCount(); i++) {
				ps.setInt(1, (Integer) model.getValueAt(i, 0));
				ResultSet rs = ps.executeQuery();
				rs.next();
				model.setValueAt(rs.getInt(2), i, 6);
				rs.close();
			}
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void saveItemBudget(Integer year, Integer month,
			Vector<Vector> dataVector) {
		for (Vector v : dataVector) {
			String item = (String) v.elementAt(1);
			int money = Integer.valueOf(v.elementAt(2).toString());
			saveBudget(year, month, item, money);
		}

		String strMonth = String.valueOf(month);
		if (month != 10 || month != 11 || month != 12) {
			strMonth = "0" + String.valueOf(month);
		}
		refreshBudgetColor(String.valueOf(year) + "-" + strMonth + "-" + "01");
	}
}
