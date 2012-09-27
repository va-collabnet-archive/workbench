package org.ihtsdo.rf2.file.packaging.mojo;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.rf2.file.packaging.Package;
import org.ihtsdo.rf2.file.packaging.Readme;
import org.ihtsdo.rf2.file.packaging.model.ReadmeConfig;
import org.w3c.dom.Document;

/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal documentation
 * 
 * @phase install
 */
public class RF2FileDocumentationMojo extends AbstractMojo {
	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Location of the source folder.
	 * 
	 * @parameter
	 * @required
	 */
	private String sourceFolder;

	/**
	 * Location of the filenamer.
	 * 
	 * @parameter
	 * @required
	 */
	private String fileName;

	/**
	 * FileNameWidth
	 * 
	 * @parameter
	 * @required
	 */
	private Integer fileNameWidth;

	/**
	 * headerLocation
	 * 
	 * @parameter
	 * @required
	 */
	private String headerLocation;
	
	/**
	 * runTwice
	 * 
	 * @parameter
	 * @required
	 */
	private Boolean runTwice;


	

	public void execute() throws MojoExecutionException {

		Readme r = new Readme();
		
		// load the configuration
		ReadmeConfig readmeConfig = new ReadmeConfig();
		
		// set the readme configuration object
		readmeConfig.setFileName(fileName);
		readmeConfig.setFileNameWidth(fileNameWidth);
		readmeConfig.setHeaderLocation(headerLocation);
		readmeConfig.setRunTwice(runTwice);
		readmeConfig.setSource(sourceFolder);
		
		r.process(readmeConfig);
		
	}
	
}
