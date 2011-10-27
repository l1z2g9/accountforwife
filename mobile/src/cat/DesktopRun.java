package cat;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.sun.lwuit.Display;

public class DesktopRun {
	private static Hashtable bundle = new Hashtable();
	static {
		bundle.put("mainTitle", "AccountForWife");
		bundle.put("loginName", "LoginName");
		bundle.put("password", "Password");
		bundle.put("login", "Login");
		bundle.put("exit", "Exit");
		bundle.put("expenditure", "Expenditure");

	}

	private static void createAndShowGUI() {
		Frame f = new Frame("run on desttop");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Display.getInstance().exitApplication();
			}
		});
		f.setLayout(new java.awt.BorderLayout());
		f.setSize(320, 480);
		//		AwtImpl.setUseNativeInput(false);
		Display.init(f);
		f.validate();
		f.setLocationByPlatform(true);
		f.setVisible(true);
		new AccountPanel().startApp(bundle);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
