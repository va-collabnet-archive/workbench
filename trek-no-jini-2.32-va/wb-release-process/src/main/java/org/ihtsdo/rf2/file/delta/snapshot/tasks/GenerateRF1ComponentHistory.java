package org.ihtsdo.rf2.file.delta.snapshot.tasks;

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

import org.ihtsdo.rf2.file.delta.snapshot.configuration.MetadataConfig;



public class GenerateRF1ComponentHistory extends AbstractTask {

	private File sortedConceptFile;
	private File sortedDescriptionFile;
	private File outputFile;
	private HashMap<String, String> RF2RF1InactStatMap;
	private File snapshotSortedConceptInactFile;
	private File snapshotSortedDescriptionInactFile;
	private String date;

	public GenerateRF1ComponentHistory(File sortedConceptFile,
			File sortedDescriptionFile,File snapshotSortedConceptInactFile,
			File snapshotSortedDescriptionInactFile,String date, File outputFile) {
		super();
		this.sortedConceptFile = sortedConceptFile;
		this.sortedDescriptionFile = sortedDescriptionFile;
		this.snapshotSortedConceptInactFile=snapshotSortedConceptInactFile;
		this.snapshotSortedDescriptionInactFile=snapshotSortedDescriptionInactFile;
		this.date=date;
		this.outputFile = outputFile;
	}

