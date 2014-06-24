/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.qa.store.model.view;

/**
 * The Enum RulesReportColumn.
 */
public enum RulesReportColumn {
	
	/** The RUL e_ name. */
	RULE_NAME(1, "Rule name"), 
 /** The RUL e_ code. */
 RULE_CODE(2, "Rule code"), 
 /** The CATEGORY. */
 CATEGORY(3, "Category"),
	
	/** The SEVERITY. */
	SEVERITY(4, "Severity"), 
 /** The OPEN. */
 OPEN(5, "Open"), 
 /** The CLOSED. */
 CLOSED(6,"Closed"), 
 /** The ESCALATED. */
 ESCALATED(7,"Escalated"),
	
	/** The DEFERRED. */
	DEFERRED(8, "Deferred"), 
 /** The CLEARED. */
 CLEARED(9,"Cleared"), 
 /** The I n_ discution. */
 IN_DISCUTION(10,"In Discussion"), 
	
	/** The DISPOSITIO n_ statu s_ filter. */
	DISPOSITION_STATUS_FILTER(11, "Disposition status filter"), 
 /** The STATUS. */
 STATUS(12,"Status filter"), 
 /** The RUL e_ date. */
 RULE_DATE(13, "Rule date");

	/** The column number. */
	private Integer columnNumber;
	
	/** The column name. */
	private String columnName;

	/**
	 * Instantiates a new rules report column.
	 *
	 * @param columnNumber the column number
	 * @param columnName the column name
	 */
	RulesReportColumn(Integer columnNumber, String columnName) {
		this.columnNumber = columnNumber;
		this.columnName = columnName;
	}

	/**
	 * Gets the column number.
	 *
	 * @return the column number
	 */
	public Integer getColumnNumber() {
		return columnNumber;
	}

	/**
	 * Sets the column number.
	 *
	 * @param columnNumber the new column number
	 */
	public void setColumnNumber(Integer columnNumber) {
		this.columnNumber = columnNumber;
	}

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return this.columnName;
	}
}
