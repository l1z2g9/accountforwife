/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sun.lwuit.Command;
import com.sun.lwuit.Display;
import com.sun.lwuit.*;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;
import java.io.IOException;
import javax.microedition.midlet.MIDlet;

/**
 * @author Jay
 */
public class Midlet extends MIDlet implements ActionListener {

    public void startApp() {
        Display.init(this);
        try {
            Resources r = Resources.open("/tipster.res");
            UIManager.getInstance().setThemeProps(r.getTheme("tipster"));
        } catch (IOException ioe) {
            // Do something here.
        }
        Form f = new Form("Hello, LWUIT!");

        f.show();

        Command exitCommand = new Command("Exit");
        f.addCommand(exitCommand);
        f.addCommandListener(this);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void actionPerformed(ActionEvent ae) {
        notifyDestroyed();
    }
}
