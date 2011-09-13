package cat.model;

public class Budget {
	private Integer id;
	private Integer categoryID;
	private String year;
	private String month;
	private String color75;
	private String color90;
	private Float money;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCategoryID() {
		return categoryID;
	}

	public void setCategoryID(Integer categoryID) {
		this.categoryID = categoryID;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getColor75() {
		return color75;
	}

	public void setColor75(String color75) {
		this.color75 = color75;
	}

	public String getColor90() {
		return color90;
	}

	public void setColor90(String color90) {
		this.color90 = color90;
	}

	public Float getMoney() {
		return money;
	}

	public void setMoney(Float money) {
		this.money = money;
	}

}
