package org.ihtsdo.mojo.qa.rf2file;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * The mojo creates txt files for upload to review site
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Alejandro Rodriguez
 * @goal rf2descriptions-qa-review
 * @phase process-resources
 */
public final class Rf2DescriptionsMojo extends AbstractMojo{

	/**
	 * Location of the RF2 Full directory.
	 * 
	 * @parameter 
	 * @required
	 */
	private String rf2FullFolder;
	
	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File rf2OutputFolder;

	/**
	 * Release date.
	 * 
	 * @parameter 
	 * @required
	 */
	private String releaseDate;

	private static final Logger log = Logger.getLogger(Rf2DescriptionsMojo.class);
	
	public void execute() throws MojoExecutionException {
		Rf2Descriptions desc;
		try {
			desc = new Rf2Descriptions( rf2FullFolder,  rf2OutputFolder,  releaseDate);
		desc.execute();
		desc=null;
		System.gc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			throw new MojoExecutionException(e.getMessage());
		}
	}

}
