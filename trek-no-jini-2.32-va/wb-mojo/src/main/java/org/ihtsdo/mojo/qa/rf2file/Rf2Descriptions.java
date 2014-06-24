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

import org.ihtsdo.rf2.postexport.CommonUtils;
import org.ihtsdo.rf2.postexport.FileHelper;
import org.ihtsdo.rf2.postexport.FileSorter;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst;


public final class Rf2Descriptions extends RF2ArtifactPostExportAbst {

	File rf2Description;
	File rf2Output;
	private String date;
	private static final String NEWDESC_FILENAME="NewDescription.txt";

	private HashMap<String,String>HashNames;
	
	public Rf2Descriptions(File rf2Description, File rf2OutputFolder, String releaseDate) {
		super();
		this.rf2Description = rf2Description;
		this.rf2Output = rf2OutputFolder;
		this.date=releaseDate;
	}
	
	public Rf2Descriptions(String rf2FullFolder, File rf2OutputFolder,
			String releaseDate) throws Exception {
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
		FileSorter fs=new FileSorter(rf2Description,sortDesc,tmpSorting,new int[]{4,0,1});
		fs.execute();
		fs=null;
		System.gc();

		controlNewDescription(sortDesc);

		FileHelper.emptyFolder(tmpSorted);
		FileHelper.emptyFolder(tmpSorting);
		
	}
	
	private void controlNewDescription(File sortDesc) throws IOException{

		FileInputStream fis = new FileInputStream(sortDesc	);
		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
		

		br.readLine();
		
		File logFile=new File(rf2Output,NEWDESC_FILENAME);
		
		FileOutputStream fos = new FileOutputStream( logFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);

		String line;
		String[] spl;
		String[] prSpl=new String[]{"","","","","","","","",""};
		while((line=br.readLine())!=null){
			spl=line.split("\t",-1);
			if (prSpl[0].compareTo(spl[0])!=0 
					&& date.compareTo(spl[1])==0
					&& spl[2].compareTo("1")==0){
				
					bw.append(line);
					bw.append("\t");
					if (HashNames.containsKey(spl[4])){
						bw.append(HashNames.get(spl[4]));
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
