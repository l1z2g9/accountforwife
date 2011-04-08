package cat.panel;

public class Income extends InOutPanel {
	public Income() {
		super("收入", new String[] { "第一步：选择或输入日期，格式如2009-09-09",
				"第二步：选择或输入项目，如工资、奖金", "第三步：编写收入金额，例如：123.456",
				"第四步：编写备注，例如路上捡的", "第五步：确认无误后，请点击添加保存。" });
	}
}
