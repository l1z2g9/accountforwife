package cat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cat.panel.BalancePane;
import cat.panel.BudgetPane;
import cat.panel.CategoryDialog;
import cat.panel.OverdrawPane;
import cat.panel.QueryPane;

public class AccountPanel extends JPanel {
	public AccountPanel() {
		super(new BorderLayout());
		JPanel tabpane = new JPanel();
		final JTabbedPane tab = new JTabbedPane();
		final BalancePane expenditure = new BalancePane("Expenditure");
		tab.addTab("支出", expenditure);
		tab.setMnemonicAt(0, KeyEvent.VK_1);

		final BalancePane income = new BalancePane("Income");
		tab.addTab("收入", income);
		tab.setMnemonicAt(1, KeyEvent.VK_2);
		final BudgetPane budget = new BudgetPane();
		tab.addTab("预算", budget);
		tab.setMnemonicAt(2, KeyEvent.VK_3);

		final QueryPane queryPane = new QueryPane();
		 tab.addTab("查询", queryPane);
		tab.setMnemonicAt(3, KeyEvent.VK_4);

		final OverdrawPane overdrawPane = new OverdrawPane();
		tab.addTab("预支付", overdrawPane);
		tab.setMnemonicAt(4, KeyEvent.VK_5);

		// tab.setSelectedIndex(4);

		tab.setPreferredSize(new Dimension(620, 460));
		tabpane.setLayout(new GridLayout(1, 1));
		tabpane.add(tab);

		tab.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (CategoryDialog.itemchanged) {
					expenditure.categoryReload();
					income.categoryReload();
					budget.refreshTableData();
					queryPane.categoryReload();
					CategoryDialog.itemchanged = false;
				}
				if (tab.getSelectedIndex() == 2) {
					budget.refreshSummary();
				}
			}
		});

		add(tabpane, BorderLayout.PAGE_START);

		JLabel sign = new JLabel("谨以此软件献给我最爱的婆婆猪－小艺 {O(∩_∩)O~");
		sign.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(sign, BorderLayout.PAGE_END);
	}

	private static void createAndShowGUI() {
		try {
			for (LookAndFeelInfo lookAndFeel : UIManager
					.getInstalledLookAndFeels()) {
				if (lookAndFeel.getName().equalsIgnoreCase("Nimbus")) {
					UIManager.setLookAndFeel(lookAndFeel.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		JFrame frame = new JFrame("小艺有数");

		frame.setIconImage(new ImageIcon(AccountPanel.class
				.getResource("/images/piggy_bank.png")).getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				DBManager.releaseConnection();
			}
		});
		frame.getContentPane().add(new AccountPanel(), BorderLayout.CENTER);
		frame.setResizable(false);
		frame.pack();
		// frame.setLocationByPlatform(true);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
