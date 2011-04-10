package cat.panel;

import backup.InOutPanel;
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

import cat.Constance;
import cat.DBManager;
import cat.editor.MoneyCellEditor;

public class Budget extends JPanel {
	static Logger log = Logger.getLogger("Budget");

	JComboBox year;

	JComboBox month;

	DefaultTableModel model = new DefaultTableModel() {
		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == 0 || column == 1) {
				return false;
			}
			return true;
		}
	};

	public Budget() {
		super(new BorderLayout());
		JPanel choose = new JPanel(new GridLayout(0, 1));

		setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(10, 30, 30, 30), BorderFactory
				.createTitledBorder(
						BorderFactory.createLineBorder(Color.black), "设置")));

		Calendar c = Calendar.getInstance();

		year = new JComboBox(new Object[] { 2009, 2010, 2011, 2012 });
		month = new JComboBox(new Object[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
				12 });
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

		// ---
		JPanel configPane = new JPanel(new FlowLayout(FlowLayout.LEFT));

		categoryConfig(configPane, "支出类别编辑", "支出");
		categoryConfig(configPane, "收入类别编辑", "收入");

		choose.add(configPane);

		JLabel title = new JLabel("支出预算表");
		title.setFont(title.getFont().deriveFont(Font.BOLD, 14));
		title.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		JPanel tmp = new JPanel();
		tmp.add(title);
		choose.add(tmp);

		add(choose, BorderLayout.PAGE_START);

		// 预算表格
		JPanel savePane = new JPanel();
		savePane.setLayout(new BoxLayout(savePane, BoxLayout.PAGE_AXIS));

		final JTable budgetTable = new JTable(model);
		getTableData();
		// sourceTable.getTableHeader().setPreferredSize(new Dimension(30, 22));
		// sourceTable.setPreferredSize(new Dimension(450, 150));
		budgetTable.setPreferredScrollableViewportSize(new Dimension(450, 170));
		budgetTable.setRowHeight(22);
		budgetTable.getColumnModel().getColumn(2).setCellEditor(
				new MoneyCellEditor());

		JScrollPane scrollPane = new JScrollPane(budgetTable);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(5, 5, 0, 5), scrollPane.getBorder()));
		// scrollPane.setPreferredSize(new Dimension(450, 150));
		budgetTable.setAutoCreateRowSorter(true);
		// sourceTable.getRowSorter().toggleSortOrder(0);
		savePane.add(scrollPane);

		JButton save = new JButton("添加预算项");
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (budgetTable.isEditing()) {
					budgetTable.getCellEditor().stopCellEditing();
				}
				DBManager.saveItemBudget((Integer) year.getSelectedItem(),
						(Integer) month.getSelectedItem(), model
								.getDataVector());
				JOptionPane.showMessageDialog(Budget.this, "预算设置保存成功。");
			}
		});

		JPanel saveButtonPane = new JPanel();
		saveButtonPane.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		saveButtonPane.add(save);
		savePane.add(saveButtonPane);
		add(savePane, BorderLayout.PAGE_END);

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				TableCellEditor editor = budgetTable.getCellEditor();
				if (editor != null) {
					editor.stopCellEditing();
				}
			}
		});
	}

	private void categoryConfig(JPanel pane, String title, final String type) {
		final JButton payoutEdit = new JButton(title);
		payoutEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Source panel = new Source(SwingUtilities
						.getWindowAncestor((JButton) e.getSource()), type);
				panel.setLocationRelativeTo(Budget.this);
				panel.setVisible(true);
			}
		});
		pane.add(payoutEdit);
	}

	@SuppressWarnings("unchecked")
	private void getTableData() {
		Vector<Vector> items = DBManager.getBudgetItems((Integer) year
				.getSelectedItem(), (Integer) month.getSelectedItem());
		model.setDataVector(items, Constance.getSourceBudgetColumns());
	}

	class SelectChangeListenter implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			getTableData();
		}
	}
}
