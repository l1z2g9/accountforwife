package cat.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.text.SimpleDateFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;

public class PopupDialog extends JDialog {
	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

	Logger log = Logger.getLogger("PopupDialog");

	public PopupDialog(final Vector data, Vector<String> columns,
			Statistic center, boolean hideFirstColumn) {

		DefaultTableModel defaultModel = new DefaultTableModel(data, columns) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable itemTable = new JTable(defaultModel);

		itemTable.getTableHeader().setPreferredSize(new Dimension(30, 22));

		itemTable.setPreferredScrollableViewportSize(new Dimension(500, 300));

		JPanel pane = new JPanel(new BorderLayout());

		pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel label = new JLabel(sf.format(center.fromDate.getValue())
				+ "  至  " + sf.format(center.toDate.getValue()));
		label.setBorder(BorderFactory.createEmptyBorder(2, 0, 8, 0));
		pane.add(label, BorderLayout.NORTH);

		JScrollPane scrollpane = new JScrollPane(itemTable);
		scrollpane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		itemTable.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
		itemTable.getActionMap().put("exit", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				PopupDialog.this.dispose();
			}
		});
		pane.add(scrollpane, BorderLayout.CENTER);

		if (hideFirstColumn) {
			TableColumn idCol = itemTable.getColumnModel().getColumn(0);

			idCol.setMaxWidth(0);
			idCol.setMinWidth(0);
			idCol.setPreferredWidth(0);

			TableColumn colorCol = itemTable.getColumnModel().getColumn(6);

			colorCol.setMaxWidth(0);
			colorCol.setMinWidth(0);
			colorCol.setPreferredWidth(0);

			JPanel chartPane = new JPanel();
			chartPane.setLayout(new BoxLayout(chartPane, BoxLayout.X_AXIS));

			JButton payoutChart = new JButton("支出统计图");

			payoutChart.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new StatChart().chart("支出", data);
				}
			});

			JButton incomeChart = new JButton("收入统计图");

			incomeChart.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new StatChart().chart("收入", data);
				}
			});
			chartPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
			chartPane.add(Box.createHorizontalGlue());
			chartPane.add(payoutChart);
			chartPane.add(Box.createHorizontalStrut(20));
			chartPane.add(incomeChart);
			chartPane.add(Box.createHorizontalGlue());
			pane.add(chartPane, BorderLayout.SOUTH);

		}

		this.getContentPane().add(pane);
		this.pack();
		this.setLocationRelativeTo(this);
		this.setVisible(true);
	}

	class StatChart {
		public void chart(String type, Vector<Vector> data) {
			Map<String, Float> items = new HashMap();

			for (Vector v : data) {
				if (!type.equalsIgnoreCase((String) v.get(1))) {
					continue;
				}
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
			JFreeChart jfreechart = ChartFactory.createPieChart(type + "统计",
					piedataset, true, true, false);
			TextTitle texttitle = jfreechart.getTitle();
			Font font = UIManager.getFont("Button.font");
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
					.getWindowAncestor(PopupDialog.this));

			pane.getInputMap().put(
					KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
			pane.getActionMap().put("exit", new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});

			dialog.getContentPane().add(pane);
			dialog.pack();
			dialog.setLocationRelativeTo(PopupDialog.this);
			dialog.setVisible(true);

		}
	}

}
