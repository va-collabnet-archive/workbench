package org.ihtsdo.rf2.identifier.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.impl.RF2IDImpl;
import org.ihtsdo.rf2.util.Config;

/**
 * Title: RF2IdGeneratorImpl Description: Generating sct identifier for all the newly created content RF2 Release File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2IdGeneratorImpl extends RF2IDImpl {

	private static Logger logger = Logger.getLogger(RF2IdGeneratorImpl.class);

	public RF2IdGeneratorImpl(Config config) {
		super(config);	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api. I_GetConceptData)
	 */

	
	
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
		RF2IdGeneratorImpl.writeCount = writeCount;
	}
	
		
	public void generateIdentifier(){
		
	
		// check if the release folder exists
		File rFile = new File(getConfig().getReleaseFolder());
		if (!rFile.exists())
			logger.info("Release folder : " + getConfig().getReleaseFolder() + " doesn't exist, exiting ..");
		
		//check if the release folder contains files
		String[] rFiles = rFile.list();
		if ( rFiles.length == 0 )
			logger.info("Release folder : " + getConfig().getReleaseFolder() + " is empty, exiting ..");

		// create the destination folder if it doesn't exist
		File dFile = new File(getConfig().getDestinationFolder());
		if (!dFile.exists()) {
			logger.info("Destination folder : " + getConfig().getDestinationFolder() + " doesn't exist, creating ..");
			dFile.mkdirs();
		}
		
		String updateWbSctId = getConfig().isUpdateWbSctId();
		
		//check configuration contains files
	
		if ( getConfig().getRf2Files().size() == 0 ){
			logger.info("No files specified in order to generate SCTID..");
			System.exit(0);
		}
		
		for (int f = 0; f < getConfig().getRf2Files().size(); f++) {
			
			File file = new File(getConfig().getReleaseFolder() + File.separator + getConfig().getRf2Files().get(f).fileName);
			File sctIdFile = new File(getConfig().getDestinationFolder() + File.separator + getConfig().getRf2Files().get(f).sctIdFileName);
			int effectiveTimeOrdinal = getConfig().getRf2Files().get(f).key.effectiveTimeOrdinal;
			ArrayList<String> Key = getConfig().getRf2Files().get(f).key.keyOrdinals;
		
			String namespaceId = getConfig().getRf2Files().get(f).sctidparam.namespaceId;
			String partitionId = getConfig().getRf2Files().get(f).sctidparam.partitionId;
			String releaseId =  getConfig().getRf2Files().get(f).sctidparam.releaseId;
			String executionId = getConfig().getRf2Files().get(f).sctidparam.executionId;
			String moduleId = getConfig().getRf2Files().get(f).sctidparam.moduleId;			
			String componentType = getConfig().getRf2Files().get(f).sctidparam.componentType;
			
				// Creating SctIds			
			logger.info("Creating SCTIds for ....................." + componentType);
			
			// open rf2 file and  check for uuid line then get sctid from webservice and update wherever applicable...
			try {			
				FileOutputStream fos = new FileOutputStream(sctIdFile);
	            OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF8");
	            BufferedWriter rf2FileWriter = new BufferedWriter(osw);
	              
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");				
				BufferedReader rf2FileReader = new BufferedReader(isr);
			   
				String lineRead = "";
				String sctid="";
				while ((lineRead = rf2FileReader.readLine()) != null) {		
					String[] part= lineRead.split("\t");
		 			for (int s = 0; s < Key.size(); s++) {
						if(part[Integer.parseInt(Key.get(s))].contains("-")){					    			
				    		String uuid = part[Integer.parseInt(Key.get(s))];
				    		try {
								sctid = getSCTId(getConfig(), UUID.fromString(uuid) , Integer.parseInt(namespaceId), partitionId , releaseId , executionId , moduleId);
								if(sctid.equals("0")){
				    				sctid = getSCTId(getConfig(), UUID.fromString(uuid) , Integer.parseInt(namespaceId), partitionId , releaseId , executionId , moduleId);
									// sctid = IdUtil.getSCTId(getConfig(), UUID.fromString(uuid));
								}
							} catch (NumberFormatException e) {
								logger.error("NumberFormatException" +e);
							} catch (Exception e) {
								logger.error("Exception" +e);
							}
							
							lineRead =lineRead.replace(part[Integer.parseInt(Key.get(s))], sctid);				    	
						 }
					}
					rf2FileWriter.append(lineRead);
					rf2FileWriter.write("\r\n");
	 			}
				rf2FileReader.close();
				rf2FileWriter.close();
			} catch (IOException e) {
				logger.error(e);
			}
		} 
		logger.info("Done.");
	}
	

}
