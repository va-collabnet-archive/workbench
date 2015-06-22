package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;
import org.ihtsdo.rf2.workflow.factory.RF2WorkflowHistoryFactory;

/**
 * @author Varsha Parekh
 * 
 * @goal export-workflow-history
 * @requiresDependencyResolution compile
 * 
 * Modified by Alejandro Rodriguez
 * Date 20150311
 */

public class ExportWorkflowHistoryMojo extends AbstractMojo {

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
	 * @parameter default-value="LAST_WF_DATA"
	 * 
	 */
	private String componentType;
	
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {			
			Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/workflowHistory.xml");
				
			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);
			config.setReleaseDate(releaseDate);
			config.setFlushCount(10000);
			config.setFileExtension("txt");
			config.setNamespaceId(namespaceId);
			config.setPartitionId(partitionId);
			config.setExecutionId(executionId);
			config.setModuleId(moduleId);
			config.setReleaseId(releaseId);
			config.setComponentType(componentType);			
			config.setUsername(username);
			config.setPassword(password);
			config.setEndPoint(endpointURL);
			
			// initialize meta hierarchy
			ExportUtil.init();

			RF2WorkflowHistoryFactory factory = new RF2WorkflowHistoryFactory(config);
			factory.export();

		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			throw new MojoExecutionException(e.getMessage());
		}
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
