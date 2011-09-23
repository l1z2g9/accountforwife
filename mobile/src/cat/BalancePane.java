package cat;

import java.util.Calendar;
import java.util.Vector;

import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.list.DefaultListModel;

public class BalancePane extends Form {
	public BalancePane(String type, ActionListener listener) {
		this.setTitle(type);

		this.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		this.addComponent(createDateTime());
		this.addComponent(createCategory());
		this.addComponent(createRow());

		this.addCommand(AccountPanel.exitCommand);
		this.addCommand(AccountPanel.backCommand);
		this.addCommandListener(listener);
		this.setBackCommand(AccountPanel.backCommand);
	}

	private Component createRow() {
		Container pane = new Container();
		pane.addComponent(ComponentFactory.getLabel("money"));
		pane.addComponent(ComponentFactory.getTextField(4, TextArea.DECIMAL));

		return pane;
	}

	private Component createCategory() {
		Container pane = new Container();
		pane.addComponent(ComponentFactory.getLabel("category"));
		ComboBox category = ComponentFactory.getCombox(new String[] { "xx",
				"yy" });
		pane.addComponent(category);

		pane.addComponent(ComponentFactory.getLabel("subCategory"));
		ComboBox subCategory = ComponentFactory.getCombox(new String[] { "xx",
				"yy" });
		pane.addComponent(subCategory);
		return pane;
	}

	private Container createDateTime() {
		Container pane = new Container();
		//year
		Calendar now = Calendar.getInstance();
		int currYear = now.get(Calendar.YEAR);

		pane.addComponent(ComponentFactory.getLabel(String.valueOf(currYear)));
		pane.addComponent(ComponentFactory.getLabel("year"));

		// month
		int currMonth = now.get(Calendar.MONTH);
		String[] months = new String[12];
		for (int i = 1; i < 13; i++) {
			months[i - 1] = String.valueOf(i);
		}
		ComboBox month = ComponentFactory.getCombox(months);

		month.setSelectedIndex(currMonth);
		pane.addComponent(month);
		pane.addComponent(ComponentFactory.getLabel("month"));

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
		ComboBox day = ComponentFactory.getCombox(new DefaultListModel(v));

		day.setSelectedIndex(currDay - 1);
		pane.addComponent(day);
		pane.addComponent(ComponentFactory.getLabel("day"));
		;

		return pane;
	}
}
