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
	
	


	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

	}

	public void setConfig(Config config) {

		config.setReleaseDate(releaseDate);
		config.setOutputFolderName(exportFolder);
		
		// set these for tesdefiniton
		config.setEndPoint(endpointURL);
		config.setUsername(username);
		config.setPassword(password);

		// defaults set in the mojo declaratons, overrriden from POM
		config.setRf2Format(rF2Format);
		config.setInvokeDroolRules(invokeDroolsRules);
		config.setIncrementalRelease(incrementalRelease);
		config.setFileExtension(fileExtension);
		config.setFlushCount(flushCount);
	}

}
