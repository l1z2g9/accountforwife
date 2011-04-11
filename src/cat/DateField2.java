package cat;


import java.awt.Component;
import net.sf.nachocalendar.components.DateField;
import net.sf.nachocalendar.components.DefaultHeaderRenderer;
import net.sf.nachocalendar.components.HeaderPanel;
import net.sf.nachocalendar.components.HeaderRenderer;


public class DateField2
    extends DateField
{
//  DateField datefield;


  public DateField2()
  {
//    datefield = CalendarFactory.createDateField();
    this.setHeaderRenderer(new HeaderRenderer()
    {
      public Component getHeaderRenderer(HeaderPanel panel, Object value, boolean isHeader, boolean isWorking)
      {
        DefaultHeaderRenderer render = new DefaultHeaderRenderer();
        String header = value.toString();
        render.setText(header.substring(header.length() - 1, header.length()));
        return render;
      }

    });
    this.setDateFormat(Configure.dateFormat);

  }
}
