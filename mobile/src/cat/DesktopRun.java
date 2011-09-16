package cat;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.sun.lwuit.Display;
import com.sun.lwuit.awtport.AwtImpl;

public class DesktopRun {
	private static void createAndShowGUI() {
		JFrame f = new JFrame("run on desttop");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Display.getInstance().exitApplication();
			}
		});
		f.setLayout(new java.awt.BorderLayout());
		f.setSize(480, 800);
		AwtImpl.setUseNativeInput(false);
		Display.init(f);
		f.validate();
		f.setLocationByPlatform(true);
		f.setVisible(true);
		new AccountPanel().startApp();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
