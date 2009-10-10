package cat.panel;


import cat.Constance;
import cat.DBManager;
import cat.DateField2;
import cat.TableUtility;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.EventObject;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;


public abstract class InOutPanel
    extends JPanel
    implements ActionListener, TableModelListener//, FocusListener
{
  static Logger log = Logger.getLogger("InOutPanel");

  JTable table;

  DefaultTableModel defaultModel;

  DateField2 incomeDate = new DateField2();

  JComboBox itemList;

  JTextField money;

  JTextArea remark;

  String type = null;

  SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

  String[] lines = null;

  boolean changedDate = false;


  public InOutPanel(String type, String[] lines)
  {
    super(new BorderLayout());
    this.type = type;
    this.lines = lines;

    if ("支出".equalsIgnoreCase(type))
    {
      itemList = new JComboBox(DBManager.getPayoutItems());
    }
    else
    {
      itemList = new JComboBox(DBManager.getIncomeItems());
    }

    JPanel itemPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 0, 2, 3);
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 1;
    firstLine(c, itemPanel);
    secondLine(c, itemPanel);
    thirdLine(c, itemPanel);
    fourLine(c, itemPanel);
    fiveLine(c, itemPanel);
    itemPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    //    itemPanel.setBorder(BorderFactory.createLineBorder(Color.red));
    this.add(itemPanel, BorderLayout.PAGE_START);
    createDataTable(this);
    this.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
    this.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (table.isEditing())
        {
          table.getCellEditor().stopCellEditing();
        }
      }
    });
  }


  private void firstLine(GridBagConstraints c, Container container)
  {
    c.gridwidth = 4;
    container.add(new JLabel(lines[0]), c);

    c.gridwidth = 1;
    container.add(new JLabel("日期:"), c);

    c.gridwidth = GridBagConstraints.REMAINDER;
    incomeDate.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        DateField2 date = (DateField2) e.getSource();
        dataChange(date);
      }
    });

    incomeDate.setPreferredSize(new Dimension(162, 25));

    //    incomeDate.addFocusListener(this);
    container.add(incomeDate, c);
  }


  private void dataChange(DateField2 date)
  {
    changedDate = true;

    Vector<Vector> obj = DBManager.getItemsByType(sf.format(date.getValue()), type);
    defaultModel.setDataVector(obj, Constance.getDateColumns());

    new TableUtility().makeTableCell(table);

    changedDate = false;

  }


  private void secondLine(GridBagConstraints c, Container container)
  {
    c.gridwidth = 4;
    container.add(new JLabel(lines[1]), c);

    c.gridwidth = 1;
    container.add(new JLabel("项目:"), c);

//    c.gridwidth = 2;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.NONE;

    itemList.setPreferredSize(new Dimension(162, 20));
    container.add(itemList, c);

    c.gridwidth = GridBagConstraints.REMAINDER;
    /*final JButton edit = new JButton("编辑");
    edit.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        Source panel = new Source(SwingUtilities.getWindowAncestor((JButton) e.getSource()), type);
        panel.setLocationRelativeTo(InOutPanel.this);
        panel.setVisible(true);
      }

    });
    container.add(edit, c);
*/  }


  private void thirdLine(GridBagConstraints c, Container container)
  {
    c.gridwidth = 4;
    container.add(new JLabel(lines[2]), c);

    c.gridwidth = 1;
    container.add(new JLabel("金额:"), c);

    c.gridwidth = GridBagConstraints.REMAINDER;
    money = new JFormattedTextField(new DecimalFormat("##.##"));
    money.setPreferredSize(new Dimension(162, 20));
    container.add(money, c);
  }


  private void fourLine(GridBagConstraints c, Container container)
  {
    c.gridwidth = 4;
    JLabel label = new JLabel(lines[3]);
    label.setPreferredSize(new Dimension(300, 40));
    container.add(label, c);

    c.gridwidth = 1;
    container.add(new JLabel("备注:"), c);

    c.gridwidth = 2;
    c.gridheight = 2;
    remark = new JTextArea(4, 23);
    remark.setLineWrap(true);
    remark.setWrapStyleWord(true);
    remark.setBorder(BorderFactory.createLineBorder(Color.gray));
    container.add(remark, c);

    c.anchor = GridBagConstraints.SOUTH;
    c.gridwidth = 2;
    JButton add = new JButton("添加");
    container.add(add, c);
    add.setActionCommand("add");
    add.addActionListener(this);
  }


  private void fiveLine(GridBagConstraints c, Container container)
  {
    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 4;
    c.gridheight = 1;
    container.add(new JLabel(lines[4]), c);

    /*c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = GridBagConstraints.REMAINDER;
    container.add(new JLabel("提示：可以选择单元格进行编辑。"), c);*/
  }


  private void createDataTable(Container container)
  {
    Vector obj = DBManager.getItemsByType(sf.format(new Date()), type);
    defaultModel = new DefaultTableModel(obj, Constance.getDateColumns()) {
      @Override
      public boolean isCellEditable(int row, int column)
      {
        if (column == 0 || column == 1)
        {
          return false;
        }
        return true;
      }
    };
    table = new JTable(defaultModel);
    //    table.getTableHeader().setFont(UIManager.getFont("Button.font").deriveFont(Font.BOLD, 14f));
    table.getTableHeader().setPreferredSize(new Dimension(30, 22));
    new TableUtility().makeTableCell(table);

    table.setPreferredScrollableViewportSize(new Dimension(400, 150));
    defaultModel.addTableModelListener(this);
    JScrollPane s = new JScrollPane(table);
    s.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (table.isEditing())
        {
          table.getCellEditor().stopCellEditing();
        }
      }
    });
    s.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    container.add(s, BorderLayout.CENTER);
  }


  /**
   * 添加项目
   */
  @SuppressWarnings("unchecked")
  public void actionPerformed(ActionEvent e)
  {
    String date = sf.format(incomeDate.getValue());
    String item = itemList.getSelectedItem().toString();
    if (money.getText().trim().isEmpty())
    {
      JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "金额不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
      return;
    }
    float volumn = Float.valueOf(money.getText());
    String desc = remark.getText();

    if ("add".equalsIgnoreCase(e.getActionCommand()))
    {
      //从数据库加载一次数据,而非defaultModel,因为Color值会动态变化的.
      DBManager.insert(type, date, item, volumn, desc);
      /*int rlt[] = 
      Vector data = new Vector();
      data.add(rlt[0]);
      data.add(type);
      data.add(date);
      data.add(item);
      data.add(volumn);
      data.add(desc);
      data.add(rlt[1]);
      defaultModel.addRow(data);
      SwingUtilities.updateComponentTreeUI(table);*/
      dataChange(incomeDate);
    }
  }


  public void tableChanged(TableModelEvent e)
  {
    new TableUtility().tableChanged(e, false);
    //执行一次全选,目的是迫使TableRender执行一次背景色的加载
    table.selectAll();
    table.clearSelection();
  }

}
