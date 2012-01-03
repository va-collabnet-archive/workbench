package org.ihtsdo.rf2.fileqa.model;

public class TestError {

	private String test;
	private String columnHeader;
	private String regex;
	private String message;
	private long lineCount;
	private String columnData;
	private int count = 0;
	

	public String getTest() {
		return test;
	}

	public String getColumnHeader() {
		return columnHeader;
	}

	public String getRegex() {
		return regex;
	}

	public String getMessage() {
		return message;
	}

	public long getLineCount() {
		return lineCount;
	}

	public String getColumnData() {
		return columnData;
	}

	public void setTest(String test) {
		this.test = test;
	}

	public void setColumnHeader(String columnHeader) {
		this.columnHeader = columnHeader;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setLineCount(long lineCount) {
		this.lineCount = lineCount;
	}

	public void setColumnData(String columnData) {
		this.columnData = columnData;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
