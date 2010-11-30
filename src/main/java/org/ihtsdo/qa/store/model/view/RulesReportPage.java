package org.ihtsdo.qa.store.model.view;

import java.util.List;

public class RulesReportPage {
	
	private List<RulesReportLine> lines;
	List<RulesReportColumn> sortBy;
	private int initialLine;
	private int finalLine;
	private int totalLines;
	
	public RulesReportPage(List<RulesReportLine> lines,
			List<RulesReportColumn> sortBy, int initialLine, int finalLine,
			int totalLines) {
		super();
		this.lines = lines;
		this.sortBy = sortBy;
		this.initialLine = initialLine;
		this.finalLine = finalLine;
		this.totalLines = totalLines;
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

	public List<RulesReportColumn> getSortBy() {
		return sortBy;
	}

	public void setSortBy(List<RulesReportColumn> sortBy) {
		this.sortBy = sortBy;
	}

	public List<RulesReportLine> getLines() {
		return lines;
	}

	public void setLines(List<RulesReportLine> lines) {
		this.lines = lines;
	}

}
