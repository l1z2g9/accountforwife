package cat;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.table.TableModel;

import cat.model.Item;
import cat.vo.StatItem;

public class DBManager {
	static Logger log = Logger.getLogger("DBManager");

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
	public static Vector<Vector> getItemsByDate(String type, Date date) {
		Calendar startTime = Calendar.getInstance(TimeZone.getDefault(),
				Locale.SIMPLIFIED_CHINESE);
		startTime.setTime(date);
		startTime.set(Calendar.HOUR_OF_DAY, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.SECOND, 0);

		Calendar endtTime = Calendar.getInstance(Locale.SIMPLIFIED_CHINESE);
		endtTime.setTime(date);
		endtTime.set(Calendar.HOUR_OF_DAY, 23);
		endtTime.set(Calendar.MINUTE, 59);
		endtTime.set(Calendar.SECOND, 59);

		Calendar monthFirstDay = Calendar
				.getInstance(Locale.SIMPLIFIED_CHINESE);
		monthFirstDay.setTime(date);
		monthFirstDay.set(Calendar.DAY_OF_MONTH, 1);
		monthFirstDay.set(Calendar.HOUR, 0);
		monthFirstDay.set(Calendar.MINUTE, 0);
		monthFirstDay.set(Calendar.SECOND, 0);

		Vector<Vector> result = new Vector<Vector>();
		// 预算比较
		Map<Integer, Float> sumMap = new HashMap<Integer, Float>();
		Map<Integer, Float> budgetMap = new HashMap<Integer, Float>();
		if ("Expenditure".equals(type)) {
			try {
				String sumSql = "SELECT cp.id, sum(money) FROM Item i, Category c, Category cp "
						+ "WHERE i.categoryID = c.id and c.parentID = cp.id and "
						+ "c.type = ? and time between ? and ? group by cp.id";

				PreparedStatement ps = conn.prepareStatement(sumSql);
				ps.setString(1, type);
				ps.setLong(2, monthFirstDay.getTimeInMillis());
				ps.setLong(3, startTime.getTimeInMillis());
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					int categoryID = rs.getInt(1);
					float sumMoney = rs.getFloat(2);
					sumMap.put(categoryID, sumMoney);
				}
				rs.close();
				ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			String sql = "select c.id, money from Category c, Budget b where c.id = b.categoryID and parentID is null and "
					+ "type = 'Expenditure' and year = ? and month = ? order by displayOrder";
			try {
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setInt(1, monthFirstDay.get(Calendar.YEAR));
				ps.setInt(2, monthFirstDay.get(Calendar.MONTH) + 1);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					budgetMap.put(rs.getInt(1), rs.getFloat(2));
				}
				rs.close();
				ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			String sql = "SELECT i.id, time, cp.name, c.name, money, user, address, remark, cp.id FROM Item i, Category c, Category cp "
					+ "WHERE i.categoryID = c.id and c.parentID = cp.id and "
					+ "c.type = ? and time between ? and ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, type);
			ps.setLong(2, startTime.getTimeInMillis());
			ps.setLong(3, endtTime.getTimeInMillis());
			ResultSet rs = ps.executeQuery();

			int seq = 0;
			while (rs.next()) {
				Vector vo = new Vector();
				vo.addElement(rs.getInt(1));
				vo.addElement(++seq);
				vo.addElement(rs.getDate(2));
				vo.addElement(rs.getString(3));
				vo.addElement(rs.getString(4));
				vo.addElement(rs.getFloat(5));
				vo.addElement(rs.getString(6));
				vo.addElement(rs.getString(7));
				vo.addElement(rs.getString(8));

				if ("Expenditure".equals(type)) {
					int categoryID = rs.getInt(9);
					if (sumMap.containsKey(categoryID)) {
						sumMap.put(categoryID, sumMap.get(categoryID)
								+ rs.getFloat(5));
					} else {
						sumMap.put(categoryID, rs.getFloat(5));
					}

					float nowSum = sumMap.get(categoryID);

					if (budgetMap.containsKey(categoryID)) {
						float categtoryBudget = budgetMap.get(categoryID);
						if (nowSum > categtoryBudget * 0.9) {
							vo.addElement(Color.red);
						} else if (nowSum > categtoryBudget * 0.7) {
							vo.addElement(Color.yellow);
						} else {
							vo.addElement(Color.white);
						}
					} else {
						vo.addElement(Color.white);
					}
				} else {
					vo.addElement(Color.white);
				}
				result.add(vo);
			}
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

	public static Map<String, Integer> getCategory(String type) {
		String sql = "SELECT ID, Name Item FROM Category WHERE Type = ? and ParentID is null order by displayOrder";

		Map<String, Integer> result = new HashMap<String, Integer>();
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, type);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				result.put(rs.getString(2), rs.getInt(1));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static Map<String, Integer> getSubCategory(Integer id) {
		String sql = "SELECT ID, Name Item FROM Category WHERE ParentID = ? ORDER BY displayOrder";

		Map<String, Integer> result = new HashMap<String, Integer>();
		try {

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				result.put(rs.getString(2), rs.getInt(1));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static int saveItem(Item item) {
		int rowID = 0;
		try {
			String sql = "insert into Item(title, time, money, categoryID, remark, user, address) values(?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, item.getTitle());
			ps.setLong(2, item.getTime());
			ps.setFloat(3, item.getMoney());
			ps.setFloat(4, item.getCategoryID());
			ps.setString(5, item.getRemark());
			ps.setString(6, item.getUser());
			ps.setString(7, item.getAddress());

			ps.executeUpdate();
			ps.close();

			ps = conn
					.prepareStatement("select ID from Item order by ID desc limit 1");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				rowID = rs.getInt(1);
			}
			rs.close();
			ps.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rowID;
	}

	public static void updateItem(Item item) {
		String sql = "update Item set title =?, money = ?, categoryID = ?, remark = ?, user = ?, address = ? where id= ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, item.getTitle());
			ps.setFloat(2, item.getMoney());
			ps.setInt(3, item.getCategoryID());
			ps.setString(4, item.getRemark());
			ps.setString(5, item.getUser());
			ps.setString(6, item.getAddress());
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
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Vector<Vector> getBudgetItems(int year, int month) {
		Vector<Vector> result = new Vector<Vector>();
		Map budget = new HashMap();
		int seq = 0;
		String sql = "select name, money from Category c, Budget b where c.id = b.categoryID and parentID is null and "
				+ "type = 'Expenditure' and year = ? and month = ? order by displayOrder";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, year);
			ps.setInt(2, month);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				budget.put(rs.getString(1), rs.getString(2));
			}
			rs.close();
			ps.close();

			sql = "select id, name from Category where parentID is null and "
					+ "type = 'Expenditure' order by displayOrder";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				Vector v = new Vector();
				v.addElement(++seq);
				v.addElement(rs.getString(1));
				String categoryName = rs.getString(2);
				v.addElement(categoryName);
				if (budget.containsKey(categoryName)) {
					v.addElement(budget.get(categoryName));
				} else {
					v.addElement(-1);
				}
				result.addElement(v);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
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

	public static int[] getBudgetColor() {
		int[] color = new int[2];
		Preferences pref = Preferences.userNodeForPackage(DBManager.class);
		color[0] = pref.getInt("Percent75", Color.GRAY.getRGB());
		color[1] = pref.getInt("Percent90", Color.MAGENTA.getRGB());
		return color;
	}

	public static void saveBudget(int year, int month,
			Map<Integer, Float> budget) {
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String findSql = "SELECT b.id FROM Budget b, Category c WHERE b.categoryID = c.id and type = 'Expenditure' and year = ? AND month = ? and categoryID = ?";
		String insertSql = "INSERT INTO Budget(categoryID, year, month, money) VALUES(?, ?, ?, ?)";
		String updateSql = "UPDATE Budget set money = ? WHERE id = ?";

		for (Integer categoryID : budget.keySet()) {
			int id = -1;
			try {
				PreparedStatement ps = conn.prepareStatement(findSql);
				ps.setInt(1, year);
				ps.setInt(2, month);
				ps.setInt(2, categoryID);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					id = rs.getInt(1);
				}
				rs.close();

				if (id != -1) {
					ps = conn.prepareStatement(updateSql);
					ps.setFloat(1, budget.get(categoryID));
					ps.setInt(2, id);
				} else {
					ps = conn.prepareStatement(insertSql);
					ps.setInt(1, categoryID);
					ps.setInt(2, year);
					ps.setInt(3, month);
					ps.setFloat(4, budget.get(categoryID));
				}
				ps.executeUpdate();
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		try {
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
