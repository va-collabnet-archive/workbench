package org.ihtsdo.mojo.release.compatibilitypkg;

import java.io.File;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.rf2.compatibilitypkg.factory.RF2AssociationId_SCTIDMapFactory;
import org.ihtsdo.rf2.compatibilitypkg.factory.RF2HistoricalAssociationIdentFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;

/**
 * @author Ale
 * 
 * @goal export-association-references-identifier
 * @requiresDependencyResolution compile
 */

public class RF2AssociationIdentExporterMojo extends AbstractMojo {

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
	 * Location of the wbAssociationId_SCTIDMapFactory.
	 * 
	 * @parameter
	 * @optional
	 */
	private String wbAssociationId_SCTIDMapFactory;
	
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
			
			Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/historicalAssociationIdentifier.xml");

			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);
			config.setReleaseDate(releaseDate);
			config.setPreviousReleaseDate(previousReleaseDate);
			config.setFlushCount(10000);
			config.setInvokeDroolRules("false");
			config.setFileExtension("txt");

			// initialize ace framwork and meta hierarchy
			ExportUtil.init();
			if (wbAssociationId_SCTIDMapFactory!=null){
				RF2AssociationId_SCTIDMapFactory factory=new RF2AssociationId_SCTIDMapFactory(config);
				factory.export();
			}else{
				RF2HistoricalAssociationIdentFactory factory = new RF2HistoricalAssociationIdentFactory(config);
				factory.export();
			}

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

	public String getWbAssociationId_SCTIDMapFactory() {
		return wbAssociationId_SCTIDMapFactory;
	}

	public void setWbAssociationId_SCTIDMapFactory(String wbAssociationId_SCTIDMapFactory) {
		this.wbAssociationId_SCTIDMapFactory = wbAssociationId_SCTIDMapFactory;
	}
}
