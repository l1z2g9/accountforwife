package cat;


import java.text.DecimalFormat;

import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.jfree.util.Log;

import sun.util.logging.resources.logging;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import java.io.File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;


public class Test
{
  static Logger log = Logger.getLogger("Test");


  public static void main(String[] args)
      throws Exception
  {
    /*    final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JButton button = new JButton("下拉");
        JPanel panel = new JPanel();
        System.out.println(panel.getFont());
        panel.add(new JLabel("测试")); 
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    String value = "100";
    DecimalFormat nf = new DecimalFormat("##.##");
    nf.format(Float.valueOf(value));
    */
    //    Preferences p = Preferences.userNodeForPackage(Test.class);
    //    p.put("cj","hg");
    /*for (String s : p.keys())
    {
      System.out.println(s);
    }
    Class.forName("org.sqlite.JDBC");
    Connection conn = DriverManager.getConnection("jdbc:sqlite:Account3.db");
    Statement stmt = conn.createStatement();
    stmt.execute("create table cj(id int)");
    stmt.close();
    conn.close();
    */
    log.info(String.format("%-"+(12-3) +"s%3.2f", "大长今", 12.0));
    log.info(String.format("%-"+(12-2) +"s%3.2f", "大长", 12.1));
  }
}
