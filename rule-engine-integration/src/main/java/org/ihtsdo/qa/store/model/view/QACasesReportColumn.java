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
 * The Enum QACasesReportColumn.
 */
public enum QACasesReportColumn {
	
	/** The CONCEP t_ name. */
	CONCEPT_NAME(1), 
 /** The STATUS. */
 STATUS(2), 
 /** The DISPOSITION. */
 DISPOSITION(3), 
 /** The ASSIGNE d_ to. */
 ASSIGNED_TO(4), 
 /** The TIME. */
 TIME(5);
	
	/** The column number. */
	private Integer columnNumber;

	/**
	 * Instantiates a new qA cases report column.
	 *
	 * @param columnNumber the column number
	 */
	QACasesReportColumn(Integer columnNumber) {
		this.columnNumber = columnNumber;
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
}
