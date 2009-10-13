package cat.panel;


import cat.DBManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;


public class Source
    extends JDialog
{
  Logger log = Logger.getLogger("listModel");

  private JTextField item;

  private JList list;

  private DefaultListModel listModel;

  private String addString = "ADD";

  private String type;


  public Source(Window frame, String type)
  {
    super(frame, type + "来源", Dialog.ModalityType.DOCUMENT_MODAL);
    setLayout(new BorderLayout());
    this.type = type;

    JPanel leftPanel = new JPanel();
    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
    JPanel itemPanel = new JPanel();

    itemPanel.add(new JLabel("名称："));
    item = new JTextField();
    AddListener addListener = new AddListener();
    item.addActionListener(addListener);
    item.setPreferredSize(new Dimension(100, 25));
    itemPanel.add(item);
    leftPanel.add(itemPanel);

    JButton add = new JButton("添加");
    add.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    add.setActionCommand(addString);
    add.addActionListener(addListener);

    leftPanel.add(add);
    leftPanel.add(Box.createVerticalStrut(150));

    JButton close = new JButton("关闭");
    close.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Source.this.setVisible(false);
        Source.this.dispose();
      }
    });
    leftPanel.add(close);
    listModel = new DefaultListModel();

    Vector<String> items = null;
    if (type.equalsIgnoreCase("支出"))
    {
      items = DBManager.getPayoutItems();
    }
    else
    {
      items = DBManager.getIncomeItems();
    }

    for (String item : items)
    {
      listModel.addElement(item);
    }
    list = new JList(listModel);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setSelectedIndex(0);

    list.setVisibleRowCount(10);
    JScrollPane listScrollPane = new JScrollPane(list);
    listScrollPane.setPreferredSize(new Dimension(100, 200));
    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.add(listScrollPane, BorderLayout.PAGE_START);

    JButton delete = new JButton("删除");
    delete.addActionListener(new DeleteListener());
    rightPanel.add(delete, BorderLayout.PAGE_END);
    leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(leftPanel, BorderLayout.LINE_START);
    add(rightPanel, BorderLayout.LINE_END);
    pack();
  }

  class AddListener
      implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      int index = listModel.getSize();
      String source = item.getText().trim();
      Component parent = null;
      if (e.getSource() instanceof JButton)
      {
        parent = (JButton) e.getSource();
      }
      else if (e.getSource() instanceof JTextField)
      {
        parent = (JTextField) e.getSource();
      }

      if (source.equalsIgnoreCase(""))
      {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent), "不能添加空白数据!", "添加错误",
            JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (listModel.indexOf(source) != -1)
      {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent), "数据已经存在,不用再添加!!", "添加错误",
            JOptionPane.WARNING_MESSAGE);
        return;

      }
      DBManager.insertSource(type, source);
      listModel.addElement(source);
      list.setSelectedIndex(index);
      list.ensureIndexIsVisible(index);

    }
  }

  class DeleteListener
      implements ActionListener
  {

    public void actionPerformed(ActionEvent e)
    {
      int index = list.getSelectedIndex();
      if (index == -1)
      {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor((JButton) e.getSource()), "列表任何内容!");
        return;
      }

      String source = (String) list.getSelectedValue();
      DBManager.deleteSource(type, source);
      listModel.removeElementAt(index);
      if (index == 0)
      {
        index = 0;
      }
      else
      {
        index--;
      }
      list.setSelectedIndex(index);
      list.ensureIndexIsVisible(index);

    }
  }
}
