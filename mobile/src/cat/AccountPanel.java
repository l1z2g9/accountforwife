package cat;

import java.io.IOException;

import javax.microedition.midlet.MIDlet;

import com.sun.lwuit.Command;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;

public class AccountPanel extends MIDlet implements ActionListener {

	public AccountPanel() {

		// TODO Auto-generated constructor stub
	}

	protected void destroyApp(boolean arg0) {
		// TODO Auto-generated method stub

	}

	protected void pauseApp() {
		// TODO Auto-generated method stub
	}

	protected void startApp() {
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

		Form f = new Form();

		f.setTitle("Hello World, Java me");
		f.setLayout(new BorderLayout());
		f.addComponent("Center", new Label("I am a Label"));
		f.show();

		Command exitCommand = new Command("Exit");
		f.addCommand(exitCommand);
		f.addCommandListener(this);
	}

	public void actionPerformed(ActionEvent ae) {
		notifyDestroyed();
	}

}
