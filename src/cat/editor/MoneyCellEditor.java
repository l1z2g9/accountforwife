package cat.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class MoneyCellEditor extends DefaultCellEditor {
	JFormattedTextField text = null;

	Logger log = Logger.getLogger("MoneyCellEditor");

	public MoneyCellEditor() {
		super(new JFormattedTextField());
		text = (JFormattedTextField) getComponent();
		text.setFormatterFactory(new DefaultFormatterFactory(
				new NumberFormatter(new DecimalFormat("##.##"))));
		text.setFocusLostBehavior(JFormattedTextField.PERSIST);
		text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				"enter");
		text.getActionMap().put("enter", new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				if (text.isEditValid()) {
					try {
						text.commitEdit();
						text.postActionEvent();
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
				} else {
					if (userSayRevert())
						text.postActionEvent();
				}
			}

		});
	}

	private boolean userSayRevert() {
		Object[] msg = { "编辑", "取消" };
		int choose = JOptionPane.showOptionDialog(SwingUtilities
				.getWindowAncestor(text), "金额不正确！\n是否继续编辑金额？", "错误",
				JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null,
				msg, msg[1]);
		if (choose == 1) {
			text.setValue(text.getValue());
			return true;
		}
		return false;
	}

	@Override
	public Object getCellEditorValue() {
		JFormattedTextField ftf = (JFormattedTextField) getComponent();
		return ftf.getValue();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		text.setValue(Float.valueOf(String.valueOf(value)));
		return text;
	}

	@Override
	public boolean stopCellEditing() {
		JFormattedTextField ftf = (JFormattedTextField) getComponent();
		if (ftf.isEditValid()) {
			try {
				ftf.commitEdit();

			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			if (!userSayRevert())
				return false;
		}
		return super.stopCellEditing();
	}

}
