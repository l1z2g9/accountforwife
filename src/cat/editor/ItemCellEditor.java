package cat.editor;

import cat.DBManager;
import java.awt.Component;

import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

public class ItemCellEditor extends DefaultCellEditor {

	JComboBox comboBox;

	public ItemCellEditor() {
		super(new JComboBox());
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		String type = (String) table.getValueAt(row, 1);
		/*
		 * if ("Ö§³ö".equalsIgnoreCase(type)) { Vector<String> payout =
		 * DBManager.getPayoutItems(); comboBox = new JComboBox(payout); //
		 * comboBox.setSelectedIndex(payout.indexOf(value)); } else { Vector<String>
		 * income = DBManager.getIncomeItems(); comboBox = new
		 * JComboBox(income); }
		 */
		comboBox.setSelectedItem(value);
		return comboBox;
	}

	@Override
	public Object getCellEditorValue() {
		return comboBox.getSelectedItem();
	}
}
