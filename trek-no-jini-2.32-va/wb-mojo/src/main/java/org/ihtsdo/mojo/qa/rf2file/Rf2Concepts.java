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
import org.ihtsdo.rf2.postexport.FileSorter;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst;

public final class Rf2Concepts extends RF2ArtifactPostExportAbst{

	private static final String CHANGEDSEMTAG_FILENAME = "ChangedSemTag.txt";
	File rf2Description;
	File rf2Output;
	private String date;
	private File rf2Concept;
	private static final String NEWFSN_FILENAME="NewFsn.txt";
	private static final String CHANGEDFSN_FILENAME = "ChangedFsn.txt";
	private static final String CHANGEDTOPRIMITIVE_FILENAME = "ChangedToPrimitive.txt";
	private static final String CHANGEDTODEFINED_FILENAME = "ChangedToDefined.txt";
	private static final String CHANGEDTOACTIVE_FILENAME = "ChangedToActive.txt";
	private static final String CHANGEDTOINACTIVE_FILENAME = "ChangedToInactive.txt";
	private HashMap<String,String>HashNames;
	
	public Rf2Concepts(File rf2Concept,File rf2Description, File rf2OutputFolder, String releaseDate) {
		super();
		this.rf2Concept=rf2Concept;
		this.rf2Description = rf2Description;
		this.rf2Output = rf2OutputFolder;
		this.date=releaseDate;
	}
	public Rf2Concepts(String rf2FullFolder, File rf2OutputFolder, String releaseDate) throws Exception {
		super();
		this.rf2Concept=getPreviousFile(rf2FullFolder, FILE_TYPE.RF2_CONCEPT);
		this.rf2Description =getPreviousFile(rf2FullFolder, FILE_TYPE.RF2_DESCRIPTION);
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
		
		File sortDesc=new File(tmpSorted,"tmp_" + rf2Description.getName());
		FileFilterAndSorter fs=new FileFilterAndSorter(rf2Description,sortDesc,tmpSorting,new int[]{4,1,2},new Integer[]{6},new String[]{"900000000000003001"});
		fs.execute();
		fs=null;
		System.gc();

		controlNewFSN(sortDesc);
		
		controlChangedSemTag( sortDesc) ;
		
		controlChangedFSN( sortDesc) ;
		

		File sortCpt=new File(tmpSorted,"tmp_" + rf2Concept.getName());
		FileSorter fso=new FileSorter(rf2Concept,sortCpt,tmpSorting,new int[]{0,1});
		fso.execute();
		fso=null;
		System.gc();
		
		controlChangedToInactive(sortCpt);

		controlChangedToActive(sortCpt);

		controlChangedToDefined(sortCpt);

		controlChangedToPrimitive(sortCpt);
		
		FileHelper.emptyFolder(tmpSorted);
		FileHelper.emptyFolder(tmpSorting);
	}
	
	private void controlNewFSN(File sortCpt) throws IOException{

		FileInputStream fis = new FileInputStream(sortCpt	);
		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
		

		br.readLine();
		
		File logFile=new File(rf2Output,NEWFSN_FILENAME);
		
		FileOutputStream fos = new FileOutputStream( logFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);

		String line;
		String[] spl;
		String[] prSpl=new String[]{"","","","","","","","",""};
		while((line=br.readLine())!=null){
			spl=line.split("\t",-1);
			if (prSpl[4].compareTo(spl[4])!=0 
					&& date.compareTo(spl[1])==0
					&& spl[2].compareTo("1")==0){
				
					bw.append(line);
					bw.append("\r\n");
			}
			prSpl=spl;
		}

		bw.close();
		br.close();
	}

	private void controlChangedSemTag(File sortCpt) throws IOException{

		FileInputStream fis = new FileInputStream(sortCpt	);
		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
		

		br.readLine();
		
		File logFile=new File(rf2Output,CHANGEDSEMTAG_FILENAME);
		
		FileOutputStream fos = new FileOutputStream( logFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);

		String line;
		String[] spl;
		String[] prSpl=new String[]{"","","","","","","","",""};
		while((line=br.readLine())!=null){
			spl=line.split("\t",-1);
			if (prSpl[4].compareTo(spl[4])==0 
						&& date.compareTo(spl[1])==0
						&& spl[2].compareTo("1")==0
						&& spl[7].compareTo(prSpl[7])!=0){
					String ST=getSemTag(spl[7]);
					String preST=getSemTag(prSpl[7]);
					if (ST.compareTo(preST)!=0){
						bw.append(line);
						bw.append("\t");
						bw.append(preST);
						bw.append("\r\n");
						
					}
			}
			prSpl=spl;
		}

		bw.close();
		br.close();
	}

