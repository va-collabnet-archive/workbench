package org.ihtsdo.rf2.identifier.mojo;

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
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.rf2.mojo.ConfigMojo;
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
public class RF2IDGeneratorMojo extends ConfigMojo {
	
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

	public void execute() throws MojoExecutionException {
		
		config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/description.xml");

		// set all the values passed via mojo
		setConfig(config);
		
		getLog().info("Running the RF2 File ID Creation with the following ");
		getLog().info("Release Folder     :" + releaseFolder);
		getLog().info("Destination Folder :" + destinationFolder);
		

		// check if the release folder exists
		File rFile = new File(releaseFolder);
		if (!rFile.exists())
			throw new MojoExecutionException("Release folder : " + destinationFolder + " doesn't exist, exiting ..");
		
		//check if the release folder contains files
		String[] rFiles = rFile.list();
		if ( rFiles.length == 0 )
			throw new MojoExecutionException("Release folder : " + destinationFolder + " is empty, exiting ..");

		// create the destination folder if it doesn't exist
		File dFile = new File(destinationFolder);
		if (!dFile.exists()) {
			getLog().info("Destination folder : " + destinationFolder + " doesn't exist, creating ..");
			dFile.mkdirs();
		}

		for (int f = 0; f < rf2Files.size(); f++) {
			File file = new File(releaseFolder + File.separator + rf2Files.get(f).fileName);
			File sctIdFile = new File(destinationFolder + File.separator + rf2Files.get(f).sctIdFileName);
			int effectiveTimeOrdinal = rf2Files.get(f).key.effectiveTimeOrdinal;
			System.out.println("===effectiveTimeOrdinal===" + effectiveTimeOrdinal);
			
			ArrayList<String> Key = rf2Files.get(f).key.keyOrdinals;
		
			// Creating SctIds			
			getLog().info("Creating SCTIds.....................");
			
			// open rf2 file and  removed line with effective date  characteristictypeId wherever applicable...
			try {
				//BufferedReader rf2FileReader = new BufferedReader(new FileReader(file));
				BufferedWriter rf2FileWriter = new BufferedWriter(new FileWriter(sctIdFile));
				//BufferedWriter rf2FileWriter = createWriter(destinationFolder + File.separator + rf2Files.get(f).sctIdFileName);
			
			/*	FileOutputStream fos = new FileOutputStream(sctIdFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				BufferedWriter rf2FileWriter = new BufferedWriter(osw);
				
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis);				
				LineNumberReader rf2FileReader = new LineNumberReader(isr);*/
				
				/*
				FileOutputStream fos = new FileOutputStream(sctIdFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
				BufferedWriter rf2FileWriter = new BufferedWriter(osw);
				
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");				
				LineNumberReader rf2FileReader = new LineNumberReader(isr);
			*/	
				
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");				
				BufferedReader rf2FileReader = new BufferedReader(isr);
			   
				String lineRead = "";
				String sctid="sctid";
				while ((lineRead = rf2FileReader.readLine()) != null) {		
					System.out.println("==Reading line===" + lineRead);
					//ignoring header row
					//if(!lineRead.contains("effectiveTime")){	
			 			String[] part= lineRead.split("\t");
		 				for (int s = 0; s < Key.size(); s++) {
							if(part[Integer.parseInt(Key.get(s))].contains("-")){					    			
				    			String uuid = part[Integer.parseInt(Key.get(s))];
				    			sctid = ExportUtil.getSCTId(getConfig(), UUID.fromString(uuid)); 
								if(sctid.equals("0")){
									sctid = ExportUtil.getSCTId(getConfig(), UUID.fromString(uuid));
								}
								
				    			System.out.println("===New UUID===" + part[Integer.parseInt(Key.get(s))] + "===replaced with ===" + sctid);
				    			lineRead =lineRead.replace(part[Integer.parseInt(Key.get(s))], sctid);
				    			System.out.println("==Reading line 2===" + lineRead);
							 }
					    }
					//}
		 				
					rf2FileWriter.append(lineRead);
					rf2FileWriter.write("\r\n");
					
	 			}
				
				System.out.println("======RF2 Final Release File created for ==========" + rf2Files.get(f).fileName);		
				
				rf2FileReader.close();
				rf2FileWriter.close();
				
			} catch (IOException e) {
				getLog().error(e);
			}
		} 
		getLog().info("Done.");
	}

	public static Config getConfig() {
		return config;
	}
	
		
	private static Logger logger = Logger.getLogger(WriteUtil.class);

	private static int writeCount = 0;

	public static void init() {
		setWriteCount(0);
	}

	public static BufferedWriter createWriter(String fileName) throws UnsupportedEncodingException, FileNotFoundException {

		FileOutputStream os = new FileOutputStream(new File(fileName));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF8"), (1 * 1024));
		
		return bw;
	}

	public static void closeWriter(BufferedWriter bw) {
		if (bw != null)
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public static void write(Config config, String str) throws IOException {

		if (str.equals("\\r\\n"))
			writeNewLine(config, str);
		else if (str.equals("\\t"))
			writeTab(config, str);
		else
			config.getBw().write(str);
	}

	public static void writeTab(Config config, String str) throws IOException {
		config.getBw().write("\t");
	}

	public static void writeNewLine(Config config, String str) throws IOException {

		// config.getBw().write("\r\n");
		config.getBw().newLine();

		int count = getWriteCount();
		setWriteCount(++count);

		if (getWriteCount() % config.getFlushCount() == 0) {
			config.getBw().flush();

			if (logger.isDebugEnabled())
				logger.debug("Flushing line no. " + getWriteCount());
		}
	}

	public static int getWriteCount() {
		return writeCount;
	}

	public static void setWriteCount(int writeCount) {
		RF2IDGeneratorMojo.writeCount = writeCount;
	}

}