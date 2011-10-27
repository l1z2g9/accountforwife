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

public class BalancePane extends Form {
	public static String expenditureCategory = "http://accountforwife.sinaapp.com/category/Expenditure";
	public static String incomeCategory = "http://accountforwife.sinaapp.com/category/Income";
	public static String subCategory = "http://accountforwife.sinaapp.com/subCategory";
	private String categoryUrl;
	private String type;

	ComboBox categoryComboBox;
	Hashtable categoryMap;

	public BalancePane(String type, ActionListener listener, String categoryUrl) {
		this.setTitle(type);
		this.categoryUrl = categoryUrl;
		this.type = type;
		this.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		this.addComponent(createDateTime());
		this.addComponent(createCategory());
		this.addComponent(createSubCategory(categoryComboBox.getModel()
				.getItemAt(0).toString()));
		this.addComponent(createMoney());
		this.addComponent(createOneRow(ComponentFactory.getLabel("汇率"),
				ComponentFactory.getTextField(10, TextArea.DECIMAL)));
		this.addComponent(createOneRow(ComponentFactory.getLabel("用户"),
				ComponentFactory.getTextField(10, TextArea.ANY)));
		this.addComponent(createOneRow(ComponentFactory.getLabel("场所"),
				ComponentFactory.getTextField(10, TextArea.ANY)));
		this.addComponent(createOneRow(ComponentFactory.getLabel("备注"),
				ComponentFactory.getTextField(10, TextArea.ANY)));

		this.addCommand(AccountPanel.exitCommand);
		this.addCommand(AccountPanel.backCommand);
		this.addCommandListener(listener);
		this.setBackCommand(AccountPanel.backCommand);
	}

	private Component createMoney() {
		Container pane = new Container(new BoxLayout(BoxLayout.X_AXIS));
		pane.addComponent(ComponentFactory.getLabel("金额"));
		pane.addComponent(ComponentFactory.getTextField(8, TextArea.DECIMAL));
		pane.addComponent(ComponentFactory.getLabel("元"));
		pane.getStyle().setMargin(Component.TOP, 10);
		return pane;
	}

	private Component createOneRow(Label label, Component item) {
		Container pane = new Container(new BoxLayout(BoxLayout.X_AXIS));
		pane.addComponent(label);
		pane.addComponent(item);
		pane.getStyle().setMargin(Component.TOP, 6);
		return pane;
	}

	private Component createCategory() {
		final Container pane = new Container();
		Vector categories = getCategories();
		categoryComboBox = ComponentFactory.getCombox(categories);
		pane.addComponent(createOneRow(ComponentFactory.getLabel("类别"),
				categoryComboBox));

		return pane;
	}

	private Component createSubCategory(String categoryID) {
		final Container pane = new Container();
		Vector categories = getSubCategories(categoryID);

		pane.addComponent(createOneRow(ComponentFactory.getLabel("小类别"),
				ComponentFactory.getCombox(categories)));
		return pane;
	}

	private Vector getSubCategories(String categoryID) {
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
		String url = subCategory + "/%s" + "/" + categoryID;
		if ("收入".equals(type)) {
			url = url.replace("%s", "Expenditure");
		} else {
			url = url.replace("%s", "Income");
		}
		con.setUrl(subCategory);
		con.setPost(false);
		NetworkManager.getInstance().addToQueueAndWait(con);

		//categories.addElement("XX");
		return categories;
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
		con.setUrl(categoryUrl);
		con.setPost(false);
		NetworkManager.getInstance().addToQueueAndWait(con);

		//categories.addElement("XX");
		return categories;
	}

	private Container createDateTime() {
		Container pane = new Container(new BoxLayout(BoxLayout.X_AXIS));
		//year
		Calendar now = Calendar.getInstance();
		int currYear = now.get(Calendar.YEAR);

		pane.addComponent(ComponentFactory.getLabel(String.valueOf(currYear)));
		pane.addComponent(ComponentFactory.getLabel("年"));

		// month
		int currMonth = now.get(Calendar.MONTH);
		String[] months = new String[12];
		for (int i = 1; i < 13; i++) {
			months[i - 1] = String.valueOf(i);
		}
		ComboBox month = ComponentFactory.getCombox(months);

		month.setSelectedIndex(currMonth);
		pane.addComponent(month);
		pane.addComponent(ComponentFactory.getLabel("月"));

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
		pane.addComponent(ComponentFactory.getLabel("日"));

		return pane;
	}

}
