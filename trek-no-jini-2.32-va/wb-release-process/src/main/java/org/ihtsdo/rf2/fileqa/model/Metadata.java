package org.ihtsdo.rf2.fileqa.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Metadata implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5274340327217207883L;
	private File file;
	private ArrayList<Column> column;
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public File getFile() {
		return file;
	}
	public ArrayList<Column> getColumn() {
		return column;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public void setColumn(ArrayList<Column> column) {
		this.column = column;
	}
	public void init() {
		
		this.file = null;
		this.column = null;
	}
	
}
