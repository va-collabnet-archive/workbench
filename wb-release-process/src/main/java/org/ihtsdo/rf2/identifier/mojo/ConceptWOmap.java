package org.ihtsdo.rf2.identifier.mojo;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.core.factory.RF2ConceptFactory;
import org.ihtsdo.rf2.identifier.factory.RF2ConceptWOmapFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;

/**
 * @author Ale Rodriguez
 * 
 * @goal export-uuid-cid-map
 * @requiresDependencyResolution compile
 */

public class ConceptWOmap extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	/**
	 * Location of the exportFoler.
	 * 
	 * @parameter
	 * @required
	 */
	private String exportFolder;
	
	/**
	 * Location of the file with conceptids.
	 * 
	 * @parameter
	 * @required
	 */
	private String idfile;
	

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			Config config;
			
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/conceptwomap.xml");
				
			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);
			
			config.setFlushCount(10000);
			config.setInvokeDroolRules("false");
			config.setFileExtension("txt");

			// initialize meta hierarchy
			ExportUtil.init(idfile);

			RF2ConceptWOmapFactory factory = new RF2ConceptWOmapFactory(config);
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

	public String getExportFolder() {
		return exportFolder;
	}

	public void setExportFolder(String exportFolder) {
		this.exportFolder = exportFolder;
	}
}
