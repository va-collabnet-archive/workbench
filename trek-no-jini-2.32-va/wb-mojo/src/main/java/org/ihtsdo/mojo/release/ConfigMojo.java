package org.ihtsdo.mojo.release;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.util.Config;

public class ConfigMojo extends AbstractMojo {

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
	 * rF2Format
	 * 
	 * @parameter default-value="false"
	 * @required
	 */
	private String rF2Format;

	/**
	 * invokeDroolsRules
	 * 
	 * @parameter default-value="false"
	 */
	private String invokeDroolsRules;

	/**
	 * incrementalRelease
	 * 
	 * @parameter default-value="false"
	 */
	private String incrementalRelease;

	/**
	 * fileExtension
	 * 
	 * @parameter default-value="txt"
	 */
	private String fileExtension;
	
	

	/**
	 * flushCount
	 * 
	 * @parameter default-value="10000"
	 */
	private int flushCount;
	
	// for text definiton
	/**
	 * endpointURL
	 * 
	 * @parameter
	 */
	private String endpointURL;
	
	/**
	 * username
	 * 
	 * @parameter
	 */
	private String username;
	
	/**
	 * password
	 * 
	 * @parameter
	 */
	private String password;
	
	
	//Below Parameters are necessary for ID-Generation
	
	
	

	/**
	 * updateWbSctId
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
	
	
	

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

	}

	public void setConfig(Config config) {

		config.setReleaseDate(releaseDate);
		config.setOutputFolderName(exportFolder);
		
		// set below properties for text-definiton
		config.setEndPoint(endpointURL);
		config.setUsername(username);
		config.setPassword(password);

		// defaults set in the mojo declarations, overrriden from POM
		config.setRf2Format(rF2Format);
		config.setInvokeDroolRules(invokeDroolsRules);
		config.setIncrementalRelease(incrementalRelease);
		config.setFileExtension(fileExtension);
		config.setFlushCount(flushCount);
		
		//Below Parameters are necessary for ID-Generation
		config.setUpdateWbSctId(updateWbSctId);
		config.setNamespaceId(namespaceId);
		config.setPartitionId(partitionId);
		config.setExecutionId(executionId);
		config.setModuleId(moduleId);
		config.setReleaseId(releaseId);
		config.setComponentType(componentType);
		
		
		//Below Parameters are required for ID-Insertion
		config.setChangesetUserName(changesetUserName);
		config.setChangesetUserConcept(changesetUserConcept);
		config.setChangesetRoot(changesetRoot);
		
	}

}
