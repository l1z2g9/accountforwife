package cat.pane;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

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
import com.sun.lwuit.events.FocusListener;
import com.sun.lwuit.io.ConnectionRequest;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.io.util.JSONParser;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.list.DefaultListModel;
import com.sun.lwuit.plaf.Border;
import com.sun.lwuit.table.DefaultTableModel;
import com.sun.lwuit.table.Table;
import com.sun.lwuit.table.TableModel;

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
		this.addCommand(new Command("编辑") {
			public void actionPerformed(ActionEvent ev) {
				Util.log("<< " + table.getSelectedRow());
			}
		});
		table.addFocusListener(new FocusListener() {

			public void focusGained(Component cmp) {
				Util.log("<< " + cmp);
			}

			public void focusLost(Component cmp) {
				// TODO Auto-generated method stub
			}

		});
	}

	private void initTable() {
		model = new DefaultTableModel(colunNames, new Object[][] {}) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		table = new Table(model, true) {
			protected Component createCell(Object value, final int row,
					final int column, boolean editable) {

				final Component c = super.createCell(value, row, column,
						editable);
				c.getStyle().setBorder(Border.createLineBorder(12, 0xffff33));

				c.getSelectedStyle().setBorder(
						Border.createLineBorder(12, 0xffff33));

				c.setFocusable(true);
				return c;
			}
		};
		//table.setScrollable(true);
		//table.setIncludeHeader(true);

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
						final Object[][] value = new Object[data.size()][colunNames.length];

						while (keys.hasMoreElements()) {
							final Object id = keys.nextElement();
							final Hashtable items = (Hashtable) data.get(id);
							value[row][0] = String.valueOf(row + 1);

							// setTimeInMillis not exist 
							final Date x = new Date();
							final String time = (String) items.get("time");
							x.setTime(Long.parseLong(time));

							final Calendar date = Calendar.getInstance();
							date.setTime(x);

							value[row][1] = String.valueOf(date
									.get(Calendar.MONDAY) + 1)
									+ "-"
									+ String.valueOf(date
											.get(Calendar.DAY_OF_MONTH));

							value[row][2] = items.get("categoryName");
							value[row][3] = items.get("money");
							row++;
						}

						final DefaultTableModel newModel = new DefaultTableModel(
								colunNames, value);
						table.setModel(newModel);
					}
				};
				con.setPost(false);
				con.setUrl(Util.searchUrl);
				String x = String.valueOf(currYear) + "-"
						+ String.valueOf(month);

				x = "2011-11";
				Util.log("x " + x);
				con.addArgument("month", x);

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
