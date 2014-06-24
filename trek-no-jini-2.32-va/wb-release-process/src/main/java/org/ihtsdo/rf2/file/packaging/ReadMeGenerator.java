package org.ihtsdo.rf2.file.packaging;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.file.packaging.model.ReadmeConfig;

/**
 * ReadMeGenerator
 * 
 * Generates the Readme files for a release. It takes the source directory and recursively walks the directories and writes the directory names, the file names and file sizes to the readme file.
 * 
 * The properties file can list multiple directories in which to create the readme files, as a comma-separated list.
 * 
 * Files needed: properties file called "conf/packaging.properties" file containing header information "conf/header.txt" that is written to all readme files Run this program by calling
 * "runbuild-generateReadme.bat" which uses "build-generateReadme.xml"
 * 
 * @author lseiden
 * 
 */
public class ReadMeGenerator {

	private static final String DEFAULT_FILENAME_WIDTH = "80";
	private static int fileNameWidthSize = 80;
	private static final String DEFAULT_README_FILENAME = "Readme.txt";
	private static String readmeFileName = DEFAULT_README_FILENAME;
	private static final String DEFAULT_RUN_TWICE = "true";
	private static final String DEFAULT_SOURCE = "target";
	private static final String DEFAULT_HEADER_LOCATION = "conf/header.txt";

	private static final String propsFile = "conf/packaging.properties";
	private static Logger logger = Logger.getLogger(ReadMeGenerator.class.getName());
	private static Properties props = new Properties();

	public static String newline = System.getProperty("line.separator");

	public static void main(String args[]) {


		// read the profperties file
		try {
			props.load(new FileInputStream(propsFile));

			String fileNameWidth = getProperty("readme.filename.width", DEFAULT_FILENAME_WIDTH);
			fileNameWidthSize = (new Integer(fileNameWidth)).intValue();

			String runTwice = getProperty("readme.run.twice", DEFAULT_RUN_TWICE);

			// read in source directories, can be a comma-separated list
			String src = getProperty("readme.source", DEFAULT_SOURCE);
			logger.debug("Creating readme file for: " + src);

			// get filename for readme files
			readmeFileName = getProperty("readme.filename", DEFAULT_README_FILENAME);

			// read in file containing the header
			String headerLocation = getProperty("readme.headerLocation", DEFAULT_HEADER_LOCATION);

			Readme r = new Readme();
			
			// load the configuration
			ReadmeConfig readmeConfig = new ReadmeConfig();
			
			// set the readme configuration object
			readmeConfig.setFileName(readmeFileName);
			readmeConfig.setFileNameWidth(fileNameWidthSize);
			readmeConfig.setHeaderLocation(headerLocation);
			readmeConfig.setRunTwice(new Boolean(runTwice));
			readmeConfig.setSource(src);
			r.process(readmeConfig);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getProperty(String propertyName, String defaultValue) {
		String value = props.getProperty(propertyName);
		if (value == null || value.equals("")) {
			value = defaultValue;
		}
		return value;
	}

}
