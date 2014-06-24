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



public class GenerateRF1CptDescLangSubs extends AbstractTask {



	public GenerateRF1CptDescLangSubs(File snapshotSortedENLanguageFile, File snapshotSortedGBLanguageFile,
			File snapshotSortedDescriptionFile, File snapshotSortedConceptFile,
			File snapshotSortedSimpleMapFile, 
			File snapshotSortedConceptInactFile, File snapshotSortedDescriptionInactFile,
			File outputGBLanguageSubsetFile,
			File outputENLanguageSubsetFile, File outputDescriptionFile,
			File outputConceptFile) {
		super();
		this.snapshotSortedENLanguageFile = snapshotSortedENLanguageFile;
		this.snapshotSortedGBLanguageFile = snapshotSortedGBLanguageFile;
		this.snapshotSortedDescriptionFile = snapshotSortedDescriptionFile;
		this.snapshotSortedConceptFile = snapshotSortedConceptFile;
		this.snapshotSortedSimpleMapFile = snapshotSortedSimpleMapFile;
		this.outputGBLanguageSubsetFile = outputGBLanguageSubsetFile;
		this.outputENLanguageSubsetFile = outputENLanguageSubsetFile;
		this.outputDescriptionFile = outputDescriptionFile;
		this.snapshotSortedConceptInactFile=snapshotSortedConceptInactFile;
		this.snapshotSortedDescriptionInactFile=snapshotSortedDescriptionInactFile;
		this.outputConceptFile = outputConceptFile;
	}

	private File snapshotSortedENLanguageFile;
	private File snapshotSortedGBLanguageFile;
	private File snapshotSortedDescriptionFile;
	private File snapshotSortedConceptFile;
	private File snapshotSortedSimpleMapFile;
	private File snapshotSortedConceptInactFile;
	private File snapshotSortedDescriptionInactFile;

	private File outputGBLanguageSubsetFile;
	private File outputENLanguageSubsetFile;
	private File outputDescriptionFile;
	private File outputConceptFile;

	private String RF2_FSN;
	private String RF2_SYNONYM;
	private String RF2_PREFERRED;

	private String RF2_ENUS_REFSET;

	private String RF2_ICS_SIGNIFICANT;

	private String RF2_DEF_STATUS_PRIMITIVE;

	private String RF2_CTV3ID_REFSETID;
	private String RF2_SNOMEDID_REFSETID;

	private String RF1_FSN;
	private String RF1_SYNONYM;
	private String RF1_PREFERRED;
	private String RF1_SUBSETDEFINED;


	private String RF1_EN_SUBSET;
	private String RF1_GB_SUBSET;
	private String RF1_GBLANG_CODE;
	private String RF1_USLANG_CODE;
	private String RF1_ENLANG_CODE;
	private HashMap<String, String> RF2RF1InactStatMap;

