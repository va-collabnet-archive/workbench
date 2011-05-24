package org.ihtsdo.qa.store.model.view;

public enum QACasesReportColumn {
	CONCEPT_NAME(1), STATUS(2), DISPOSITION(3), ASSIGNED_TO(4), TIME(5);
	
	private Integer columnNumber;

	QACasesReportColumn(Integer columnNumber) {
		this.columnNumber = columnNumber;
	}

	public Integer getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(Integer columnNumber) {
		this.columnNumber = columnNumber;
	}
}
