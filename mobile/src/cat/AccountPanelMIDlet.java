package cat;

import java.util.Hashtable;

import javax.microedition.midlet.MIDlet;

import com.sun.lwuit.Display;

public class AccountPanelMIDlet extends MIDlet {
	private AccountPanel main = new AccountPanel();
	private Hashtable bundle = new Hashtable();

	public AccountPanelMIDlet() {
		bundle.put("mainTitle", "小艺有数");
		bundle.put("loginName", "用户名");
		bundle.put("password", "密码");
		bundle.put("login", "登录");
		bundle.put("exit", "退出");
		bundle.put("expenditure", "支出");
		bundle.put("income", "收入");
		bundle.put("query", "查询");
		bundle.put("overdraw", "预支付");
	}

	public void startApp() {
		Display.init(this);
		main.startApp(bundle);
	}

	public void pauseApp() {
		main.pauseApp();
	}

	public void destroyApp(boolean unconditional) {
		main.destroyApp(unconditional);
	}

}
