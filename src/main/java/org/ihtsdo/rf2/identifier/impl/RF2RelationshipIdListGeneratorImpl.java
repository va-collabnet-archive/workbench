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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.impl.RF2IDImpl;
import org.ihtsdo.rf2.util.Config;

/**
 * Title: RF2RelationshipIdListGeneratorImpl Relationship: Generating sct identifier for all the newly created relationship content present in RF2 Release File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2RelationshipIdListGeneratorImpl extends RF2IDImpl {

	private static Logger logger = Logger.getLogger(RF2RelationshipIdListGeneratorImpl.class);
	private HashMap<String, HashMap<UUID, Long>> hmTypeMap;

	public RF2RelationshipIdListGeneratorImpl(Config config) {
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
		RF2RelationshipIdListGeneratorImpl.writeCount = writeCount;
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
		File sortedSnapPreviousfile=new File(getConfig().getPreviousIdNotReleasedFile());
		if (!sortedSnapPreviousfile.exists()){
			try {
				sortedSnapPreviousfile.createNewFile();
			} catch (IOException e) {
				logger.error("Exception	" +e);
			}
		}
		File newFilePrev=new File(sortedSnapPreviousfile.getParentFile().getAbsolutePath(),"tmp_" + sortedSnapPreviousfile.getName()); 
		FileOutputStream fosd=null;
		OutputStreamWriter oswd=null;
		BufferedWriter bwd=null;
		try {
			fosd = new FileOutputStream(newFilePrev );
			oswd = new OutputStreamWriter(fosd,"UTF-8");
			bwd = new BufferedWriter(oswd);

			FileInputStream fip = new FileInputStream(sortedSnapPreviousfile	);
			InputStreamReader psr = new InputStreamReader(fip,"UTF-8");
			BufferedReader brP = new BufferedReader(psr);

			bwd.append("id");
			bwd.append("\t");
			bwd.append("effectiveTime");
			bwd.append("\t");
			bwd.append("active");
			bwd.append("\t");
			bwd.append("moduleId");
			bwd.append("\t");
			bwd.append("sourceId");
			bwd.append("\t");
			bwd.append("destinationId");
			bwd.append("\t");
			bwd.append("relationshipGroup");
			bwd.append("\t");
			bwd.append("typeId");
			bwd.append("\t");
			bwd.append("characteristicTypeId");
			bwd.append("\t");
			bwd.append("modifierId");
			bwd.append("\r\n");

			brP.readLine();
			String line;
			while ((line=brP.readLine())!=null){
				bwd.append(line);
				bwd.append("\r\n");
			}
			brP.close();
			brP=null;

			System.gc();

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//check configuration contains files
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			String componentTypeAction = getConfig().getComponentType();
			if (componentTypeAction==null){
				componentTypeAction="";
			}
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
				boolean bNewId;
				while ((lineRead = rf2FileReader.readLine()) != null) {		
					String[] part= lineRead.split("\t",-1);
					bNewId=false;
					for (int s = 0; s < Key.size(); s++) {
						if(part[Integer.parseInt(Key.get(s))].contains("-")){ // contains uuid pattern
							//String uuid = part[Integer.parseInt(Key.get(s))];
							//id	effectiveTime	active	moduleId	sourceId	destinationId	relationshipGroup	typeId	characteristicTypeId	modifierId
							UUID uuid= null;
							try {
								uuid = Type5UuidFactory.get(part[4] + part[5] + part[7] + part[6]);	// sourceId + destinationId + typeId + relationshipGroup

								Long lSctId=null;
								if (hmTmp!=null){
									//lSctId=hmTmp.get(UUID.fromString(uuid));
									lSctId=hmTmp.get(uuid);
								}

								if (lSctId!=null){
									sctid=String.valueOf(lSctId);
								}else if (!componentTypeAction.toLowerCase().equals("nosctidcreate")){
									//sctid = getSCTId(getConfig(), UUID.fromString(uuid) , Integer.parseInt(namespaceId), partitionId , releaseId , executionId , moduleId);
									logger.info("Inferred relationship uuid sending to webservice "  +uuid);
									sctid = getSCTId(getConfig(), uuid , Integer.parseInt(namespaceId), partitionId , releaseId , executionId , moduleId);
									if(sctid.equals("0")){
										//sctid = getSCTId(getConfig(), UUID.fromString(uuid) , Integer.parseInt(namespaceId), partitionId , releaseId , executionId , moduleId);
										sctid = getSCTId(getConfig(), uuid , Integer.parseInt(namespaceId), partitionId , releaseId , executionId , moduleId);
									}
								}else{
									sctid=uuid.toString();
								}
								bNewId=true;
							} catch (NoSuchAlgorithmException e) {
								logger.error("NoSuchAlgorithmException	" +e);
							} catch (NumberFormatException e) {
								logger.error("NumberFormatException	" +e);
							} catch (Exception e) {
								logger.error("Exception	" +e);
							}

							lineRead =lineRead.replace(part[Integer.parseInt(Key.get(s))], sctid);
							
						}
					}
					if (bNewId && bwd!=null){
						bwd.append(lineRead);
						bwd.append("\r\n");
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
		try {
			bwd.close();
			oswd.close();
			fosd.close();
			bwd=null;
			oswd=null;
			fosd=null;
			String strOutput=sortedSnapPreviousfile.getAbsolutePath();
			sortedSnapPreviousfile.delete();
			newFilePrev.renameTo(new File(strOutput));
			
		} catch (IOException e) {
			logger.error("Exception	" +e);
		}

		System.gc();
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
									uuid = Type5UuidFactory.get(part[4] + part[5] + part[7] + part[6]).toString();	// sourceId + destinationId + typeId + relationshipGroup

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
								logger.info(list.size() + " inferred relationships in list sending to web service.");

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
