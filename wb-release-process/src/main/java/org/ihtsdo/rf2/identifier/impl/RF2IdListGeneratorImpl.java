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
import java.util.HashMap;
import java.util.List;
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

public class RF2IdListGeneratorImpl extends RF2IDImpl {

	private static Logger logger = Logger.getLogger(RF2IdListGeneratorImpl.class);
	private HashMap<String, HashMap<UUID, Long>> hmTypeMap;

	public RF2IdListGeneratorImpl(Config config) {
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
		RF2IdListGeneratorImpl.writeCount = writeCount;
	}


	public void generateIdentifier(){

		// create the destination folder if it doesn't exist
		File dFile = new File(getConfig().getDestinationFolder());
		if (!dFile.exists()) {
			logger.info("Destination folder : " + getConfig().getDestinationFolder() + " doesn't exist, creating ..");
			logger.info("Creating dirs does not existent." );
			dFile.mkdirs();

			logger.info("Creating dirs does not existent." );
		}

		String updateWbSctId = getConfig().isUpdateWbSctId();

		//check configuration contains files

		if ( getConfig().getRf2Files().size() == 0 ){
			logger.info("No files specified in order to generate SCTID..");
			System.exit(0);
		}

		hmTypeMap=new HashMap<String, HashMap<UUID,Long>>();

		logger.info("Creating SCTIds lists" );
		extractUuidToList();

		logger.info("Assign ids from list." );
		HashMap<UUID,Long> hmTmp;
		for (int f = 0; f < getConfig().getRf2Files().size(); f++) {

			File file = new File( getConfig().getRf2Files().get(f).fileName);
			File sctIdFile = new File(getConfig().getRf2Files().get(f).sctIdFileName);
			sctIdFile.getParentFile().mkdirs();
			int effectiveTimeOrdinal = getConfig().getRf2Files().get(f).key.effectiveTimeOrdinal;
			ArrayList<String> Key = getConfig().getRf2Files().get(f).key.keyOrdinals;

			String namespaceId = getConfig().getRf2Files().get(f).sctidparam.namespaceId;
			String partitionId = getConfig().getRf2Files().get(f).sctidparam.partitionId;
			String releaseId =  getConfig().getRf2Files().get(f).sctidparam.releaseId;
			String executionId = getConfig().getRf2Files().get(f).sctidparam.executionId;
			String moduleId = getConfig().getRf2Files().get(f).sctidparam.moduleId;			
			String componentType = getConfig().getRf2Files().get(f).sctidparam.componentType;
			String idType = getConfig().getRf2Files().get(f).sctidparam.idType;

			hmTmp=hmTypeMap.get(idType);
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
					sctid="";
					for (int s = 0; s < Key.size(); s++) {
						if(part[Integer.parseInt(Key.get(s))].contains("-")){	

							String uuid = part[Integer.parseInt(Key.get(s))];
							Long lSctId=null;
							if (hmTmp!=null){
								lSctId=hmTmp.get(UUID.fromString(uuid));
							}
							if (lSctId!=null){
								sctid=String.valueOf(lSctId);
							}else{
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
							}
							if (sctid.compareTo("")!=0)
								part[Integer.parseInt(Key.get(s))]=sctid;
						}
					}
					if (sctid.compareTo("")!=0){
						for (int i=0;i<part.length;i++){
							rf2FileWriter.append(part[i]);
							if (i+1 < part.length){
								rf2FileWriter.write("\t");
							}
						}
					}else{
						rf2FileWriter.append(lineRead);
					}
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


	private void extractUuidToList() {

		for (int f = 0; f < getConfig().getRf2Files().size(); f++) {
			File inputFile = new File(getConfig().getRf2Files().get(f).fileName);
			String idSaveTolist = getConfig().getRf2Files().get(f).sctidparam.idSaveTolist;
			String idType = getConfig().getRf2Files().get(f).sctidparam.idType;
			String idColumnIndex= getConfig().getRf2Files().get(f).sctidparam.idColumnIndex;
			String idMapFile= getConfig().getRf2Files().get(f).sctidparam.idMapFile;

			if (idSaveTolist != null && idSaveTolist.toLowerCase().equals("true") ){
				if (idType!=null ){
					try{
						if (idColumnIndex!=null && Integer.parseInt(idColumnIndex)>-1) {
							int colIx=Integer.parseInt(idColumnIndex);

							List<UUID> list = new ArrayList<UUID>();

							FileInputStream fis = new FileInputStream(inputFile);
							InputStreamReader isr = new InputStreamReader(fis, "UTF-8");				
							BufferedReader rf2FileReader = new BufferedReader(isr);

							String lineRead = "";
							while ((lineRead = rf2FileReader.readLine()) != null) {		
								String[] part= lineRead.split("\t");
								String uuid=part[colIx];
								if(uuid.contains("-")){	
									list.add(UUID.fromString(uuid));

								}
							}
							rf2FileReader.close();
							isr.close();
							fis.close();

							HashMap<UUID, Long> res = new HashMap<UUID, Long>();
							if (list.size()>0){
								String namespaceId = getConfig().getRf2Files().get(f).sctidparam.namespaceId;
								String partitionId = getConfig().getRf2Files().get(f).sctidparam.partitionId;
								String releaseId =  getConfig().getRf2Files().get(f).sctidparam.releaseId;
								String executionId = getConfig().getRf2Files().get(f).sctidparam.executionId;
								String moduleId = getConfig().getRf2Files().get(f).sctidparam.moduleId;			

								res=getSCTIdList(getConfig(),list,Integer.parseInt(namespaceId), partitionId, releaseId, executionId,"1");

								if (idMapFile!=null && !idMapFile.equals("")){

									File mapFile=new File(idMapFile);
									mapFile.getParentFile().mkdirs();
									FileOutputStream fos = new FileOutputStream(idMapFile);
									OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF8");
									BufferedWriter rf2FileWriter = new BufferedWriter(osw);

									for (UUID uuid:res.keySet()){
										rf2FileWriter.append(uuid.toString() );
										rf2FileWriter.append("\t");
										rf2FileWriter.append(String.valueOf(res.get(uuid)));
										rf2FileWriter.append("\r\n");
									}
									rf2FileWriter.close();
									osw.close();
									fos.close();
								}
							}

							hmTypeMap.put(idType, res);
						}
					} catch (IOException e) {
						logger.error(e);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}
		}

	}


}
