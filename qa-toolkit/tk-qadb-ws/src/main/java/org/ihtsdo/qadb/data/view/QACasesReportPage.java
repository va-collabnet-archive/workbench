package org.ihtsdo.qadb.data.view;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class QACasesReportPage {
	
	private List<QACasesReportLine> lines;
	private LinkedHashMap<Integer,Boolean> sortBy;
	private HashMap<Integer, Object> filter;
	private int initialLine;
	private int finalLine;
	private int totalLines;
	
	public QACasesReportPage(List<QACasesReportLine> lines,
			LinkedHashMap<Integer,Boolean> sortBy,
			HashMap<Integer, Object> filter, int initialLine,
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

	public LinkedHashMap<Integer,Boolean> getSortBy() {
		return sortBy;
	}

	public void setSortBy(LinkedHashMap<Integer,Boolean> sortBy) {
		this.sortBy = sortBy;
	}

	public HashMap<Integer, Object> getFilter() {
		return filter;
	}

	public void setFilter(HashMap<Integer, Object> filter) {
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
