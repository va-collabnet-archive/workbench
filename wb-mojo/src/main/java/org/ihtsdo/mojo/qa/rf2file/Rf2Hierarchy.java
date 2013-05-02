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
import java.util.HashSet;

import org.ihtsdo.rf2.postexport.CommonUtils;
import org.ihtsdo.rf2.postexport.FileHelper;
import org.ihtsdo.rf2.postexport.FileSorter;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst;
import org.ihtsdo.rf2.postexport.SnapshotGeneratorMultiColumn;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst.FILE_TYPE;

public final class Rf2Hierarchy extends RF2ArtifactPostExportAbst{

	private static final String CHANGEDTOPLEVEL_FILENAME = "ChangedToplevel.txt";
	File rf2Description;
	File rf2Output;
	private String date;
	private HashMap<String,String>HashNames;
	private String prevDate;
	private File rf2Relationship;
	private HashMap<String, String> hashCpt;
	private HashMap<String, String> hashPrevCpt;

	private HashMap<String, String[]> cptsTL;
	private HashMap<String, String[]> prevCptsTL;
	private HashSet<String> inactCpt;
	private HashSet<String> topLevel;
	private BufferedWriter bw;

	public Rf2Hierarchy(File rf2Description,File rf2Relationship, File rf2OutputFolder, String releaseDate,String previousDate) {
		super();
		this.rf2Description = rf2Description;
		this.rf2Relationship=rf2Relationship;
		this.rf2Output = rf2OutputFolder;
		this.date=releaseDate;
		this.prevDate=previousDate;
	}

	public Rf2Hierarchy(String rf2FullFolder, File rf2OutputFolder,
			String releaseDate,String prevReleaseDate) throws Exception {

		this.rf2Description =getPreviousFile(rf2FullFolder, FILE_TYPE.RF2_DESCRIPTION);
		this.rf2Relationship=getPreviousFile(rf2FullFolder, FILE_TYPE.RF2_RELATIONSHIP);
		this.rf2Output = rf2OutputFolder;
		this.date=releaseDate;	
		this.prevDate=prevReleaseDate;	
	}

	public void execute() throws IOException{

		inactCpt=new HashSet<String>();
		inactCpt.add("363660007");
		inactCpt.add("363662004");
		inactCpt.add("363664003");
		inactCpt.add("443559000");
		inactCpt.add("370126003");
		inactCpt.add("363663009");
		inactCpt.add("363661006");
		
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

		File sortRel=new File(tmpSorted,"tmp_" + rf2Relationship.getName());
		FileSorter fs=new FileSorter(rf2Relationship,sortRel,tmpSorting,new int[]{0,1});
		fs.execute();
		fs=null;
		System.gc();

		File snapRel=new File(tmpSorted,"Snap_" + rf2Relationship.getName());

		SnapshotGeneratorMultiColumn ssh=new SnapshotGeneratorMultiColumn(sortRel, date, new int[]{0}, 1, snapRel, null, null);

		ssh.execute();
		ssh=null;
		System.gc();

		File snapPrevRel=new File(tmpSorted,"SnapPrev_" + rf2Relationship.getName());

		ssh=new SnapshotGeneratorMultiColumn(sortRel, prevDate, new int[]{0}, 1, snapPrevRel, null, null);

		ssh.execute();
		ssh=null;
		System.gc();

		hashCpt = new HashMap<String,String>();
		hashPrevCpt = new HashMap<String,String>();
		cptsTL=new HashMap<String, String[]>();
		prevCptsTL=new HashMap<String, String[]>();
		
		createConceptSups(snapRel,hashCpt);
		createConceptSups(snapPrevRel,hashPrevCpt);

		File logFile=new File(rf2Output,CHANGEDTOPLEVEL_FILENAME);

		FileOutputStream fos = new FileOutputStream( logFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		bw = new BufferedWriter(osw);

		HashNames=CommonUtils.getNames(rf2Description,date);
		
		controlTopLevel();

		bw.close();
		System.gc();

		FileHelper.emptyFolder(tmpSorted);
		FileHelper.emptyFolder(tmpSorting);
	}

	private void controlTopLevel() throws IOException {
		
		String prCpt;
		topLevel = new HashSet<String>();
		for(String cpt:hashCpt.keySet()){
			prCpt=hashPrevCpt.get(cpt) ;
			if (prCpt!=null ){
				topLevel.clear();
				
				getTopLevels(cpt,hashCpt,cptsTL);
				
				String[] TL=new String[topLevel.size()];
				TL=topLevel.toArray(TL);
				cptsTL.put(cpt, TL);

				topLevel.clear();
				
				getTopLevels(cpt,hashPrevCpt,prevCptsTL);
				
				String[] prevTL= new String[topLevel.size()];
				prevTL=topLevel.toArray(prevTL);
				prevCptsTL.put(cpt, prevTL);
				
				addDiffToFile(cpt,TL,prevTL);
				
			}
			
		}
		
	}

	private void addDiffToFile(String cpt, String[] TL, String[] prevTL) throws IOException {
		boolean bExists=false;
		for (String str:TL){
			bExists=false;
			for(String prStr:prevTL){
				if (str.compareTo(prStr)==0){
					bExists=true;
					break;
				}
			}
			if (!bExists){
				break;
			}
		}
		if (bExists){
			for (String prStr:prevTL){
				bExists=false;
				for(String str:TL){
					if (str.compareTo(prStr)==0){
						bExists=true;
						break;
					}
				}
				if (!bExists){
					break;
				}
			}
		}
		if (!bExists){
			bw.append(cpt);
			bw.append("\t");
			bw.append(HashNames.get(cpt));
			bw.append("\t");
			for (int i=0;i<TL.length;i++){
				bw.append(HashNames.get(TL[i]));
				if (i<TL.length-1){
					bw.append(", ");
				}
			}

			bw.append("\t");
			for (int i=0;i<prevTL.length;i++){
				bw.append(HashNames.get(prevTL[i]));
				if (i<prevTL.length-1){
					bw.append(", ");
				}
			}
			bw.append("\r\n");
		}
		
	}

	private void getTopLevels(String cpt, HashMap<String, String> hash, HashMap<String, String[]> cptTL) {

		String lineDest;
		
		String[] destines;
		lineDest=hash.get(cpt);
		destines=lineDest.split(",");
		for (String cpt2:destines){
			if (cptTL.containsKey(cpt2)){
				String[] tLevels=cptTL.get(cpt2);
				for (String tLevel:tLevels){
					topLevel.add(tLevel);
				}
				continue;
			}
			if (cpt2.compareTo("138875005")==0){
				topLevel.add(cpt);
				continue;
			}
			getTopLevels(cpt2,hash,cptTL);
		}
		
	}

	private void createConceptSups(File snap,
			HashMap<String, String> hash) throws IOException {

		FileInputStream fis = new FileInputStream(snap	);
		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		BufferedReader br = new BufferedReader(isr);

		br.readLine();

		String line;
		String[] spl;
		while((line=br.readLine())!=null){
			spl=line.split("\t",-1);

			if (spl[2].compareTo("1")==0
					&& spl[7].compareTo("116680003")==0
					&& !inactCpt.contains(spl[5])){

				if (hash.containsKey(spl[4])){
					String dest=hash.get(spl[4]);
					dest+="," + spl[5];
					hash.put(spl[4],dest);
				}else{
					hash.put(spl[4],spl[5]);
				}
			}
		}

		br.close();
	}

}
