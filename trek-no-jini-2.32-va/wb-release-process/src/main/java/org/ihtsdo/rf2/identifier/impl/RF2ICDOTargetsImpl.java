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
import java.util.HashMap;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.postexport.AbstractTask;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportImpl;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst.FILE_TYPE;
import org.ihtsdo.rf2.refset.impl.RF2SnomedIdImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

public class RF2ICDOTargetsImpl extends AbstractTask {

	private File snapshotSortedPreviousfile;
	private File snapshotSortedExportedfile;
	private File ouputFile;
	private String releaseDate;
	private String newLine="\r\n";
	private BufferedWriter bw;
	private Config config;
	private String rf2FullFolder;
	private String previousReleaseDate;
	private File targetDirectory;
	private String outputFolder;
	private File exportedFileName;

	private static Logger logger = Logger.getLogger(RF2SnomedIdImpl.class);
	public RF2ICDOTargetsImpl(Config config, String releaseDate,
			File snapshotSortedPreviousfile, File snapshotSortedExportedfile,
			String rf2FullFolder, String previousReleaseDate, File targetDirectory, 
			String outputFolder, 
			File ouputFile) {

		this.rf2FullFolder=rf2FullFolder;
		this.previousReleaseDate=previousReleaseDate;
		this.targetDirectory=targetDirectory;
		this.outputFolder=outputFolder;
		this.exportedFileName=exportedFileName;
		this.snapshotSortedPreviousfile=snapshotSortedPreviousfile;	
		this.snapshotSortedExportedfile=snapshotSortedExportedfile;
		this.ouputFile=ouputFile;
		this.releaseDate=releaseDate;
		this.config=config;
	}

	@Override
	public void execute() throws Exception {

		try {

			FileOutputStream fos = new FileOutputStream( ouputFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
			bw = new BufferedWriter(osw);

			FileInputStream fis = new FileInputStream(snapshotSortedPreviousfile	);
			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			BufferedReader br1 = new BufferedReader(isr);

			FileInputStream fis2 = new FileInputStream(snapshotSortedExportedfile	);
			InputStreamReader isr2 = new InputStreamReader(fis2,"UTF-8");
			BufferedReader br2 = new BufferedReader(isr2);


			String line1;
			br1.readLine();
			br2.readLine();
			
			bw.append("id");
			bw.append("\t");
			bw.append("effectiveTime");
			bw.append("\t");
			bw.append("active");
			bw.append("\t");
			bw.append("moduleId");
			bw.append("\t");
			bw.append("MAPSETID");
			bw.append("\t");
			bw.append("TARGETID");
			bw.append("\t");
			bw.append("TARGETCODES");
			bw.append("\t");
			bw.append("TARGETRULE");
			bw.append("\t");
			bw.append("TARGETADVICE");
			bw.append("\r\n");

			String[] splittedLine1;
			String line2;
			String[] splittedLine2=null;
			HashMap<String, String> expTgt=new HashMap<String, String>();

			while ((line2=br2.readLine()) != null) {
				splittedLine2 = line2.split("\t",-1);
				if (!expTgt.containsKey(splittedLine2[6])){
					expTgt.put(splittedLine2[6], splittedLine2[2]);
				}else{
					if (splittedLine2[2].compareTo("1")==0){
						expTgt.put(splittedLine2[6], splittedLine2[2]);
					}
				}
			}		
			br2.close();
			String status="";
			while ((line1= br1.readLine()) != null) {
				splittedLine1 = line1.split("\t",-1);
				status=expTgt.get(splittedLine1[6]);

				if (status!=null){
					expTgt.remove(splittedLine1[6]);
					if (status.compareTo(splittedLine1[2])!=0){
						bw.append(splittedLine1[0]);
						bw.append("\t");
						bw.append(releaseDate);
						bw.append("\t");
						bw.append(status);
						bw.append("\t");
						bw.append(I_Constants.CORE_MODULE_ID);
						bw.append("\t");
						bw.append(I_Constants.ICDO_SUBSET_ID);
						bw.append("\t");
						bw.append(splittedLine1[5]);
						bw.append("\t");
						bw.append(splittedLine1[6]);
						bw.append("\t");
						bw.append(splittedLine1[7]);
						bw.append("\t");
						bw.append(splittedLine1[8]);
						bw.append(newLine);
					}else{
						bw.append(line1);
						bw.append(newLine);
					}
				}else{
					bw.append(splittedLine1[0]);
					bw.append("\t");
					bw.append(releaseDate);
					bw.append("\t");
					bw.append("0");
					bw.append("\t");
					bw.append(I_Constants.CORE_MODULE_ID);
					bw.append("\t");
					bw.append(I_Constants.ICDO_SUBSET_ID);
					bw.append("\t");
					bw.append(splittedLine1[5]);
					bw.append("\t");
					bw.append(splittedLine1[6]);
					bw.append("\t");
					bw.append(splittedLine1[7]);
					bw.append("\t");
					bw.append(splittedLine1[8]);
					bw.append(newLine);
				}
			}
			br1.close();
			UUID uuid;
			Integer nspce=0;
			String sctid;
			UUID id;
			for (String key:expTgt.keySet()){
				status=expTgt.get(key);
				uuid=Type5UuidFactory.get(I_Constants.ICDO_SUBSET_ID + key );

				sctid = ExportUtil.getSCTId(config, uuid , nspce, "05" ,releaseDate ,releaseDate , I_Constants.CORE_MODULE_ID);
				if(sctid==null || sctid.equals("0") || sctid.equals("")){

					logger.info("=====Error creating mapTargetId for uuid===" + uuid.toString());
					System.out.println("=====Error creating mapTargetId for uuid===" + uuid.toString());
				}else{				
					id=Type5UuidFactory.get(I_Constants.ICDO_SUBSET_ID + sctid );
					bw.append(id.toString());
					bw.append("\t");
					bw.append(releaseDate);
					bw.append("\t");
					bw.append(status);
					bw.append("\t");
					bw.append(I_Constants.CORE_MODULE_ID);
					bw.append("\t");
					bw.append(I_Constants.ICDO_SUBSET_ID);
					bw.append("\t");
					bw.append(sctid);
					bw.append("\t");
					bw.append(key);
					bw.append("\t");
					bw.append("");
					bw.append("\t");
					bw.append("");
					bw.append(newLine);
				}
			}
			bw.close();

			RF2ArtifactPostExportImpl pExp=new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_ICDO_TARGETS, new File( rf2FullFolder),
					ouputFile, new File(outputFolder), targetDirectory,
					previousReleaseDate, releaseDate);
			pExp.postProcess();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}	

}

