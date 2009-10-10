package cat;


import java.text.DecimalFormat;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;


public class Test
{

  public static void main(String[] args)
  {
    /*    final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JButton button = new JButton("œ¬¿≠");
        JPanel panel = new JPanel();
        System.out.println(panel.getFont());
        panel.add(new JLabel("≤‚ ‘")); 
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    */
    String value = "100";
    DecimalFormat nf = new DecimalFormat("##.##");
    nf.format(Float.valueOf(value));

  }
}
