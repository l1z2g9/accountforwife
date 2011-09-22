package cat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import com.sun.lwuit.Button;
import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.List;
import com.sun.lwuit.animations.Transition;
import com.sun.lwuit.animations.Transition3D;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.io.ConnectionRequest;
import com.sun.lwuit.io.NetworkManager;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.list.ListCellRenderer;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;

public class AccountPanel implements ActionListener {
	private boolean started;
	private static final int EXIT_COMMAND = 1;
	private static final int BACK_COMMAND = 3;

	private static final int EXPENDITURE_COMMAND = 5;
	private static final int INCOME_COMMAND = 6;
	private static final int QUERY_COMMAND = 7;
	private static final int OVERDRAW_COMMAND = 8;

	public static final Command exitCommand = new Command("exit", EXIT_COMMAND);
	public static final Command backCommand = new Command("back", BACK_COMMAND);
	Resources imagesRes;

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
			String name = "theme.res";
			UIManager.getInstance().setResourceBundle(bundle);

			try {
				Resources r = Resources.open("/" + name);
				UIManager.getInstance().setThemeProps(
						r.getTheme(r.getThemeResourceNames()[0]));

				imagesRes = Resources.open("/images.res");
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
		loginForm.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
		loginForm.setTitle("mainTitle");

		Container userPanel = new Container();
		userPanel.addComponent(new Label("loginName"));

		final ComboBox user = new ComboBox(new String[] { "cat", "forest" });
		userPanel.addComponent(user);

		user.setRenderer(new ListCellRenderer() {
			public Component getListCellRendererComponent(List list,
					Object value, int index, boolean isSelected) {
				Label label = new Label();
				label.setIcon(imagesRes.getImage(value + ".jpg"));
				return label;
			}

			public Component getListFocusComponent(List list) {
				list.setSmoothScrolling(true);
				Label label = new Label();
				return label;
			}
		});
		loginForm.addComponent(userPanel);

		Command login = new Command("login") {
			public void actionPerformed(ActionEvent ae) {
				showMainForm();
			}
		};
		Button searchButton = new Button(login);
		Container searchContainer = new Container(new BorderLayout());
		searchContainer.addComponent(BorderLayout.WEST, searchButton);
		loginForm.addComponent(searchContainer);

		loginForm.addCommand(exitCommand);
		loginForm.addCommand(login);
		loginForm.addCommandListener(this);
		loginForm.show();
	}

	Form mainForm;

	private void showMainForm() {
		mainForm = new Form();
		mainForm.setTitle("mainTitle");

		Transition in, out;
		out = Transition3D.createFlyIn(500);
		in = Transition3D.createCube(500, true);
		mainForm.setTransitionInAnimator(in);
		mainForm.setTransitionOutAnimator(out);

		mainForm.setLayout(new BoxLayout(BoxLayout.Y_AXIS));

		mainForm.addComponent(createButton("expenditure", EXPENDITURE_COMMAND));
		mainForm.addComponent(createButton("income", INCOME_COMMAND));
		mainForm.addComponent(createButton("query", QUERY_COMMAND));
		mainForm.addComponent(createButton("overdraw", OVERDRAW_COMMAND));

		mainForm.addCommand(exitCommand);
		mainForm.addCommandListener(this);
		mainForm.show();
	}

	private Button createButton(String string, int id) {
		Button button = new Button(string);
		button.getUnselectedStyle().setAlignment(Label.CENTER);
		button.getSelectedStyle().setAlignment(Label.CENTER);
		button.getPressedStyle().setAlignment(Label.CENTER);
		button.setCommand(new Command(string, id));
		button.addActionListener(this);
		return button;
	}

	private void testConnectHttp(final Label label) {
		NetworkManager.getInstance().start();
		ConnectionRequest con = new ConnectionRequest() {
			protected void readResponse(InputStream input) throws IOException {
				byte[] buffer = new byte[10000];
				int length = input.read(buffer);
				while (length != -1) {
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
		final Command cmd = evt.getCommand();

		switch (cmd.getId()) {
		case EXIT_COMMAND:
			Display.getInstance().exitApplication();
			break;
		case BACK_COMMAND:
			mainForm.showBack();
			break;

		case EXPENDITURE_COMMAND:
			new BalancePane("expenditure", this).show();
			break;
		case INCOME_COMMAND:
			new BalancePane("income", this).show();
			break;
		}
	}
}
