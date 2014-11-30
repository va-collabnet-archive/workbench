package org.ihtsdo.mojo.release.compatibilitypkg;

import java.io.File;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.rf2.compatibilitypkg.factory.RF2RetiredIsaStatedRelationshipFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;

/**
 * @author Ale
 * 
 * @goal export-retired-isa-stated-relationship
 * @requiresDependencyResolution compile
 */

public class RF2RetiredIsaStatedRelationshipExporterMojo extends AbstractMojo {

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
	
	/**
	 * Location of the rF2Format.
	 * 
	 * @parameter
	 * @required
	 */
	private String rF2Format;

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
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/retiredisastatedrelationship.xml");
			else
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/retiredisastatedrelationshipqa.xml");
		
			
			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);

//			DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
//			Date time = df.parse(releaseDate);
//			DateFormat releaseFormat = new SimpleDateFormat("yyyyMMdd");
//			String releaseDateString = releaseFormat.format(time);
			config.setReleaseDate(releaseDate);

			config.setPreviousReleaseDate(previousReleaseDate);
			config.setRf2Format(rF2Format);
			config.setFlushCount(10000);
			config.setInvokeDroolRules("false");
			config.setFileExtension("txt");

			// initialize ace framwork and meta hierarchy
			ExportUtil.init();

			RF2RetiredIsaStatedRelationshipFactory factory = new RF2RetiredIsaStatedRelationshipFactory(config);
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
