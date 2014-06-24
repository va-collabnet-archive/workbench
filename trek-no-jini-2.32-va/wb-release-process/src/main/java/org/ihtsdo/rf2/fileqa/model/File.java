package org.ihtsdo.rf2.fileqa.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class File implements java.io.Serializable {
	private static final long serialVersionUID = 3934630179141814154L;
	private ArrayList<Regex> regex;
	private String description;
	private String delimiter;
	private String encoding;
	private String carryForward;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public ArrayList<Regex> getRegex() {
		return regex;
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

	public void setRegexList(ArrayList<Regex> regex) {
		this.regex = regex;
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
