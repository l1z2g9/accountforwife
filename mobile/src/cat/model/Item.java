package cat.model;

public class Item {
	private Integer id;
	private String title;
	private String time;
	private String money;
	private String categoryID;
	private String remark;
	private String user;
	private String address;
	private String categoryName;
	private String parentCategoryName;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getCategoryID() {
		return categoryID;
	}

	public void setCategoryID(String categoryID) {
		this.categoryID = categoryID;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getParentCategoryName() {
		return parentCategoryName;
	}

	public void setParentCategoryName(String parentCategoryName) {
		this.parentCategoryName = parentCategoryName;
	}

	public String toJson() {
		String s = "{\"title\":\"" + title + "\",";
		s += "\"time\":\"" + time + "\",";
		s += "\"money\":\"" + money + "\",";
		s += "\"categoryID\":\"" + categoryID + "\",";
		s += "\"remark\":\"" + remark + "\",";
		s += "\"user\":\"" + user + "\",";
		s += "\"address\":\"" + address + "\",";
		s += "\"categoryName\":\"" + categoryName + "\",";
		s += "\"parentCategoryName\":\"" + parentCategoryName + "\"}";
		//		System.out.println(s);
		return s;
	}
}