	private String getSemTag(String fsn) {
		int pos=fsn.lastIndexOf(")");
		String st="";
		if (pos>0){
			int ini=fsn.lastIndexOf("(");
			if (ini>0){
				st=fsn.substring(ini +1,pos);
			}
		}
		return st;
	}
	private void controlChangedFSN(File sortCpt) throws IOException{
		
		FileInputStream fis = new FileInputStream(sortCpt	);
		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
		
		
		br.readLine();
		
		File logFile=new File(rf2Output,CHANGEDFSN_FILENAME);
		
		FileOutputStream fos = new FileOutputStream( logFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);
		
		String line;
		String[] spl;
		String[] prSpl=new String[]{"","","","","","","","",""};
		while((line=br.readLine())!=null){
			spl=line.split("\t",-1);
			if (prSpl[4].compareTo(spl[4])==0 
					&& date.compareTo(spl[1])==0
					&& spl[2].compareTo("1")==0
					&& spl[7].compareTo(prSpl[7])!=0){

				String fsn=getTerm(spl[7]);
				String preFsn=getTerm(prSpl[7]);
				
				if (fsn.compareTo(preFsn)!=0){
					
					bw.append(line);
					bw.append("\t");
					bw.append(prSpl[7]);
					bw.append("\r\n");
					
				}
			}
			prSpl=spl;
		}
		
		bw.close();
		br.close();
	}

	private String getTerm(String fsn) {
		int pos=fsn.lastIndexOf("(");
		String st="";
		if (pos>0){
			st=fsn.substring(0,pos);
		}else{
			return fsn;
		}
		return st;
	}
	
	private void controlChangedToInactive(File sortCpt) throws IOException{

		FileInputStream fis = new FileInputStream(sortCpt	);
		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
		

		br.readLine();
		
		File logFile=new File(rf2Output,CHANGEDTOINACTIVE_FILENAME);
		
		FileOutputStream fos = new FileOutputStream( logFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);

		String line;
		String[] spl;
		String[] prSpl=new String[]{"","","","",""};
		while((line=br.readLine())!=null){
			spl=line.split("\t",-1);
			if (prSpl[0].compareTo(spl[0])==0 
						&& date.compareTo(spl[1])==0
						&& spl[2].compareTo("0")==0
						&& prSpl[2].compareTo("1")==0){
				
						bw.append(line);
						bw.append("\t");
						if (HashNames.containsKey(spl[0])){
							bw.append(HashNames.get(spl[0]));
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
	private void controlChangedToActive(File sortCpt) throws IOException{

		FileInputStream fis = new FileInputStream(sortCpt);
		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
		

		br.readLine();
		
		File logFile=new File(rf2Output,CHANGEDTOACTIVE_FILENAME);
		
		FileOutputStream fos = new FileOutputStream( logFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);

		String line;
		String[] spl;
		String[] prSpl=new String[]{"","","","",""};
		while((line=br.readLine())!=null){
			spl=line.split("\t",-1);
			if (prSpl[0].compareTo(spl[0])==0 
						&& date.compareTo(spl[1])==0
						&& spl[2].compareTo("1")==0
						&& prSpl[2].compareTo("0")==0){
				
						bw.append(line);
						bw.append("\t");
						if (HashNames.containsKey(spl[0])){
							bw.append(HashNames.get(spl[0]));
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
	private void controlChangedToPrimitive(File sortCpt) throws IOException{

		FileInputStream fis = new FileInputStream(sortCpt);
		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
		

		br.readLine();
		
		File logFile=new File(rf2Output,CHANGEDTOPRIMITIVE_FILENAME);
		
		FileOutputStream fos = new FileOutputStream( logFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);

		String line;
		String[] spl;
		String[] prSpl=new String[]{"","","","",""};
		while((line=br.readLine())!=null){
			spl=line.split("\t",-1);
			if (prSpl[0].compareTo(spl[0])==0 
						&& date.compareTo(spl[1])==0
						&& spl[2].compareTo("1")==0
						&& prSpl[4].compareTo("900000000000073002")==0
						&& spl[4].compareTo("900000000000074008")==0){
				
						bw.append(line);
						bw.append("\t");
						if (HashNames.containsKey(spl[0])){
							bw.append(HashNames.get(spl[0]));
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

	private void controlChangedToDefined(File sortCpt) throws IOException{
		
		FileInputStream fis = new FileInputStream(sortCpt);
		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
		
		
		br.readLine();
		
		File logFile=new File(rf2Output,CHANGEDTODEFINED_FILENAME);
		
		FileOutputStream fos = new FileOutputStream( logFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);
		
		String line;
		String[] spl;
		String[] prSpl=new String[]{"","","","",""};
		while((line=br.readLine())!=null){
			spl=line.split("\t",-1);
			if (prSpl[0].compareTo(spl[0])==0 
					&& date.compareTo(spl[1])==0
					&& spl[2].compareTo("1")==0
					&& spl[4].compareTo("900000000000073002")==0
					&& prSpl[4].compareTo("900000000000074008")==0){
				
				bw.append(line);
				bw.append("\t");
				if (HashNames.containsKey(spl[0])){
					bw.append(HashNames.get(spl[0]));
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
