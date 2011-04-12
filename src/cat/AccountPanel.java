package cat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import cat.panel.BalancePane;
import cat.panel.Budget;
import cat.panel.QueryPane;
import cat.panel.Statistic;

public class AccountPanel extends JPanel {
	public AccountPanel() {
		super(new BorderLayout());
		JPanel tabpane = new JPanel();
		JTabbedPane tab = new JTabbedPane();
		tab.addTab("支出", new BalancePane("Expenditure"));
		tab.setMnemonicAt(0, KeyEvent.VK_1);
		
		tab.addTab("收入", new BalancePane("Income"));
		tab.setMnemonicAt(1, KeyEvent.VK_2);
		
		tab.addTab("统计", new Statistic());
		tab.setMnemonicAt(2, KeyEvent.VK_3);
		
		tab.addTab("预算", new Budget());
		tab.setMnemonicAt(3, KeyEvent.VK_4);
		
		tab.addTab("查询", new QueryPane());
		tab.setMnemonicAt(3, KeyEvent.VK_5);
		
		tab.setPreferredSize(new Dimension(620, 460));
		tabpane.setLayout(new GridLayout(1, 1));
		tabpane.add(tab);
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
