package cat;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingUtilities;

import cat.model.Item;

import com.sun.lwuit.Display;

public class DesktopRun {

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
		new AccountPanel().startApp();
	}

	private static void run() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	static void test() throws Exception {
		Item item = new Item();
		item.setAddress("addressXX");
		item.setCategoryID("1000");
		item.setMoney("12.5");
		item.setTime("2011-10-29");
		//		String content = "{date:\"2011-10-31\",category:1000:subCategory:1100}";
		URL url = new URL("http://accountforwife.sinaapp.com/index.php/save");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.getOutputStream().write(("item=" + item.toJson()).getBytes());
		conn.getOutputStream().flush();

		byte[] str = new byte[890000];
		if (conn.getResponseCode() != 200) {
			int b = conn.getErrorStream().read(str);
			System.out.println("<<$$$ " + new String(str, 0, b));
		} else {
			int a = conn.getInputStream().read(str);
			System.out.println("<< " + new String(str, 0, a));
		}
	}

	static void test2() throws Exception {
		String time = "1320076800000";
		Date date = new Date();
		date.setTime(Long.parseLong(time));
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Util.log("<< " + sf.format(date) + " , " + (date.getYear() + 1900));
	}

	public static void main(String[] args) throws Exception {
		run();
		//		test();
		//		test2();
	}
}
