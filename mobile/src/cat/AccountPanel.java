package cat;

import java.io.IOException;
import java.io.InputStream;

import com.sun.lwuit.Command;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TabbedPane;
import com.sun.lwuit.Tabs;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.io.ConnectionRequest;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;

public class AccountPanel implements ActionListener {

	public AccountPanel() {

	}

	protected void destroyApp(boolean arg0) {
		// TODO Auto-generated method stub

	}

	protected void pauseApp() {
		// TODO Auto-generated method stub
	}

	protected void startApp() {
		String name = "TipsterTheme";
		Resources r;
		try {
			r = Resources.open("/" + name + ".res");
			UIManager.getInstance().setThemeProps(
					r.getTheme(r.getThemeResourceNames()[0]));

		} catch (IOException e) {
			e.printStackTrace();
		}
		Display.getInstance().callSerially(new Runnable() {
			public void run() {
				setMainForm();
			}
		});
	}

	protected void setMainForm() {
		Form main = new Form();
		main.setTitle("Hello World");

		Tabs tab = new Tabs();
		tab.addTab("Tab 1", new Label("Welcome to TabbedPane demo!"));

		Form f = new Form();

		//f.setLayout(new BorderLayout());
		Label label = new Label("I am a Label");
		f.addComponent( label);

		testConnectHttp(label);
		tab.addTab("Tab 2", f);

		main.addComponent(tab);
		Command exitCommand = new Command("Exit");
		main.addCommand(exitCommand);
		main.addCommandListener(this);
		main.show();
	}

	private void testConnectHttp(final Label label) {
		NetworkManager.getInstance().start();
		ConnectionRequest con = new ConnectionRequest() {
			protected void readResponse(InputStream input) throws IOException {
				byte[] buffer = new byte[10000];
				int length = input.read(buffer);
				while (length != -1) {
					System.out.println("------ "
							+ new String(buffer, 0, length));
					label.setText(new String(buffer, 0, length));
					length = input.read(buffer);
				}
			}
		};
		con.setUrl("http://accountforwife.sinaapp.com/");
		con.setPost(false);
		NetworkManager.getInstance().addToQueue(con);
	}

	public void actionPerformed(ActionEvent ae) {
		Display.getInstance().exitApplication();
	}

}
