package cat;

import javax.microedition.midlet.MIDlet;

import com.sun.lwuit.Display;

public class AccountPanelMIDlet extends MIDlet {
	private AccountPanel main = new AccountPanel();

	public void startApp() {
		Display.init(this);
		main.startApp();
	}

	public void pauseApp() {
		main.pauseApp();
	}

	public void destroyApp(boolean unconditional) {
		main.destroyApp(unconditional);
	}

}
