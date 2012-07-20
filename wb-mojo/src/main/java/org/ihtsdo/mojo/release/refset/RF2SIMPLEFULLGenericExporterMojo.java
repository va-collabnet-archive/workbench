package org.ihtsdo.mojo.release.refset;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.rf2.refset.factory.RF2SimpleFullRefsetGenericFactory;
import org.ihtsdo.rf2.refset.factory.SctidUuid;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;

/**
 * @author Varsha Parekh
 * 
 * @goal export-simple-full-generic-refset
 * @requiresDependencyResolution compile
 */

public class RF2SIMPLEFULLGenericExporterMojo extends AbstractMojo {

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

	/**
	 * Location of the exportFoler.
	 * 
	 * @parameter
	 * @required
	 */
	private String moduleid;

	/**
	 * List of refset sctid and uuid objects
	 * 
	 * @parameter
	 */
	private List<SctidUuid> sctidUuidList;

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

			Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/simpleFullRefset.xml");

			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);
			config.setReleaseDate(releaseDate);
			config.setFlushCount(10000);
			config.setInvokeDroolRules("false");
			config.setFileExtension("txt");

			// initialize ace framwork and meta hierarchy
			ExportUtil.init();

			// Exports VTM , VMP and Non-Human
			for (SctidUuid sctidUuid : sctidUuidList) {
				RF2SimpleFullRefsetGenericFactory factory = new RF2SimpleFullRefsetGenericFactory(sctidUuid, config, moduleid);
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
}