	public void execute(){

		try {
			long start1 = System.currentTimeMillis();

			getMetadataValues();

			FileInputStream fis = new FileInputStream(sortedConceptFile);
			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			BufferedReader br = new BufferedReader(isr);

			double lines = 0;
			String nextLine;
			br.readLine();


			FileInputStream ifis = new FileInputStream(snapshotSortedConceptInactFile);
			InputStreamReader iisr = new InputStreamReader(ifis,"UTF-8");
			BufferedReader ibr = new BufferedReader(iisr);

			if (outputFile.exists()){
				outputFile.delete();
			}
			FileOutputStream fos = new FileOutputStream( outputFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);

			bw.append("ComponentId");
			bw.append("\t");
			bw.append("ReleaseVersion");
			bw.append("\t");
			bw.append("ChangeType");
			bw.append("\t");
			bw.append("Status");
			bw.append("\t");
			bw.append("Reason");
			bw.append("\r\n");

			//TODO reason
			String prevCompo="";
			String prevStatus="";
			String reason="";
			String changeType="";
			String[] splittedLine;

			String inactLine;
			String[] inactSplittedLine;
			String iMemberId="";
			String inactValue="";
			String iStatus = "";
			String iEffTime="";

			inactLine=ibr.readLine();
			if (inactLine!=null){
				//rf2 inactivation description
				inactSplittedLine=inactLine.split("\t");
				iEffTime=inactSplittedLine[1];
				iMemberId=inactSplittedLine[5];
				inactValue=inactSplittedLine[6];
			}
			int iComp=0;
			while ((nextLine= br.readLine()) != null) {
				splittedLine = nextLine.split("\t");

				if (splittedLine[1].compareTo(date)<=0){
					reason="";
					if(splittedLine[0].equals(prevCompo)){
						if (splittedLine[2].compareTo(prevStatus)!=0){
							changeType="1";
							prevStatus=splittedLine[2];
							reason="Status CHANGE";

						}else{
							changeType="2";
						}
					}else{
						changeType="0";
						prevCompo=splittedLine[0];
						prevStatus=splittedLine[2];
					}

					iStatus=splittedLine[2].compareTo("0")!=0? "0":"1";
					if (iStatus.compareTo("1")==0){
						if (inactLine!=null){
							iComp=iMemberId.compareTo(splittedLine[0]);
							while (iComp<0 || (iComp==0 && splittedLine[1].compareTo(iEffTime)>0)){
								inactLine=ibr.readLine();
								if (inactLine!=null){
									//rf2 inactivation description
									inactSplittedLine=inactLine.split("\t");
									iEffTime=inactSplittedLine[1];
									iMemberId=inactSplittedLine[5];
									inactValue=inactSplittedLine[6];
									iComp=iMemberId.compareTo(splittedLine[0]);
								}else{
									break;
								}

							}
							if (iComp==0 && splittedLine[1].compareTo(iEffTime)==0){
								iStatus=RF2RF1InactStatMap.get(inactValue);
							}
						}
					}
					bw.append(splittedLine[0]);
					bw.append("\t");
					bw.append(splittedLine[1]);
					bw.append("\t");
					bw.append(changeType);
					bw.append("\t");
					bw.append(iStatus);
					bw.append("\t");
					bw.append(reason);
					bw.append("\r\n");
					lines++;
				}
			}	
			br.close();
			ibr.close();


			//TODO reason - desc type change

			fis = new FileInputStream(sortedDescriptionFile);
			isr = new InputStreamReader(fis,"UTF-8");
			br = new BufferedReader(isr);

			br.readLine();

			ifis = new FileInputStream(snapshotSortedDescriptionInactFile);
			iisr = new InputStreamReader(ifis,"UTF-8");
			ibr = new BufferedReader(iisr);

			prevCompo="";
			prevStatus="";
			reason="";
			changeType="";

			String prevType="";
			String prevICS="";
			String prevLang="";

			inactLine=ibr.readLine();
			if (inactLine!=null){
				//rf2 inactivation description
				inactSplittedLine=inactLine.split("\t");
				iEffTime=inactSplittedLine[1];
				iMemberId=inactSplittedLine[5];
				inactValue=inactSplittedLine[6];
			}

			while ((nextLine= br.readLine()) != null) {
				splittedLine = nextLine.split("\t");
				if (splittedLine[1].compareTo(date)<=0){
					reason="";
					if(splittedLine[0].equals(prevCompo)){
						if (splittedLine[2].compareTo(prevStatus)!=0){
							changeType="1";
							prevStatus=splittedLine[2];
							reason="DESCRIPTIONSTATUS CHANGE";
						}else{
							changeType="2";
						}
						if ( prevType.compareTo(splittedLine[6])!=0){
							reason+=",DESCRIPTIONTYPE CHANGE";
						}

						if ( prevICS.compareTo(splittedLine[8])!=0){
							reason+=",INITIALCAPITALSTATUS CHANGE";
						}

						if ( prevLang.compareTo(splittedLine[5])!=0){
							reason+=",LANGUAGECODE CHANGE";
						}
						if (reason.startsWith(","))
							reason=reason.substring(1);
					}else{
						changeType="0";
						prevCompo=splittedLine[0];
						prevStatus=splittedLine[2];
						prevType=splittedLine[6];
						prevICS=splittedLine[8];
						prevLang=splittedLine[5];

					}
					iStatus=splittedLine[2].compareTo("0")!=0? "0":"1";
					if (iStatus.compareTo("1")==0){
						if (inactLine!=null){
							iComp=iMemberId.compareTo(splittedLine[0]);
							while (iComp<0 || (iComp==0 && splittedLine[1].compareTo(iEffTime)>0)){
								inactLine=ibr.readLine();
								if (inactLine!=null){
									//rf2 inactivation description
									inactSplittedLine=inactLine.split("\t");
									iEffTime=inactSplittedLine[1];
									iMemberId=inactSplittedLine[5];
									inactValue=inactSplittedLine[6];
									iComp=iMemberId.compareTo(splittedLine[0]);
								}else{
									break;
								}

							}
							if (iComp==0 && splittedLine[1].compareTo(iEffTime)==0){
								iStatus=RF2RF1InactStatMap.get(inactValue);
							}
						}
					}
					bw.append(splittedLine[0]);
					bw.append("\t");
					bw.append(splittedLine[1]);
					bw.append("\t");
					bw.append(changeType);
					bw.append("\t");
					bw.append(iStatus);
					bw.append("\t");
					bw.append(reason);
					bw.append("\r\n");
					lines++;
				}
			}		
			br.close();
			ibr.close();

			bw.close();
			long end1 = System.currentTimeMillis();
			long elapsed1 = (end1 - start1);
			System.out.println("Lines in output file  : " + lines);
			System.out.println("Completed in " + elapsed1 + " ms");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getMetadataValues() throws Exception {

		MetadataConfig config =new MetadataConfig();

		RF2RF1InactStatMap=config.getRF2RF1inactStatMap();
	}

}
