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
import java.util.TreeSet;

import org.ihtsdo.rf2.file.delta.snapshot.configuration.MetadataConfig;


public class GenerateRF1Relationships extends AbstractTask {

	private File snapshotSortedRelationshipFile;
	private File snapshotSortedRefinabilityFile;
	private File outputFile;

	private HashMap<String,String> RF1Refina;
	private HashMap<String,String> RF1charType;
	private File snapshotSortedAssociationFile;
	private HashMap<String, String> RF1Association;
	private String RF2_ISA;
	private File snapshotSortedQualifiersFile;
	private File RF1SortedConceptsFile;
	private TreeSet<String> causeRetCpt;
	private HashMap<String, String> causeRetCptStatusMap;

	public GenerateRF1Relationships(
			File snapshotSortedRelationshipFile,File snapshotSortedQualifiersFile,
			File snapshotSortedRefinabilityFile, File RF1SortedConceptsFile, 
			File snapshotSortedAssociationFile, File outputFile) {
		super();
		this.snapshotSortedRelationshipFile = snapshotSortedRelationshipFile;
		this.snapshotSortedQualifiersFile=snapshotSortedQualifiersFile;
		this.snapshotSortedRefinabilityFile = snapshotSortedRefinabilityFile;
		this.RF1SortedConceptsFile=RF1SortedConceptsFile;
		this.snapshotSortedAssociationFile = snapshotSortedAssociationFile;
		this.outputFile = outputFile;
	}



