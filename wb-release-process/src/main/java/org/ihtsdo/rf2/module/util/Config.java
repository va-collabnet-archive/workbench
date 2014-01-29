package org.ihtsdo.rf2.module.util;

import java.io.BufferedWriter;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.ihtsdo.rf2.identifier.mojo.RF2IdentifierFile;

// TODO: Auto-generated Javadoc
/**
 * The Class Config.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {

	// from jsxb marshalling
	/** The export file name. */
	private String exportFileName;
	
	/** The column. */
	private ArrayList<Column> column;

	// internal
	/** The release date. */
	@XmlTransient
	private String releaseDate;
	
	/** The previous release date. */
	@XmlTransient
	private String previousReleaseDate;
	
	/** The output folder name. */
	@XmlTransient
	private String outputFolderName;
	
	/** The rf2 format. */
	@XmlTransient
	private String rf2Format;
	// for accessing the web service
	/** The end point. */
	@XmlTransient
	private String endPoint;
	
	/** The username. */
	@XmlTransient
	private String username;
	
	/** The password. */
	@XmlTransient
	private String password;
	
	/** The release folder. */
	private String releaseFolder;
	
	/** The destination folder. */
	private String destinationFolder;
	
	/** The previous id not released file. */
	private String previousIdNotReleasedFile;
	
	/** The rf2 files. */
	private ArrayList<RF2IdentifierFile> rf2Files;
	
	//Below Parameters are necessary for ID-Generation
	/** The namespace id. */
	private String namespaceId;
	
	/** The partition id. */
	private String partitionId;
	
	/** The execution id. */
	private String executionId;
	
	/** The release id. */
	private String releaseId;
	
	/** The component type. */
	private String componentType;
	
	/** The test filters. */
	private TestFilters testFilters;
	
	/** The bw. */
	@XmlTransient
	private BufferedWriter bw;

	/** The flush count. */
	@XmlTransient
	private int flushCount;
	
	/** The refset uuid. */
	private String refsetUuid;
	
	/** The refset sct id. */
	private String refsetSCTId;
	
	/** The refset data. */
	private ArrayList<RefSetParam> refsetData;
	
	/** The file extension. */
	private String fileExtension;
	
	/** The language code. */
	private String languageCode;
	
	/** The default module. */
	private String defaultModule;

	/**
	 * Gets the export file name.
	 *
	 * @return the export file name
	 */
	public String getExportFileName() {
		return exportFileName;
	}

	/**
	 * Sets the export file name.
	 *
	 * @param exportFileName the new export file name
	 */
	public void setExportFileName(String exportFileName) {
		this.exportFileName = exportFileName;
	}

	/**
	 * Gets the column.
	 *
	 * @return the column
	 */
	public ArrayList<Column> getColumn() {
		return column;
	}

	/**
	 * Sets the column.
	 *
	 * @param column the new column
	 */
	public void setColumn(ArrayList<Column> column) {
		this.column = column;
	}

	/**
	 * Gets the release date.
	 *
	 * @return the release date
	 */
	public String getReleaseDate() {
		return releaseDate;
	}

	/**
	 * Sets the release date.
	 *
	 * @param releaseDate the new release date
	 */
	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	/**
	 * Gets the previous release date.
	 *
	 * @return the previous release date
	 */
	public String getPreviousReleaseDate() {
		return previousReleaseDate;
	}

	/**
	 * Sets the previous release date.
	 *
	 * @param previousReleaseDate the new previous release date
	 */
	public void setPreviousReleaseDate(String previousReleaseDate) {
		this.previousReleaseDate = previousReleaseDate;
	}
	
	/**
	 * Gets the file extension.
	 *
	 * @return the file extension
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * Sets the file extension.
	 *
	 * @param fileExtension the new file extension
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	/**
	 * Gets the output folder name.
	 *
	 * @return the output folder name
	 */
	public String getOutputFolderName() {
		return outputFolderName;
	}

	/**
	 * Sets the output folder name.
	 *
	 * @param outputFolderName the new output folder name
	 */
	public void setOutputFolderName(String outputFolderName) {
		this.outputFolderName = outputFolderName;
	}

	/**
	 * Gets the rf2 format.
	 *
	 * @return the rf2 format
	 */
	public String getRf2Format() {
		return rf2Format;
	}

	/**
	 * Sets the rf2 format.
	 *
	 * @param rf2Format the new rf2 format
	 */
	public void setRf2Format(String rf2Format) {
		this.rf2Format = rf2Format;
	}

	/**
	 * Gets the bw.
	 *
	 * @return the bw
	 */
	public BufferedWriter getBw() {
		return bw;
	}

	/**
	 * Sets the bw.
	 *
	 * @param bw the new bw
	 */
	public void setBw(BufferedWriter bw) {
		this.bw = bw;
	}

	/**
	 * Gets the flush count.
	 *
	 * @return the flush count
	 */
	public int getFlushCount() {
		return flushCount;
	}

	/**
	 * Sets the flush count.
	 *
	 * @param flushCount the new flush count
	 */
	public void setFlushCount(int flushCount) {
		this.flushCount = flushCount;
	}

	/**
	 * Sets the defaults.
	 */
	public void setDefaults() {
		this.setRf2Format("false");
		this.setFileExtension("txt");
		this.setFlushCount(100000);
	}

	/**
	 * Gets the end point.
	 *
	 * @return the end point
	 */
	public String getEndPoint() {
		return endPoint;
	}

	/**
	 * Sets the end point.
	 *
	 * @param endPoint the new end point
	 */
	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 *
	 * @param username the new username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 *
	 * @param password the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Gets the release folder.
	 *
	 * @return the release folder
	 */
	public String getReleaseFolder() {
		return releaseFolder;
	}

	/**
	 * Sets the release folder.
	 *
	 * @param releaseFolder the new release folder
	 */
	public void setReleaseFolder(String releaseFolder) {
		this.releaseFolder = releaseFolder;
	}

	/**
	 * Gets the destination folder.
	 *
	 * @return the destination folder
	 */
	public String getDestinationFolder() {
		return destinationFolder;
	}

	/**
	 * Sets the destination folder.
	 *
	 * @param destinationFolder the new destination folder
	 */
	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	/**
	 * Gets the rf2 files.
	 *
	 * @return the rf2 files
	 */
	public ArrayList<RF2IdentifierFile> getRf2Files() {
		return rf2Files;
	}

	/**
	 * Sets the rf2 files.
	 *
	 * @param rf2Files the new rf2 files
	 */
	public void setRf2Files(ArrayList<RF2IdentifierFile> rf2Files) {
		this.rf2Files = rf2Files;
	}
	
	/**
	 * Gets the namespace id.
	 *
	 * @return the namespace id
	 */
	public String getNamespaceId() {
		return namespaceId;
	}

	/**
	 * Sets the namespace id.
	 *
	 * @param namespaceId the new namespace id
	 */
	public void setNamespaceId(String namespaceId) {
		this.namespaceId = namespaceId;
	}

	/**
	 * Gets the partition id.
	 *
	 * @return the partition id
	 */
	public String getPartitionId() {
		return partitionId;
	}

	/**
	 * Sets the partition id.
	 *
	 * @param partitionId the new partition id
	 */
	public void setPartitionId(String partitionId) {
		this.partitionId = partitionId;
	}

	/**
	 * Gets the execution id.
	 *
	 * @return the execution id
	 */
	public String getExecutionId() {
		return executionId;
	}

	/**
	 * Sets the execution id.
	 *
	 * @param executionId the new execution id
	 */
	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	/**
	 * Gets the release id.
	 *
	 * @return the release id
	 */
	public String getReleaseId() {
		return releaseId;
	}

	/**
	 * Sets the release id.
	 *
	 * @param releaseId the new release id
	 */
	public void setReleaseId(String releaseId) {
		this.releaseId = releaseId;
	}

	/**
	 * Gets the component type.
	 *
	 * @return the component type
	 */
	public String getComponentType() {
		return componentType;
	}

	/**
	 * Sets the component type.
	 *
	 * @param componentType the new component type
	 */
	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

	/**
	 * Gets the previous id not released file.
	 *
	 * @return the previous id not released file
	 */
	public String getPreviousIdNotReleasedFile() {
		return previousIdNotReleasedFile;
	}

	/**
	 * Sets the previous id not released file.
	 *
	 * @param previousIdNotReleasedFile the new previous id not released file
	 */
	public void setPreviousIdNotReleasedFile(String previousIdNotReleasedFile) {
		this.previousIdNotReleasedFile = previousIdNotReleasedFile;
	}

	/**
	 * Gets the test filters.
	 *
	 * @return the test filters
	 */
	public TestFilters getTestFilters() {
		return testFilters;
	}

	/**
	 * Sets the test filters.
	 *
	 * @param testFilters the new test filters
	 */
	public void setTestFilters(TestFilters testFilters) {
		this.testFilters = testFilters;
	}

	/**
	 * Gets the refset uuid.
	 *
	 * @return the refset uuid
	 */
	public String getRefsetUuid() {
		return refsetUuid;
	}

	/**
	 * Sets the refset uuid.
	 *
	 * @param refsetUuid the new refset uuid
	 */
	public void setRefsetUuid(String refsetUuid) {
		this.refsetUuid = refsetUuid;
	}

	/**
	 * Gets the refset sctid.
	 *
	 * @return the refset sctid
	 */
	public String getRefsetSCTID() {
		return refsetSCTId;
	}

	/**
	 * Sets the refset sctid.
	 *
	 * @param refsetSCTId the new refset sctid
	 */
	public void setRefsetSCTID(String refsetSCTId) {
		this.refsetSCTId = refsetSCTId;
	}

	/**
	 * Sets the refset data.
	 *
	 * @param refsetData the new refset data
	 */
	public void setRefsetData(ArrayList<RefSetParam> refsetData) {
		this.refsetData=refsetData;
		
	}

	/**
	 * Gets the refset data.
	 *
	 * @return the refset data
	 */
	public ArrayList<RefSetParam> getRefsetData() {
		return refsetData;
	}

	/**
	 * Sets the language code.
	 *
	 * @param languageCode the new language code
	 */
	public void setLanguageCode(String languageCode) {
		this.languageCode=languageCode;
	}

	/**
	 * Gets the language code.
	 *
	 * @return the language code
	 */
	public String getLanguageCode() {
		return languageCode;
	}

	/**
	 * Gets the default module.
	 *
	 * @return the default module
	 */
	public String getDefaultModule() {
		return defaultModule;
	}

	/**
	 * Sets the default module.
	 *
	 * @param defaultModule the new default module
	 */
	public void setDefaultModule(String defaultModule) {
		this.defaultModule = defaultModule;
	}
	
	
}