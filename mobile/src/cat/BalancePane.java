package cat;

import java.util.Calendar;
import java.util.Vector;

import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Font;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;

import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.io.util.Log;
import com.sun.lwuit.list.DefaultListModel;

public class BalancePane extends Form {
	public BalancePane(String type, ActionListener listener) {
		this.setTitle(type);

		// year
		Calendar now = Calendar.getInstance();
		int currYear = now.get(Calendar.YEAR);
		this.addComponent(new Label(String.valueOf(currYear)));
		Label label = new Label("year");
		this.addComponent(label);
		label.setPreferredW(100);

		// month
		int currMonth = now.get(Calendar.MONTH);
		String[] months = new String[12];
		for (int i = 1; i < 13; i++) {
			months[i - 1] = String.valueOf(i);
		}
		ComboBox month = new ComboBox(months);
		alignComboBox(month, 50, 20);
		month.setSelectedIndex(currMonth);
		this.addComponent(month);
		this.addComponent(new Label("month"));

		// day
		final short[] month31 = new short[] { 1, 3, 5, 7, 8, 10, 12 };
		boolean show31 = false;
		for (short i = 0; i < month31.length; i++) {
			if (month31[i] == currMonth) {
				show31 = true;
			}
		}
		int currDay = now.get(Calendar.DAY_OF_MONTH);

		Vector v = new Vector(31);
		if (show31) {
			for (int i = 1; i < 32; i++) {
				v.addElement(String.valueOf(i));
			}
		} else {
			for (int i = 1; i < 31; i++) {
				v.addElement(String.valueOf(i));
			}
		}
		ComboBox day = new ComboBox(new DefaultListModel(v));
		alignComboBox(day, 50, 20);
		day.setSelectedIndex(currDay - 1);
		this.addComponent(day);
		this.addComponent(new Label("day"));

		this.addCommand(AccountPanel.exitCommand);
		this.addCommand(AccountPanel.backCommand);
		this.addCommandListener(listener);
		this.setBackCommand(AccountPanel.backCommand);
	}

	private void alignComboBox(ComboBox box, int w, int h) {
		box.setPreferredW(w);
		box.setWidth(w);
		box.setPreferredH(h);
		box.setHeight(h);
	}
}