	public void execute(){

		try {
			long start1 = System.currentTimeMillis();

			getMetadataValues();
			
			String nextLine;
			String[] splittedLine;
			
			FileInputStream fis = new FileInputStream(RF1SortedConceptsFile	);
			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			BufferedReader br = new BufferedReader(isr);
			br.readLine();
			HashMap<String, String> hashCpt = new HashMap<String,String>();
			
			while ((nextLine=br.readLine())!=null){
				splittedLine=nextLine.split("\t");
				hashCpt.put(splittedLine[0], splittedLine[1]);
			}
			br.close();
			
			fis = new FileInputStream(snapshotSortedRelationshipFile	);
			isr = new InputStreamReader(fis,"UTF-8");
			br = new BufferedReader(isr);

			FileInputStream rfis = new FileInputStream(snapshotSortedRefinabilityFile	);
			InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
			BufferedReader rbr = new BufferedReader(risr);

			double lines = 0;
			br.readLine();
			rbr.readLine();

			if (outputFile.exists()){
				outputFile.delete();
			}
			FileOutputStream fos = new FileOutputStream( outputFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);

			bw.append("RelationshipId");
			bw.append("\t");
			bw.append("ConceptId1");
			bw.append("\t");
			bw.append("RelationshipType");
			bw.append("\t");
			bw.append("ConceptId2");
			bw.append("\t");
			bw.append("CharacteristicType");
			bw.append("\t");
			bw.append("Refinability");
			bw.append("\t");
			bw.append("RelationshipGroup");
			bw.append("\r\n");

			String c1="";
			String rid="";
			String rType="";
			String c2="";
			String rGroup="";
			String cType="";
			String refina;
			String refLine;
			String[] refSplittedLine=new String[7];
			refLine=rbr.readLine();
			if (refLine!=null){
				refSplittedLine=refLine.split("\t");
			}			

			refina="";
			String cptStatus;
			boolean activeIsa;
			while ((nextLine= br.readLine()) != null) {
				splittedLine = nextLine.split("\t");

				c1=splittedLine[4];
				rType=splittedLine[7];
				c2=splittedLine[5];
				activeIsa=false;
				if(splittedLine[2].compareTo("0")==0 && rType.compareTo(RF2_ISA)==0 && causeRetCpt.contains(c2)){
					cptStatus=hashCpt.get(c1);
					if (isInactiveStatusForDate(cptStatus,splittedLine[1])){
						activeIsa=matchReasonWithStatus(cptStatus,c2);
					}
				}
				
				
				if(splittedLine[2].compareTo("1")==0 || activeIsa){
					rid=splittedLine[0];
					rGroup=splittedLine[6];

					if (rType.compareTo(RF2_ISA)==0){
						refina="0";
						cType="0";
					}else{
						cType=RF1charType.get(splittedLine[8]);

						refina="1";
						if (refLine!=null){

							while ( rid.compareTo(refSplittedLine[5])>=0){
								if (rid.compareTo(refSplittedLine[5])==0){
									//								if (RF2_REFINABILITY_REFSETID.compareTo(refSplittedLine[4])==0){
									refina=RF1Refina.get( refSplittedLine[6]);
									//								}
								}
								refLine=rbr.readLine();
								if (refLine==null) break;
								refSplittedLine=refLine.split("\t");
							}
						}
					}
					bw.append(rid);
					bw.append("\t");
					bw.append(c1);
					bw.append("\t");
					bw.append(rType);
					bw.append("\t");
					bw.append(c2);
					bw.append("\t");
					bw.append(cType);
					bw.append("\t");
					bw.append(refina);
					bw.append("\t");
					bw.append(rGroup);
					bw.append("\r\n");
					lines++;

				}
			}
			br.close();
			rbr.close();
			hashCpt=null;
			System.gc();
			//Qualifiers

			fis = new FileInputStream(snapshotSortedQualifiersFile	);
			isr = new InputStreamReader(fis,"UTF-8");
			br = new BufferedReader(isr);

			rfis = new FileInputStream(snapshotSortedRefinabilityFile	);
			risr = new InputStreamReader(rfis,"UTF-8");
			rbr = new BufferedReader(risr);

			lines = 0;
			br.readLine();
			rbr.readLine();

			c1="";
			rid="";
			rType="";
			c2="";
			rGroup="";
			cType="";
			
			refSplittedLine=new String[7];
			refLine=rbr.readLine();
			if (refLine!=null){
				refSplittedLine=refLine.split("\t");
			}			

			refina="";
			while ((nextLine= br.readLine()) != null) {
				splittedLine = nextLine.split("\t");

				if(splittedLine[2].compareTo("1")==0){
					rid=splittedLine[0];
					c1=splittedLine[4];
					c2=splittedLine[5];
					rGroup=splittedLine[6];
					rType=splittedLine[7];

					cType=RF1charType.get(splittedLine[8]);

					refina="2";
					if (refLine!=null){

						while ( rid.compareTo(refSplittedLine[5])>=0){
							if (rid.compareTo(refSplittedLine[5])==0){
								//								if (RF2_REFINABILITY_REFSETID.compareTo(refSplittedLine[4])==0){
								refina=RF1Refina.get( refSplittedLine[6]);
								//								}
							}
							refLine=rbr.readLine();
							if (refLine==null) break;
							refSplittedLine=refLine.split("\t");
						}
					}
					bw.append(rid);
					bw.append("\t");
					bw.append(c1);
					bw.append("\t");
					bw.append(rType);
					bw.append("\t");
					bw.append(c2);
					bw.append("\t");
					bw.append(cType);
					bw.append("\t");
					bw.append(refina);
					bw.append("\t");
					bw.append(rGroup);
					bw.append("\r\n");
					lines++;

				}
			}
			br.close();
			rbr.close();
			
			
			// Associations

			fis = new FileInputStream(snapshotSortedAssociationFile	);
			isr = new InputStreamReader(fis,"UTF-8");
			br = new BufferedReader(isr);
			rType=null;
			rid=""; 
			rGroup="0";
			cType="2";
			refina="0";
			while ((nextLine= br.readLine()) != null) {
				splittedLine = nextLine.split("\t");

				if(splittedLine[2].compareTo("1")==0){
					//TO DO  Id =to be defined
					rType=RF1Association.get(splittedLine[4]);

					if (rType!=null){
						rid=splittedLine[0]; 
						c1=splittedLine[5];
						c2=splittedLine[6];
						bw.append(rid);
						bw.append("\t");
						bw.append(c1);
						bw.append("\t");
						bw.append(rType);
						bw.append("\t");
						bw.append(c2);
						bw.append("\t");
						bw.append(cType);
						bw.append("\t");
						bw.append(refina);
						bw.append("\t");
						bw.append(rGroup);
						bw.append("\r\n");

						lines++;
					}
				}
			}
			br.close();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	private boolean matchReasonWithStatus(String cptStatus, String c2) {
		return (causeRetCptStatusMap.get(c2).compareTo(cptStatus)==0);

	}



	private boolean isInactiveStatusForDate(String cptStatus, String key) {
		if (cptStatus.compareTo("0")==0)
			return false;
		if (cptStatus.compareTo("6")==0 && key.compareTo("20100131")<0)
			return false;
		
		return true;
	}



	private void getMetadataValues()  throws Exception {
		MetadataConfig config =new MetadataConfig();

		RF1Refina=config.getRF2RF1RefinaMap();
		RF1charType=config.getRF2RF1charTypeMap();
		RF1Association=config.getRF2RF1AssociationMap();
		RF2_ISA=config.getRF2_ISA_RELATIONSHIP();
		causeRetCpt=config.getRF1CauseInactiveConcept();
		causeRetCptStatusMap=config.getRF1CptRetiredCauseStatusMap();


	}
}
