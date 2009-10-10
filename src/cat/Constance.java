package cat;


import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Vector;


public class Constance
{
  static final DateFormat dateFormat = new SimpleDateFormat(" yyyy - MM - dd ");

  static final Vector<String> dateColumns = new Vector<String>();

  static final Vector<String> fieldColumns = new Vector<String>();

  static final Vector<String> statColumns = new Vector<String>();

  static final Vector<String> itemStatColumns = new Vector<String>();

  static final Vector<String> sourceBudgetColumns = new Vector<String>();

  static final String LOGIN_USER = "cat";
  static
  {
    dateColumns.addElement("序号");
    dateColumns.addElement("类别");
    dateColumns.addElement("日期");
    dateColumns.addElement("项目");
    dateColumns.addElement("金额");
    dateColumns.addElement("备注");
    dateColumns.addElement("颜色");

    fieldColumns.addElement("Type");
    fieldColumns.addElement("Date");
    fieldColumns.addElement("Item");
    fieldColumns.addElement("Money");
    fieldColumns.addElement("Remark");
    fieldColumns.addElement("Color");

    statColumns.addElement("日期");
    statColumns.addElement("支出");
    statColumns.addElement("收入");

    itemStatColumns.addElement("项目");
    itemStatColumns.addElement("金额");
    itemStatColumns.addElement("百分比(%)");

    sourceBudgetColumns.addElement("序号");
    sourceBudgetColumns.addElement("项目");
    sourceBudgetColumns.addElement("预算金额");
  }


  public static Vector<String> getDateColumns()
  {
    return dateColumns;
  }


  public static Vector<String> getStatColumns()
  {
    return statColumns;
  }


  public static Vector<String> getItemStatColumns()
  {
    return itemStatColumns;
  }


  public static Vector<String> getFieldColumns()
  {
    return fieldColumns;
  }


  public static Vector<String> getSourceBudgetColumns()
  {
    return sourceBudgetColumns;
  }
}
