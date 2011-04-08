package cat.panel;

public class Payout extends InOutPanel {
	public Payout() {
		super("支出", new String[] { "第一步：选择或输入日期，格式如2009-09-09",
				"第二步：选择或输入项目，如早饭、交通", "第三步：编写支出金额，例如：123.456",
				"第四步：编写备注，例如吃了十碗饭，只付一碗钱", "第五步：确认无误后，请点击添加保存。" });
	}
}
