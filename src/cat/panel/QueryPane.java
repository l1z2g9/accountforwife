package cat.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;

import cat.Configure;
import cat.DBManager;
import cat.DateField2;
import cat.model.Category;

public class QueryPane extends JPanel {
	static Logger log = Logger.getLogger("BalancePane");
	DateField2 selectedDate = new DateField2();

	Map<String, Category> categories;
	Map<String, Category> subcategories;
	JComboBox categoryCombox;
	JComboBox subCategoryCombox;
	JComboBox year;
	JComboBox month;
	JComboBox typeCombox;
	JTextField user;
	Map<String, String> typeMap = new HashMap<String, String>();
	JTable table;
	DefaultTableModel tableModel;
	JLabel summaryMoney = new JLabel();

	public QueryPane() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 5));

		add(createItems(), BorderLayout.NORTH);
		add(createButtons(), BorderLayout.CENTER);
		add(createDataTable(), BorderLayout.SOUTH);
	}

	public void categoryReload() {
		int selected = typeCombox.getSelectedIndex();

		typeCombox.setSelectedIndex(0);
		typeCombox.setSelectedIndex(1);
		typeCombox.setSelectedIndex(2);
		typeCombox.setSelectedIndex(selected);
	}

	private JPanel createButtons() {
		JPanel search = new JPanel();
		JButton find = new JButton("查询");
		find.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int parentCategoryID = -1;
				int categoryID = -1;
				if (typeCombox.getSelectedIndex() > 0) {
					String cateName = categoryCombox.getSelectedItem()
							.toString();

					if (!cateName.equals("全部")) {
						parentCategoryID = categories.get(cateName).getId();
					}

					String subCateName = subCategoryCombox.getSelectedItem()
							.toString();
					if (!cateName.equals("全部") && !subCateName.equals("全部")) {
						categoryID = subcategories.get(subCateName).getId();
					}
				}

				Vector<Vector> data = DBManager.query((Integer) year
						.getSelectedItem(), (Integer) month.getSelectedItem(),
						typeMap.get(typeCombox.getSelectedItem()),
						parentCategoryID, categoryID, user.getText().trim());
				tableModel.setDataVector(data, Configure.getDateColumns());
				arrangeColumn();

				float incomeTotal = 0f;
				float expenditureTotal = 0f;
				for (int rownum = 0; rownum < table.getRowCount(); rownum++) {
					if (table.getValueAt(rownum, 9).toString()
							.equalsIgnoreCase("Income"))
						incomeTotal += Float.valueOf(table
								.getValueAt(rownum, 5).toString());
					else
						expenditureTotal += Float.valueOf(table.getValueAt(
								rownum, 5).toString());
				}
				if (typeCombox.getSelectedIndex() == 0) {
					summaryMoney.setText("总收入：" + incomeTotal + " 元   总支出："
							+ expenditureTotal + " 元");
				} else if (typeCombox.getSelectedIndex() == 1)
					summaryMoney.setText("总收入：" + incomeTotal + " 元");
				else if (typeCombox.getSelectedIndex() == 2)
					summaryMoney.setText("总支出：" + expenditureTotal + " 元");
			}
		});
		search.add(find);

		JButton chartButton = new JButton("统计图");
		chartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Vector<Vector> data = tableModel.getDataVector();
				Map<String, Float> categoryStat = new HashMap<String, Float>();
				for (int row = 0; row < table.getRowCount(); row++) {
					String categoryName = tableModel.getValueAt(row, 3)
							.toString();
					Float money = Float.valueOf(tableModel.getValueAt(row, 5)
							.toString());
					if (categoryStat.containsKey(categoryName)) {
						categoryStat.put(categoryName, categoryStat
								.get(categoryName)
								+ money);
					} else {
						categoryStat.put(categoryName, money);
					}
				}

				// 图表
				DefaultPieDataset piedataset = new DefaultPieDataset();
				for (String item : categoryStat.keySet()) {
					piedataset.setValue(item, categoryStat.get(item));
				}
				JFreeChart jfreechart = ChartFactory.createPieChart(year
						.getSelectedItem()
						+ "年" + month.getSelectedItem() + "月统计图", piedataset,
						true, true, false);
				TextTitle texttitle = jfreechart.getTitle();
				Font font = UIManager.getFont("Button.font");// 使用系统提供的字体
				texttitle.setFont(font.deriveFont(Font.BOLD, 20f));
				texttitle.setToolTipText(typeCombox.getSelectedItem() + "统计");

				jfreechart.getLegend().setItemFont(font.deriveFont(Font.BOLD));

				PiePlot pieplot = (PiePlot) jfreechart.getPlot();
				pieplot.setLabelFont(font.deriveFont(14f));
				pieplot.setNoDataMessage("没有数据");
				pieplot.setCircular(false);
				pieplot.setLabelGenerator(new StandardPieSectionLabelGenerator(
						"{0} = {1}"));
				pieplot.setLabelGap(0.02D);
				ChartPanel pane = new ChartPanel(jfreechart, 500, 350, 500,
						350, 400, 350, true, true, true, true, true, true);

				// 显示
				final JDialog dialog = new JDialog(SwingUtilities
						.getWindowAncestor(QueryPane.this));
				pane.getInputMap().put(
						KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
				pane.getActionMap().put("exit", new AbstractAction() {
					public void actionPerformed(ActionEvent e) {
						dialog.dispose();
					}
				});

				dialog.getContentPane().add(pane);
				dialog.pack();
				dialog.setLocationRelativeTo(QueryPane.this);
				dialog.setVisible(true);

			}
		});
		search.add(chartButton);

		JButton exportExcel = new JButton("导出到Excel");
		exportExcel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HSSFWorkbook wb = new HSSFWorkbook();

				HSSFSheet sheet = wb.createSheet("sheet1");

				HSSFFont f = wb.createFont();
				f.setFontHeightInPoints((short) 12);
				// f.setColor(HSSFColor.RED.index);
				f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

				HSSFCellStyle cs = wb.createCellStyle();
				cs.setFont(f);

				TableColumnModel headModel = table.getTableHeader()
						.getColumnModel();

				// 标题
				HSSFRow row = sheet.createRow(0);
				for (int colnum = 1; colnum < headModel.getColumnCount() - 2; colnum++) {
					HSSFCell header = row.createCell(colnum - 1);

					header.setCellValue(headModel.getColumn(colnum)
							.getHeaderValue().toString());
					header.setCellStyle(cs);
				}

				// 内容
				for (int rownum = 1; rownum < table.getRowCount() + 1; rownum++) {
					HSSFRow data = sheet.createRow(rownum);
					for (int colnum = 1; colnum < tableModel.getColumnCount() - 2; colnum++) {
						HSSFCell cell = data.createCell(colnum - 1);
						cell.setCellValue(tableModel.getValueAt(rownum - 1,
								colnum).toString());

						if (colnum == 5) {
							Color color = (Color) tableModel.getValueAt(
									rownum - 1, 10);

							if (!Color.white.equals(color)) {
								HSSFCellStyle moneyHighlight = wb
										.createCellStyle();
								if (Color.yellow.equals(color)) {
									moneyHighlight
											.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
									moneyHighlight
											.setFillForegroundColor(HSSFColor.YELLOW.index);
								} else if (Color.red.equals(color)) {
									moneyHighlight
											.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
									moneyHighlight
											.setFillForegroundColor(HSSFColor.RED.index);
								}
								cell.setCellStyle(moneyHighlight);
							}
						}
					}
				}

				HSSFRow data = sheet.createRow(table.getRowCount() + 3);
				HSSFCell cell = data.createCell(0);
				cell.setCellValue(summaryMoney.getText());

				// 调整列宽
				sheet.setColumnWidth(1, (short) 3000);
				sheet.setColumnWidth(3, (short) 3500);
				sheet.setColumnWidth(4, (short) 2600);

				// Save
				String file = year.getSelectedItem() + "年"
						+ month.getSelectedItem() + "月流水账.xls";
				try {
					FileOutputStream out = new FileOutputStream(file);
					wb.write(out);
					out.close();

					Desktop.getDesktop().open(new File(file));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		search.add(exportExcel);
		return search;
	}

	private JPanel createItems() {
		JPanel items = new JPanel(new FlowLayout(FlowLayout.LEFT));

		year = new JComboBox(new Object[] { 2011, 2012, 2013, 2014, 2015 });
		month = new JComboBox(new Object[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
				12 });

		Calendar c = Calendar.getInstance();
		year.setSelectedItem(c.get(Calendar.YEAR));
		month.setSelectedItem(c.get(Calendar.MONTH) + 1);

		items.add(year);
		items.add(new JLabel("年"));
		items.add(month);
		items.add(new JLabel("月"));

		items.add(new JLabel("类型"));
		typeMap.put("全部", "All");
		typeMap.put("收入", "Income");
		typeMap.put("支出", "Expenditure");
		typeCombox = new JComboBox(new Object[] { "全部", "收入", "支出" });
		items.add(typeCombox);

		items.add(new JLabel("类别"));

		categoryCombox = new JComboBox();
		initCategory(typeCombox, categoryCombox);
		categoryCombox.setPreferredSize(new Dimension(85, 30));
		typeCombox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initCategory(typeCombox, categoryCombox);
			}
		});
		items.add(categoryCombox);

		items.add(new JLabel("小类别"));
		subCategoryCombox = new JComboBox();
		initSubCategory(categoryCombox, subCategoryCombox);
		categoryCombox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initSubCategory(categoryCombox, subCategoryCombox);
			}
		});
		items.add(subCategoryCombox);

		items.add(new JLabel("用户"));
		user = new JTextField(5);
		items.add(user);
		return items;
	}

	private void initCategory(JComboBox typeCombox, JComboBox categoryCombox) {
		DefaultComboBoxModel model = (DefaultComboBoxModel) categoryCombox
				.getModel();
		model.removeAllElements();
		if (typeCombox.getSelectedIndex() > 0) {
			categories = DBManager.getCategory(typeMap.get(typeCombox
					.getSelectedItem().toString()));
			model.addElement("全部");
			for (String name : categories.keySet()) {
				model.addElement(name);
			}
		} else {
			model.addElement("全部");
		}
	}

	private void initSubCategory(JComboBox categoryCombox,
			JComboBox subCategoryCombox) {
		if (categoryCombox.getSelectedItem() != null) {
			DefaultComboBoxModel model = (DefaultComboBoxModel) subCategoryCombox
					.getModel();
			model.removeAllElements();
			String cateName = categoryCombox.getSelectedItem().toString();
			if (cateName.equals("全部")) {
				model.addElement("全部");
			} else {
				subcategories = DBManager.getSubCategory(categories.get(
						cateName).getId());
				model.addElement("全部");
				for (String name : subcategories.keySet()) {
					model.addElement(name);
				}
			}
		}
	}

	/**
	 * 根BalancePane的table的结构一样。
	 */
	private JPanel createDataTable() {
		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		summaryMoney.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		pane.add(summaryMoney, BorderLayout.PAGE_START);
		Vector<Vector> data = new Vector<Vector>();
		tableModel = new DefaultTableModel(data, Configure.getDateColumns()) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		table = new JTable(tableModel);
		table.setRowHeight(22);
		arrangeColumn();
		table.setAutoCreateRowSorter(true);

		table.setPreferredScrollableViewportSize(new Dimension(400, 280));
		JScrollPane s = new JScrollPane(table);
		s.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		s.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(5, 8, 10, 10), s.getBorder()));
		pane.add(s, BorderLayout.PAGE_END);
		return pane;
	}

	private void arrangeColumn() {
		// 隐藏ID列
		TableColumn idCol = table.getColumnModel().getColumn(0);
		idCol.setMaxWidth(0);
		idCol.setMinWidth(0);
		idCol.setPreferredWidth(0);

		// 隐藏类别列
		TableColumn typeCol = table.getColumnModel().getColumn(9);
		typeCol.setMaxWidth(0);
		typeCol.setMinWidth(0);
		typeCol.setPreferredWidth(0);

		// 隐藏颜色列
		TableColumn colorCol = table.getColumnModel().getColumn(10);
		colorCol.setMaxWidth(0);
		colorCol.setMinWidth(0);
		colorCol.setPreferredWidth(0);

		TableColumn seqCol = table.getColumnModel().getColumn(1);
		seqCol.setMaxWidth(45);

		table.getColumnModel().getColumn(5).setCellRenderer(
				new DefaultTableCellRenderer() {
					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {

						Color color = (Color) table.getModel().getValueAt(row,
								10);
						if (!Color.white.equals(color)) {
							super.setBackground(color);
						} else {
							super.setBackground(null);
						}

						return super.getTableCellRendererComponent(table,
								value, isSelected, hasFocus, row, column);
					}
				});
	}
}
