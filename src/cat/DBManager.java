package cat;


import cat.vo.StatItem;
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

import javax.swing.UIManager;
import javax.swing.table.TableModel;


public class DBManager
{
  static Logger log = Logger.getLogger("Util");

  static DecimalFormat df = new DecimalFormat("##.##");

  static Connection conn = null;
  static
  {
    try
    {
      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:Account.db");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }


  /**
   * 统计页使用,读取当日的所有收支情况.
   * @param date
   * @return
   */
  @SuppressWarnings("unchecked")
  public static Vector<Vector> getItemsByDate(String date)
  {
    Vector<Vector> result = new Vector<Vector>();
    try
    {
      String sql = "SELECT ID, Type, Date, Item, Money, Remark, Color FROM Account WHERE Date = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, date);
      ResultSet rs = ps.executeQuery();
      assemble(result, rs);
      rs.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return result;
  }


  @SuppressWarnings("unchecked")
  private static void assemble(Vector<Vector> result, ResultSet rs)
  {
    try
    {
      while (rs.next())
      {
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
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }


  @SuppressWarnings("unchecked")
  public static Vector<Vector> getItemsBetweenDates(String fromDate, String toDate)
  {
    Vector<Vector> result = new Vector<Vector>();
    try
    {
      String sql = "SELECT ID, Type, Date, Item, Money, Remark, Color FROM Account WHERE Date BETWEEN ? AND ? ORDER BY Date";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, fromDate);
      ps.setString(2, toDate);
      ResultSet rs = ps.executeQuery();
      assemble(result, rs);
      rs.close();
      ps.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return result;
  }


  @SuppressWarnings("unchecked")
  public static Vector<Vector> getItemsByType(String date, String type)
  {
    Vector<Vector> result = new Vector<Vector>();
    try
    {
      String sql = "SELECT ID, Type, Date, Item, Money, Remark, Color FROM Account WHERE Date = ? and Type = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, date);
      ps.setString(2, type);
      ResultSet rs = ps.executeQuery();
      assemble(result, rs);
      rs.close();
      ps.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return result;
  }


  /**
   * 某一天的金额发生变化，可能增加，可能减少，这样可能导致当月超支，或者超支的地方变成没有超支，所以需要同步更新当月的所有预算背景色。
   * @param date
   */
  private static void refreshBudgetColor(String date)
  {
    String fromDate = date.substring(0, date.length() - 2) + "01";
    int year = Integer.valueOf(date.substring(0, 4));
    int month = Integer.valueOf(date.substring(5, 7));
    //    log.info("month: " + month);
    int[] bigMon = new int[]{1, 3, 5, 7, 8, 10, 12};
    String lastDay = "30";
    for (int m : bigMon)
    {
      if (m == month)
      {
        lastDay = "31";
      }
    }

    String toDate = date.substring(0, date.length() - 2) + lastDay;
    //    float payout = queryPayoutTotal(fromDate, toDate, "支出");
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
    for (Vector item : items)
    {
      String i = (String) item.get(1);
      if (!"支出".equalsIgnoreCase(i))
      {
        continue;
      }

      int id = (Integer) item.get(0);
      String _item = (String) item.get(3);
      float budget = getBudget(year, month, _item);

      float sum = Float.valueOf(item.get(4).toString());
      if (itemSum.containsKey(_item))
      {
        sum += itemSum.get(_item);
      }
      else
      {
        itemSum.put(_item, sum);
      }

      if (budget * 0.75 > sum)
      {
        normal.put(id, Color.WHITE.getRGB());
      }
      else if (budget * 0.75 < sum && budget * 0.9 >= sum)
      {
        percent75.put(id, percent75color);
      }
      else if (budget * 0.9 < sum && budget >= sum)
      {
        percent90.put(id, percent90color);
      }
      else
      {
        over_budget.put(id, Color.RED.getRGB());
      }
    }
    try
    {
      conn.setAutoCommit(false);
      PreparedStatement ps = conn.prepareStatement("UPDATE Account SET Color = ? WHERE ID = ?");
      for (int id : normal.keySet())
      {
        ps.setInt(1, normal.get(id));
        ps.setInt(2, id);
        ps.executeUpdate();
      }

      for (int id : percent75.keySet())
      {
        ps.setInt(1, percent75.get(id));
        ps.setInt(2, id);
        ps.executeUpdate();
      }

      for (int id : percent90.keySet())
      {
        ps.setInt(1, percent90.get(id));
        ps.setInt(2, id);
        ps.executeUpdate();
      }

      for (int id : over_budget.keySet())
      {
        ps.setInt(1, over_budget.get(id));
        ps.setInt(2, id);
        ps.executeUpdate();
      }

      conn.commit();
      ps.close();

    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }


  public static void insert(String type, String date, String item, float money, String remark)
  {
    /*
    int lastId = 0;
    int[] rlt = new int[2];
    */try
    {
      int color = -1;
      String sql = "insert into Account(Type, Date, Item, Money, Remark, User, Color) values(?, ?, ?, ?, ?, ?, ?)";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, type);
      ps.setString(2, date);
      ps.setString(3, item);
      ps.setFloat(4, money);
      ps.setString(5, remark);
      ps.setString(6, Constance.LOGIN_USER);
      ps.setInt(7, color);

      ps.executeUpdate();
      ps.close();

      /*Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("select max(id) from Account");
      rs.next();
      lastId = rs.getInt(1);
      rs.close();
      stmt.close();
      */
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    if ("支出".equalsIgnoreCase(type))
    {
      refreshBudgetColor(date);
    }

    /*    rlt[0] = lastId;
        rlt[1] = color;
        return rlt;
    */
  }


  public static void update(int id, String field, String value)
  {
    String sql = "update Account SET " + field + " = ? WHERE ID = ?";
    log.info(sql);
    String date = "";
    try
    {
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, value);
      ps.setInt(2, id);
      ps.executeUpdate();
      ps.close();

      ps = conn.prepareStatement("SELECT Date FROM Account WHERE ID = ?");
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      rs.next();
      date = rs.getString(1);
      rs.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    refreshBudgetColor(date);
  }


  public static void delete(int id)
  {
    String sql = "delete from Account where id = ?";
    String date = "";
    try
    {
      PreparedStatement ps = conn.prepareStatement("SELECT Date FROM Account WHERE ID = ?");
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      rs.next();
      date = rs.getString(1);
      rs.close();

      ps = conn.prepareStatement(sql);
      ps.setInt(1, id);
      ps.executeUpdate();
      ps.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    refreshBudgetColor(date);
  }


  public static Vector<String> getPayoutItems()
  {
    return getItems("支出");
  }


  public static Vector<String> getIncomeItems()
  {
    return getItems("收入");
  }


  private static Vector<String> getItems(String type)
  {
    Vector<String> v = new Vector<String>();
    try
    {
      String sql = "select item from Source where type = ?";

      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, type);
      ResultSet rs = ps.executeQuery();
      while (rs.next())
      {
        v.addElement(rs.getString(1));
      }
      rs.close();
      ps.close();

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return v;
  }


  private static void addList(String date, String type, float money, Map<String, StatItem> dateMap)
  {
    StatItem stat = null;
    if (dateMap.containsKey(date))
    {
      stat = dateMap.get(date);
      if ("支出".equalsIgnoreCase(type))
      {
        stat.setPayout(money);
      }
      else
      {
        stat.setIncome(money);
      }
    }
    else
    {
      stat = new StatItem();
      dateMap.put(date, stat);
      stat.setDate(date);
      if ("支出".equalsIgnoreCase(type))
      {
        stat.setPayout(money);
      }
      else
      {
        stat.setIncome(money);
      }
    }
  }


  public static float queryPayoutTotal(String fromDate, String toDate, String type)
  {
    String sql = "SELECT sum(Money) FROM Account WHERE date BETWEEN ? AND ? and Type = ?";
    float total = 0;
    try
    {
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, fromDate);
      ps.setString(2, toDate);
      ps.setString(3, type);
      ResultSet rs = ps.executeQuery();
      while (rs.next())
      {
        total = rs.getFloat(1);
      }
      rs.close();
      ps.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    return total;
  }


  public static Vector<Vector<String>> query(String fromDate, String toDate)
  {
    Map<String, StatItem> dateMap = new TreeMap<String, StatItem>();
    try
    {
      String sql = "SELECT Date, Type, sum(Money) FROM Account WHERE date BETWEEN ? AND ? and Type = ? GROUP BY Date";

      PreparedStatement ps = conn.prepareStatement(sql);

      ps.setString(1, fromDate);
      ps.setString(2, toDate);
      ps.setString(3, "支出");
      ResultSet rs = ps.executeQuery();
      while (rs.next())
      {
        String date = rs.getString(1);
        String type = rs.getString(2);
        float money = rs.getFloat(3);
        addList(date, type, money, dateMap);
      }
      rs.close();

      ps.setString(3, "收入");
      rs = ps.executeQuery();
      while (rs.next())
      {
        String date = rs.getString(1);
        String type = rs.getString(2);
        float money = rs.getFloat(3);
        addList(date, type, money, dateMap);
      }

      rs.close();
      ps.close();

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    Vector<Vector<String>> list = new Vector<Vector<String>>();

    for (StatItem stat : dateMap.values())
    {
      Vector<String> v = new Vector<String>();
      v.addElement(stat.getDate());
      v.addElement(df.format(stat.getPayout()));
      v.addElement(df.format(stat.getIncome()));
      list.add(v);
    }
    return list;
  }


  public static void releaseConnection()
  {
    try
    {
      log.info("释放数据库连接");
      conn.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }


  public static void insertSource(String type, String item)
  {
    String sql = "insert into Source(Type, Item) values(?, ?) ";
    try
    {
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, type);
      ps.setString(2, item);
      ps.executeUpdate();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }


  public static void deleteSource(String type, String item)
  {
    String sql = "delete from Source where Type = ? and Item = ? ";
    try
    {
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, type);
      ps.setString(2, item);
      ps.executeUpdate();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }


  /**
   * 统计页使用,获取各项统计结果
   * @param type
   * @param fromDate
   * @param toDate
   * @return
   */
  public static Vector<Vector<String>> getItemStat(String type, String fromDate, String toDate)
  {
    Vector<Vector<String>> result = new Vector<Vector<String>>();
    String sumsql = "SELECT Item, SUM(money) FROM Account WHERE Date BETWEEN ? and ? and Type = ? ";
    String sql = "SELECT Item, SUM(Money) FROM Account WHERE Date BETWEEN ? and ? and Type = ? group by Item";

    try
    {
      PreparedStatement ps = conn.prepareStatement(sumsql);
      ps.setString(1, fromDate);
      ps.setString(2, toDate);
      ps.setString(3, type);

      ResultSet rs = ps.executeQuery();
      float total = 0;
      while (rs.next())
      {
        total = rs.getFloat(2);
      }
      rs.close();

      ps = conn.prepareStatement(sql);
      ps.setString(1, fromDate);
      ps.setString(2, toDate);
      ps.setString(3, type);

      rs = ps.executeQuery();
      while (rs.next())
      {
        Vector<String> v = new Vector<String>();
        v.addElement(rs.getString(1));
        v.addElement(df.format(rs.getFloat(2)));
        v.addElement(df.format(rs.getFloat(2) / total * 100));
        result.addElement(v);
      }

      rs.close();
      ps.close();

    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    return result;
  }


  public static int getTotalBudget(int year, int month)
  {
    return getBudget(year, month, "Total");
  }


  public static int getBudget(int year, int month, String item)
  {
    int budget = 0;
    String userSql = "SELECT Budget FROM Budget WHERE USER = ? AND Year = ? AND Month = ? AND Item = ?";
    try
    {
      PreparedStatement ps = conn.prepareStatement(userSql);
      ps.setString(1, Constance.LOGIN_USER);
      ps.setInt(2, year);
      ps.setInt(3, month);
      ps.setString(4, item);

      ResultSet rs = ps.executeQuery();
      if (rs.next())
      {
        budget = rs.getInt(1);
      }
      rs.close();
      ps.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    return budget;
  }


  public static int[] getBudgetColor()
  {
    int[] color = null;
    String sql = "SELECT Percent75, Percent90 FROM BudgetColor WHERE USER = ?";
    try
    {
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, Constance.LOGIN_USER);
      ResultSet rs = ps.executeQuery();
      if (rs.next())
      {
        color = new int[2];
        color[0] = rs.getInt(1);
        color[1] = rs.getInt(2);
      }
      rs.close();
      ps.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    return color;
  }


  public static void saveBudgetColor(int percent75, int percent90)
  {
    String userSql = "SELECT ID FROM BudgetColor WHERE USER = ?";
    String updateSql = "UPDATE BudgetColor set Percent75 = ?, Percent90 = ? WHERE ID = ?";
    String insertSql = "INSERT INTO BudgetColor(User, Percent75, Percent90) VALUES(?, ?, ?)";

    int id = -1;
    try
    {
      PreparedStatement ps = conn.prepareStatement(userSql);
      ps.setString(1, Constance.LOGIN_USER);
      ResultSet rs = ps.executeQuery();
      if (rs.next())
      {
        id = rs.getInt(1);
      }
      rs.close();

      if (id != -1)
      {
        ps = conn.prepareStatement(updateSql);
        ps.setInt(1, percent75);
        ps.setInt(2, percent90);
        ps.setInt(3, id);
      }
      else
      {
        ps = conn.prepareStatement(insertSql);
        ps.setString(1, Constance.LOGIN_USER);
        ps.setInt(2, percent75);
        ps.setInt(3, percent90);
      }
      ps.executeUpdate();
      ps.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    DBManager.refreshBudgetColor(df.format(new Date()));
  }


  public static void saveTotalBudget(int year, int month, int budget)
  {
    saveBudget(year, month, "Total", budget);
  }


  public static void saveBudget(int year, int month, String item, int budget)
  {
    String userSql = "SELECT ID FROM Budget WHERE USER = ? AND Year = ? AND Month = ? AND Item = ?";
    String insertSql = "INSERT INTO Budget(User, Year, Month, Item, Budget) VALUES(?, ?, ?, ?, ?)";
    String updateSql = "UPDATE Budget set Budget = ? WHERE ID = ?";

    int id = -1;
    try
    {
      PreparedStatement ps = conn.prepareStatement(userSql);
      ps.setString(1, Constance.LOGIN_USER);
      ps.setInt(2, year);
      ps.setInt(3, month);
      ps.setString(4, item);
      ResultSet rs = ps.executeQuery();
      if (rs.next())
      {
        id = rs.getInt(1);
      }
      rs.close();

      if (id != -1)
      {
        ps = conn.prepareStatement(updateSql);
        ps.setFloat(1, budget);
        //        ps.setInt(2, percent75);
        //        ps.setInt(3, percent90);
        ps.setInt(2, id);
      }
      else
      {
        ps = conn.prepareStatement(insertSql);
        ps.setString(1, Constance.LOGIN_USER);
        ps.setInt(2, year);
        ps.setInt(3, month);
        ps.setString(4, "Total");
        ps.setFloat(5, budget);
        //        ps.setInt(6, percent75);
        //        ps.setInt(7, percent90);
      }
      ps.executeUpdate();
      ps.close();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
  }


  public static void updateColor(TableModel model)
  {
    String sql = "SELECT ID, Color FROM Account WHERE ID = ?";
    try
    {
      PreparedStatement ps = conn.prepareStatement(sql);
      for (int i = 0; i < model.getRowCount(); i++)
      {
        ps.setInt(1, (Integer) model.getValueAt(i, 0));
        ResultSet rs = ps.executeQuery();
        rs.next();
        model.setValueAt(rs.getInt(2), i, 6);
        rs.close();
      }
      ps.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

  }


  public static void saveItemBudget(Integer selectedItem, Integer selectedItem2, Vector dataVector)
  {

  }
}
