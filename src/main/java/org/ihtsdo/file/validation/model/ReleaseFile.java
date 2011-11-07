package org.ihtsdo.file.validation.model;

import java.util.ArrayList;
import java.util.HashMap;

public class ReleaseFile implements java.io.Serializable {
	private static final long serialVersionUID = 3934630179141814154L;
	private ArrayList<Regex> regexList;
	private String description;
	private String delimiter;
	private String encoding;
	private String carryForward;
	
	private HashMap<Integer,Column> columns;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public ArrayList<Regex> getRegexList() {
		return regexList;
	}

	public String getDescription() {
		return description;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getCarryForward() {
		return carryForward;
	}

	public void setRegexList(ArrayList<Regex> regexList) {
		this.regexList = regexList;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setCarryForward(String carryForward) {
		this.carryForward = carryForward;
	}
}
