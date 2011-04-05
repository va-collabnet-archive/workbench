package org.ihtsdo.mojo.release.core;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.core.factory.RF2ConceptFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;

/**
 * @author Alo
 * 
 * @goal post-process-concept
 * @requiresDependencyResolution compile
 */

public class RF2ConceptPostMojo extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;
	
	/**
	 * release date. 20100731
	 * 
	 * @parameter
	 * @required
	 */
	private String releaseDate;
	
	/**
	 * previuous release date. 20100731
	 * 
	 * @parameter
	 * @required
	 */
	private String previousReleaseDate;

	/**
	 * Location of the exportFoler. (input in this mojo)
	 * 
	 * @parameter
	 * @required
	 */
	private String exportFolder;
	
	/**
	 * Location of the rf2 full. (input in this mojo)
	 * 
	 * @parameter
	 * @required
	 */
	private String rf2FullFolder;
	
	/**
	 * Location of the outputFolder. (output in this mojo)
	 * 
	 * @parameter
	 * @required
	 */
	private String outputFolder;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/concept.xml");

			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);
			config.setFileExtension("txt");
			File conceptsFileName = new File(targetDirectory, 
					config.getExportFileName() + releaseDate + "." + config.getFileExtension());
			
			

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
