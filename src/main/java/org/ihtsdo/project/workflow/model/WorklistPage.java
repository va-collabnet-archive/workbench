package org.ihtsdo.project.workflow.model;

import java.io.Serializable;

public class WorklistPage implements Serializable{
	private Integer startLine;
	private Integer pageLenght;

	public WorklistPage() {
		super();
	}

	public WorklistPage(Integer startLine, Integer pageLenght) {
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
