package cat.pane;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import com.sun.lwuit.table.DefaultTableModel;
import com.sun.lwuit.table.Table;
import com.sun.lwuit.table.TableModel;

import cat.AccountPanel;
import cat.ComponentFactory;
import cat.Util;

import com.sun.lwuit.Button;
import com.sun.lwuit.ButtonGroup;
import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.RadioButton;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.io.ConnectionRequest;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.io.util.JSONParser;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.list.DefaultListModel;

public class QueryPane extends Form {
	private int currYear;
	private ComboBox month;

	private ComboBox categoryComboBox;
	private Hashtable categoryData = new Hashtable();
	private TextField keyword;
	private Table table;
	private TableModel model;
	private static String[] colunNames = new String[] { "序号", "日期", "类别", "金额" };

	public QueryPane(ActionListener listener) {
		this.setTitle("查询");
		this.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		this.addComponent(createDateTime());
		this.addComponent(createType());
		this.addCommandListener(listener);
		this.addComponent(createCategory("Expenditure"));
		this.addComponent(createKeyword());
		this.addComponent(createSearchButton());
		initTable();
		this.addCommand(AccountPanel.exitCommand);
		this.addCommand(AccountPanel.backCommand);
	}

	private void initTable() {
		model = new DefaultTableModel(colunNames, new Object[][] {}) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		table = new Table(model);
		this.addComponent(table);
	}

	private Component createKeyword() {
		keyword = ComponentFactory.getTextField(10, TextArea.ANY);
		return Util.createOneRow(new Label("关键字"), keyword);
	}

	private Component createSearchButton() {
		Command searchCommand = new Command("查询") {
			public void actionPerformed(ActionEvent ev) {
				searchItems((String) month.getSelectedItem(),
						(String) categoryComboBox.getSelectedItem(), keyword
								.getText());
			}

			private void searchItems(String month, String categoryName,
					String keyword) {
				NetworkManager.getInstance().start();
				final ConnectionRequest con = new ConnectionRequest() {
					protected void readResponse(InputStream input)
							throws IOException {

						final JSONParser p = new JSONParser();
						Hashtable data = p.parse(new InputStreamReader(input));
						final Enumeration keys = data.keys();

						int row = 0;
						Object[][] value = new Object[data.size()][3];

						while (keys.hasMoreElements()) {
							Object cat = keys.nextElement();

							table.getModel().setValueAt(row, 1,
									String.valueOf(row));
							row++;
						}

						DefaultTableModel newModel = new DefaultTableModel(
								colunNames, value);

						table.refreshTheme();
					}
				};
				con.setPost(false);
				con.setUrl(Util.searchUrl);
				con.addArgument("month", String.valueOf(currYear) + "-"
						+ String.valueOf(month));

				con.addArgument("categoryID", categoryData.get(categoryName)
						.toString());
				con.addArgument("keyword", keyword);

				NetworkManager.getInstance().addToQueueAndWait(con);
			}
		};

		Button searchButton = new Button(searchCommand);
		return Util.createOneRow(searchButton);
	}

	private Component createType() {
		final RadioButton expenditureButton = new RadioButton("支出");
		expenditureButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				fireChangeCategory(Util.expenditureCategory);
			}
		});

		final RadioButton incomeButton = new RadioButton("收入");
		incomeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				fireChangeCategory(Util.incomeCategory);
			}
		});

		final ButtonGroup checkboxGroup = new ButtonGroup();

		checkboxGroup.add(expenditureButton);
		checkboxGroup.add(incomeButton);
		checkboxGroup.setSelected(0);
		Container pane = new Container(new BoxLayout(BoxLayout.X_AXIS));
		pane.addComponent(expenditureButton);
		pane.addComponent(incomeButton);
		return Util.createOneRow(new Label("类型"), pane);
	}

	private Component createCategory(String type) {
		final Container pane = new Container();
		Vector categories = null;
		if (type == "Expenditure") {
			categories = Util.getCategories(Util.expenditureCategory,
					categoryData);
		} else {
			categories = Util.getCategories(Util.incomeCategory, categoryData);
		}
		categoryComboBox = ComponentFactory.getCombox(categories);
		pane.addComponent(Util.createOneRow(ComponentFactory.getLabel("类别"),
				categoryComboBox));

		return pane;
	}

	private void fireChangeCategory(String url) {
		categoryData.clear();
		Vector categories = Util.getCategories(url, categoryData);
		final DefaultListModel model = (DefaultListModel) categoryComboBox
				.getModel();
		model.removeAll();

		final Enumeration e = categories.elements();
		while (e.hasMoreElements()) {
			model.addItem(e.nextElement());
		}
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

		return pane;
	}
}
