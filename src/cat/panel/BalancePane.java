package cat.panel;

import cat.Configure;
import cat.DBManager;
import cat.DateField2;
import cat.model.Item;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import sun.swing.DefaultLookup;

public class BalancePane extends JPanel {
	static Logger log = Logger.getLogger("PreparePane");
	DateField2 selectedDate = new DateField2();
	Map<String, Integer> subcategories;
	JComboBox categoryCombox;
	JComboBox subCategoryCombox;
	JTextField moneyField;
	String type = "Expenditure";
	final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
	Map<String, Integer> categories;

	public BalancePane(String type) {
		super(new BorderLayout());
		this.type = type;
		categories = DBManager.getCategory(type);
		add(createItems(), BorderLayout.NORTH);
		add(createItems2(), BorderLayout.CENTER);
		add(createDataTable(), BorderLayout.SOUTH);
	}

	public void refreshData() {
		Vector<Vector> data = DBManager.getItemsByDate(type,
				(Date) selectedDate.getValue());
		tableModel.setDataVector(data, Configure.getDateColumns());
		arrangeColumn();
	}

	private JPanel createItems() {
		JPanel pane = new JPanel();
		pane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 5));
		pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));
		pane.add(setBorder(new JLabel("日期："), 20, 0));
		selectedDate.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				refreshData();
			}
		});

		selectedDate.setMaximumSize(new Dimension(120, 30));
		pane.add(selectedDate);

		// 创建类别
		// pane.add(Box.createHorizontalGlue());
		pane.add(setBorder(new JLabel("类别："), 20, 0));

		categoryCombox = new JComboBox(categories.keySet().toArray());
		pane.add(categoryCombox);
		pane.add(setBorder(new JLabel("小类别："), 20, 0));
		subcategories = DBManager.getSubCategory(categories
				.get((String) categoryCombox.getSelectedItem()));

		subCategoryCombox = new JComboBox(subcategories.keySet().toArray());
		subCategoryCombox.setPreferredSize(new Dimension(100, 30));
		pane.add(subCategoryCombox);
		categoryCombox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultComboBoxModel model = (DefaultComboBoxModel) subCategoryCombox
						.getModel();
				model.removeAllElements();
				subcategories = DBManager.getSubCategory(categories
						.get((String) categoryCombox.getSelectedItem()));
				for (String name : subcategories.keySet()) {
					model.addElement(name);
				}
			}
		});

		// 创建金额
		pane.add(setBorder(new JLabel("金额："), 20, 0));
		moneyField = new JTextField();
		pane.add(moneyField);
		pane.add(setBorder(new JLabel("元"), 2, 9));
		return pane;
	}

	private JPanel createItems2() {
		JPanel pane = new JPanel();
		pane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));
		pane.add(setBorder(new JLabel("用户："), 20, 0));
		final JTextField user = new JTextField();
		pane.add(user);

		pane.add(setBorder(new JLabel("场所："), 20, 0));
		final JTextField address = new JTextField();
		pane.add(address);

		pane.add(setBorder(new JLabel("备注："), 20, 0));
		final JTextField remark = new JTextField();
		pane.add(remark);

		pane.add(Box.createHorizontalStrut(20));

		final JButton save = new JButton("添加");
		pane.add(save);

		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (moneyField.getText().trim().isEmpty()) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor(BalancePane.this), "金额不能为空！",
							"输入错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// 验证金额是否为数字，没有使用JFormattedTextField
				Pattern pattern = Pattern.compile("^[0-9\\.]+$");
				Matcher m = pattern.matcher(moneyField.getText());
				if (!m.matches()) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor(BalancePane.this), "请输入正确的金额！",
							"输入错误", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// 保存
				float money = Float.valueOf(moneyField.getText());
				Item item = new Item();
				item.setTime(((Date) selectedDate.getValue()).getTime());
				item.setMoney(money);
				item.setCategoryID(subcategories.get((String) subCategoryCombox
						.getSelectedItem()));
				item.setRemark(remark.getText());
				item.setUser(user.getText());
				item.setAddress(address.getText());
				int rowID = DBManager.saveItem(item);

				// 显示
				/*
				 * Vector data = new Vector(); data.add(rowID);
				 * data.add(table.getRowCount() + 1);
				 * data.add(sf.format(selectedDate.getValue()));
				 * data.add(categoryCombox.getSelectedItem());
				 * 
				 * data.add(subCategoryCombox.getSelectedItem());
				 * data.add(moneyField.getText()); data.add(user.getText());
				 * data.add(address.getText()); data.add(remark.getText());
				 * 
				 * tableModel.addRow(data);
				 * 
				 * Vector<Vector> data = DBManager.getItemsByDate(type, (Date)
				 * selectedDate.getValue()); tableModel.setDataVector(data,
				 * Configure.getDateColumns()); arrangeColumn();
				 */
				refreshData();
			}
		});
		return pane;
	}

	private JLabel setBorder(JLabel label, int left, int right) {
		label.setBorder(BorderFactory.createEmptyBorder(0, left, 0, right));
		return label;
	}

	JTable table;
	DefaultTableModel tableModel;

	private void arrangeColumn() {
		// 隐藏ID列
		TableColumn idCol = table.getColumnModel().getColumn(0);
		idCol.setMaxWidth(0);
		idCol.setMinWidth(0);
		idCol.setPreferredWidth(0);

		// 隐藏颜色列

		TableColumn colorCol = table.getColumnModel().getColumn(9);
		colorCol.setMaxWidth(0);
		colorCol.setMinWidth(0);
		colorCol.setPreferredWidth(0);

		TableColumn seqCol = table.getColumnModel().getColumn(1);
		seqCol.setMaxWidth(45);

		table.getColumnModel().getColumn(5).setCellRenderer(
				new DefaultTableCellRenderer() {
					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {

						Color color = (Color) table.getModel().getValueAt(row,
								9);
						if (!Color.white.equals(color)) {
							super.setBackground(color);
						} else {
							super.setBackground(null);
						}

						return super.getTableCellRendererComponent(table,
								value, isSelected, hasFocus, row, column);
					}
				});

	}

	private JScrollPane createDataTable() {
		Vector<Vector> data = DBManager.getItemsByDate(type,
				(Date) selectedDate.getValue());
		// type);
		tableModel = new DefaultTableModel(data, Configure.getDateColumns()) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		table = new JTable(tableModel);
		table.setRowHeight(22);
		arrangeColumn();

		table.setAutoCreateRowSorter(true);
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
					JPopupMenu popup = new JPopupMenu();
					JMenuItem menuItem = new JMenuItem("编辑");
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int selectedRow = table.getSelectedRow();
							if (selectedRow == -1) {
								JOptionPane.showMessageDialog(SwingUtilities
										.getWindowAncestor(BalancePane.this),
										"请选择要编辑的行！", "错误",
										JOptionPane.ERROR_MESSAGE);
								return;
							}

							Item item = new Item();
							item.setId((Integer) table.getValueAt(selectedRow,
									0));
							item.setTime(((Date) table.getValueAt(selectedRow,
									2)).getTime());
							item.setParentCategoryName((String) table
									.getValueAt(selectedRow, 3));
							item.setCategoryName((String) table.getValueAt(
									selectedRow, 4));
							item.setMoney((Float) table.getValueAt(selectedRow,
									5));
							item.setUser((String) table.getValueAt(selectedRow,
									6));
							item.setAddress((String) table.getValueAt(
									selectedRow, 7));
							item.setRemark((String) table.getValueAt(
									selectedRow, 8));

							EditDialog dialog = new EditDialog(SwingUtilities
									.getWindowAncestor(BalancePane.this), item,
									type, BalancePane.this, selectedRow);
							dialog.setVisible(true);
						}
					});
					popup.add(menuItem);
					menuItem = new JMenuItem("删除");
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int selectedRow = table.getSelectedRow();
							if (selectedRow == -1) {
								JOptionPane.showMessageDialog(SwingUtilities
										.getWindowAncestor(BalancePane.this),
										"请选择要编辑的行！", "错误",
										JOptionPane.ERROR_MESSAGE);
								return;
							}
							int result = JOptionPane
									.showConfirmDialog(
											SwingUtilities
													.getWindowAncestor(BalancePane.this),
											"你确定要删除选择的内容？", "删除内容",
											JOptionPane.YES_NO_OPTION,
											JOptionPane.INFORMATION_MESSAGE);
							if (result == 0)
								DBManager.deleteItem((Integer) tableModel
										.getValueAt(selectedRow, 0));
							tableModel.removeRow(selectedRow);
						}
					});
					popup.add(menuItem);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		// new TableUtility().makeTableCell(table);

		table.setPreferredScrollableViewportSize(new Dimension(400, 300));
		JScrollPane s = new JScrollPane(table);
		s.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		s.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(20, 20, 10, 10), s.getBorder()));
		return s;
	}
}
