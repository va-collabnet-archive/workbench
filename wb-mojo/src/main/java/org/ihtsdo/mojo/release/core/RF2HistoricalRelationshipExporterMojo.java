package org.ihtsdo.mojo.release.core;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.core.factory.RF2HistoricalRelationshipFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;

/**
 * @author Varsha Parekh
 * 
 * @goal export-historical-relationship
 * @requiresDependencyResolution compile
 */

public class RF2HistoricalRelationshipExporterMojo extends AbstractMojo {

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
	 * previuous release date. 
	 * 
	 * @parameter
	 * @required
	 */
	private String previousReleaseDate;

	/**
	 * Location of the exportFoler.
	 * 
	 * @parameter
	 * @required
	 */
	private String exportFolder;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/historicalrelationship.xml");

			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);

			config.setReleaseDate(releaseDate);
			config.setPreviousReleaseDate(previousReleaseDate);
			
			config.setFlushCount(10000);
			config.setInvokeDroolRules("false");
			config.setFileExtension("txt");

			// initialize meta hierarchy
			ExportUtil.init();


			RF2HistoricalRelationshipFactory factory = new RF2HistoricalRelationshipFactory(config);
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
