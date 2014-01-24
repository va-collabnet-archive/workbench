package org.ihtsdo.rf2.module.util;

import java.io.BufferedWriter;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.ihtsdo.rf2.identifier.mojo.RF2IdentifierFile;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {

	// from jsxb marshalling
	private String exportFileName;
	private ArrayList<Column> column;

	// internal
	@XmlTransient
	private String releaseDate;
	@XmlTransient
	private String previousReleaseDate;
	@XmlTransient
	private String outputFolderName;
	@XmlTransient
	private String rf2Format;
	// for accessing the web service
	@XmlTransient
	private String endPoint;
	@XmlTransient
	private String username;
	@XmlTransient
	private String password;
	private String releaseFolder;
	private String destinationFolder;
	private String previousIdNotReleasedFile;
	private ArrayList<RF2IdentifierFile> rf2Files;
	
	//Below Parameters are necessary for ID-Generation
	private String namespaceId;
	private String partitionId;
	private String executionId;
	private String releaseId;
	private String componentType;
	
	private TestFilters testFilters;
	
	@XmlTransient
	private BufferedWriter bw;

	@XmlTransient
	private int flushCount;
	private String refsetUuid;
	private String refsetSCTId;
	private ArrayList<RefSetParam> refsetData;
	private String fileExtension;
	private String languageCode;
	private String defaultModule;

	public String getExportFileName() {
		return exportFileName;
	}

	public void setExportFileName(String exportFileName) {
		this.exportFileName = exportFileName;
	}

	public ArrayList<Column> getColumn() {
		return column;
	}

	public void setColumn(ArrayList<Column> column) {
		this.column = column;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getPreviousReleaseDate() {
		return previousReleaseDate;
	}

	public void setPreviousReleaseDate(String previousReleaseDate) {
		this.previousReleaseDate = previousReleaseDate;
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
		this.setRf2Format("false");
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
	
	public String getReleaseFolder() {
		return releaseFolder;
	}

	public void setReleaseFolder(String releaseFolder) {
		this.releaseFolder = releaseFolder;
	}

	public String getDestinationFolder() {
		return destinationFolder;
	}

	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	public ArrayList<RF2IdentifierFile> getRf2Files() {
		return rf2Files;
	}

	public void setRf2Files(ArrayList<RF2IdentifierFile> rf2Files) {
		this.rf2Files = rf2Files;
	}
	
	public String getNamespaceId() {
		return namespaceId;
	}

	public void setNamespaceId(String namespaceId) {
		this.namespaceId = namespaceId;
	}

	public String getPartitionId() {
		return partitionId;
	}

	public void setPartitionId(String partitionId) {
		this.partitionId = partitionId;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getReleaseId() {
		return releaseId;
	}

	public void setReleaseId(String releaseId) {
		this.releaseId = releaseId;
	}

	public String getComponentType() {
		return componentType;
	}

	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

	public String getPreviousIdNotReleasedFile() {
		return previousIdNotReleasedFile;
	}

	public void setPreviousIdNotReleasedFile(String previousIdNotReleasedFile) {
		this.previousIdNotReleasedFile = previousIdNotReleasedFile;
	}

	public TestFilters getTestFilters() {
		return testFilters;
	}

	public void setTestFilters(TestFilters testFilters) {
		this.testFilters = testFilters;
	}

	public String getRefsetUuid() {
		return refsetUuid;
	}

	public void setRefsetUuid(String refsetUuid) {
		this.refsetUuid = refsetUuid;
	}

	public String getRefsetSCTID() {
		return refsetSCTId;
	}

	public void setRefsetSCTID(String refsetSCTId) {
		this.refsetSCTId = refsetSCTId;
	}

	public void setRefsetData(ArrayList<RefSetParam> refsetData) {
		this.refsetData=refsetData;
		
	}

	public ArrayList<RefSetParam> getRefsetData() {
		return refsetData;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode=languageCode;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public String getDefaultModule() {
		return defaultModule;
	}

	public void setDefaultModule(String defaultModule) {
		this.defaultModule = defaultModule;
	}
	
	
}