package backup;

import cat.DBManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class TableUtility {
	static Logger log = Logger.getLogger("TableUtility");

	// static String[] fields = {"type", "date", "item", "money", "remark",
	// "color"};

	public void deleteData(final JTable table) {
		if (table.getSelectionModel().isSelectionEmpty()) {
			JOptionPane.showMessageDialog(SwingUtilities
					.getWindowAncestor(table), "请选择数据后再执行删除!");
			return;
		}
		int row = table.getSelectedRow();
		String message = "你确定删除这些数据：";
		for (int i = 1; i < 6; i++) {
			if (i == 4) {
				message = message + table.getValueAt(row, i);
			} else {
				message = message + table.getValueAt(row, i) + " , ";
			}
		}

		int choose = JOptionPane.showConfirmDialog(null, message, "删除数据",
				JOptionPane.YES_NO_OPTION);
		if (choose == 0) {
			int id = (Integer) table.getValueAt(row, 0);
			DBManager.deleteItem(id);
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.removeRow(row);
			// DBManager.updateColor(model);
		}
	}

	public void makeTableCell(final JTable table) {
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				"delete");
		table.getActionMap().put("delete", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				deleteData(table);
			}
		});
		table.setRowHeight(22);
		// table.getColumnModel().getColumn(1).setCellEditor(new
		// DefaultCellEditor(new JFormattedTextField(sf)));
		// table.getColumnModel().getColumn(2).setCellEditor(new
		// DefaultCellEditor(itemList));
		table.getColumnModel().getColumn(2).setCellEditor(new DateCellEditor());

		table.getColumnModel().getColumn(3).setCellEditor(new ItemCellEditor());

		table.getColumnModel().getColumn(4)
				.setCellEditor(new MoneyCellEditor());
		table.getColumnModel().getColumn(4).setCellRenderer(
				new DefaultTableCellRenderer() {
					@Override
					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						if (isSelected) {
							setBackground(table.getSelectionBackground());
						} else {
							Object color = table.getValueAt(row, 6);
							if (color != null) {
								setBackground(new Color(Integer.valueOf(color
										.toString())));
							}
						}

						return super.getTableCellRendererComponent(table,
								value, isSelected, hasFocus, row, column);
					}
				});
		DefaultTableCellRenderer render = new DefaultTableCellRenderer();
		render.setToolTipText("选择项目...");

		table.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		// 隐藏ID列
		table.getColumnModel().getColumn(3).setCellRenderer(render);

		TableColumn idCol = table.getColumnModel().getColumn(0);

		idCol.setMaxWidth(0);
		idCol.setMinWidth(0);
		idCol.setPreferredWidth(0);
		// 隐藏Color列
		TableColumn colorCol = table.getColumnModel().getColumn(6);

		colorCol.setMaxWidth(0);
		colorCol.setMinWidth(0);
		colorCol.setPreferredWidth(0);

		/*
		 * if (table.getModel().getRowCount() > 0) {
		 * table.setRowSelectionInterval(0, 0);
		 * 设置了ListSelectionModel.SINGLE_SELECTION，可以省略次操作 }
		 */

		table.setAutoCreateRowSorter(true);
		table.getRowSorter().toggleSortOrder(2);// 按日期排序
	}

	public void tableChanged(TableModelEvent e, boolean changedDate) {
		int row = e.getFirstRow();
		int column = e.getColumn();

		if (TableModelEvent.INSERT == e.getType()
				|| TableModelEvent.DELETE == e.getType() || changedDate
				|| row == -1 || column == 6) {
			return;
		}
		TableModel model = (TableModel) e.getSource();
		int id = (Integer) model.getValueAt(row, 0);
		Object data = model.getValueAt(row, column);

		/*
		 * DBManager.updateItem(id, Constance.getFieldColumns().get(column - 1),
		 * data.toString()); DBManager.updateColor(model);
		 */
	}

}
