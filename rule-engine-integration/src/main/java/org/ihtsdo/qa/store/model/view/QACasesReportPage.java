package org.ihtsdo.qa.store.model.view;

import java.util.HashMap;
import java.util.List;

public class QACasesReportPage {
	
	private List<QACasesReportLine> lines;
	private List<QACasesReportColumn> sortBy;
	private HashMap<QACasesReportColumn, Object> filter;
	private int initialLine;
	private int finalLine;
	private int totalLines;
	
	public QACasesReportPage(List<QACasesReportLine> lines,
			List<QACasesReportColumn> sortBy,
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

	public List<QACasesReportLine> getLines() {
		return lines;
	}

	public void setLines(List<QACasesReportLine> lines) {
		this.lines = lines;
	}

	public List<QACasesReportColumn> getSortBy() {
		return sortBy;
	}

	public void setSortBy(List<QACasesReportColumn> sortBy) {
		this.sortBy = sortBy;
	}

	public HashMap<QACasesReportColumn, Object> getFilter() {
		return filter;
	}

	public void setFilter(HashMap<QACasesReportColumn, Object> filter) {
		this.filter = filter;
	}

	public int getInitialLine() {
		return initialLine;
	}

	public void setInitialLine(int initialLine) {
		this.initialLine = initialLine;
	}

	public int getFinalLine() {
		return finalLine;
	}

	public void setFinalLine(int finalLine) {
		this.finalLine = finalLine;
	}

	public int getTotalLines() {
		return totalLines;
	}

	public void setTotalLines(int totalLines) {
		this.totalLines = totalLines;
	}
}
