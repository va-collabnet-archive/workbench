package org.ihtsdo.qa.store.model.view;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class RulesReportPage {
	
	private List<RulesReportLine> lines;
	private LinkedHashMap<RulesReportColumn,Boolean> sortBy;
	private HashMap<RulesReportColumn, Object> filter;
	private int initialLine;
	private int finalLine;
	private int totalLines;
	
	public RulesReportPage(){
		super();
	}
	
	public RulesReportPage(List<RulesReportLine> lines,
			LinkedHashMap<RulesReportColumn,Boolean> sortBy,
			HashMap<RulesReportColumn, Object> filter, int initialLine,
			int finalLine, int totalLines) {
		super();
		this.lines = lines;
		this.sortBy = sortBy;
		this.filter = filter;
		this.initialLine = initialLine;
		this.finalLine = finalLine;
		this.totalLines = totalLines;
	}

	public List<RulesReportLine> getLines() {
		return lines;
	}

	public void setLines(List<RulesReportLine> lines) {
		this.lines = lines;
	}

	public LinkedHashMap<RulesReportColumn,Boolean> getSortBy() {
		return sortBy;
	}

	public void setSortBy(LinkedHashMap<RulesReportColumn,Boolean> sortBy) {
		this.sortBy = sortBy;
	}

	public HashMap<RulesReportColumn, Object> getFilter() {
		return filter;
	}

	public void setFilter(HashMap<RulesReportColumn, Object> filter) {
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
