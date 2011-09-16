package cat.panel;

import java.util.Date;

import cat.DBManager;
import cat.DateField2;
import cat.model.Overdraw;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class OverDrawDailog extends JDialog {
	DateField2 overdrawDate = new DateField2();
	JTextField outputMoney = new JTextField();
	JTextField address = new JTextField();
	JTextField remark = new JTextField();

	DateField2 returnDate = new DateField2();
	JTextField inputMoney = new JTextField();
	JTextField returnRemark = new JTextField();
	JCheckBox completed = new JCheckBox();
	JButton save = new JButton("保存");
	OverdrawPane overdrawPane;

	public OverDrawDailog(Window parent, final int overdrawId,
			final OverdrawPane overdrawPane) {
		super(parent, "编辑项目", ModalityType.APPLICATION_MODAL);
		this.overdrawPane = overdrawPane;
		this.getContentPane().add(createItems(overdrawId));
		setData(overdrawId);

		setPreferredSize(new Dimension(250, 380));
		this.pack();
		this.setLocationRelativeTo(this);
	}

	private JPanel createItems(final int overdrawId) {
		JPanel pane = new JPanel();
		pane.setLayout(new GridLayout(0, 1));
		pane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		JPanel items = new JPanel(new FlowLayout(FlowLayout.LEFT));
		items.add(new JLabel("支付日期："));
		items.add(overdrawDate);
		pane.add(items);

		items = new JPanel(new FlowLayout(FlowLayout.LEFT));
		items.add(new JLabel("支付金额："));
		outputMoney.setPreferredSize(new Dimension(100, 30));
		items.add(outputMoney);
		items.add(new JLabel("元"));
		pane.add(items);

		items = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("场所：");
		items.add(label);
		address.setPreferredSize(new Dimension(100, 30));
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 25));
		items.add(address);
		pane.add(items);

		items = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("备注：");
		items.add(label);
		remark.setPreferredSize(new Dimension(100, 30));
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 25));
		items.add(remark);
		pane.add(items);

		items = new JPanel(new FlowLayout(FlowLayout.LEFT));
		items.add(new JLabel("归还日期："));
		items.add(returnDate);
		pane.add(items);

		items = new JPanel(new FlowLayout(FlowLayout.LEFT));
		items.add(new JLabel("归还金额："));
		inputMoney.setPreferredSize(new Dimension(100, 30));
		items.add(inputMoney);
		pane.add(items);

		items = new JPanel(new FlowLayout(FlowLayout.LEFT));
		items.add(new JLabel("归还备注："));
		returnRemark.setPreferredSize(new Dimension(100, 30));
		items.add(returnRemark);
		pane.add(items);

		items = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel completedLabel = new JLabel("完成：");
		completedLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 25));
		items.add(completedLabel);
		items.add(completed);
		pane.add(items);

		items = new JPanel();
		items.add(save);
		pane.add(items);

		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String money = outputMoney.getText();
				if (!money.matches("^[\\d\\.]+$")) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor(OverDrawDailog.this),
							"请输入正确的金额！", "输入错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String returnMoney = inputMoney.getText();
				if (returnMoney.length() != 0
						&& !returnMoney.matches("^[\\d\\.]+$")) {
					JOptionPane.showMessageDialog(SwingUtilities
							.getWindowAncestor(OverDrawDailog.this),
							"请输入正确的金额！", "输入错误", JOptionPane.ERROR_MESSAGE);
					return;
				}

				Overdraw overdraw = new Overdraw();
				overdraw.setId(overdrawId);
				overdraw.setTime(((Date) overdrawDate.getValue()).getTime());
				overdraw.setMoney(Float.parseFloat(money));
				overdraw.setRemark(remark.getText());
				overdraw.setAddress(address.getText());
				overdraw.setCompleted(completed.isSelected() ? 1 : 0);

				if (returnMoney.length() != 0) {
					overdraw.setReturnMoney(Float.parseFloat(returnMoney));
					overdraw.setReturnTime(((Date) returnDate.getValue())
							.getTime());
					overdraw.setReturnRemark(returnRemark.getText());
				}
				DBManager.updateOverDrawItems(overdraw);
				overdrawPane.refreshTableData();
				OverDrawDailog.this.dispose();
			}
		});

		return pane;
	}

	private void setData(int id) {
		Overdraw overdraw = DBManager.getOverDrawItems(id);
		Date outputDate = new Date(overdraw.getTime());
		overdrawDate.setValue(outputDate);

		outputMoney.setText(String.valueOf(overdraw.getMoney()));
		address.setText(overdraw.getAddress());
		remark.setText(overdraw.getRemark());
		if (overdraw.getCompleted() == 1)
			completed.setSelected(true);
		if (overdraw.getReturnMoney() != 0f) {
			Date inputDate = new Date(overdraw.getReturnTime());
			returnDate.setValue(inputDate);
			inputMoney.setText(String.valueOf(overdraw.getReturnMoney()));
			returnRemark.setText(overdraw.getReturnRemark());
		}
	}
}
