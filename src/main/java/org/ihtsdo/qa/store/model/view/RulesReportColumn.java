package org.ihtsdo.qa.store.model.view;

public enum RulesReportColumn {
	RULE_NAME(1), RULE_CODE(2), CATEGORY(3), SEVERITY(4), OPEN(5), CLOSED(6), ESCALATED(7), DEFERRED(8), CLEARED(9);

	private Integer columnNumber;

	RulesReportColumn(Integer columnNumber) {
		this.columnNumber = columnNumber;
	}

	public Integer getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(Integer columnNumber) {
		this.columnNumber = columnNumber;
	}
}
