package cat.panel;

import cat.Configure;
import cat.DBManager;
import cat.DateField2;
import cat.model.NavigatePage;
import cat.model.Overdraw;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class OverdrawPane extends JPanel implements ItemListener {
	DateField2 overdrawDate = new DateField2();
	JTextField outputMoney = new JTextField();
	JTextField address = new JTextField();
	JTextField remark = new JTextField();

	DateField2 returnDate = new DateField2();
	JTextField inputMoney = new JTextField();
	JTextField returnRemark = new JTextField();

	JButton save = new JButton("保存");

	// 数据表格
	final JButton previous = new JButton(new ImageIcon(getClass().getResource(
			"/images/back.png")));
	final JButton forward = new JButton(new ImageIcon(getClass().getResource(
			"/images/forward.png")));
	JLabel summaryMoney = new JLabel();
	final JLabel number = new JLabel();
	JComboBox year = new JComboBox(
			new Object[] { 2011, 2012, 2013, 2014, 2015 });
	JComboBox month = new JComboBox(new Object[] { 1, 2, 3, 4, 5, 6, 7, 8, 9,
			10, 11, 12 });
	NumberFormat nf = DecimalFormat.getCurrencyInstance();
	int currentPage = 1;
	int totalPage = 1;
	DefaultTableModel tableModel = new DefaultTableModel(new Vector(),
			Configure.getOverDrawColumns()) {
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	JTable table = new JTable(tableModel);

	public OverdrawPane() {
		super(new BorderLayout());
		add(createItems(), BorderLayout.NORTH);
		add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.CENTER);
		add(createDataTable(), BorderLayout.SOUTH);
		configAction();
	}

	private JPanel createItems() {
		JPanel itemsPane = new JPanel(new GridLayout(0, 1));
		itemsPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 5));

		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));

		pane.add(setBorder(new JLabel("支付日期："), 20, 0));
		overdrawDate.setMaximumSize(new Dimension(120, 30));
		pane.add(overdrawDate);

		pane.add(setBorder(new JLabel("支付金额："), 20, 0));
		pane.add(outputMoney);
		pane.add(setBorder(new JLabel("元"), 0, 0));

		pane.add(setBorder(new JLabel("场所："), 20, 0));
		pane.add(address);

		pane.add(setBorder(new JLabel("备注："), 20, 0));
		pane.add(remark);
		itemsPane.add(pane);
		// 第二行
		pane = new JPanel();

		pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));
		pane.add(setBorder(new JLabel("归还日期："), 20, 0));
		returnDate.setMaximumSize(new Dimension(120, 30));
		pane.add(returnDate);

		pane.add(setBorder(new JLabel("归还金额："), 20, 0));
		pane.add(inputMoney);
		pane.add(setBorder(new JLabel("元"), 0, 0));

		pane.add(setBorder(new JLabel("归还备注："), 20, 0));
		pane.add(returnRemark);
		returnRemark.setPreferredSize(new Dimension(40, 30));
		pane.add(Box.createHorizontalStrut(23));
		pane.add(save);

		itemsPane.add(pane);
		return itemsPane;
	}

	private JLabel setBorder(JLabel label, int left, int right) {
		label.setBorder(BorderFactory.createEmptyBorder(0, left, 0, right));
		return label;
	}

	private JPanel createDataTable() {
		// 年月
		JPanel pane = new JPanel(new BorderLayout());
		JPanel controlPane = new JPanel(new BorderLayout());
		JPanel itemPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		itemPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 10));

		Calendar c = Calendar.getInstance();
		itemPane.add(new JLabel("选择年月："));
		year.setSelectedItem(c.get(Calendar.YEAR));
		month.setSelectedItem(c.get(Calendar.MONTH) + 1);

		itemPane.add(new JLabel("年"));
		itemPane.add(year);
		itemPane.add(new JLabel("月"));
		itemPane.add(month);
		itemPane.add(summaryMoney);
		controlPane.add(itemPane, BorderLayout.LINE_START);
		// 导航
		previous.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		forward.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

		JPanel navigator = new JPanel();
		navigator.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		navigator.setLayout(new BoxLayout(navigator, BoxLayout.LINE_AXIS));
		navigator.add(previous);
		navigator.add(Box.createHorizontalGlue());
		navigator.add(number);
		navigator.add(Box.createHorizontalGlue());
		navigator.add(forward);
		controlPane.add(navigator, BorderLayout.LINE_END);

		pane.add(controlPane, BorderLayout.NORTH);

		// 表格
		table.setRowHeight(22);
		table.setAutoCreateRowSorter(true);
		// 隐藏ID列
		TableColumn idCol = table.getColumnModel().getColumn(0);
		idCol.setMaxWidth(0);
		idCol.setMinWidth(0);
		idCol.setPreferredWidth(0);

		table.setPreferredScrollableViewportSize(new Dimension(400, 270));
		JScrollPane s = new JScrollPane(table);
		s.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		s.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(0, 20, 10, 10), s.getBorder()));

		pane.add(s, BorderLayout.SOUTH);
		refreshTableData();
		return pane;
	}

	private void configAction() {
		year.addItemListener(this);
		month.addItemListener(this);
		previous.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentPage >= 2) {
					currentPage = Integer.valueOf(currentPage) - 1;
				}
				if (currentPage <= 1) {
					previous.setEnabled(false);
				}
				forward.setEnabled(true);
				refreshTableData();
			}
		});

		forward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentPage < totalPage) {
					currentPage = Integer.valueOf(currentPage) + 1;
				}
				if (currentPage >= totalPage) {
					forward.setEnabled(false);
				}
				previous.setEnabled(true);
				refreshTableData();
			}
		});
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String money = outputMoney.getText();
				if (!money.matches("^[\\d\\.]+$")) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor(OverdrawPane.this), "请输入正确的金额！",
							"输入错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String returnMoney = inputMoney.getText();
				if (returnMoney.length() != 0
						&& !returnMoney.matches("^[\\d\\.]+$")) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor(OverdrawPane.this), "请输入正确的金额！",
							"输入错误", JOptionPane.ERROR_MESSAGE);
					return;
				}

				Overdraw overdraw = new Overdraw();
				overdraw.setTime(((Date) overdrawDate.getValue()).getTime());
				overdraw.setMoney(Float.parseFloat(money));
				overdraw.setRemark(remark.getText());
				overdraw.setAddress(address.getText());

				if (returnMoney.length() != 0) {
					overdraw.setReturnMoney(Float.parseFloat(returnMoney));
					overdraw.setReturnTime(((Date) returnDate.getValue())
							.getTime());
					overdraw.setReturnRemark(returnRemark.getText());
				}

				DBManager.saveOverDrawItems(overdraw);
				refreshTableData();
			}
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
					JPopupMenu popup = new JPopupMenu();
					JMenuItem menuItem = new JMenuItem("编辑");
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int selectedRow = table.getSelectedRow();
							if (selectedRow == -1) {
								JOptionPane.showMessageDialog(SwingUtilities
										.getWindowAncestor(OverdrawPane.this),
										"请选择要编辑的行！", "错误",
										JOptionPane.ERROR_MESSAGE);
								return;
							}

							int id = (Integer) table.getValueAt(selectedRow, 0);

							OverDrawDailog dialog = new OverDrawDailog(
									SwingUtilities
											.getWindowAncestor(OverdrawPane.this),
									id, OverdrawPane.this);
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
										.getWindowAncestor(OverdrawPane.this),
										"请选择要编辑的行！", "错误",
										JOptionPane.ERROR_MESSAGE);
								return;
							}
							int result = JOptionPane
									.showConfirmDialog(
											SwingUtilities
													.getWindowAncestor(OverdrawPane.this),
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

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		refreshTableData();
	}

	public void refreshTableData() {
		final NavigatePage navigatePage = DBManager.getOverDrawItems(
				(Integer) year.getSelectedItem(), (Integer) month
						.getSelectedItem(), currentPage);

		totalPage = navigatePage.getTotalPage();

		tableModel.setDataVector(navigatePage.getCurrentPageResult(), Configure
				.getOverDrawColumns());

		// 隐藏ID列
		TableColumn idCol = table.getColumnModel().getColumn(0);
		idCol.setMaxWidth(0);
		idCol.setMinWidth(0);
		idCol.setPreferredWidth(0);

		TableColumn seqCol = table.getColumnModel().getColumn(1);
		seqCol.setMaxWidth(45);
		// 更新统计结果面板
		float incomeTotal = navigatePage.getTotalIncome();
		float expenditureTotal = navigatePage.getTotalExpenditure();

		summaryMoney.setText("总归还额：" + nf.format(incomeTotal) + "   总预付额："
				+ nf.format(expenditureTotal));

		number.setText(currentPage + " / " + totalPage + " 页");

		if (currentPage == 1)
			previous.setEnabled(false);
		if (currentPage < totalPage)
			forward.setEnabled(true);
		else
			forward.setEnabled(false);
	}
}
