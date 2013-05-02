package org.ihtsdo.mojo.qa.rf2file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import org.ihtsdo.rf2.file.delta.snapshot.tasks.FileFilterAndSorter;
import org.ihtsdo.rf2.postexport.CommonUtils;
import org.ihtsdo.rf2.postexport.FileHelper;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst;

public final class Rf2SimpleRefset extends RF2ArtifactPostExportAbst{

	File rf2Simple;
	File rf2Output;
	private String date;
	private File rf2Description;
	private static final String VMP_REFSET_ID = "447566000";
	private static final String VTM_REFSET_ID = "447565001";
	private static final String NH_REFSET_ID = "447564002";
	private static final String NEWVTM = "VTMAddition.txt";
	private static final String REMVTM = "VTMRemoval.txt";
	private static final String NEWVMP = "VMPAddition.txt";
	private static final String REMVMP = "VMPRemoval.txt";
	private static final String NEWNH = "NonHumanAddition.txt";
	private static final String REMNH = "NonHumanRemoval.txt";

	private HashMap<String,String>HashNames;
	
	public Rf2SimpleRefset(File rf2Description,File rf2Simple, File rf2OutputFolder, String releaseDate) {
		super();
		this.rf2Description = rf2Description;
		this.rf2Simple = rf2Simple;
		this.rf2Output = rf2OutputFolder;
		this.date=releaseDate;
	}
	
	public Rf2SimpleRefset(String rf2FullFolder, File rf2OutputFolder,
			String releaseDate) throws Exception {

		this.rf2Description =getPreviousFile(rf2FullFolder, FILE_TYPE.RF2_DESCRIPTION);
		this.rf2Simple=getPreviousFile(rf2FullFolder, FILE_TYPE.RF2_SIMPLE);
		this.rf2Output = rf2OutputFolder;
		this.date=releaseDate;
	}

	public void execute() throws IOException{
		
		File tmpSorted=new File("tmpSorted");
		File tmpSorting=new File("tmpSorting");
		
		if (!tmpSorted.exists()){
			tmpSorted.mkdirs();
		}
		if (!tmpSorting.exists()){
			tmpSorting.mkdirs();
		}
		if (!rf2Output.exists()){
			rf2Output.mkdirs();
		}

		HashNames=CommonUtils.getNames(rf2Description,date);
		
		File sortFile=new File(tmpSorted,"tmp_" + rf2Simple.getName());
		FileFilterAndSorter fs=new FileFilterAndSorter(rf2Simple,sortFile,tmpSorting,new int[]{5,1},new Integer[]{4},new String[]{VTM_REFSET_ID});
		fs.execute();
		fs=null;
		System.gc();

		controlNewMembers(sortFile,NEWVTM);
		controlRemMembers(sortFile,REMVTM);
		
		fs=new FileFilterAndSorter(rf2Simple,sortFile,tmpSorting,new int[]{5,1},new Integer[]{4},new String[]{VMP_REFSET_ID});
		fs.execute();
		fs=null;
		System.gc();

		controlNewMembers(sortFile,NEWVMP);
		controlRemMembers(sortFile,REMVMP);
		
		fs=new FileFilterAndSorter(rf2Simple,sortFile,tmpSorting,new int[]{5,1},new Integer[]{4},new String[]{NH_REFSET_ID});
		fs.execute();
		fs=null;
		System.gc();

		controlNewMembers(sortFile,NEWNH);
		controlRemMembers(sortFile,REMNH);
		

		FileHelper.emptyFolder(tmpSorted);
		FileHelper.emptyFolder(tmpSorting);
	}
	
	private void controlNewMembers(File sortFile, String fileName) throws IOException{

		FileInputStream fis = new FileInputStream(sortFile);
		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
		

		br.readLine();
		
		File logFile=new File(rf2Output,fileName);
		
		FileOutputStream fos = new FileOutputStream( logFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);

		String line;
		String[] spl;
		String[] prSpl=new String[]{"","","","","",""};
		while((line=br.readLine())!=null){
			spl=line.split("\t",-1);
			if ((prSpl[5].compareTo(spl[5])!=0 
					&& date.compareTo(spl[1])==0
					&& spl[2].compareTo("1")==0)
				||(prSpl[5].compareTo(spl[5])==0 
						&& date.compareTo(spl[1])==0
						&& spl[2].compareTo("1")==0
						&& prSpl[2].compareTo("0")==0)){
				
					bw.append(line);
					bw.append("\t");
					if (HashNames.containsKey(spl[5])){
						bw.append(HashNames.get(spl[5]));
					}else{
						bw.append("");
					}
					bw.append("\r\n");
			}
			prSpl=spl;
		}

		bw.close();
		br.close();
	}

	private void controlRemMembers(File sortFile, String fileName) throws IOException{

		FileInputStream fis = new FileInputStream(sortFile);
		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
		

		br.readLine();
		
		File logFile=new File(rf2Output,fileName);
		
		FileOutputStream fos = new FileOutputStream( logFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);

		String line;
		String[] spl;
		String[] prSpl=new String[]{"","","","","",""};
		while((line=br.readLine())!=null){
			spl=line.split("\t",-1);
			if (prSpl[5].compareTo(spl[5])==0 
					&& date.compareTo(spl[1])==0
					&& spl[2].compareTo("0")==0
					&& prSpl[2].compareTo("1")==0){
				
					bw.append(line);
					bw.append("\t");
					if (HashNames.containsKey(spl[5])){
						bw.append(HashNames.get(spl[5]));
					}else{
						bw.append("");
					}
					bw.append("\r\n");
			}
			prSpl=spl;
		}

		bw.close();
		br.close();
	}
}
