package org.ihtsdo.file.validation.model;

import java.util.ArrayList;

public class QA implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5274340327217207883L;
	private File file;
	private ArrayList<Column> columnsList;
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public File getFile() {
		return file;
	}
	public ArrayList<Column> getColumnsList() {
		return columnsList;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public void setColumnsList(ArrayList<Column> columnsList) {
		this.columnsList = columnsList;
	}
	public void init() {
		
		this.file = null;
		this.columnsList = null;
	}
	
}
