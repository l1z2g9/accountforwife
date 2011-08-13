package cat.model;

import java.util.Vector;

public class NavigatePage {
	private int totalPage;
	private int total;
	private Vector<Vector> currentPageResult;
	private Float totalIncome = 0f;
	private Float totalExpenditure = 0f;
	private Float totalBalance = 0f;
	
	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public Vector<Vector> getCurrentPageResult() {
		return currentPageResult;
	}

	public void setCurrentPageResult(Vector<Vector> currentPageResult) {
		this.currentPageResult = currentPageResult;
	}

	public Float getTotalIncome() {
		return totalIncome;
	}

	public void setTotalIncome(Float totalIncome) {
		this.totalIncome = totalIncome;
	}

	public Float getTotalExpenditure() {
		return totalExpenditure;
	}

	public void setTotalExpenditure(Float totalExpenditure) {
		this.totalExpenditure = totalExpenditure;
	}

	public Float getTotalBalance() {
		return totalBalance;
	}

	public void setTotalBalance(Float totalBalance) {
		this.totalBalance = totalBalance;
	}
}
