package org.ihtsdo.mojo.release.core;

import java.io.File;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.rf2.core.factory.RF2StatedRelationshipFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;

/**
 * @author Varsha Parekh
 * 
 * @goal export-stated-relationship
 * @requiresDependencyResolution compile
 */

public class RF2StatedRelationshipExporterMojo extends AbstractMojo {

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
	 * Location of the exportFoler.
	 * 
	 * @parameter
	 * @required
	 */
	private String exportFolder;

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

			Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/statedrelationship.xml");

			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);

//			DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
//			Date time = df.parse(releaseDate);
//			DateFormat releaseFormat = new SimpleDateFormat("yyyyMMdd");
//			String releaseDateString = releaseFormat.format(time);
			config.setReleaseDate(releaseDate);
			
			config.setFlushCount(10000);
			config.setInvokeDroolRules("false");
			config.setFileExtension("txt");

			// initialize ace framwork and meta hierarchy
			ExportUtil.init();

			RF2StatedRelationshipFactory factory = new RF2StatedRelationshipFactory(config);
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
