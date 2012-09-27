package org.ihtsdo.rf2.fileqa.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Column implements Comparable<Column>, java.io.Serializable {
	private static final long serialVersionUID = -426419520158608353L;
	private String header;
	private int position;
	private ArrayList<Regex> regex;

	public int compareTo(Column o) {
		int lastCmp = 0;

		if (position < o.position)
			lastCmp = -1;
		else if (position > o.position)
			lastCmp = position;
		return lastCmp;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getHeader() {
		return header;
	}

	public int getPosition() {
		return position;
	}

	public ArrayList<Regex> getRegex() {
		return regex;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setRegexList(ArrayList<Regex> regex) {
		this.regex = regex;
	}
}