	public void execute() {

		try {
			long start1 = System.currentTimeMillis();

			getMetadataValues();

			FileInputStream enfis = new FileInputStream(snapshotSortedENLanguageFile);
			InputStreamReader enisr = new InputStreamReader(enfis,"UTF-8");
			BufferedReader enbr = new BufferedReader(enisr);


			FileInputStream gbfis = new FileInputStream(snapshotSortedGBLanguageFile);
			InputStreamReader  gbisr = new InputStreamReader( gbfis,"UTF-8");
			BufferedReader  gbbr = new BufferedReader( gbisr);

			FileInputStream dfis = new FileInputStream(snapshotSortedDescriptionFile);
			InputStreamReader disr = new InputStreamReader(dfis,"UTF-8");
			BufferedReader dbr = new BufferedReader(disr);

			FileInputStream cfis = new FileInputStream(snapshotSortedConceptFile);
			InputStreamReader cisr = new InputStreamReader(cfis,"UTF-8");
			BufferedReader cbr = new BufferedReader(cisr);

			FileInputStream sfis = new FileInputStream(snapshotSortedSimpleMapFile);
			InputStreamReader sisr = new InputStreamReader(sfis,"UTF-8");
			BufferedReader sbr = new BufferedReader(sisr);

			FileInputStream icfis = new FileInputStream(snapshotSortedConceptInactFile);
			InputStreamReader icisr = new InputStreamReader(icfis,"UTF-8");
			BufferedReader icbr = new BufferedReader(icisr);

			FileInputStream idfis = new FileInputStream(snapshotSortedDescriptionInactFile);
			InputStreamReader idisr = new InputStreamReader(idfis,"UTF-8");
			BufferedReader idbr = new BufferedReader(idisr);


			enbr.readLine();
			gbbr.readLine();

			dbr.readLine();

			cbr.readLine();

			sbr.readLine();

			icbr.readLine();

			idbr.readLine();

			if (outputGBLanguageSubsetFile.exists()){
				outputGBLanguageSubsetFile.delete();
			}
			FileOutputStream gbfos = new FileOutputStream( outputGBLanguageSubsetFile);
			OutputStreamWriter gbosw = new OutputStreamWriter(gbfos,"UTF-8");
			BufferedWriter gbbw = new BufferedWriter(gbosw);

			gbbw.append("SubsetId");
			gbbw.append("\t");
			gbbw.append("MemberId");
			gbbw.append("\t");
			gbbw.append("MemberStatus");
			gbbw.append("\t");
			gbbw.append("LinkedId");
			gbbw.append("\r\n");

			if (outputENLanguageSubsetFile.exists()){
				outputENLanguageSubsetFile.delete();
			}
			FileOutputStream enfos = new FileOutputStream( outputENLanguageSubsetFile);
			OutputStreamWriter enosw = new OutputStreamWriter(enfos,"UTF-8");
			BufferedWriter enbw = new BufferedWriter(enosw);

			enbw.append("SubsetId");
			enbw.append("\t");
			enbw.append("MemberId");
			enbw.append("\t");
			enbw.append("MemberStatus");
			enbw.append("\t");
			enbw.append("LinkedId");
			enbw.append("\r\n");

			if (outputDescriptionFile.exists()){
				outputDescriptionFile.delete();
			}
			FileOutputStream dfos = new FileOutputStream( outputDescriptionFile);
			OutputStreamWriter dosw = new OutputStreamWriter(dfos,"UTF-8");
			BufferedWriter dbw = new BufferedWriter(dosw);

			dbw.append("DescriptionId");
			dbw.append("\t");
			dbw.append("DescriptionStatus");
			dbw.append("\t");
			dbw.append("ConceptId");
			dbw.append("\t");
			dbw.append("Term");
			dbw.append("\t");
			dbw.append("InitialCapitalStatus");
			dbw.append("\t");
			dbw.append("DescriptionType");
			dbw.append("\t");
			dbw.append("LanguageCode");
			dbw.append("\r\n");

			if (outputConceptFile.exists()){
				outputConceptFile.delete();
			}
			FileOutputStream cfos = new FileOutputStream( outputConceptFile);
			OutputStreamWriter cosw = new OutputStreamWriter(cfos,"UTF-8");
			BufferedWriter cbw = new BufferedWriter(cosw);

			cbw.append("ConceptId");
			cbw.append("\t");
			cbw.append("ConceptStatus");
			cbw.append("\t");
			cbw.append("FullySpecifiedName");
			cbw.append("\t");
			cbw.append("CTV3ID");
			cbw.append("\t");
			cbw.append("SNOMEDID");
			cbw.append("\t");
			cbw.append("IsPrimitive");
			cbw.append("\r\n");

			HashMap<String,String[]> hlang=new HashMap<String,String[]>();
			HashMap<String,String> hInactDes=new HashMap<String,String>();

			String enMemberId="";
			String gbMemberId="";
			String enLangStatus="";
			String gbLangStatus="";
			String enRF2Accept="";
			String gbRF2Accept="";
			String[] gbLangSplittedLine;
			String gbLangLine;
			String[] enLangSplittedLine;
			String enLangLine;
			String inactLine;
			String[] inactSplittedLine;
			String iMemberId="";
			String inactValue="";
			String iStatus = null;
			String inactCptStat="";
			String inactDescStat="";
			String icid="";
			String iCptValue="";

			String inCptLine;
			String[] inCptSplittedLine;

			int comp=0;
			int iComp=0;


			inCptLine=icbr.readLine();
			if (inCptLine!=null){
				//rf2 inactivation concept
				inCptSplittedLine=inCptLine.split("\t");
				inactCptStat=inCptSplittedLine[2];
				icid=inCptSplittedLine[5];
				iCptValue=inCptSplittedLine[6];
			}

			while ((inactLine=idbr.readLine())!=null){
				//rf2 inactivation description
				inactSplittedLine=inactLine.split("\t");
				inactDescStat=inactSplittedLine[2];
				if (inactDescStat.compareTo("1")==0){
					iMemberId=inactSplittedLine[5];
					inactValue=inactSplittedLine[6];
					hInactDes.put(iMemberId, inactValue);
				}
			}
			idbr.close();
			System.gc();

			gbLangLine=gbbr.readLine();
			if ( gbLangLine  != null){
				//rf2 gb language
				gbLangSplittedLine = gbLangLine.split("\t");
				gbLangStatus=gbLangSplittedLine[2];
				gbMemberId=gbLangSplittedLine[5];
				gbRF2Accept=gbLangSplittedLine[6];
			}
			while ((enLangLine=enbr.readLine())!=null){
				//rf2 en language
				enLangSplittedLine = enLangLine.split("\t");
				enLangStatus=enLangSplittedLine[2];
				enMemberId=enLangSplittedLine[5];

				enRF2Accept=enLangSplittedLine[6];
				if ( gbLangLine  != null){
					comp=gbMemberId.compareTo(enMemberId);
					if (comp<0){
						while (comp<0){
							hlang.put(gbMemberId, new String[]{gbLangStatus,gbRF2Accept,null,null});
							//rf2 gb language
							gbLangLine=gbbr.readLine();
							if (gbLangLine==null){
								comp=-1;
								break;
							}

							gbLangSplittedLine = gbLangLine.split("\t");
							gbLangStatus=gbLangSplittedLine[2];
							gbMemberId=gbLangSplittedLine[5];
							gbRF2Accept=gbLangSplittedLine[6];
							comp= gbMemberId.compareTo(enMemberId);
						}
					}
					if (comp==0){
						hlang.put(enMemberId, new String[]{gbLangStatus,gbRF2Accept,enLangStatus,enRF2Accept});

						gbLangLine=gbbr.readLine();
						if (gbLangLine!=null){

							gbLangSplittedLine = gbLangLine.split("\t");
							gbLangStatus=gbLangSplittedLine[2];
							gbMemberId=gbLangSplittedLine[5];
							gbRF2Accept=gbLangSplittedLine[6];

						}
					}else{
						hlang.put(enMemberId, new String[]{null,null,enLangStatus,enRF2Accept});

					}

				}else{
					hlang.put(enMemberId, new String[]{null,null,enLangStatus,enRF2Accept});

				}
			}
			if (gbLangLine  != null){
				hlang.put(gbMemberId, new String[]{gbLangStatus,gbRF2Accept,null,null});

				while (( gbLangLine = gbbr.readLine())!=null){
					//rf2 gb language

					gbLangSplittedLine = gbLangLine.split("\t");
					gbLangStatus=gbLangSplittedLine[2];
					gbMemberId=gbLangSplittedLine[5];
					gbRF2Accept=gbLangSplittedLine[6];

					hlang.put(gbMemberId, new String[]{gbLangStatus,gbRF2Accept,null,null});

				}
			}
			String cid="";
			String cStatus="";
			String defStat="";
			String[] conSplittedLine;
			String conLine;


			String did="";
			String dStatus="";
			String dConceptId="";
			String language="";
			String rf2Type="";
			String term="";
			String ics="";
			String[] desSplittedLine;
			String desLine;


			String membType="";
			Boolean difType=null;
			String fsnTerm="";
			String preFsn="";

			String simLine="";
			String[] simSplittedLine;
			String ctv3_code;
			String snomedId_code;
			String[] refsetMem;
			simLine=sbr.readLine();
			if (simLine==null){
				simSplittedLine=new String[7];
			}
			else{
				simSplittedLine=simLine.split("\t");
			}
			desLine= dbr.readLine();
			if (desLine != null) {
				//rf2 description
				desSplittedLine = desLine.split("\t");

				did=desSplittedLine[0];
				dStatus=desSplittedLine[2];
				dConceptId=desSplittedLine[4];
				language=desSplittedLine[5];
				rf2Type=desSplittedLine[6];
				term=desSplittedLine[7];
				ics=desSplittedLine[8];

				while ((conLine=cbr.readLine())!=null){
					//rf2 concept
					conSplittedLine = conLine.split("\t");
					cid=conSplittedLine[0];
					cStatus=conSplittedLine[2];
					defStat=conSplittedLine[4];
					fsnTerm="";
					preFsn="";
					//descriptions belong to concept
					while (cid.compareTo(dConceptId)==0) {

						//only types for RF1
						if (rf2Type.compareTo(RF2_FSN)==0 || rf2Type.compareTo(RF2_SYNONYM)==0){

							membType="";
							if (rf2Type.compareTo(RF2_FSN)==0){
								membType=RF1_FSN;
							}
							difType=false;
							//languages for description
							refsetMem=hlang.get(did);
							if (refsetMem!=null){
								gbLangStatus=refsetMem[0]==null? "":refsetMem[0];
								gbRF2Accept=refsetMem[1];
								enLangStatus=refsetMem[2]==null? "":refsetMem[2];
								enRF2Accept=refsetMem[3];
							}else{;
							enRF2Accept=null;
							enLangStatus="";
							gbRF2Accept=null;
							gbLangStatus="";

							}
							inactValue=hInactDes.get(did);
							if (enRF2Accept!=null){
								if (membType.compareTo(RF1_FSN)!=0){
									if (enRF2Accept.compareTo(RF2_PREFERRED)==0){
										membType=RF1_PREFERRED;
									}else{
										membType=RF1_SYNONYM;
									}
								}

								if (enLangStatus.compareTo("1")==0 
										&& dStatus.compareTo("1")==0
										&& cStatus.compareTo("1")==0){
									enbw.append(RF1_EN_SUBSET);
									enbw.append("\t");
									enbw.append(did);
									enbw.append("\t");
									enbw.append(membType);
									enbw.append("\t");
									enbw.append("");
									enbw.append("\r\n");
								}

							}

							if (gbRF2Accept!=null){
								if (membType.compareTo(RF1_FSN)!=0){
									if (gbRF2Accept.compareTo(RF2_PREFERRED)==0){
										if (membType.compareTo("")!=0 && membType.compareTo(RF1_PREFERRED)!=0){
											difType=true;
										}
										membType=RF1_PREFERRED;
									}else{
										if (membType.compareTo("")!=0 && membType.compareTo(RF1_SYNONYM)!=0){
											difType=true;
										}
										membType=RF1_SYNONYM;
									}
								}

								//belong to GB
								if (gbLangStatus.compareTo("1")==0 
										&& dStatus.compareTo("1")==0 &&
										cStatus.compareTo("1")==0){
									gbbw.append(RF1_GB_SUBSET);
									gbbw.append("\t");
									gbbw.append(did);
									gbbw.append("\t");
									gbbw.append(membType);
									gbbw.append("\t");
									gbbw.append("");
									gbbw.append("\r\n");
								}
							}

//														if (gbLangStatus.compareTo("1")==0 && enLangStatus.compareTo("1")==0){
//															language=RF1_ENLANG_CODE;
//														}else if (gbLangStatus.compareTo("")==0 || gbLangStatus.compareTo("0")==0){
//															if (enLangStatus.compareTo("0")==0 || enLangStatus.compareTo("")==0){
//																language=RF1_ENLANG_CODE;
//															}else{
//																language=RF1_USLANG_CODE;
//															}
//														}else if (enLangStatus.compareTo("0")==0 || enLangStatus.compareTo("")==0){
//															language=RF1_GBLANG_CODE;
//							
//														}else{
//															language=RF1_ENLANG_CODE;
//														}
//							if (gbLangStatus.compareTo("1")==0 && enLangStatus.compareTo("1")==0){
//								language=RF1_ENLANG_CODE;
//							}else if (gbLangStatus.compareTo("0")==0 && enLangStatus.compareTo("1")==0){
//								language=RF1_USLANG_CODE;
//							}else if (gbLangStatus.compareTo("")==0 && enLangStatus.compareTo("1")==0){
//								language=RF1_USLANG_CODE;
//							}else if (gbLangStatus.compareTo("1")==0 && enLangStatus.compareTo("0")==0){
//								language=RF1_GBLANG_CODE;
//							}else if (gbLangStatus.compareTo("1")==0 && enLangStatus.compareTo("")==0){
//								language=RF1_GBLANG_CODE;
//							}else if (gbLangStatus.compareTo("")==0 && enLangStatus.compareTo("0")==0){
//								language=RF1_USLANG_CODE;
//							}else if (gbLangStatus.compareTo("0")==0 && enLangStatus.compareTo("0")==0){
//								language=RF1_ENLANG_CODE;
//							}else if (gbLangStatus.compareTo("0")==0 && enLangStatus.compareTo("")==0){
//								language=RF1_GBLANG_CODE;
//							}else if (gbLangStatus.compareTo("")==0 && enLangStatus.compareTo("")==0){
//								language=RF1_ENLANG_CODE;
//							}
							if (gbLangStatus.compareTo("1")==0 && enLangStatus.compareTo("1")==0){
								language=RF1_ENLANG_CODE;
							}else if (gbLangStatus.compareTo("0")==0 && enLangStatus.compareTo("1")==0){
								language=RF1_USLANG_CODE;
							}else if (gbLangStatus.compareTo("")==0 && enLangStatus.compareTo("1")==0){
								language=RF1_USLANG_CODE;
							}else if (gbLangStatus.compareTo("1")==0 && enLangStatus.compareTo("0")==0){
								language=RF1_GBLANG_CODE;
							}else if (gbLangStatus.compareTo("1")==0 && enLangStatus.compareTo("")==0){
								language=RF1_GBLANG_CODE;
							}else if (gbLangStatus.compareTo("")==0 && enLangStatus.compareTo("0")==0){
								language=RF1_ENLANG_CODE;
							}else if (gbLangStatus.compareTo("0")==0 && enLangStatus.compareTo("0")==0){
								language=RF1_ENLANG_CODE;
							}else if (gbLangStatus.compareTo("0")==0 && enLangStatus.compareTo("")==0){
								language=RF1_GBLANG_CODE;
							}else if (gbLangStatus.compareTo("")==0 && enLangStatus.compareTo("")==0){
								language=RF1_ENLANG_CODE;
							}
//							if (gbLangStatus.compareTo("1")==0 && enLangStatus.compareTo("1")==0){
//								language=RF1_ENLANG_CODE;
//							}else if (gbLangStatus.compareTo("0")==0 && enLangStatus.compareTo("1")==0){
//								language=RF1_USLANG_CODE;
//							}else if (gbLangStatus.compareTo("")==0 && enLangStatus.compareTo("1")==0){
//								language=RF1_USLANG_CODE;
//							}else if (gbLangStatus.compareTo("1")==0 && enLangStatus.compareTo("0")==0){
//								language=RF1_GBLANG_CODE;
//							}else if (gbLangStatus.compareTo("1")==0 && enLangStatus.compareTo("")==0){
//								language=RF1_GBLANG_CODE;
//							}else if (gbLangStatus.compareTo("")==0 && enLangStatus.compareTo("0")==0){
//								language=RF1_ENLANG_CODE;
//							}else if (gbLangStatus.compareTo("0")==0 && enLangStatus.compareTo("0")==0){
//								language=RF1_USLANG_CODE;
//							}else if (gbLangStatus.compareTo("0")==0 && enLangStatus.compareTo("")==0){
//								language=RF1_GBLANG_CODE;
//							}else if (gbLangStatus.compareTo("")==0 && enLangStatus.compareTo("")==0){
//								language=RF1_ENLANG_CODE;
//							}
							dbw.append(did);
							dbw.append("\t");

							if (dStatus.compareTo("1")==0){
								if (cStatus.compareTo("0")==0){
									dStatus="8";
									//									dbw.append("8");
								}else{
									if (inactValue==null){
										dStatus="0";
										//										dbw.append("0");
									}else {

										dStatus=RF2RF1InactStatMap.get(inactValue);
										//										dbw.append(RF2RF1InactStatMap.get(inactValue));
									}

								}
							}else if (inactValue==null){

								dStatus="1";
								//								dbw.append("1");
							}else{
								dStatus=RF2RF1InactStatMap.get(inactValue);
								//								dbw.append(RF2RF1InactStatMap.get(inactValue));
							}
							dbw.append(dStatus);
							dbw.append("\t");

							dbw.append(dConceptId);
							dbw.append("\t");

							dbw.append(term);
							dbw.append("\t");

							dbw.append(ics.compareTo(RF2_ICS_SIGNIFICANT)!=0? "0":"1");
							dbw.append("\t");
							if (difType){
								dbw.append(RF1_SUBSETDEFINED);
							}else{
								if (membType.compareTo("")==0){
									dbw.append("2");
								}else{
									dbw.append(membType);
								}
							}
							dbw.append("\t");
							dbw.append(language);
							dbw.append("\r\n");
							if (membType.compareTo(RF1_FSN )==0 ){
								if ("068".indexOf(dStatus)>-1){
									fsnTerm=term;
								}else{
									preFsn=term;
								}
							}
						}
						desLine= dbr.readLine();
						if (desLine==null) break;
						//rf2 description
						desSplittedLine = desLine.split("\t");

						did=desSplittedLine[0];
						dStatus=desSplittedLine[2];
						dConceptId=desSplittedLine[4];
						language=desSplittedLine[5];
						rf2Type=desSplittedLine[6];
						term=desSplittedLine[7];
						ics=desSplittedLine[8];

					}
					if (fsnTerm.compareTo("")==0 && preFsn.compareTo("")==0){
						//TODO log concept without descriptions
					}else{
						snomedId_code="";
						ctv3_code="";
						if (simLine!=null){
							while ( cid.compareTo(simSplittedLine[5])>=0){
								if (cid.compareTo(simSplittedLine[5])==0){
									if (RF2_CTV3ID_REFSETID.compareTo(simSplittedLine[4])==0){
										ctv3_code=simSplittedLine[6];
									}
									if (RF2_SNOMEDID_REFSETID.compareTo(simSplittedLine[4])==0){
										snomedId_code=simSplittedLine[6];
									}
								}
								simLine=sbr.readLine();
								if (simLine==null) break;
								simSplittedLine=simLine.split("\t");
							}
						}

						iStatus=null;
						if (inCptLine!=null){
							iComp=icid.compareTo(cid);
							while (iComp<0){
								inCptLine=icbr.readLine();
								if (inCptLine!=null){
									//rf2 inactivation concept
									inCptSplittedLine=inCptLine.split("\t");
									inactCptStat=inCptSplittedLine[2];
									icid=inCptSplittedLine[5];
									iCptValue=inCptSplittedLine[6];
									iComp=icid.compareTo(cid);
								}else{
									break;
								}

							}
							if (iComp==0 && inactCptStat.compareTo("1")==0){
								iStatus=iCptValue;
							}
						}
						cbw.append(cid);
						cbw.append("\t");

						if (cStatus.compareTo("1")==0){
							if (iStatus==null){
								cbw.append("0");
							}else{
								cbw.append(RF2RF1InactStatMap.get(iStatus));
							}
						}else if (iStatus==null){

							cbw.append("1");
						}else{
							cbw.append(RF2RF1InactStatMap.get(iStatus));
						}
						//						cbw.append(cStatus.compareTo("0")!=0? "0":"1");
						cbw.append("\t");
						cbw.append(fsnTerm.compareTo("")==0? preFsn:fsnTerm);
						cbw.append("\t");
						cbw.append(ctv3_code);
						cbw.append("\t");
						cbw.append(snomedId_code);
						cbw.append("\t");
						cbw.append(defStat.compareTo(RF2_DEF_STATUS_PRIMITIVE)!=0? "0":"1");
						cbw.append("\r\n");
					}

				}
			}

			gbbw.close();
			enbw.close();
			dbw.close();
			cbw.close();
			enbr.close();
			gbbr.close();
			cbr.close();
			dbr.close();
			sbr.close();
			icbr.close();




			long end1 = System.currentTimeMillis();
			long elapsed1 = (end1 - start1);
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

	private void getMetadataValues() throws Exception {
		MetadataConfig config =new MetadataConfig();

		RF2_FSN=config.getRF2_FSN();
		RF2_SYNONYM=config.getRF2_SYNONYM();
		RF2_PREFERRED=config.getRF2_PREFERRED();

		RF2_ENUS_REFSET=config.getRF2_ENUS_REFSET();

		RF2_ICS_SIGNIFICANT=config.getRF2_ICS_SIGNIFICANT();

		RF2_DEF_STATUS_PRIMITIVE=config.getRF2_DEF_STATUS_PRIMITIVE();

		RF2_CTV3ID_REFSETID=config.getRF2_CTV3ID_REFSETID();
		RF2_SNOMEDID_REFSETID=config.getRF2_SNOMEDID_REFSETID();

		RF1_FSN=config.getRF1_FSN();
		RF1_SYNONYM=config.getRF1_SYNONYM();
		RF1_PREFERRED=config.getRF1_PREFERRED();
		RF1_SUBSETDEFINED=config.getRF1_SUBSET_DEFINED();


		RF1_EN_SUBSET=config.getRF1_EN_SUBSET();
		RF1_GB_SUBSET=config.getRF1_GB_SUBSET();
		RF1_GBLANG_CODE=config.getRF1_GBLANG_CODE();
		RF1_USLANG_CODE=config.getRF1_USLANG_CODE();
		RF1_ENLANG_CODE=config.getRF1_ENLANG_CODE();

		RF2RF1InactStatMap=config.getRF2RF1inactStatMap();
	}

}
