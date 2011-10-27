package cat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.io.ConnectionRequest;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.io.util.JSONParser;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.GridLayout;

public class BalancePane extends Form {
	Hashtable categoryMap;

	public BalancePane(String type, ActionListener listener) {
		this.setTitle(type);

		this.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		this.addComponent(createDateTime());
		this.addComponent(createCategory());

		this.addComponent(createMoney());

		this.addCommand(AccountPanel.exitCommand);
		this.addCommand(AccountPanel.backCommand);
		this.addCommandListener(listener);
		this.setBackCommand(AccountPanel.backCommand);
	}

	private Component createMoney() {
		Container pane = new Container();
		pane.addComponent(ComponentFactory.getLabel("money"));
		pane.addComponent(ComponentFactory.getTextField(4, TextArea.DECIMAL));

		return pane;
	}

	private Component createOneRow(Label label, Component item) {
		Container pane = new Container();
		pane.addComponent(label);
		pane.addComponent(item);
		return pane;
	}

	private Component createCategory() {
		final Container pane = new Container();

		Vector categories = getCategories();

		pane.addComponent(createOneRow(ComponentFactory.getLabel("category"),
				ComponentFactory.getCombox(categories)));

		/*		pane.addComponent(createOneRow(
						ComponentFactory.getLabel("subCategory"), ComponentFactory
								.getCombox(categories)));
		*/
		return pane;
	}

	private Vector getCategories() {
		final Vector categories = new Vector();
		NetworkManager.getInstance().start();
		ConnectionRequest con = new ConnectionRequest() {
			protected void readResponse(InputStream input) throws IOException {
				final JSONParser p = new JSONParser();

				categoryMap = p.parse(new InputStreamReader(input));
				Enumeration keys = categoryMap.keys();

				while (keys.hasMoreElements()) {
					Object cat = keys.nextElement();
					categories.addElement(cat);
				}

			}
		};
		con.setUrl("http://accountforwife.sinaapp.com/category/Expenditure");
		con.setPost(false);
		NetworkManager.getInstance().addToQueueAndWait(con);

		return categories;
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
		ComboBox day = ComponentFactory.getCombox(v);

		day.setSelectedIndex(currDay - 1);
		pane.addComponent(day);
		pane.addComponent(ComponentFactory.getLabel("day"));

		return pane;
	}

}
