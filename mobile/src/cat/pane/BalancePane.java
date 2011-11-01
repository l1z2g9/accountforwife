package cat.pane;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import cat.AccountPanel;
import cat.ComponentFactory;
import cat.Util;
import cat.model.Item;

import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.io.ConnectionRequest;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.io.util.JSONParser;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.list.DefaultListModel;

public class BalancePane extends Form {

	private String categoryUrl;
	private String type;

	private int currYear;
	private ComboBox month;
	private ComboBox day;

	private ComboBox categoryComboBox;
	private ComboBox subCategoryComboBox;

	private Hashtable categoryData = new Hashtable();
	private Hashtable subCategoryData = new Hashtable();

	private TextField money;
	private TextField exchangeRate;
	private TextField user;
	private TextField address;
	private TextField remark;

	public BalancePane(final String type, ActionListener listener,
			String categoryUrl) {
		this.setTitle(type);
		this.categoryUrl = categoryUrl;
		this.type = type;
		this.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		this.addComponent(createDateTime());
		this.addComponent(createCategory());
		this.addComponent(createSubCategory(categoryComboBox.getModel()
				.getItemAt(0).toString()));
		this.addComponent(createMoney());

		exchangeRate = ComponentFactory.getTextField(10, TextArea.DECIMAL);
		this.addComponent(Util.createOneRow(ComponentFactory.getLabel("汇率"),
				exchangeRate));

		user = ComponentFactory.getTextField(10, TextArea.ANY);
		this.addComponent(Util.createOneRow(ComponentFactory.getLabel("用户"),
				user));

		address = ComponentFactory.getTextField(10, TextArea.ANY);
		this.addComponent(Util.createOneRow(ComponentFactory.getLabel("场所"),
				address));

		remark = ComponentFactory.getTextField(10, TextArea.ANY);
		this.addComponent(Util.createOneRow(ComponentFactory.getLabel("备注"),
				remark));

		this.addCommand(AccountPanel.exitCommand);
		this.addCommand(AccountPanel.backCommand);

		this.addCommand(new Command("保存") {
			public void actionPerformed(ActionEvent evt) {
				final Item item = new Item();
				item.setCategoryID(subCategoryData.get(
						subCategoryComboBox.getSelectedItem()).toString());
				item.setMoney(money.getText());
				item.setRemark(remark.getText());
				item.setUser(user.getText());
				item.setAddress(address.getText());
				String dateString = String.valueOf(currYear) + "-"
						+ month.getSelectedItem() + "-" + day.getSelectedItem();

				item.setTime(dateString);

				NetworkManager.getInstance().start();
				final ConnectionRequest con = new ConnectionRequest() {
					protected void readResponse(InputStream input)
							throws IOException {
						byte[] resp = new byte[409600];
						int len = input.read(resp);
						String content = new String(resp, 0, len);
						if (content.equals("OK")) {
							Dialog.show(type, "保存成功!", Dialog.TYPE_INFO, null,
									"确定", null);
						} else {
							//System.out.println("@@ " + content);
						}
					}
				};
				con.addArgument("item", item.toJson());
				con.setUrl(Util.saveUrl);
				con.setPost(true);
				NetworkManager.getInstance().addToQueueAndWait(con);
			}
		});
		this.addCommandListener(listener);
		this.setBackCommand(AccountPanel.backCommand);

		initCategoryButton();
	}

	private void initCategoryButton() {
		categoryComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				final Object label = categoryComboBox.getSelectedItem();

				final Vector subCategories = Util.getSubCategories(label
						.toString(), categoryData, subCategoryData,
						BalancePane.this.type);

				final DefaultListModel model = (DefaultListModel) subCategoryComboBox
						.getModel();
				model.removeAll();

				final Enumeration e = subCategories.elements();
				while (e.hasMoreElements()) {
					model.addItem(e.nextElement());
				}
			}
		});
	}

	private Component createMoney() {
		final Container pane = new Container(new BoxLayout(BoxLayout.X_AXIS));
		pane.addComponent(ComponentFactory.getLabel("金额"));
		money = ComponentFactory.getTextField(8, TextArea.DECIMAL);
		pane.addComponent(money);
		pane.addComponent(ComponentFactory.getLabel("元"));
		pane.getStyle().setMargin(Component.TOP, 10);
		return pane;
	}

	private Component createCategory() {
		final Container pane = new Container();
		Vector categories = Util.getCategories(categoryUrl, categoryData);
		categoryComboBox = ComponentFactory.getCombox(categories);
		pane.addComponent(Util.createOneRow(ComponentFactory.getLabel("类别"),
				categoryComboBox));

		return pane;
	}

	private Component createSubCategory(String categoryID) {
		final Container pane = new Container();
		final Vector categories = Util.getSubCategories(categoryID,
				categoryData, subCategoryData, this.type);
		subCategoryComboBox = ComponentFactory.getCombox(categories);

		pane.addComponent(Util.createOneRow(ComponentFactory.getLabel("小类别"),
				subCategoryComboBox));
		return pane;
	}

	private Container createDateTime() {
		Container pane = new Container(new BoxLayout(BoxLayout.X_AXIS));
		//year
		Calendar now = Calendar.getInstance();
		currYear = now.get(Calendar.YEAR);

		pane.addComponent(ComponentFactory.getLabel(String.valueOf(currYear)));
		pane.addComponent(ComponentFactory.getLabel("年"));

		// month
		int currMonth = now.get(Calendar.MONTH);
		String[] months = new String[12];
		for (int i = 1; i < 13; i++) {
			months[i - 1] = String.valueOf(i);
		}
		month = ComponentFactory.getCombox(months);

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

		day = ComponentFactory.getCombox(v);

		day.setSelectedIndex(currDay - 1);
		pane.addComponent(day);
		pane.addComponent(ComponentFactory.getLabel("日"));

		return pane;
	}

}
