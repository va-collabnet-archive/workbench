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
 * @goal rf2hierarchy-qa-review
 * @phase process-resources
 */
public final class Rf2HierarchyMojo extends AbstractMojo{

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

	/**
	 * Previous release date.
	 * 
	 * @parameter 
	 * @required
	 */
	private String prevReleaseDate;

	private static final Logger log = Logger.getLogger(Rf2HierarchyMojo.class);
	
	public void execute() throws MojoExecutionException {
		Rf2Hierarchy hier;
		try {
			hier = new Rf2Hierarchy( rf2FullFolder,  rf2OutputFolder,  releaseDate, prevReleaseDate);
		hier.execute();
		hier=null;
		System.gc();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			throw new MojoExecutionException(e.getMessage());
		}
	}

}
