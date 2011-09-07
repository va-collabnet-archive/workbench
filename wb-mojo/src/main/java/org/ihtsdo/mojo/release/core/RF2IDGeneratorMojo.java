package org.ihtsdo.mojo.release.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.ihtsdo.rf2.core.factory.RF2ConceptFactory;
import org.ihtsdo.rf2.identifier.dao.RF2IdentifierFile;
import org.ihtsdo.rf2.identifier.factory.RF2IdGeneratorFactory;

import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;
import org.ihtsdo.rf2.util.WriteUtil;



/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal rf2-id-generator
 * 
 * @phase install
 */
public class RF2IDGeneratorMojo extends AbstractMojo {
	
	private static Config config;

	/**
	 * Location of the release folder.
	 * 
	 * @parameter
	 * @required
	 */
	private String releaseFolder;

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
	 * Workbench sctid update flag
	 * 
	 * @parameter
	 * @required
	 */
	private boolean updateWbSctId;

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
		config.setDestinationFolder(destinationFolder);
		config.setReleaseFolder(releaseFolder);
		config.setRf2Files(rf2Files);
		config.setUpdateWbSctId(updateWbSctId);
		
		// Initialize meta hierarchy
		ExportUtil.init();

		RF2IdGeneratorFactory factory = new RF2IdGeneratorFactory(config);
		factory.export();		
		
		getLog().info("Running the RF2 File ID Creation with the following ");
		getLog().info("Release Folder     :" + releaseFolder);
		getLog().info("Destination Folder :" + destinationFolder);

	}

}