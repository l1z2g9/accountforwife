package cat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Label;
import com.sun.lwuit.io.ConnectionRequest;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.io.util.JSONParser;
import com.sun.lwuit.layouts.BoxLayout;

public class Util {
	public static String expenditureCategory = "http://accountforwife.sinaapp.com/category/Expenditure";
	public static String incomeCategory = "http://accountforwife.sinaapp.com/category/Income";
	public static String subCategory = "http://accountforwife.sinaapp.com/subCategory";
	public static String saveUrl = "http://accountforwife.sinaapp.com/index.php/save";
	public static String searchUrl = "http://accountforwife.sinaapp.com/index.php/search";

	public static Vector getCategories(String categoryUrl,
			final Hashtable categoryData) {
		final Vector categories = new Vector();
		NetworkManager.getInstance().start();
		ConnectionRequest con = new ConnectionRequest() {
			protected void readResponse(InputStream input) throws IOException {
				final JSONParser p = new JSONParser();
				Hashtable data = p.parse(new InputStreamReader(input));
				Enumeration keys = data.keys();
				while (keys.hasMoreElements()) {
					Object cat = keys.nextElement();
					categories.addElement(cat);
					categoryData.put(cat, data.get(cat));
				}
			}
		};
		con.setUrl(categoryUrl);
		con.setPost(false);
		NetworkManager.getInstance().addToQueueAndWait(con);

		//categories.addElement("XX");
		return categories;
	}

	public static Vector getSubCategories(String categoryID,
			final Hashtable categoryData, final Hashtable subCategoryData,
			String type) {
		final Vector categories = new Vector();
		NetworkManager.getInstance().start();
		final ConnectionRequest con = new ConnectionRequest() {
			protected void readResponse(InputStream input) throws IOException {
				final JSONParser p = new JSONParser();
				Hashtable data = p.parse(new InputStreamReader(input));
				final Enumeration keys = data.keys();

				while (keys.hasMoreElements()) {
					Object cat = keys.nextElement();
					categories.addElement(cat);
					subCategoryData.put(cat, data.get(cat));
				}
			}
		};
		String url = Util.subCategory;

		if ("支出".equals(type)) {
			url += "/Expenditure";
		} else {
			url += "/Income";
		}
		url += "/" + categoryData.get(categoryID);
		con.setUrl(url);
		con.setPost(false);
		NetworkManager.getInstance().addToQueueAndWait(con);

		//categories.addElement("XX");
		return categories;
	}

	public static Component createOneRow(Label label, Component item) {
		Container pane = new Container(new BoxLayout(BoxLayout.X_AXIS));
		pane.addComponent(label);
		pane.addComponent(item);
		pane.getStyle().setMargin(Component.TOP, 6);
		return pane;
	}

	public static Component createOneRow(Component item) {
		Container pane = new Container(new BoxLayout(BoxLayout.X_AXIS));
		pane.addComponent(item);
		pane.getStyle().setMargin(Component.TOP, 6);
		return pane;
	}

	public static void logInputStream(InputStream input) {
		byte[] b = new byte[409600];
		try {
			int x = input.read(b);
			System.out.println("log: " + new String(b, 0, x));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void log(Object str) {
		System.out.println(str);
	}
}
