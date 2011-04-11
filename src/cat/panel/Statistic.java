package cat.panel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import cat.Configure;
import cat.DBManager;
import cat.DateField2;
import cat.TableUtility;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import net.sf.nachocalendar.components.DateField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;

public class Statistic extends JPanel implements ListSelectionListener,
		TableModelListener {
	static Logger log = Logger.getLogger("StatisticPanel");

	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

	DefaultTableModel statModel;

	DefaultTableModel dateModel;

	JTable statTable;

	JTable dateTable;

	static String QUERY = "query";

	public DateField fromDate = new DateField2();

	public DateField toDate = new DateField2();

	JLabel dateDiff = new JLabel("     ");

	JLabel payoutTotal = new JLabel("     ");

	JLabel imcomeTotal = new JLabel("     ");

	JLabel budget = new JLabel();

	// JLabel dateAverageIncome = new JLabel();

	JLabel balance = new JLabel();

	JButton query;

	public Statistic() {
		super(new BorderLayout());

		this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JPanel itemPanel = new JPanel(new BorderLayout());
		addDatePanel(itemPanel);
		createStatDataList(itemPanel);
		add(itemPanel, BorderLayout.NORTH);

		JPanel tablePanel = new JPanel(new BorderLayout());
		createDataTable(tablePanel);

		JPanel changePanel = new JPanel(new BorderLayout());
		// changePanel.setBorder(BorderFactory.createLineBorder(Color.red));
		// changePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		changePanel.setLayout(new BoxLayout(changePanel, BoxLayout.Y_AXIS));
		JButton delete = new JButton("删除");
		JButton payoutChart = new JButton("支出统计图");

		payoutChart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new StatChart().chart("支出");
			}
		});

		JButton incomeChart = new JButton("收入统计图");

		incomeChart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new StatChart().chart("收入");
			}
		});
		changePanel.add(Box.createVerticalGlue());
		changePanel.add(delete);
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new TableUtility().deleteData(dateTable);
				updateStatPanel();
			}
		});
		changePanel.add(Box.createVerticalStrut(5));
		changePanel.add(payoutChart);
		changePanel.add(Box.createVerticalStrut(5));
		changePanel.add(incomeChart);

		tablePanel.add(changePanel, BorderLayout.LINE_END);
		add(tablePanel, BorderLayout.SOUTH);

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				TableCellEditor editor = dateTable.getCellEditor();
				if (editor != null) {
					editor.stopCellEditing();
				}
			}
		});
	}

	private void addDatePanel(Container container) {
		JPanel searchPane = new JPanel(new BorderLayout());
		JPanel datePane = new JPanel();
		datePane.setLayout(new BoxLayout(datePane, BoxLayout.X_AXIS));
		// datePane.setBorder(BorderFactory.createLineBorder(Color.red));

		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.DAY_OF_MONTH, 1);
		fromDate.setValue(cal.getTime());
		datePane.add(fromDate);
		datePane.add(Box.createHorizontalStrut(10));
		datePane.add(new JLabel("—"));
		datePane.add(Box.createHorizontalStrut(10));

		datePane.add(toDate);

		query = new JButton("查询");
		query.setActionCommand(QUERY);
		query.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateStatPanel();
			}
		});
		final JButton listButton = new JButton("↓");
		listButton.setMargin(new Insets(0, -8, 0, -8));

		listButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPopupMenu menu = new JPopupMenu();

				// 详细显示每天所有收支记录
				JMenuItem detail = new JMenuItem("详细显示每天所有收支记录");
				detail.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Vector<Vector> data = DBManager.getItemsBetweenDates(sf
								.format(fromDate.getValue()), sf.format(toDate
								.getValue()));

						new PopupDialog(data, Configure.getDateColumns(),
								Statistic.this, true);
					}
				});

				menu.add(detail);
				menu.addSeparator();

				// 统计各个支出项目所占比例
				JMenuItem payoutRatio = new JMenuItem("统计各个支出项目所占比例");
				payoutRatio.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Vector<Vector<String>> data = DBManager.getItemStat(
								"支出", sf.format(fromDate.getValue()), sf
										.format(toDate.getValue()));
						new PopupDialog(data, Configure.getItemStatColumns(),
								Statistic.this, false);
					}
				});

				menu.add(payoutRatio);

				// 统计各个收入项目所占比例
				JMenuItem incomeRatio = new JMenuItem("统计各个收入项目所占比例");
				incomeRatio.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Vector<Vector<String>> data = DBManager.getItemStat(
								"收入", sf.format(fromDate.getValue()), sf
										.format(toDate.getValue()));
						new PopupDialog(data, Configure.getItemStatColumns(),
								Statistic.this, false);
					}
				});
				menu.add(incomeRatio);

				menu.addSeparator();
				JMenu output = new JMenu("导出为文本文件");
				JMenuItem format1 = new JMenuItem("格式： 日期、项目、收入、支出、备注");
				format1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						fc.addChoosableFileFilter(new FileFilter() {
							@Override
							public boolean accept(File f) {
								return true;
							}

							@Override
							public String getDescription() {
								return "Text File (*.txt)";
							}
						});
						int returnVal = fc.showSaveDialog(Statistic.this);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							try {
								FileWriter fw = new FileWriter(file + ".txt");
								saveTextFormat(fw);
								fw.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				});
				output.add(format1);

				menu.add(output);

				menu.show((JButton) e.getSource(), 0, 0);

			}

		});
		datePane.add(Box.createHorizontalStrut(20));
		datePane.add(query);
		datePane.add(Box.createHorizontalStrut(5));
		datePane.add(listButton);

		searchPane.add(datePane, BorderLayout.PAGE_START);

		statModel = new DefaultTableModel(new Vector(), Configure
				.getStatColumns()) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		statTable = new JTable(statModel);
		statTable.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		statTable.getSelectionModel().addListSelectionListener(this);

		// statTable.getTableHeader().setFont(new Font("Default", Font.BOLD,
		// 12));
		// statTable.getTableHeader().setPreferredSize(new Dimension(30, 22))
		// Nimbus下不需要;
		statTable.setPreferredScrollableViewportSize(new Dimension(400, 130));

		JScrollPane scrollPane = new JScrollPane(statTable);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(10, 0, 0, 0), scrollPane.getBorder()));

		searchPane.add(scrollPane, BorderLayout.PAGE_END);

		container.add(searchPane, BorderLayout.LINE_START);
	}

	private void saveTextFormat(FileWriter fw) {
		String from = sf.format(fromDate.getValue());
		String to = sf.format(toDate.getValue());
		Vector<Vector> data = DBManager.getItemsBetweenDates(from, to);
		try {
			String format = "%-14s%-16s%10s%14s     %-10s\n";
			fw.write(String.format(format, "  日期", "项目", "收入", "支出", "备注"));
			fw
					.write("-------------------------------------------------------------------------------\n");
			float payout = 0;
			float income = 0;
			for (Vector v : data) {
				String item = v.get(3).toString();
				float money = Float.valueOf(v.get(4).toString());

				if (String.valueOf(v.get(1)).equalsIgnoreCase("支出")) {
					payout += money;
					fw.write(String.format("%-16s%-" + (16 - item.length())
							+ "s%-16s%14.2f     %-10s\n", v.get(2), item, " ",
							money, v.get(5)));
				} else {
					income += money;
					fw.write(String.format("%-16s%-" + (16 - item.length())
							+ "s%14.2f%-14s       %-10s\n", v.get(2), item,
							money, " ", v.get(5)));
				}
			}
			fw.write("\n");
			fw.write(String.format("%-16s%-14s%14.2f%16.2f     %-10s\n", " ",
					"合计", income, payout, from + " 至 " + to + " "
							+ dateDiff.getText() + "天"));
			fw.write(String.format("%-16s%-14s%14.2f%16s     %-10s\n", " ",
					"结余", income - payout, " ", from + " 至 " + to + " "
							+ dateDiff.getText() + "天"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void createStatDataList(Container container) {
		JPanel statNumPane = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.NORTH;
		statNumPane.add(new JLabel("查询天数："), c);

		statNumPane.add(dateDiff, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		statNumPane.add(new JLabel(" 天"), c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		statNumPane.add(new JLabel("  "), c);

		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.SOUTH;
		statNumPane.add(new JLabel("支出合计："), c);
		statNumPane.add(payoutTotal, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		statNumPane.add(new JLabel(" 元"), c);

		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.SOUTH;
		statNumPane.add(new JLabel("支出预算："), c);
		statNumPane.add(budget, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		statNumPane.add(new JLabel(" 元"), c);

		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.NORTH;
		statNumPane.add(new JLabel("收入合计："), c);
		statNumPane.add(imcomeTotal, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		statNumPane.add(new JLabel(" 元"), c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		statNumPane.add(new JLabel("  "), c);

		/*
		 * c.gridwidth = 1; c.gridheight = 1; statNumPane.add(new
		 * JLabel("日均支出："), c); statNumPane.add(dateAveragePayout, c);
		 * c.gridwidth = GridBagConstraints.REMAINDER; statNumPane.add(new
		 * JLabel("元"), c);
		 * 
		 * c.gridwidth = 1; c.gridheight = 1; statNumPane.add(new
		 * JLabel("日均收入："), c); statNumPane.add(dateAverageIncome, c);
		 * c.gridwidth = GridBagConstraints.REMAINDER; statNumPane.add(new
		 * JLabel("元"), c);
		 * 
		 * c.gridwidth = GridBagConstraints.REMAINDER; statNumPane.add(new
		 * JLabel(" "), c);
		 */

		c.gridwidth = 1;
		c.gridheight = 1;
		statNumPane.add(new JLabel("结余："), c);
		statNumPane.add(balance, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		statNumPane.add(new JLabel(" 元"), c);

		statNumPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));
		container.add(statNumPane, BorderLayout.LINE_END);
	}

	private void createDataTable(Container container) {
		dateModel = new DefaultTableModel(new Vector(), Configure
				.getDateColumns()) {
			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 0 || column == 1) {
					return false;
				}
				return true;
			}
		};

		dateModel.addTableModelListener(this);
		dateTable = new JTable(dateModel);

		TableColumn idCol = dateTable.getColumnModel().getColumn(0);

		idCol.setMaxWidth(0);
		idCol.setMinWidth(0);
		idCol.setPreferredWidth(0);

		TableColumn colorCol = dateTable.getColumnModel().getColumn(6);

		colorCol.setMaxWidth(0);
		colorCol.setMinWidth(0);
		colorCol.setPreferredWidth(0);
		// dateTable.getTableHeader().setFont(new Font("Default", Font.BOLD,
		// 12));
		// dateTable.getTableHeader().setPreferredSize(new Dimension(30,
		// 22));Nimbus下不需要;
		// dateTable.setPreferredSize(new Dimension(450, 150));
		dateTable.setPreferredScrollableViewportSize(new Dimension(450, 150));

		JScrollPane scrollPane = new JScrollPane(dateTable);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(10, 0, 0, 0), scrollPane.getBorder()));
		scrollPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				TableCellEditor editor = dateTable.getCellEditor();
				if (editor != null) {
					editor.stopCellEditing();
				}
			}
		});
		container.add(scrollPane, BorderLayout.LINE_START);
	}

	public void valueChanged(ListSelectionEvent e) {
		DefaultListSelectionModel model = (DefaultListSelectionModel) e
				.getSource();
		int row = model.getMaxSelectionIndex();
		if (row == -1) {
			return;
		}
		String date = statTable.getValueAt(row, 0).toString();
		// Vector obj = DBManager.getItemsByDate(date);
		// dateModel.setDataVector(obj, Constance.getDateColumns());
		new TableUtility().makeTableCell(dateTable);
	}

	public void tableChanged(TableModelEvent e) {
		new TableUtility().tableChanged(e, false);
		if (e.getColumn() == 4) {
			log.info("金额发生改变,重新读取统计数据");
			updateStatPanel();
		}
	}

	private void updateStatPanel() {
		Calendar c1 = Calendar.getInstance();
		c1.setTime((Date) fromDate.getValue());
		c1.set(Calendar.HOUR, 0);
		c1.set(Calendar.MINUTE, 0);
		c1.set(Calendar.SECOND, 0);

		Calendar c2 = Calendar.getInstance();
		c2.setTime((Date) toDate.getValue());

		Vector<Vector<String>> data = DBManager.query(sf.format(c1.getTime()),
				sf.format(c2.getTime()));
		statModel.setDataVector(data, Configure.getStatColumns());
		int diff = 0;
		if ((c1 != null) && (c2 != null)) {
			long t = c2.getTimeInMillis() - c1.getTimeInMillis();
			if (t < 0)
				t *= -1;
			diff = (int) (t / (3600000 * 24));
			dateDiff.setText(String.valueOf(diff));
		}
		float payout = DBManager.queryPayoutTotal(sf.format(c1.getTime()), sf
				.format(c2.getTime()), "支出");
		float income = DBManager.queryPayoutTotal(sf.format(c1.getTime()), sf
				.format(c2.getTime()), "收入");

		DecimalFormat df = new DecimalFormat("##.##");

		payoutTotal.setText(df.format(payout));
		imcomeTotal.setText(df.format(income));

		// budget.setText(String.valueOf(DBManager.getTotalBudget(c1
		// .get(Calendar.YEAR), c1.get(Calendar.MONTH) + 1)));
		// dateAveragePayout.setText(df.format(payout / diff));
		// dateAverageIncome.setText(df.format(income / diff));
		balance.setText(df.format(income - payout));
	}

	class StatChart {
		public void chart(String type) {
			int selectRow = statTable.getSelectedRow();
			if (selectRow == -1) {
				JOptionPane.showMessageDialog(SwingUtilities
						.getWindowAncestor(Statistic.this), "没有选择日期！");
				return;
			}
			Map<String, Float> items = new HashMap();
			String date = (String) statTable.getValueAt(statTable
					.getSelectedRow(), 0);
			Vector<Vector> data = null; // DBManager.getItemsByType(date, type);
			for (Vector v : data) {
				String item = (String) v.get(3);
				Float money = Float.valueOf((String) v.get(4));
				if (items.containsKey(item)) {
					items.put(item, items.get(item) + money);
				} else {
					items.put(item, money);
				}
			}

			DefaultPieDataset piedataset = new DefaultPieDataset();
			for (String item : items.keySet()) {
				piedataset.setValue(item, items.get(item));
			}
			JFreeChart jfreechart = ChartFactory.createPieChart(date + " 的"
					+ type + "统计", piedataset, true, true, false);
			TextTitle texttitle = jfreechart.getTitle();
			Font font = UIManager.getFont("Button.font");// 使用系统提供的字体
			texttitle.setFont(font.deriveFont(Font.BOLD, 20f));
			texttitle.setToolTipText(type + "统计");

			jfreechart.getLegend().setItemFont(font.deriveFont(Font.BOLD));

			PiePlot pieplot = (PiePlot) jfreechart.getPlot();
			pieplot.setLabelFont(font.deriveFont(14f));
			pieplot.setNoDataMessage("没有数据");
			pieplot.setCircular(false);
			pieplot.setLabelGap(0.02D);
			ChartPanel pane = new ChartPanel(jfreechart, 500, 350, 500, 350,
					400, 350, true, true, true, true, true, true);
			// ChartPanel pane = new ChartPanel(jfreechart);
			final JDialog dialog = new JDialog(SwingUtilities
					.getWindowAncestor(Statistic.this));
			pane.getInputMap().put(
					KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
			pane.getActionMap().put("exit", new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});

			dialog.getContentPane().add(pane);
			dialog.pack();
			dialog.setLocationRelativeTo(Statistic.this);
			dialog.setVisible(true);

		}
	}
}
