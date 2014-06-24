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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Class QACasesReportPage.
 */
public class QACasesReportPage {
	
	/** The lines. */
	private List<QACasesReportLine> lines;
	
	/** The sort by. */
	private LinkedHashMap<QACasesReportColumn, Boolean> sortBy;
	
	/** The filter. */
	private HashMap<QACasesReportColumn, Object> filter;
	
	/** The initial line. */
	private int initialLine;
	
	/** The final line. */
	private int finalLine;
	
	/** The total lines. */
	private int totalLines;
	
	/**
	 * Instantiates a new qA cases report page.
	 *
	 * @param lines the lines
	 * @param sortBy the sort by
	 * @param filter the filter
	 * @param initialLine the initial line
	 * @param finalLine the final line
	 * @param totalLines the total lines
	 */
	public QACasesReportPage(List<QACasesReportLine> lines,
			LinkedHashMap<QACasesReportColumn, Boolean> sortBy,
			HashMap<QACasesReportColumn, Object> filter, int initialLine,
			int finalLine, int totalLines) {
		super();
		this.lines = lines;
		this.sortBy = sortBy;
		this.filter = filter;
		this.initialLine = initialLine;
		this.finalLine = finalLine;
		this.totalLines = totalLines;
	}

	/**
	 * Instantiates a new qA cases report page.
	 */
	public QACasesReportPage() {
		super();
	}

	/**
	 * Gets the lines.
	 *
	 * @return the lines
	 */
	public List<QACasesReportLine> getLines() {
		return lines;
	}

	/**
	 * Sets the lines.
	 *
	 * @param lines the new lines
	 */
	public void setLines(List<QACasesReportLine> lines) {
		this.lines = lines;
	}

	/**
	 * Gets the sort by.
	 *
	 * @return the sort by
	 */
	public LinkedHashMap<QACasesReportColumn, Boolean> getSortBy() {
		return sortBy;
	}

	/**
	 * Sets the sort by.
	 *
	 * @param sortBy the sort by
	 */
	public void setSortBy(LinkedHashMap<QACasesReportColumn, Boolean> sortBy) {
		this.sortBy = sortBy;
	}

	/**
	 * Gets the filter.
	 *
	 * @return the filter
	 */
	public HashMap<QACasesReportColumn, Object> getFilter() {
		return filter;
	}

	/**
	 * Sets the filter.
	 *
	 * @param filter the filter
	 */
	public void setFilter(HashMap<QACasesReportColumn, Object> filter) {
		this.filter = filter;
	}

	/**
	 * Gets the initial line.
	 *
	 * @return the initial line
	 */
	public int getInitialLine() {
		return initialLine;
	}

	/**
	 * Sets the initial line.
	 *
	 * @param initialLine the new initial line
	 */
	public void setInitialLine(int initialLine) {
		this.initialLine = initialLine;
	}

	/**
	 * Gets the final line.
	 *
	 * @return the final line
	 */
	public int getFinalLine() {
		return finalLine;
	}

	/**
	 * Sets the final line.
	 *
	 * @param finalLine the new final line
	 */
	public void setFinalLine(int finalLine) {
		this.finalLine = finalLine;
	}

	/**
	 * Gets the total lines.
	 *
	 * @return the total lines
	 */
	public int getTotalLines() {
		return totalLines;
	}

	/**
	 * Sets the total lines.
	 *
	 * @param totalLines the new total lines
	 */
	public void setTotalLines(int totalLines) {
		this.totalLines = totalLines;
	}
}
