package cat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.midlet.MIDlet;

import com.sun.lwuit.Display;
import com.sun.lwuit.io.util.JSONParser;

public class AccountPanelMIDlet extends MIDlet {
	private AccountPanel main = new AccountPanel();
	private Hashtable bundle = new Hashtable();

	public AccountPanelMIDlet() {
		bundle.put("mainTitle", "小艺有数");
		bundle.put("loginName", "用户");
		bundle.put("password", "密码");
		bundle.put("login", "登录");
		bundle.put("exit", "退出");
		bundle.put("back", "后退");
		bundle.put("expenditure", "支出");
		bundle.put("income", "收入");
		bundle.put("query", "查询");
		bundle.put("overdraw", "预支付");
		bundle.put("year", "年");
		bundle.put("month", "月");
		bundle.put("day", "日");
		bundle.put("category", "类别");
		bundle.put("subCategory", "子类别");
		bundle.put("money", "金额");
	}

	public void startApp() {
		Display.init(this);
		main.startApp(bundle);
		/*Display d = Display.getDisplay(this);
		*/

	}

	public void pauseApp() {
		main.pauseApp();
	}

	public void destroyApp(boolean unconditional) {
		main.destroyApp(unconditional);
	}

}
