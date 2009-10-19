package cat.panel;


import cat.Constance;
import cat.DBManager;
import cat.editor.MoneyCellEditor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;


public class Budget
    extends JPanel
{
  static Logger log = Logger.getLogger("Budget");

  final JTextField totalPayout = new JTextField(10);

  final JLabel percent1 = new JLabel("超支 1");

  final JLabel percent2 = new JLabel("超支 2");

  JComboBox year;

  JComboBox month;

  DefaultTableModel model = new DefaultTableModel()
  {
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


  public Budget(final InOutPanel inOutPanel)
  {
    super(new BorderLayout());
    JPanel choose = new JPanel(new GridLayout(0, 1));

    setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 30, 30, 30), BorderFactory
        .createTitledBorder(BorderFactory.createLineBorder(Color.black), "设置")));

    Calendar c = Calendar.getInstance();

    year = new JComboBox(new Object[]{2009, 2010, 2011, 2012});
    month = new JComboBox(new Object[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
    JPanel yearMonth = new JPanel(new FlowLayout(FlowLayout.LEFT));
    yearMonth.add(new JLabel("选择年月："));
    year.setSelectedItem(c.get(Calendar.YEAR));
    month.setSelectedItem(c.get(Calendar.MONTH) + 1);

    yearMonth.add(year);
    yearMonth.add(new JLabel("年"));
    yearMonth.add(month);
    yearMonth.add(new JLabel("月"));
    choose.add(yearMonth);

    year.addItemListener(new SelectChangeListenter());
    month.addItemListener(new SelectChangeListenter());

    JPanel payoutBudget = new JPanel(new FlowLayout(FlowLayout.LEFT));
    payoutBudget.add(new JLabel("总支出预算："));

    totalPayout.setText(String.valueOf(DBManager.getTotalBudget((Integer) year.getSelectedItem(), (Integer) month
        .getSelectedItem())));
    payoutBudget.add(totalPayout);
    payoutBudget.add(new JLabel("元"));
    choose.add(payoutBudget);

    //---
    JPanel configPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton color = new JButton("超支颜色设定");

    color.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        final JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(Budget.this), ModalityType.DOCUMENT_MODAL);
        //        Container pane = dialog.getContentPane();
        JPanel pane = new JPanel(new GridLayout(0, 1));
        int[] color = DBManager.getBudgetColor();
        percent1.setBackground(new Color(color[0]));
        percent2.setBackground(new Color(color[1]));

        pane.add(colorSet(percent1, "75%"));
        pane.add(colorSet(percent2, "90%"));

        JPanel buttonPane = new JPanel();
        JButton saveColor = new JButton("保存");
        saveColor.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            DBManager.saveBudgetColor(percent1.getBackground().getRGB(), percent2.getBackground().getRGB());

            JOptionPane.showMessageDialog(Budget.this, "超支颜色设置成功！");

            inOutPanel.fireDataChange();
          }
        });
        buttonPane.add(saveColor);
        pane.add(buttonPane);

        pane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
        pane.getActionMap().put("exit", new AbstractAction()
        {
          public void actionPerformed(ActionEvent e)
          {
            dialog.dispose();
          }
        });

        dialog.getContentPane().add(pane);
        dialog.pack();
        dialog.setLocationRelativeTo(Budget.this);
        dialog.setVisible(true);

      }

    });
    configPane.add(color);
    sourceConfig(configPane, "支出项目编辑", "支出");
    sourceConfig(configPane, "收入项目编辑", "收入");

    choose.add(configPane);

    JLabel title = new JLabel("项目预算表");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 14));
    title.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    JPanel tmp = new JPanel();
    tmp.add(title);
    choose.add(tmp);

    add(choose, BorderLayout.PAGE_START);

    //预算表格

    JPanel savePane = new JPanel();
    savePane.setLayout(new BoxLayout(savePane, BoxLayout.PAGE_AXIS));

    final JTable budgetTable = new JTable(model);
    getTableData();
    //    sourceTable.getTableHeader().setPreferredSize(new Dimension(30, 22));
    //    sourceTable.setPreferredSize(new Dimension(450, 150));
    budgetTable.setPreferredScrollableViewportSize(new Dimension(450, 130));
    budgetTable.setRowHeight(22);
    budgetTable.getColumnModel().getColumn(2).setCellEditor(new MoneyCellEditor());

    JScrollPane scrollPane = new JScrollPane(budgetTable);
    scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5), scrollPane
        .getBorder()));
    //    scrollPane.setPreferredSize(new Dimension(450, 150));
    budgetTable.setAutoCreateRowSorter(true);
    //    sourceTable.getRowSorter().toggleSortOrder(0);
    savePane.add(scrollPane);

    JButton save = new JButton("保存");
    save.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if ("".equalsIgnoreCase(totalPayout.getText().trim()))
        {
          JOptionPane.showMessageDialog(Budget.this, "预算数值不合法！");
          return;
        }
        if (budgetTable.isEditing())
        {
          budgetTable.getCellEditor().stopCellEditing();
        }
        DBManager.saveTotalBudget((Integer) year.getSelectedItem(), (Integer) month.getSelectedItem(), Integer
            .valueOf(totalPayout.getText()));
        DBManager.saveItemBudget((Integer) year.getSelectedItem(), (Integer) month.getSelectedItem(), model
            .getDataVector());
        inOutPanel.fireDataChange();
        JOptionPane.showMessageDialog(Budget.this, "预算设置保存成功。");
      }
    });
    JPanel tmp2 = new JPanel();
    tmp2.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    tmp2.add(save);
    savePane.add(tmp2);
    add(savePane, BorderLayout.PAGE_END);

    this.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        TableCellEditor editor = budgetTable.getCellEditor();
        if (editor != null)
        {
          editor.stopCellEditing();
        }
      }
    });

  }


  private void sourceConfig(JPanel pane, String title, final String type)
  {
    final JButton payoutEdit = new JButton(title);
    payoutEdit.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Source panel = new Source(SwingUtilities.getWindowAncestor((JButton) e.getSource()), type);
        panel.setLocationRelativeTo(Budget.this);
        panel.setVisible(true);
      }

    });
    pane.add(payoutEdit);

  }


  private JPanel colorSet(final JLabel label, String percent)
  {
    JPanel overspend = new JPanel(new FlowLayout(FlowLayout.LEFT));
    overspend.add(new JLabel("超支 " + percent + " 时候的颜色设定："));

    label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
    label.setOpaque(true);
    overspend.add(label);

    JButton colorChoose = new JButton("选择颜色");
    colorChoose.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Color newColor = JColorChooser.showDialog(Budget.this, "选择超支颜色", label.getBackground());
        if (newColor != null)
        {
          label.setBackground(newColor);
        }
      }
    });

    overspend.add(colorChoose);
    return overspend;
  }


  @SuppressWarnings("unchecked")
  private void getTableData()
  {
    Vector<Vector> data = new Vector<Vector>();
    Vector<String> items = DBManager.getPayoutItems();
    int i = 1;
    for (String item : items)
    {
      Vector v = new Vector();
      v.addElement(i++);
      v.addElement(item);
      v.addElement(DBManager.getBudget((Integer) year.getSelectedItem(), (Integer) month.getSelectedItem(), item));
      data.addElement(v);
    }
    model.setDataVector(data, Constance.getSourceBudgetColumns());
  }

  class SelectChangeListenter
      implements ItemListener
  {
    @Override
    public void itemStateChanged(ItemEvent e)
    {
      totalPayout.setText(String.valueOf(DBManager.getTotalBudget((Integer) year.getSelectedItem(), (Integer) month
          .getSelectedItem())));
      getTableData();
    }
  }
}
