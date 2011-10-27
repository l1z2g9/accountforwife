package cat;

import java.util.Vector;

import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextField;
import com.sun.lwuit.list.DefaultListModel;

public class ComponentFactory {
	public static Label getLabel(String msg) {
		Label label = new Label(msg);
		label.getStyle().setMargin(0, 0, 2, 0);
		label.getStyle().setPadding(0, 0, 0, 0);
		return label;
	}

	public static TextField getTextField(int columns, int constraint) {
		TextField field = new TextField(columns);
		field.setConstraint(constraint);
		field.getStyle().setMargin(0, 0, 2, 0);
		field.getStyle().setPadding(0, 0, 0, 0);
		field.setPreferredW(40);
		field.setWidth(40);
		field.setPreferredH(20);
		field.setHeight(20);

		return field;
	}

	public static ComboBox getCombox(Vector v) {
		DefaultListModel model = new DefaultListModel(v);
		ComboBox box = new ComboBox(model);
		return alignComboBox(box);
	}

	public static ComboBox getCombox(Object[] elements) {
		ComboBox box = new ComboBox(elements);
		return alignComboBox(box);
	}

	private static ComboBox alignComboBox(final ComboBox box) {
		/*box.setPreferredW(40);
		box.setWidth(40);
		box.setPreferredH(18);
		box.setHeight(18);
		box.getStyle().setMargin(0, 0, 2, 0);
		box.getStyle().setPadding(0, 0, 0, 0);*/
		return box;
	}

}
