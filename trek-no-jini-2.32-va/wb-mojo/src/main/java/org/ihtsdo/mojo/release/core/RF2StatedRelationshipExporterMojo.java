package org.ihtsdo.mojo.release.core;

import java.io.File;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.rf2.core.factory.RF2StatedRelationshipFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;

/**
 * @author Varsha Parekh
 * 
 * @goal export-stated-relationship
 * @requiresDependencyResolution compile
 */

public class RF2StatedRelationshipExporterMojo extends AbstractMojo {

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
	 * @required
	 */
	private String releaseDate;

	/**
	 * Location of the exportFoler.
	 * 
	 * @parameter
	 * @required
	 */
	private String exportFolder;
	
	/**
	 * Location of the rF2Format.
	 * 
	 * @parameter
	 * @required
	 */
	private String rF2Format;
	
	
	//Below Parameters are necessary for ID-Generation

	/**
	 * namespaceId
	 * 
	 * @parameter default-value="false"
	 * 
	 */
	private String updateWbSctId;
	
	
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
	 * @parameter default-value="Core Component"
	 * 
	 */
	private String moduleId;
	
	/**
	 * moduleId
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

	public void execute() throws MojoExecutionException, MojoFailureException {
		System.setProperty("java.awt.headless", "true");
		try {
			try {
				if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName(), this.getClass(), targetDirectory)) {
					return;
				}
			} catch (NoSuchAlgorithmException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
			
			Config config;
			
			if(rF2Format.equals("true"))
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/statedrelationship.xml");
			else
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/statedrelationshipqa.xml");
		
			
			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);
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
		
			// initialize ace framwork and meta hierarchy
			ExportUtil.init();

			RF2StatedRelationshipFactory factory = new RF2StatedRelationshipFactory(config);
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
}
