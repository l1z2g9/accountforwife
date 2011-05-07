package cat.panel;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cat.DBManager;
import cat.model.Category;

public class CategoryDialog extends JDialog {
	Logger log = Logger.getLogger("CategoryPane");

	private JList list;
	private JButton add;
	private JButton delete;
	private JButton close;
	private JButton modify;
	private JTextField modifyText;
	private JTextField modifyDisplayOrder;
	private JTextField categoryName;
	private JTextField displayOrder;
	private DefaultListModel listModel;
	private Map<String, Category> cates;
	public static boolean itemchanged = false;
	private String type;

	public CategoryDialog(Window frame, final String type) {
		super(frame, "类别设置", Dialog.ModalityType.DOCUMENT_MODAL);
		this.type = type;
		setLayout(new BorderLayout());
		cates = DBManager.getCategory(type);
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

		JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel nameLabel = new JLabel("名称：");
		nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 23));
		itemPanel.add(nameLabel);
		categoryName = new JTextField();
		categoryName.setPreferredSize(new Dimension(80, 25));
		itemPanel.add(categoryName);
		leftPanel.add(itemPanel);

		itemPanel = new JPanel();
		itemPanel.add(new JLabel("显示顺序："));
		displayOrder = new JTextField("0");
		displayOrder.setToolTipText("数值越大，越靠前显示");
		displayOrder.setPreferredSize(new Dimension(80, 25));
		itemPanel.add(displayOrder);
		leftPanel.add(itemPanel);

		add = new JButton("添加");
		add.setAlignmentX(JComponent.LEFT_ALIGNMENT);

		leftPanel.add(add);
		leftPanel.add(Box.createVerticalStrut(20));

		// 修改类名
		JPanel modifPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel modifyLabel = new JLabel("名称：");
		modifyLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 23));
		modifPanel.add(modifyLabel);
		modifyText = new JTextField();
		modifyText.setPreferredSize(new Dimension(80, 25));
		modifPanel.add(modifyText);
		leftPanel.add(modifPanel);

		modifPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel modifyDisplayOrderLabel = new JLabel("显示顺序：");
		modifPanel.add(modifyDisplayOrderLabel);
		modifyDisplayOrder = new JTextField();
		modifyDisplayOrder.setPreferredSize(new Dimension(80, 25));
		modifPanel.add(modifyDisplayOrder);
		leftPanel.add(modifPanel);

		modify = new JButton("修改");
		leftPanel.add(modify);

		leftPanel.add(Box.createVerticalStrut(20));
		close = new JButton("关闭");
		leftPanel.add(close);

		listModel = new DefaultListModel();
		for (String cate : cates.keySet()) {
			Category category = cates.get(cate);
			listModel.addElement(category.getName() + "  [ "
					+ category.getDisplayOrder() + " ]");
		}
		list = new JList(listModel);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);

		updateModifyText();

		list.setVisibleRowCount(10);

		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setPreferredSize(new Dimension(100, 200));
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(listScrollPane, BorderLayout.PAGE_START);

		delete = new JButton("删除");
		rightPanel.add(delete, BorderLayout.PAGE_END);
		leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(leftPanel, BorderLayout.LINE_START);
		add(rightPanel, BorderLayout.LINE_END);

		configAction();
		pack();
	}

	private void configAction() {
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = listModel.getSize();
				String cate = categoryName.getText().trim();
				int dispOrder = Integer.valueOf(displayOrder.getText().trim());
				if (cate.equalsIgnoreCase("")) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor(CategoryDialog.this),
							"不能添加空白数据!", "添加错误", JOptionPane.ERROR_MESSAGE);
					return;
				}

				int categoryID = DBManager.saveCategory(type, cate, dispOrder);
				listModel.addElement(cate + "  [ " + dispOrder + " ]");
				list.setSelectedIndex(index);
				list.ensureIndexIsVisible(index);

				Category category = new Category();
				category.setId(categoryID);
				category.setName(cate);
				category.setDisplayOrder(dispOrder);

				cates.put(cate, category);
				itemchanged = true;
			}
		});

		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();
				if (index == -1) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor((JButton) e.getSource()),
							"列表任何内容!");
					return;
				}

				String cateName = (String) list.getSelectedValue();
				cateName = cateName.replaceAll("^(.*)\\[.*$", "$1").trim();
				boolean subCategoryCount = DBManager.deleteCategory(cates.get(
						cateName).getId());
				if (!subCategoryCount) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor((JButton) e.getSource()),
							"请先删除小类别，然后删除该类别!");
					return;
				}

				listModel.removeElementAt(index);
				if (index == 0) {
					index = 0;
				} else {
					index--;
				}
				list.setSelectedIndex(index);
				list.ensureIndexIsVisible(index);
				itemchanged = true;
			}
		});

		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CategoryDialog.this.setVisible(false);
				CategoryDialog.this.dispose();
			}
		});

		modify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (modifyText.getText().equalsIgnoreCase("")) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor(CategoryDialog.this),
							"不能设置空白数据!", "修改错误", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (list.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor(CategoryDialog.this), "请选择类别!",
							"修改错误", JOptionPane.ERROR_MESSAGE);
					return;
				}

				String name = (String) list.getSelectedValue();
				String cateName = name.replaceAll("^(.*)\\s+\\[(.*)\\]$", "$1")
						.trim();
				Category cate = cates.get(cateName);
				int dispOrder = Integer.valueOf(modifyDisplayOrder.getText()
						.trim());
				DBManager.updateCategory(cate.getId(), modifyText.getText(),
						dispOrder);
				cates = DBManager.getCategory(type);
				listModel.removeAllElements();
				for (String c : cates.keySet()) {
					Category category = cates.get(c);
					listModel.addElement(category.getName() + "  [ "
							+ category.getDisplayOrder() + " ]");
				}
				list.setSelectedIndex(0);
			}
		});

		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateModifyText();
			}
		});
	}

	private void updateModifyText() {
		if (list.getSelectedValue() != null) {
			String name = (String) list.getSelectedValue();
			String cateName = name.replaceAll("^(.*)\\s+\\[(.*)\\]$", "$1")
					.trim();
			String cateOrder = name.replaceAll("^(.*)\\s+\\[(.*)\\]$", "$2")
					.trim();
			modifyText.setText(cateName);
			modifyDisplayOrder.setText(cateOrder);
		}
	}
}
