package org.ihtsdo.rf2.mojo;

import java.util.ArrayList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.identifier.mojo.RF2IdentifierFile;
import org.ihtsdo.rf2.util.Config;

public class ReleaseConfigMojo extends AbstractMojo {

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
	 * @parameter
	 * @required
	 */
	private String rF2Format;

	/**
	 * invokeDroolsRules
	 * 
	 * @parameter default-value="true"
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
	
	
	// for idgenerator below parameter is required
	/**
	 * releaseFolder.
	 * 
	 * @parameter
	 * 
	 */
	private String releaseFolder;

	/**
	 * Location of the destinationFolder.
	 * 
	 * @parameter
	 * 
	 */
	private String destinationFolder;
	
	
	/**
	 * RF2 file list with column sequence list
	 * 
	 * @parameter
	 * 
	 */	
	private ArrayList<RF2IdentifierFile> rf2Files;
	
	/**
	 * Workbench sctid update flag
	 * 
	 * @parameter default-value="false"
	 * 
	 */	
	private String updateWbSctId;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

	}

	public void setConfig(Config config) {

		config.setReleaseDate(releaseDate);
		config.setOutputFolderName(exportFolder);
		
		// set these for text-definiton
		config.setEndPoint(endpointURL);
		config.setUsername(username);
		config.setPassword(password);

		// defaults set in the mojo declaratons, overrriden from POM
		config.setRf2Format(rF2Format);
		config.setInvokeDroolRules(invokeDroolsRules);
		config.setIncrementalRelease(incrementalRelease);
		config.setFileExtension(fileExtension);
		config.setFlushCount(flushCount);
		
		// set these for isgeneration
		config.setReleaseFolder(releaseFolder);
		config.setDestinationFolder(destinationFolder);
		config.setUpdateWbSctId(updateWbSctId);		
		config.setRf2Files(rf2Files);		
	}

}
