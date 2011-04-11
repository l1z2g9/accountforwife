package cat.panel;

import cat.Configure;
import cat.DBManager;
import cat.model.Item;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class EditDialog extends JDialog {
	private Item item;
	Map<String, Integer> categories;
	Map<String, Integer> subcategories;

	public EditDialog(Window parent, final Item item, String type,
			final BalancePane balancePane, final int selectedRow) {
		super(parent, "编辑项目", ModalityType.APPLICATION_MODAL);
		this.item = item;
		categories = DBManager.getCategory(type);
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
		JPanel pane = new JPanel();
		arrange(pane, "日期：");
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

		JLabel date = new JLabel(sf.format(item.getTime()));
		date.setPreferredSize(new Dimension(100, 30));
		pane.add(date);
		container.add(pane);

		pane = new JPanel();
		arrange(pane, "类别：");
		final JComboBox categoryCombox = new JComboBox(categories.keySet()
				.toArray());
		categoryCombox.setPreferredSize(new Dimension(100, 30));
		categoryCombox.setSelectedItem(item.getParentCategoryName());
		pane.add(categoryCombox);
		container.add(pane);

		pane = new JPanel();
		arrange(pane, "小类别：");
		subcategories = DBManager.getSubCategory(categories
				.get((String) categoryCombox.getSelectedItem()));

		final JComboBox subCategoryCombox = new JComboBox(subcategories
				.keySet().toArray());
		subCategoryCombox.setPreferredSize(new Dimension(100, 30));
		subCategoryCombox.setSelectedItem(item.getCategoryName());
		pane.add(subCategoryCombox);
		container.add(pane);

		categoryCombox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultComboBoxModel model = (DefaultComboBoxModel) subCategoryCombox
						.getModel();
				model.removeAllElements();
				subcategories = DBManager.getSubCategory(categories
						.get((String) categoryCombox.getSelectedItem()));
				for (String name : subcategories.keySet()) {
					model.addElement(name);
				}
			}
		});

		pane = new JPanel();
		arrange(pane, "金额：");
		final JTextField money = new JTextField(String.valueOf(item.getMoney()));
		money.setPreferredSize(new Dimension(80, 30));
		pane.add(money, BorderLayout.CENTER);
		pane.add(new JLabel("元"));
		container.add(pane);

		pane = new JPanel();
		arrange(pane, "用户：");
		final JTextField user = new JTextField(item.getUser());
		user.setPreferredSize(new Dimension(100, 30));
		pane.add(user);
		container.add(pane);

		pane = new JPanel();
		arrange(pane, "场所：");
		final JTextField address = new JTextField(item.getAddress());
		address.setPreferredSize(new Dimension(100, 30));
		pane.add(address);
		container.add(pane);

		pane = new JPanel();
		arrange(pane, "备注：");
		final JTextField remark = new JTextField(item.getRemark());
		remark.setPreferredSize(new Dimension(100, 30));
		pane.add(remark);
		container.add(pane);

		pane = new JPanel();
		JButton update = new JButton("保存");
		update.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (money.getText().trim().isEmpty()) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor(EditDialog.this), "金额不能为空！",
							"输入错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// 验证金额是否为数字，没有使用JFormattedTextField
				Pattern pattern = Pattern.compile("^[0-9\\.]+$");
				Matcher m = pattern.matcher(money.getText());
				if (!m.matches()) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor(EditDialog.this), "请输入正确的金额！",
							"输入错误", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// 保存
				float _money = Float.valueOf(money.getText());
				Item toSaveItem = new Item();
				toSaveItem.setId(item.getId());
				toSaveItem.setTime(item.getTime());
				toSaveItem.setMoney(_money);
				toSaveItem.setCategoryID(subcategories
						.get((String) subCategoryCombox.getSelectedItem()));
				toSaveItem.setRemark(remark.getText());
				toSaveItem.setUser(user.getText());
				toSaveItem.setAddress(address.getText());
				DBManager.updateItem(toSaveItem);

				/*
				 * JOptionPane.showMessageDialog(SwingUtilities
				 * .getWindowAncestor(EditDialog.this), "成功更新内容", "更新内容",
				 * JOptionPane.INFORMATION_MESSAGE);
				 */
				EditDialog.this.dispose();

				balancePane.refreshData();

				/*
				 * tableModel.setValueAt(categoryCombox.getSelectedItem(),
				 * selectedRow, 3);
				 * tableModel.setValueAt(subCategoryCombox.getSelectedItem(),
				 * selectedRow, 4); tableModel.setValueAt(toSaveItem.getMoney(),
				 * selectedRow, 5); tableModel.setValueAt(toSaveItem.getUser(),
				 * selectedRow, 6);
				 * tableModel.setValueAt(toSaveItem.getAddress(), selectedRow,
				 * 7); tableModel.setValueAt(toSaveItem.getRemark(),
				 * selectedRow, 8);
				 */
			}
		});
		pane.add(update);
		container.add(pane);

		this.getContentPane().add(container);
		setPreferredSize(new Dimension(250, 350));
		this.pack();
		this.setLocationRelativeTo(this);
	}

	private void arrange(JPanel pane, String text) {
		JLabel label = new JLabel(text);
		label.setPreferredSize(new Dimension(50, 30));
		pane.add(label, FlowLayout.LEFT);
	}
}
