package cat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import com.sun.lwuit.Button;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.animations.Transition;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.io.ConnectionRequest;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.GridLayout;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;

public class AccountPanel implements ActionListener {
	private boolean started;
	private static final int EXIT_COMMAND = 1;
	private static final int BACK_COMMAND = 3;
	private static final Command exitCommand = new Command("Exit", EXIT_COMMAND);
	private static final Command backCommand = new Command("Back", BACK_COMMAND);

	public AccountPanel() {

	}

	protected void destroyApp(boolean arg0) {
		// TODO Auto-generated method stub

	}

	protected void pauseApp() {
		// TODO Auto-generated method stub
	}

	protected void startApp(Hashtable bundle) {
		if (!started) {
			started = true;
			String name = "theme";

			try {
				Resources r = Resources.open("/" + name + ".res");
				UIManager.getInstance().setThemeProps(
						r.getTheme(r.getThemeResourceNames()[0]));
				UIManager.getInstance().setResourceBundle(bundle);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Display.getInstance().callSerially(new Runnable() {
				public void run() {
					showLoginForm();
				}
			});
		}
	}

	protected void showLoginForm() {
		Form loginForm = new Form();

		loginForm.setTitle("mainTitle");
		loginForm.addComponent(new Label("loginName"));
		final TextField searchFor = new TextField("cat", 50);
		loginForm.addComponent(searchFor);

		loginForm.addComponent(new Label("password"));
		final TextField password = new TextField("741258", 50);
		password.setConstraint(TextArea.PASSWORD);
		loginForm.addComponent(password);

		Command login = new Command("login") {
			public void actionPerformed(ActionEvent ae) {
				showMainForm();
			}
		};
		Button searchButton = new Button(login);
		Container searchContainer = new Container(new BorderLayout());
		searchContainer.addComponent(BorderLayout.EAST, searchButton);
		loginForm.addComponent(searchContainer);

		loginForm.addCommand(exitCommand);
		loginForm.addCommand(login);
		loginForm.show();
	}

	private void showMainForm() {
		Form mainForm = new Form();
		mainForm.setTitle("mainTitle");

		Transition in, out;
		out = CommonTransitions.createSlide(CommonTransitions.SLIDE_HORIZONTAL,
				false, 500);
		in = CommonTransitions.createSlide(CommonTransitions.SLIDE_HORIZONTAL,
				true, 500);
		mainForm.setTransitionInAnimator(in);
		mainForm.setTransitionOutAnimator(out);

		mainForm.setLayout(new BoxLayout(BoxLayout.Y_AXIS));

		Button expenditure = createButton("expenditure");
		expenditure.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final BalancePane expenditure = new BalancePane("Expenditure");
			}
		});

		mainForm.addComponent(expenditure);
		mainForm.addComponent(createButton("income"));
		mainForm.addComponent(createButton("query"));
		mainForm.addComponent(createButton("overdraw"));

		mainForm.addCommand(exitCommand);

		mainForm.show();
	}

	private Button createButton(String string) {
		Button button = new Button(string);
		button.getUnselectedStyle().setAlignment(Label.CENTER);
		button.getSelectedStyle().setAlignment(Label.CENTER);
		button.getPressedStyle().setAlignment(Label.CENTER);
		return button;
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

	public void actionPerformed(ActionEvent evt) {
		Command cmd = evt.getCommand();
		switch (cmd.getId()) {
		case EXIT_COMMAND:
			Display.getInstance().exitApplication();
			break;
		case BACK_COMMAND:
//			mainMenu.showBack();
			break;
		}
	}
}
