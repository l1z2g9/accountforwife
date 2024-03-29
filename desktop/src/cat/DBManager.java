package cat;

import cat.model.Category;
import cat.model.Item;
import cat.model.NavigatePage;
import cat.model.Overdraw;
import java.awt.Color;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

public class DBManager {
	static Logger log = Logger.getLogger("DBManager");
	static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static DecimalFormat df = new DecimalFormat("##.##");
	static int limit = 30;
	static Connection conn = null;
	static {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:Account.db");
		} catch (Exception e) {
			e.printStackTrace();
		}

		FileHandler fileHandler = null;
		try {
			fileHandler = new FileHandler("db.log");
			fileHandler.setFormatter(new Formatter() {
				@Override
				public String format(LogRecord record) {
					DateFormat df = DateFormat.getInstance();
					String time = df.format(new Date());

					return time + "： " + record.getMessage() + "\r\n";
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.addHandler(fileHandler);
	}

	// --------- start items ---------
	public static NavigatePage getItemsByDate(String type, Date date) {
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

		return DBManager.getItemsByDate(type, startTime, endtTime, -1, -1,
				null, -1);
	}

	public static float getTotalMonthMoney(int year, int month, String type) {
		Calendar monthFirstDay = Calendar
				.getInstance(Locale.SIMPLIFIED_CHINESE);
		monthFirstDay.set(Calendar.YEAR, year);
		monthFirstDay.set(Calendar.MONTH, month - 1);
		monthFirstDay.set(Calendar.DAY_OF_MONTH, 1);
		monthFirstDay.set(Calendar.HOUR, 0);
		monthFirstDay.set(Calendar.MINUTE, 0);
		monthFirstDay.set(Calendar.SECOND, 0);

		Calendar monthEndDay = Calendar.getInstance(Locale.SIMPLIFIED_CHINESE);
		monthEndDay.set(Calendar.YEAR, year);
		monthEndDay.set(Calendar.MONTH, month - 1);
		monthEndDay.set(Calendar.DAY_OF_MONTH, 30);
		monthEndDay.set(Calendar.HOUR, 23);
		monthEndDay.set(Calendar.MINUTE, 59);
		monthEndDay.set(Calendar.SECOND, 59);

		float sumMoney = 0f;
		String sumSql = "SELECT sum(money) FROM Item i, Category c, Category cp "
				+ "WHERE i.categoryID = c.id and c.parentID = cp.id and "
				+ "c.type = ? and time between ? and ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sumSql);
			ps.setString(1, type);
			ps.setLong(2, monthFirstDay.getTimeInMillis());
			ps.setLong(3, monthEndDay.getTimeInMillis());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				sumMoney = rs.getFloat(1);
			}
		} catch (Exception e) {
			exitProgram(e);
		}

		return sumMoney;
	}

	private static String buildCondition(String sql, String type, int parentID,
			int subCategoryID, String user, int currentPage) {
		StringBuilder builder = new StringBuilder(sql);
		if (!type.equalsIgnoreCase("All"))
			builder.append(" and c.type = '" + type + "'");
		if (parentID != -1)
			builder.append(" and cp.id = " + parentID);
		if (subCategoryID != -1)
			builder.append(" and c.id = " + subCategoryID);
		if (user != null)
			builder.append(" and user like '%" + user + "%'");

		return builder.toString();
	}

	public static NavigatePage getItemsByDate(String type, Calendar startTime,
			Calendar endtTime, int parentID, int subCategoryID, String user,
			int currentPage) {
		final NavigatePage navigatePage = new NavigatePage();

		Calendar monthFirstDay = Calendar
				.getInstance(Locale.SIMPLIFIED_CHINESE);
		monthFirstDay.setTime(endtTime.getTime());
		monthFirstDay.set(Calendar.DAY_OF_MONTH, 1);
		monthFirstDay.set(Calendar.HOUR, 0);
		monthFirstDay.set(Calendar.MINUTE, 0);
		monthFirstDay.set(Calendar.SECOND, 0);

		// 计算总页数和offset
		String totalSql = buildCondition(
				"SELECT count(i.id) FROM Item i, Category c, Category cp "
						+ "WHERE i.categoryID = c.id and c.parentID = cp.id and time between ? and ?",
				type, parentID, subCategoryID, user, currentPage);

		int total = 0;
		try {
			PreparedStatement ps = conn.prepareStatement(totalSql);
			ps.setLong(1, startTime.getTimeInMillis());
			ps.setLong(2, endtTime.getTimeInMillis());
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				total = rs.getInt(1);
			}

			rs.close();
			ps.close();
		} catch (Exception e) {
			exitProgram(e);
		}
		int total_pages = 1;
		if (total > 0)
			total_pages = (int) Math.ceil(total / limit) + 1;

		if (currentPage > total_pages)
			currentPage = total_pages;

		int offset = limit * currentPage - limit;
		if (offset < 0)
			offset = 0;

		navigatePage.setTotalPage(total_pages);
		navigatePage.setTotal(total);

		Map<Integer, Float> sumMap = new HashMap<Integer, Float>();
		Map<Integer, Float> budgetMap = new HashMap<Integer, Float>();
		if (!"Income".equalsIgnoreCase(type)) {
			// 获取月初到开始时间之前的开销
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
				exitProgram(e);
			}

			// 获取当月预算
			String sql = "select c.id, money from Category c, Budget b where c.id = b.categoryID and parentID is null and "
					+ "type = 'Expenditure' and year = ? and month = ? order by displayOrder desc";
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
				exitProgram(e);
			}
		}

		// 封装结果
		Vector<Vector> result = new Vector<Vector>();
		try {
			String sql = buildCondition(
					"SELECT i.id, time, cp.name, c.name, money, user, address, remark, cp.id, c.type FROM Item i, Category c, Category cp "
							+ "WHERE i.categoryID = c.id and c.parentID = cp.id and time between ? and ?",
					type, parentID, subCategoryID, user, currentPage);
			if (currentPage != -1) // == -1时候处理分页内容
				sql += String.format(
						" order by time asc, i.id asc limit %s offset %s ",
						limit, offset);
			else
				sql += String.format(" order by time asc, i.id asc ");

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, startTime.getTimeInMillis());
			ps.setLong(2, endtTime.getTimeInMillis());
			ResultSet rs = ps.executeQuery();

			int seq = (currentPage - 1) * offset;
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
				// vo.addElement(rs.getString(9));
				vo.addElement(rs.getString(10));

				if (!"Income".equalsIgnoreCase(type)) {
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
			exitProgram(e);
		}

		navigatePage.setCurrentPageResult(result);

		// 总输入支出统计
		String sql = buildCondition(
				"SELECT c.type, sum(money) FROM Item i, Category c, Category cp "
						+ "WHERE i.categoryID = c.id and c.parentID = cp.id and time between ? and ?",
				type, parentID, subCategoryID, user, currentPage);

		sql += " group by c.type";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, startTime.getTimeInMillis());
			ps.setLong(2, endtTime.getTimeInMillis());
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				if ("Expenditure".equals(rs.getString(1))) {
					navigatePage.setTotalExpenditure(rs.getFloat(2));
				}
				if ("Income".equals(rs.getString(1))) {
					navigatePage.setTotalIncome(rs.getFloat(2));
				}
			}

			rs.close();
			ps.close();
		} catch (Exception e) {
			exitProgram(e);
		}
		return navigatePage;
	}

	public static int saveItem(Item item) {
		log.info("新增项目, time=" + sf.format(item.getTime()) + " , money="
				+ item.getMoney() + "categoryID=" + item.getCategoryID());
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
			exitProgram(e);
		}
		return rowID;
	}

	public static void updateItem(Item item) {
		String sql = "update Item set title =?, money = ?, categoryID = ?, remark = ?, user = ?, address = ? where id = ?";
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
			exitProgram(e);
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
			exitProgram(e);
		}
	}

	// --------- end items ---------

	// ---------- start 类别 ------------
	public static Map<String, Category> getCategory(String type) {
		String sql = "SELECT name, id, displayOrder FROM Category WHERE Type = ? and ParentID is null order by displayOrder desc";

		Map<String, Category> result = new LinkedHashMap<String, Category>();
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, type);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Category category = new Category();
				category.setId(rs.getInt(2));
				category.setName(rs.getString(1));
				category.setDisplayOrder(rs.getInt(3));

				result.put(rs.getString(1), category);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			exitProgram(e);
		}
		return result;
	}

	public static Map<String, Category> getSubCategory(Integer id) {
		String sql = "SELECT name, id, displayOrder FROM Category WHERE parentID = ? ORDER BY displayOrder desc";

		Map<String, Category> result = new LinkedHashMap<String, Category>();
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Category category = new Category();
				category.setId(rs.getInt(2));
				category.setName(rs.getString(1));
				category.setDisplayOrder(rs.getInt(3));

				result.put(rs.getString(1), category);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			exitProgram(e);
		}
		return result;
	}

	public static int saveCategory(String type, String name, int displayOrder) {
		String findLastCategoryIDsql = "select max(id) from Category where parentID is null";
		int lastID = -1;
		try {
			PreparedStatement ps = conn.prepareStatement(findLastCategoryIDsql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				lastID = rs.getInt(1);
				lastID += 100;
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}

		String sql = "insert into Category(id, type, name, displayOrder) values(?, ?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, lastID);
			ps.setString(2, type);
			ps.setString(3, name);
			ps.setInt(4, displayOrder);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}

		return lastID;
	}

	public static int saveSubCategory(int parentID, String type, String name,
			int displayOrder) {
		String findLastCategoryIDsql = "select max(id) from Category where parentID = ?";
		int lastID = -1;
		try {
			PreparedStatement ps = conn.prepareStatement(findLastCategoryIDsql);
			ps.setInt(1, parentID);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				lastID = rs.getInt(1);
				if (lastID == 0)
					lastID = parentID;
				lastID += 1;
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}
		String sql = "insert into Category(id, parentId, type, name, displayOrder) values(?, ?, ?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			log.info("保存子类别：id: " + lastID);
			log.info("父类别：parentID: " + parentID);
			ps.setInt(1, lastID);
			ps.setInt(2, parentID);
			ps.setString(3, type);
			ps.setString(4, name);
			ps.setInt(5, displayOrder);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}
		return lastID;
	}

	public static void updateCategory(int id, String name, int displayOrder) {
		String sql = "update Category set name = ?, displayOrder = ? where id = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, name);
			ps.setInt(2, displayOrder);
			ps.setInt(3, id);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}
	}

	public static boolean deleteCategory(int id) {
		String checkSubCategories = "select count(*) from Category where parentID = ?";
		int subCategorySum = 0;
		try {
			PreparedStatement ps = conn.prepareStatement(checkSubCategories);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				subCategorySum = rs.getInt(1);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}

		if (subCategorySum > 0)
			return false;
		String sql = "delete from Category where id = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}
		return true;
	}

	// ---------- end 类别 ------------
	// ---------- start 预算 ------------
	public static Vector<Vector> getBudgetItems(int year, int month) {
		Vector<Vector> result = new Vector<Vector>();
		Map budget = new HashMap();
		int seq = 0;
		String sql = "select name, money from Category c, Budget b where c.id = b.categoryID and parentID is null and "
				+ "type = 'Expenditure' and year = ? and month = ? order by displayOrder desc";
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
					+ "type = 'Expenditure' order by displayOrder desc";
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
			exitProgram(e);
		}
		return result;
	}

	public static void saveBudget(int year, int month,
			Map<Integer, Float> budget) {
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			exitProgram(e);
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
				exitProgram(e);
			}
		}

		try {
			conn.commit();
		} catch (SQLException e) {
			exitProgram(e);
		}
	}

	// ---------- end 预算 ------------
	public static NavigatePage query(int year, int month, String type,
			int parentCategoryID, int categoryID, String user, int currentPage) {

		Calendar startTime = Calendar.getInstance(TimeZone.getDefault(),
				Locale.SIMPLIFIED_CHINESE);
		startTime.set(Calendar.YEAR, year);
		startTime.set(Calendar.MONTH, month - 1);
		startTime.set(Calendar.DAY_OF_MONTH, 1);
		startTime.set(Calendar.HOUR_OF_DAY, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.SECOND, 0);

		Calendar endtTime = Calendar.getInstance(Locale.SIMPLIFIED_CHINESE);
		endtTime.set(Calendar.YEAR, year);
		endtTime.set(Calendar.MONTH, month - 1);
		endtTime.set(Calendar.DAY_OF_MONTH, 30);
		endtTime.set(Calendar.HOUR_OF_DAY, 23);
		endtTime.set(Calendar.MINUTE, 59);
		endtTime.set(Calendar.SECOND, 59);
		return getItemsByDate(type, startTime, endtTime, parentCategoryID,
				categoryID, user, currentPage);
	}

	public static void releaseConnection() {
		try {
			log.info("释放数据库连接");
			conn.close();
		} catch (SQLException e) {
			exitProgram(e);
		}
	}

	// --------- start 预支付
	public static NavigatePage getOverDrawItems(int year, int month,
			int currentPage) {
		NavigatePage navigatePage = new NavigatePage();
		Calendar startTime = Calendar.getInstance(TimeZone.getDefault(),
				Locale.SIMPLIFIED_CHINESE);
		startTime.set(Calendar.YEAR, year);
		startTime.set(Calendar.MONTH, month - 1);
		startTime.set(Calendar.DAY_OF_MONTH, 1);
		startTime.set(Calendar.HOUR_OF_DAY, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.SECOND, 0);

		Calendar endtTime = Calendar.getInstance(Locale.SIMPLIFIED_CHINESE);
		endtTime.set(Calendar.YEAR, year);
		endtTime.set(Calendar.MONTH, month - 1);
		endtTime.set(Calendar.DAY_OF_MONTH, 30);
		endtTime.set(Calendar.HOUR_OF_DAY, 23);
		endtTime.set(Calendar.MINUTE, 59);
		endtTime.set(Calendar.SECOND, 59);
		// 总数
		String sql = "SELECT COUNT(id) FROM Overdraw WHERE time between ? and ?";
		int total = 0;
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, startTime.getTimeInMillis());
			ps.setLong(2, endtTime.getTimeInMillis());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				total = rs.getInt(1);
				navigatePage.setTotal(total);
			}

			rs.close();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}
		int total_pages = 1;
		if (total > 0)
			total_pages = (int) Math.ceil(total / limit) + 1;

		if (currentPage > total_pages)
			currentPage = total_pages;

		int offset = limit * currentPage - limit;
		if (offset < 0)
			offset = 0;
		navigatePage.setTotalPage(total_pages);

		// 结果集
		sql = String
				.format(
						"SELECT id, time, money, address, remark, returnTime, returnMoney, returnRemark, (returnMoney - money), completed FROM Overdraw "
								+ "WHERE time BETWEEN ? and ? order by completed desc, id limit %s offset %s",
						limit, offset);
		Vector<Vector> currentPageResult = new Vector<Vector>();

		int seq = (currentPage - 1) * offset;
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, startTime.getTimeInMillis());
			ps.setLong(2, endtTime.getTimeInMillis());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Vector v = new Vector();
				v.addElement(rs.getInt(1));
				v.addElement(++seq);
				v.addElement(rs.getDate(2));
				v.addElement(rs.getFloat(3));
				v.addElement(rs.getString(4));
				v.addElement(rs.getString(5));
				if (rs.getFloat(7) == 0f) {
					v.addElement("");
					v.addElement("");
					v.addElement("");
				} else {
					v.addElement(rs.getDate(6));
					v.addElement(rs.getFloat(7));
					v.addElement(rs.getString(8));
				}
				v.addElement(rs.getInt(10) == 1 ? rs.getFloat(9):"");
				v.addElement(rs.getInt(10) == 1 ? "是" : "否");
				currentPageResult.add(v);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}
		navigatePage.setCurrentPageResult(currentPageResult);

		// 支出收入
		sql = "SELECT SUM(money), SUM(returnMoney) FROM Overdraw WHERE time between ? and ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, startTime.getTimeInMillis());
			ps.setLong(2, endtTime.getTimeInMillis());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				navigatePage.setTotalExpenditure(rs.getFloat(1));
				navigatePage.setTotalIncome(rs.getFloat(2));
				navigatePage.setTotalBalance(rs.getFloat(2) - rs.getFloat(1));
			}

			rs.close();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}

		return navigatePage;
	}

	public static void saveOverDrawItems(Overdraw overdraw) {
		log.info("新增预支付项" + sf.format(overdraw.getTime()) + " , 透支: "
				+ overdraw.getMoney());
		String sql = "insert into Overdraw(time, money, remark, address, returnTime, returnMoney, returnRemark) values(?,?,?,?,?,?,?)";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, overdraw.getTime());
			ps.setFloat(2, overdraw.getMoney());
			ps.setString(3, overdraw.getRemark());
			ps.setString(4, overdraw.getAddress());
			ps.setLong(5, overdraw.getReturnTime());
			ps.setFloat(6, overdraw.getReturnMoney());
			ps.setString(7, overdraw.getReturnRemark());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}
	}

	public static void updateOverDrawItems(Overdraw overdraw) {
		String sql = "update Overdraw set time = ?, money = ?, remark = ?, address = ?, returnTime = ?, returnMoney = ?, returnRemark = ?, completed = ? where id = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, overdraw.getTime());
			ps.setFloat(2, overdraw.getMoney());
			ps.setString(3, overdraw.getRemark());
			ps.setString(4, overdraw.getAddress());
			ps.setLong(5, overdraw.getReturnTime());
			ps.setFloat(6, overdraw.getReturnMoney());
			ps.setString(7, overdraw.getReturnRemark());
			ps.setInt(8, overdraw.getCompleted());
			ps.setInt(9, overdraw.getId());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}
	}

	public static void deleteOverDrawItems(int id) {
		String sql = "delete from Overdraw where id = ?";
		log.info("删除预支付项，id = " + id);
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}
	}

	public static Overdraw getOverDrawItems(int id) {
		Overdraw overdraw = new Overdraw();
		String sql = "select time, money, remark, address, returnTime, returnMoney, returnRemark, completed from Overdraw where id = ?";

		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				overdraw.setId(id);
				overdraw.setTime(rs.getLong(1));
				overdraw.setMoney(rs.getFloat(2));
				overdraw.setRemark(rs.getString(3));
				overdraw.setAddress(rs.getString(4));
				overdraw.setReturnTime(rs.getLong(5));
				overdraw.setReturnMoney(rs.getFloat(6));
				overdraw.setReturnRemark(rs.getString(7));
				overdraw.setCompleted(rs.getInt(8));
			}

			rs.close();
			ps.close();
		} catch (SQLException e) {
			exitProgram(e);
		}
		return overdraw;
	}

	// --------- end 预支付
	private static void exitProgram(Exception e) {
		log.severe(e.toString());
		StackTraceElement[] trace = e.getStackTrace();
		for (int i = 0; i < trace.length; i++)
			log.severe("\tat " + trace[i]);
		releaseConnection();
		JOptionPane.showMessageDialog(null, "程序出现意想不到的错误，请与小强联系！", "程序出错",
				JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
}
