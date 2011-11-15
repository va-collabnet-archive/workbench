package org.ihtsdo.rf2.identifier.mojo;

import java.io.File;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.rf2.identifier.factory.RF2IdListGeneratorFactory;
import org.ihtsdo.rf2.mojo.ReleaseConfigMojo;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.JAXBUtil;



/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal rf2-id-list-creator
 * 
 */
public class RF2IDListCreatorMojo extends ReleaseConfigMojo {
	
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
	 * Files
	 * 
	 * @parameter
	 * @required
	 */
	private String updateWbSctId;
	
	
	// for accessing the web service
	/**
	 * Files
	 * 
	 * @parameter
	 * @required
	 */
	private String endpointURL;
	
	/**
	 * Files
	 * 
	 * @parameter
	 * @required
	 */
	private String username;
	
	/**
	 * Files
	 * 
	 * @parameter
	 * @required
	 */
	private String password;

	
	//This mojo needs to be used only for replacing sctid with existing uuid
	public void execute() throws MojoExecutionException {			
		Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/idGenerator.xml");
		// set all the values passed via mojo
		config.setOutputFolderName(exportFolder);
//		DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
//		Date time = df.parse(releaseDate);
//		DateFormat releaseFormat = new SimpleDateFormat("yyyyMMdd");
//		String releaseDateString = releaseFormat.format(time);
		config.setReleaseDate(releaseDate);		
		config.setFlushCount(10000);
		config.setInvokeDroolRules("false");
		config.setFileExtension("txt");
		config.setUsername(username);
		config.setPassword(password);
		config.setEndPoint(endpointURL);
		config.setDestinationFolder(destinationFolder);
		config.setRf2Files(rf2Files);
		config.setUpdateWbSctId(updateWbSctId);
		
		getLog().info("Running the RF2 File ID Creation with the following ");
		getLog().info("Destination Folder :" + destinationFolder);
		
		// Initialize meta hierarchy
		//ExportUtil.init();

		RF2IdListGeneratorFactory factory = new RF2IdListGeneratorFactory(config);
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