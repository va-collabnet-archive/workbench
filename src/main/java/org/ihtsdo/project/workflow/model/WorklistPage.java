package org.ihtsdo.project.workflow.model;

public class WorklistPage {
	private Integer startLine;
	private Integer pageLenght;

	private WorklistPage(Integer startLine, Integer pageLenght) {
		super();
		this.startLine = startLine;
		this.pageLenght = pageLenght;
	}

	public Integer getStartLine() {
		return startLine;
	}

	public void setStartLine(Integer startLine) {
		this.startLine = startLine;
	}

	public Integer getPageLenght() {
		return pageLenght;
	}

	public void setPageLenght(Integer pageLenght) {
		this.pageLenght = pageLenght;
	}

}
