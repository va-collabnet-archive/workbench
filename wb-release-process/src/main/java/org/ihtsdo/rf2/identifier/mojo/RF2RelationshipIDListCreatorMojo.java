package org.ihtsdo.rf2.identifier.mojo;

import java.io.File;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.rf2.identifier.factory.RF2RelationshipIdListGeneratorFactory;
import org.ihtsdo.rf2.mojo.ReleaseConfigMojo;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.JAXBUtil;



/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal rf2-relationship-id-list-creator
 * 
 */
public class RF2RelationshipIDListCreatorMojo extends ReleaseConfigMojo {
	
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
	 * 
	 */
	private String exportFolder;
	

	/**
	 * Location of the destination folder.
	 * 
	 * @parameter
	 * @required
	 */
	private String destinationFolder;

	

	/**
	 * Files
	 * 
	 * @parameter
	 * @required
	 */
	private ArrayList<RF2IdentifierFile> rf2Files;
	
	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * 
	 */
	private File targetDirectory;
	
	/**
	 * Location of the previous Id not released file. (input in this mojo)
	 * 
	 * @parameter
	 * @required
	 */
	private String previousIdNotReleasedFile;

	// for accessing the web service
	/**
	 * WS URL
	 * 
	 * @parameter
	 * @required
	 */
	private String endpointURL;
	
	/**
	 * WS username 
	 * 
	 * @parameter
	 * @required
	 */
	private String username;
	
	/**
	 * WS Password 
	 * 
	 * @parameter
	 * @required
	 */
	private String password;

	/**
	 * Component type to decide create or not sctid
	 * 
	 * @parameter
	 * @required
	 */
	private String componentType;

	
	//This mojo needs to be used only for replacing sctid with existing uuid
	public void execute() throws MojoExecutionException {			
		Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/idGenerator.xml");
		// set all the values passed via mojo
		config.setOutputFolderName(exportFolder);
		config.setReleaseDate(releaseDate);		
		config.setFlushCount(10000);
		config.setFileExtension("txt");
		config.setUsername(username);
		config.setPassword(password);
		config.setEndPoint(endpointURL);
		config.setDestinationFolder(destinationFolder);
		config.setRf2Files(rf2Files);
		config.setPreviousIdNotReleasedFile(previousIdNotReleasedFile);
		config.setComponentType(componentType);
		
		getLog().info("Running the RF2 File ID Creation with the following ");
		getLog().info("Destination Folder :" + destinationFolder);
		
//		ExportUtil.init();

		RF2RelationshipIdListGeneratorFactory factory = new RF2RelationshipIdListGeneratorFactory(config);
		factory.export();		
		
		getLog().info("Running the RF2 File ID Creation with the following ");
		getLog().info("Destination Folder :" + destinationFolder);

	}
	
	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

}