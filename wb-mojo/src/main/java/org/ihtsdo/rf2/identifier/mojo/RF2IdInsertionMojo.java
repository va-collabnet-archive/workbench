package org.ihtsdo.rf2.identifier.mojo;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.core.factory.RF2ConceptFactory;
import org.ihtsdo.rf2.identifier.factory.RF2IdInsertionFactory;
import org.ihtsdo.rf2.qa.factory.RF2QAFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;

/**
 * @author Varsha Parekh
 * 
 * @goal identifier-insertion
 * @requiresDependencyResolution compile
 */

public class RF2IdInsertionMojo extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;
	
	/**
	 * release date.
	 * 
	 * @parameter
	 *
	 */
	private String releaseDate;

	/**
	 * Location of the exportFoler.
	 * 
	 * @parameter
	 * 
	 */
	private String exportFolder;
	
	/**
	 * Location of the rF2Format.
	 * 
	 * @parameter
	 * 
	 */
	private String rF2Format;
	
	
	
	
	//Below Parameters are necessary for ID-Generation

	/**
	 * updateWbSctId
	 * 
	 * @parameter default-value="false"
	 * 
	 */
	private String updateWbSctId;
	
	
	// for accessing the web service
	/**
	 * endpointURL
	 * 
	 * @parameter
	 * 
	 */
	private String endpointURL;
	
	/**
	 * username
	 * 
	 * @parameter
	 * 
	 */
	private String username;
	
	/**
	 * password
	 * 
	 * @parameter
	 * 
	 */
	private String password;
	
	
	/**
	 * namespaceId
	 * 
	 * @parameter default-value="0"
	 * 
	 */
	private String namespaceId;
	
	/**
	 * partitionId
	 * 
	 * @parameter default-value="00"
	 * 
	 */
	private String partitionId;
	
	/**
	 * executionId
	 * 
	 * @parameter default-value="Daily-build"
	 * 
	 */
	private String executionId;
	
	/**
	 * moduleId
	 * 
	 * @parameter default-value="Core Concept Component"
	 * 
	 */
	private String moduleId;
	
	/**
	 * releaseId
	 * 
	 * @parameter default-value="20110131"
	 * 
	 */
	private String releaseId;
	
	/**
	 * componentType
	 * 
	 * @parameter default-value="Concept"
	 * 
	 */
	private String componentType;
	
	
	
	/**
	 * changesetUserName
	 * 
	 * @parameter default-value="testvp"
	 * 
	 */
	private String changesetUserName;
	
	
	/**
	 * changesetUserConcept
	 * 
	 * @parameter default-value="f7495b58-6630-3499-a44e-2052b5fcf06c"
	 * 
	 */
	private String changesetUserConcept;
	
	
	/**
	 * changesetRoot
	 * 
	 * @parameter default-value="E:/Workbench_Bundle/Prod/SyncPRODNov06/profiles/testvp"
	 * 
	 */
	private String changesetRoot;
	

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			Config config;
			
			if(rF2Format.equals("true"))
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/snapshotqa.xml");
			else
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/snapshotqa.xml");
				
			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);
			
//			DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
//			Date time = df.parse(releaseDate);
//			DateFormat releaseFormat = new SimpleDateFormat("yyyyMMdd");
//			String releaseDateString = releaseFormat.format(time);
			config.setReleaseDate(releaseDate);
			config.setRf2Format(rF2Format);
			config.setFlushCount(10000);
			config.setInvokeDroolRules("false");
			config.setFileExtension("txt");
			
			//Below Parameters are necessary for ID-Generation
			config.setUpdateWbSctId(updateWbSctId);
			config.setNamespaceId(namespaceId);
			config.setPartitionId(partitionId);
			config.setExecutionId(executionId);
			config.setModuleId(moduleId);
			config.setReleaseId(releaseId);
			config.setComponentType(componentType);			
			config.setUsername(username);
			config.setPassword(password);
			config.setEndPoint(endpointURL);
			
			//Below Parameters are required for ID-Insertion
			config.setChangesetUserName(changesetUserName);
			config.setChangesetUserConcept(changesetUserConcept);
			config.setChangesetRoot(changesetRoot);
			
			
			// initialize meta hierarchy
			ExportUtil.init();

			RF2IdInsertionFactory factory = new RF2IdInsertionFactory(config);
			factory.export();

		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			throw new MojoExecutionException(e.getMessage());
		}
	}

	public String getrF2Format() {
		return rF2Format;
	}

	public void setrF2Format(String rF2Format) {
		this.rF2Format = rF2Format;
	}

	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getExportFolder() {
		return exportFolder;
	}

	public void setExportFolder(String exportFolder) {
		this.exportFolder = exportFolder;
	}
}
