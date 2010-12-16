package org.ihtsdo.qa.store.model.view;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class QACasesReportPage {
	
	private List<QACasesReportLine> lines;
	private LinkedHashMap<QACasesReportColumn, Boolean> sortBy;
	private HashMap<QACasesReportColumn, Object> filter;
	private int initialLine;
	private int finalLine;
	private int totalLines;
	
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

	public QACasesReportPage() {
		super();
	}

	public List<QACasesReportLine> getLines() {
		return lines;
	}

	public void setLines(List<QACasesReportLine> lines) {
		this.lines = lines;
	}

	public LinkedHashMap<QACasesReportColumn, Boolean> getSortBy() {
		return sortBy;
	}

	public void setSortBy(LinkedHashMap<QACasesReportColumn, Boolean> sortBy) {
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
