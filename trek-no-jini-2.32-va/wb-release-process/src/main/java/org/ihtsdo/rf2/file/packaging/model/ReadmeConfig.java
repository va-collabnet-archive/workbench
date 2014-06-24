package org.ihtsdo.rf2.file.packaging.model;

public class ReadmeConfig {
	
	String source;
	String fileName;
	int fileNameWidth;
	String headerLocation;
	boolean runTwice;
	
	public ReadmeConfig() {
		super();
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getFileNameWidth() {
		return fileNameWidth;
	}

	public void setFileNameWidth(int fileNameWidth) {
		this.fileNameWidth = fileNameWidth;
	}

	public String getHeaderLocation() {
		return headerLocation;
	}

	public void setHeaderLocation(String headerLocation) {
		this.headerLocation = headerLocation;
	}

	public boolean isRunTwice() {
		return runTwice;
	}

	public void setRunTwice(boolean runTwice) {
		this.runTwice = runTwice;
	}

}
