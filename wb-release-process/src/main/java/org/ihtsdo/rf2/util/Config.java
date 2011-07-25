package org.ihtsdo.rf2.util;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {

	// from jsxb marshalling
	private String exportFileName;
	private String droolsDrlFile;
	private ArrayList<Column> column;

	// internal
	@XmlTransient
	private String database;
	@XmlTransient
	private String description;
	@XmlTransient
	private String releaseDate;
	@XmlTransient
	private String fileExtension;
	@XmlTransient
	private String outputFolderName;
	@XmlTransient
	private String rf2Format;
	@XmlTransient
	private String incrementalRelease;
	@XmlTransient
	private String invokeDroolRules;
	@XmlTransient
	private Date fromReleaseDate;
	@XmlTransient
	private Date toReleaseDate;
	
	// for accessing the web service
	@XmlTransient
	private String endPoint;
	@XmlTransient
	private String username;
	@XmlTransient
	private String password;
	

	@XmlTransient
	private BufferedWriter bw;

	@XmlTransient
	private int flushCount;

	public String getExportFileName() {
		return exportFileName;
	}

	public void setExportFileName(String exportFileName) {
		this.exportFileName = exportFileName;
	}

	public String getDroolsDrlFile() {
		return droolsDrlFile;
	}

	public void setDroolsDrlFile(String droolsDrlFile) {
		this.droolsDrlFile = droolsDrlFile;
	}

	public ArrayList<Column> getColumn() {
		return column;
	}

	public void setColumn(ArrayList<Column> column) {
		this.column = column;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public String getOutputFolderName() {
		return outputFolderName;
	}

	public void setOutputFolderName(String outputFolderName) {
		this.outputFolderName = outputFolderName;
	}

	public String getRf2Format() {
		return rf2Format;
	}

	public void setRf2Format(String rf2Format) {
		this.rf2Format = rf2Format;
	}

	public String getIncrementalRelease() {
		return incrementalRelease;
	}

	public void setIncrementalRelease(String incrementalRelease) {
		this.incrementalRelease = incrementalRelease;
	}

	public String getInvokeDroolRules() {
		return invokeDroolRules;
	}

	public void setInvokeDroolRules(String invokeDroolRules) {
		this.invokeDroolRules = invokeDroolRules;
	}

	public Date getFromReleaseDate() {
		return fromReleaseDate;
	}

	public void setFromReleaseDate(Date fromReleaseDate) {
		this.fromReleaseDate = fromReleaseDate;
	}

	public Date getToReleaseDate() {
		return toReleaseDate;
	}

	public void setToReleaseDate(Date toReleaseDate) {
		this.toReleaseDate = toReleaseDate;
	}

	public BufferedWriter getBw() {
		return bw;
	}

	public void setBw(BufferedWriter bw) {
		this.bw = bw;
	}

	public int getFlushCount() {
		return flushCount;
	}

	public void setFlushCount(int flushCount) {
		this.flushCount = flushCount;
	}

	public void setDefaults() {

		this.setRf2Format("true");
		this.setInvokeDroolRules("false");
		this.setIncrementalRelease("false");
		this.setFileExtension("txt");
		this.setFlushCount(100000);

	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}