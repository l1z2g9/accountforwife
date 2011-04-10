package backup;

import cat.Constance;
import cat.DBManager;
import cat.DateField2;
import cat.TableUtility;
import cat.model.Item;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public abstract class InOutPanel extends JPanel implements TableModelListener// ,
// FocusListener
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

	public InOutPanel(String type, String[] lines) {
		super(new BorderLayout());
		this.type = type;
		this.lines = lines;

		/*
		 * if ("支出".equalsIgnoreCase(type)) { itemList = new
		 * JComboBox(DBManager.getPayoutItems()); } else { itemList = new
		 * JComboBox(DBManager.getIncomeItems()); }
		 */
		JPanel itemPanel = new JPanel(new BorderLayout());
		itemPanel.setPreferredSize(new Dimension(460, 170));

		firstColumn(itemPanel);
		secondColumn(itemPanel);
		thirdColumn(itemPanel);

		itemPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		this.add(itemPanel, BorderLayout.PAGE_START);
		createDataTable(this);
		this.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (table.isEditing()) {
					table.getCellEditor().stopCellEditing();
				}
			}
		});

		addComponentListener(new ComponentAdapter() // 窗体显示时，获得焦点
		{
			@Override
			public void componentShown(ComponentEvent ce) {
				money.requestFocusInWindow();
			}
		});
	}

	private void firstColumn(Container container) {
		JPanel pane = new JPanel(new GridLayout(0, 1));
		pane.setPreferredSize(new Dimension(280, 200));
		pane.add(new JLabel(lines[0]));
		pane.add(new JLabel(lines[1]));
		pane.add(new JLabel(lines[2]));
		pane.add(new JLabel(lines[3]));
		pane.add(new JLabel(lines[4]));
		container.add(pane, BorderLayout.WEST);
	}

	private void secondColumn(Container container) {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		// pane.setPreferredSize(new Dimension(0, 200));
		// 第一列
		JPanel datePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		datePane.add(createBorder(new JLabel("日期:")));
		incomeDate.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// DateField2 date = (DateField2) e.getSource();
				fireDataChange();
			}
		});

		incomeDate.setPreferredSize(new Dimension(130, 30));

		datePane.add(incomeDate);
		pane.add(datePane);
		// 第二列
		JPanel itemPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		itemPane.add(createBorder(new JLabel("项目:")));
		itemList.setPreferredSize(new Dimension(130, 30));
		itemPane.add(itemList);
		pane.add(itemPane);
		// 第三列
		JPanel moneyPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		moneyPane.add(createBorder(new JLabel("金额:")));
		money = new JFormattedTextField(new DecimalFormat("##.##"));
		money.setColumns(11);
		money.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				"submit");
		money.getActionMap().put("submit", new SubmitAction(this));
		moneyPane.add(money);
		pane.add(moneyPane);
		// 第四列
		JPanel remarkPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		remarkPane.add(createBorder(new JLabel("备注:")));
		remark = new JTextArea(2, 11);
		remark.setLineWrap(true);
		remark.setWrapStyleWord(true);

		// remark.setBorder(BorderFactory.createLineBorder(Color.gray));
		remarkPane.add(remark);
		pane.add(remarkPane);
		container.add(pane, BorderLayout.CENTER);
	}

	private JLabel createBorder(JLabel label) {
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
		return label;
	}

	private void thirdColumn(Container container) {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		pane.setPreferredSize(new Dimension(100, 0));
		pane.add(Box.createVerticalGlue());
		JButton add = new JButton(new SubmitAction(this));
		// add.setActionCommand("add");
		add.setText("添加");
		pane.add(add);
		container.add(pane, BorderLayout.EAST);
	}

	public void fireDataChange() {
		changedDate = true;

		Vector<Vector> obj = null;// DBManager.getItemsByType(sf.format(incomeDate
		// .getValue()), type);
		defaultModel.setDataVector(obj, Constance.getDateColumns());

		new TableUtility().makeTableCell(table);

		changedDate = false;
	}

	private void createDataTable(Container container) {
		Vector obj = null;// DBManager.getItemsByType(sf.format(new Date()),
		// type);
		defaultModel = new DefaultTableModel(obj, Constance.getDateColumns()) {
			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 0 || column == 1) {
					return false;
				}
				return true;
			}
		};
		table = new JTable(defaultModel);
		new TableUtility().makeTableCell(table);

		table.setPreferredScrollableViewportSize(new Dimension(400, 150));
		defaultModel.addTableModelListener(this);
		JScrollPane s = new JScrollPane(table);
		s.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (table.isEditing()) {
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
	class SubmitAction extends AbstractAction {
		JComponent c;

		public SubmitAction(JComponent c) {
			this.c = c;
		}

		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			String date = sf.format(incomeDate.getValue());
			String item = itemList.getSelectedItem().toString();
			if (money.getText().trim().isEmpty()) {
				JOptionPane.showMessageDialog(SwingUtilities
						.getWindowAncestor(this.c), "金额不能为空！", "错误",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			float volumn = Float.valueOf(money.getText());
			String desc = remark.getText();

			// if ("add".equalsIgnoreCase(e.getActionCommand()))
			// {
			// 从数据库加载一次数据,而非defaultModel,因为Color值会动态变化的，所以取消使用defaultModel.addRow(data);
			Item data = new Item();

			// DBManager.insertItem(type, date, item, volumn, desc);
			fireDataChange();
			// }
		}
	}

	public void tableChanged(TableModelEvent e) {
		new TableUtility().tableChanged(e, false);
		// 执行一次全选,目的是迫使TableRender执行一次背景色的加载
		table.selectAll();
		table.clearSelection();
	}

}
