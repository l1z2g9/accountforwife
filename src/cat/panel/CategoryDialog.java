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

import cat.DBManager;
import cat.model.Category;

public class CategoryDialog extends JDialog {
	Logger log = Logger.getLogger("CategoryPane");

	private JTextField categoryName;
	private JTextField displayOrder;
	private JList list;

	private DefaultListModel listModel;

	private Map<String, Category> cates;

	public CategoryDialog(Window frame, final String type) {
		super(frame, "�������", Dialog.ModalityType.DOCUMENT_MODAL);
		setLayout(new BorderLayout());
		cates = DBManager.getCategory(type);
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

		JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel nameLabel = new JLabel("���ƣ�");
		nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 23));
		itemPanel.add(nameLabel);
		categoryName = new JTextField();
		categoryName.setPreferredSize(new Dimension(80, 25));
		itemPanel.add(categoryName);
		leftPanel.add(itemPanel);

		itemPanel = new JPanel();
		itemPanel.add(new JLabel("��ʾ˳��"));
		displayOrder = new JTextField();
		displayOrder.setPreferredSize(new Dimension(80, 25));
		itemPanel.add(displayOrder);
		leftPanel.add(itemPanel);

		JButton add = new JButton("����");
		add.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = listModel.getSize();
				String cate = categoryName.getText().trim();
				int dispOrder = Integer.valueOf(displayOrder.getText().trim());
				if (cate.equalsIgnoreCase("")) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor(CategoryDialog.this),
							"�������ӿհ�����!", "���Ӵ���", JOptionPane.ERROR_MESSAGE);
					return;
				}
				DBManager.saveCategory(type, cate, dispOrder);
				listModel.addElement(cate + "  [ " + dispOrder + " ]");
				list.setSelectedIndex(index);
				list.ensureIndexIsVisible(index);

			}
		});

		leftPanel.add(add);
		leftPanel.add(Box.createVerticalStrut(150));

		JButton close = new JButton("�ر�");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CategoryDialog.this.setVisible(false);
				CategoryDialog.this.dispose();
			}
		});
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

		list.setVisibleRowCount(10);
		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setPreferredSize(new Dimension(100, 200));
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(listScrollPane, BorderLayout.PAGE_START);

		JButton delete = new JButton("ɾ��");
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();
				if (index == -1) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor((JButton) e.getSource()),
							"�б��κ�����!");
					return;
				}

				String cateName = (String) list.getSelectedValue();
				cateName = cateName.replaceAll("^(.*)\\[.*$", "$1").trim();
				DBManager.deleteCategory(cates.get(cateName).getId());
				listModel.removeElementAt(index);
				if (index == 0) {
					index = 0;
				} else {
					index--;
				}
				list.setSelectedIndex(index);
				list.ensureIndexIsVisible(index);
			}
		});

		rightPanel.add(delete, BorderLayout.PAGE_END);
		leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(leftPanel, BorderLayout.LINE_START);
		add(rightPanel, BorderLayout.LINE_END);
		pack();
	}
}