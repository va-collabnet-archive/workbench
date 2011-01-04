package org.ihtsdo.qa.store.model.view;

public enum RulesReportColumn {
	RULE_NAME(1, "Rule name"), RULE_CODE(2, "Rule code"), CATEGORY(3, "Category"),
	SEVERITY(4, "Severity"), OPEN(5, "Open"), CLOSED(6,"Closed"), ESCALATED(7,"Escalated"),
	DEFERRED(8, "Deferred"), CLEARED(9,"Cleared"), IN_DISCUTION(10,"In Discussion"), 
	DISPOSITION_STATUS_FILTER(11, "Disposition status filter"), STATUS(12,"Status filter");

	private Integer columnNumber;
	private String columnName;

	RulesReportColumn(Integer columnNumber, String columnName) {
		this.columnNumber = columnNumber;
		this.columnName = columnName;
	}

	public Integer getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(Integer columnNumber) {
		this.columnNumber = columnNumber;
	}

	@Override
	public String toString() {
		return this.columnName;
	}
}
