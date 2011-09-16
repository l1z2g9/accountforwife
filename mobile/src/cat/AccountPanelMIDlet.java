package cat;

import java.io.IOException;

import javax.microedition.midlet.MIDlet;

import com.sun.lwuit.Display;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;

public class AccountPanelMIDlet extends MIDlet {
	private AccountPanel main = new AccountPanel();

	public void startApp() {
		Display.init(this);
		String name = "TipsterTheme";
		Resources r;
		try {
			r = Resources.open("/" + name + ".res");
			UIManager.getInstance().setThemeProps(
					r.getTheme(r.getThemeResourceNames()[0]));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void pauseApp() {

	}

	public void destroyApp(boolean unconditional) {

	}

}
