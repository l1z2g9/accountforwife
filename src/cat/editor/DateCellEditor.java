package cat.editor;

import cat.DateField2;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.logging.Logger;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class DateCellEditor extends AbstractCellEditor implements
		TableCellEditor {
	Logger log = Logger.getLogger("DateCellEditor");

	DateField2 field;

	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

	public DateCellEditor() {
		field = new DateField2();
		field.setDateFormat(sf);
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		Date date = (Date) field.getValue();
		try {
			date = sf.parse((String) value);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		field.setValue(date);

		return field;
	}

	public Object getCellEditorValue() {
		return sf.format(field.getValue());
	}

}
