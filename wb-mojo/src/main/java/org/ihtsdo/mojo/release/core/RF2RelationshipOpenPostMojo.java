package org.ihtsdo.mojo.release.core;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst.FILE_TYPE;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.JAXBUtil;

/**
 * @author Alejandro Rodriguez
 * 
 * @goal post-process-relationship-open
 * @requiresDependencyResolution compile
 */

public class RF2RelationshipOpenPostMojo extends AbstractMojo {

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
	
	/**
	 * Location of the rF2Format.
	 * 
	 * @parameter
	 * @required
	 */
	private String rF2Format;

	/**
	 * Namespace for file names of release. 
	 * 
	 * @parameter
	 * @required
	 */
	private String namespace;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			Config config;
			
			if(rF2Format.equals("true"))
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/relationship.xml");
			else
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/relationshipqa.xml");
			
			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);
			config.setFileExtension("txt");
			File relationshipFileName = new File(exportFolder, 
					config.getExportFileName() + releaseDate + "." + config.getFileExtension());
			
			RF2ArtifactPostExportImpl pExp=new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_RELATIONSHIP, new File( rf2FullFolder),
					relationshipFileName, new File(outputFolder), targetDirectory,
					 previousReleaseDate, releaseDate, config.getFileExtension(),"",namespace);
			pExp.process();
			
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

	public String getPreviousReleaseDate() {
		return previousReleaseDate;
	}

	public void setPreviousReleaseDate(String previousReleaseDate) {
		this.previousReleaseDate = previousReleaseDate;
	}

	public String getRf2FullFolder() {
		return rf2FullFolder;
	}

	public void setRf2FullFolder(String rf2FullFolder) {
		this.rf2FullFolder = rf2FullFolder;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}
}
