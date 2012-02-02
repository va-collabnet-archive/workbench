package org.ihtsdo.project.dataexport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.task.refset.members.RefsetUtilImpl;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.idgeneration.IdAssignmentBI;
import org.ihtsdo.idgeneration.IdAssignmentImpl;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.identifier.factory.RF2IdListGeneratorFactory;
import org.ihtsdo.rf2.identifier.mojo.Key;
import org.ihtsdo.rf2.identifier.mojo.RF2IdentifierFile;
import org.ihtsdo.rf2.identifier.mojo.SctIDParam;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportImpl;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst.FILE_TYPE;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

public class TranslationRF2Export extends RF2DataExport {

	private I_GetConceptData languageRefset;
	private I_GetConceptData sourceRefset;
	private SimpleDateFormat formatter;
	private int CONCEPT_RETIRED;
	private int LIMITED;
	private int FSN;
	private int PREFERRED;
	private I_GetConceptData snomedRoot;
	private BufferedWriter reportFileWriter;
	private BufferedWriter descFileWriter;
	private BufferedWriter langFileWriter;
	private String begEnd;
	private String sep;
	private Object snomedIntId;
	private RefsetUtilImpl rUtil;
	private I_GetConceptData moduleConcept;
	private String expFolder;
	private I_IntSet allowedDestRelTypes;
	private I_GetConceptData activeValue;
	private I_GetConceptData inactiveValue;
	private NidSetBI allSnomedStatus;
	private String moduleSCTID;
	private BufferedWriter bwi;
	private BufferedWriter bwl;
	private BufferedWriter bwd;
	private String refsetSCTID;
	private boolean Idgenerate;
	private boolean bCreateRefsetConcept;
	private boolean bCreateModuleConcept;
	private String previousRF2Folder;
	private File conceptTmpFile;
	private File descriptionTmpFile;
	private File relationshipTmpFile;
	private File statedRelationshipTmpFile;
	private File languageTmpFile;
	private File inactDescTmpFile;
	private File expFolderFile;
	private String previousReleaseDate;
	private String endpointURL;
	private String password;
	private String username;
	private Integer nspNr;
	private HashMap<String,File> hashIdMap;
	private String idMapPath;
	private boolean completeFSNNotTranslated;
	private String tgtLangCode;
	private File reportFile;

	public TranslationRF2Export(I_ConfigAceFrame releaseConfig,
			I_GetConceptData moduleConcept, Integer nspNr, String expFolder,
			File reportFile, I_GetConceptData languageRefset,
			String releaseDate, I_GetConceptData sourceRefset,
			boolean completeFSNNotTranslated, boolean Idgenerate,
			String previousRF2Folder,String previousReleaseDate,
			String endpointURL, String password, String username) throws Exception {


		super(releaseConfig,releaseDate,Terms.get().getActiveAceFrameConfig());

		this.releaseConfig=releaseConfig;
		this.sourceRefset=sourceRefset;
		this.moduleConcept=moduleConcept;
		this.expFolder=expFolder;
		this.Idgenerate=Idgenerate;
		this.nspNr=nspNr;
		this.reportFile=reportFile;
		this.previousRF2Folder=previousRF2Folder;
		this.previousReleaseDate=previousReleaseDate;
		this.baseConfig=Terms.get().getActiveAceFrameConfig();
		this.endpointURL=endpointURL;
		this.username=username;
		this.password=password;
		this.completeFSNNotTranslated=completeFSNNotTranslated;
		//		Terms.get().setActiveAceFrameConfig(releaseConfig);
		formatter=new SimpleDateFormat("yyyyMMdd");
		this.languageRefset=languageRefset;
		hashIdMap=new HashMap<String, File>();

		CONCEPT_RETIRED=SnomedMetadataRf2.CONCEPT_NON_CURRENT_RF2.getLenient().getNid();
		LIMITED=SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getNid();
		FSN=SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();
		//TODO change logic for detect preferred from language refset
		PREFERRED=SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid();
		snomedIntId = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());

		snomedRoot = Terms.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));

		allowedDestRelTypes =  Terms.get().newIntSet();
		allowedDestRelTypes.add(Terms.get().uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
		activeValue = Terms.get().getConcept(UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f"));
		inactiveValue = Terms.get().getConcept(UUID.fromString("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"));

		allSnomedStatus=getSnomedStatuses();
		//			String exportDescFile=	expFolder.trim() + File.separator + "sct2_Description_Ful"  + releaseDate + ".txt" ;
		//			String exportLangFile=expFolder.trim() + File.separator + "der1_SubsetMembers_"  + releaseDate + ".txt" ;

		reportFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile),"UTF8"));
		//			descFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportDescFile),"UTF8"));
		//			langFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportLangFile),"UTF8"));

		LanguageMembershipRefset tgtLangRefset=new LanguageMembershipRefset(languageRefset, baseConfig);
		tgtLangCode=tgtLangRefset.getLangCode(baseConfig);
		Set<? extends I_GetConceptData> promRefsets = Terms.get().getRefsetHelper(releaseConfig).getPromotionRefsetForRefset(languageRefset, baseConfig);
		I_GetConceptData promoRefset = promRefsets.iterator().next();
		if (promoRefset==null){
			reportFileWriter.append("The promotion refset concept for target language refset " + languageRefset + " doesn't exists." + "\r\n");
			throw new Exception("The promotion refset concept for target language refset " + languageRefset + " doesn't exists.");
		}else{

			//refset id and module id verify

			rUtil=new RefsetUtilImpl();
			bCreateRefsetConcept = false;
			refsetSCTID = rUtil.getSnomedId(languageRefset.getNid(),Terms.get()).toString();
			if (refsetSCTID==null){
				bCreateRefsetConcept=true;
			}else{
				try{
					Long.parseLong(refsetSCTID);
				}catch(NumberFormatException e){
					bCreateRefsetConcept=true;
				}
			}

			bCreateModuleConcept = false;
			moduleSCTID = rUtil.getSnomedId(moduleConcept.getNid(),Terms.get()).toString();
			if (moduleSCTID==null){
				bCreateModuleConcept=true;
			}else{
				try{
					Long.parseLong(moduleSCTID);
				}catch(NumberFormatException e){
					bCreateModuleConcept=true;
				}
			}
			expFolderFile=new File (expFolder);
			if (!expFolderFile.exists())
				expFolderFile.mkdirs();

			idMapPath=expFolderFile.getAbsolutePath() + "/idMap";
			
			// create concepts if need
			String PartitionID=null;
			if (nspNr==0)
				PartitionID="00";
			else
				PartitionID="10";

			IdAssignmentBI idAssignment=null;
			if (bCreateRefsetConcept){
				if (Idgenerate){
					//"http://mgr.servers.aceworkspace.net:50040/axis2/services/id_generator"
					idAssignment = new IdAssignmentImpl(endpointURL,username,password);

					Long newSctId = idAssignment.createSCTID(languageRefset.getUids().iterator().next(), nspNr, PartitionID, releaseDate, releaseDate, "");
					System.out.println("New refset SCTID: " + newSctId); 

					refsetSCTID=newSctId.toString();

				}else{
					refsetSCTID=languageRefset.getUids().iterator().next().toString();
				}
			}

			if (bCreateModuleConcept){
				if (Idgenerate){
					if (idAssignment==null)
						idAssignment = new IdAssignmentImpl(endpointURL,username,password);

					Long newSctId = idAssignment.createSCTID(moduleConcept.getUids().iterator().next(), nspNr, PartitionID, releaseDate, releaseDate, "");
					System.out.println("New module SCTID: " + newSctId); 

					moduleSCTID=newSctId.toString();
				}else{
					moduleSCTID=moduleConcept.getUids().iterator().next().toString();
				}
			}
			//open file and write headers

			File tmpFolder=new File (expFolderFile,"tmp");
			tmpFolder.mkdir();

			conceptTmpFile=null;
			descriptionTmpFile=null;
			relationshipTmpFile=null;
			statedRelationshipTmpFile=null;
			languageTmpFile=null;
			inactDescTmpFile=null;
			BufferedWriter bwc=null;
			bwd=null;
			BufferedWriter bwr=null;
			bwl=null;
			bwi=null;
			BufferedWriter bws=null;

			descriptionTmpFile=File.createTempFile("des", ".txt",tmpFolder);
			bwd = WriteUtil.createWriter(descriptionTmpFile.getAbsolutePath());
			writeHeader (bwd, getDescriptionHeader());

			if (bCreateRefsetConcept || bCreateModuleConcept){
				conceptTmpFile=File.createTempFile("con", ".txt",tmpFolder);
				bwc = WriteUtil.createWriter(conceptTmpFile.getAbsolutePath());
				writeHeader (bwc, getConceptHeader());


				relationshipTmpFile=File.createTempFile("rel", ".txt",tmpFolder);
				bwr = WriteUtil.createWriter(relationshipTmpFile.getAbsolutePath());
				writeHeader (bwr, getRelationshipHeader());


				statedRelationshipTmpFile=File.createTempFile("statedrel", ".txt",tmpFolder);
				bws = WriteUtil.createWriter(statedRelationshipTmpFile.getAbsolutePath());
				writeHeader (bws, getRelationshipHeader());
			}

			languageTmpFile=File.createTempFile("lan", ".txt",tmpFolder);
			bwl = WriteUtil.createWriter(languageTmpFile.getAbsolutePath());
			writeHeader (bwl, getLanguageHeader());


			inactDescTmpFile=File.createTempFile("inac", ".txt",tmpFolder);
			bwi = WriteUtil.createWriter(inactDescTmpFile.getAbsolutePath());
			writeHeader (bwi, getInactdescriptionHeader());

			//write raw export process
			if (bCreateRefsetConcept){
				exportConcept(languageRefset,refsetSCTID,moduleSCTID, bwc);
				HashMap<I_DescriptionTuple, RefexChronicleBI> descs=getDescriptions(languageRefset,languageRefset.getNid());
				if (descs.size()==0){

					reportFileWriter.append("The  refset concept " + languageRefset.toUserString()  + " has not descriptions on language refset.\r\n" );

				}else{
					boolean FSNExists=false;
					for (I_DescriptionTuple description:descs.keySet()){
						exportDescription(description, languageRefset,
								refsetSCTID, moduleSCTID, tgtLangCode,  bwd,reportFileWriter);

						if (description.getTypeNid()==FSN)
							FSNExists=true;
						I_ExtendByRef extension = Terms.get().getExtension(description.getNid());
						I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);

						exportLanguage(lastPart, description, languageRefset, moduleSCTID, refsetSCTID, bwl,reportFileWriter);
					}
					if (!FSNExists && completeFSNNotTranslated){
						//description will be exported with its decriptionid from source and moduleid from extension g
						descs=getDescriptions( languageRefset,sourceRefset.getNid()); 
						if (descs.size()>0){
							for (I_DescriptionTuple desc:descs.keySet()){
								if (desc.getTypeNid()==FSN && desc.getStatusNid()==activeValue.getNid()){

									exportDescription(desc,languageRefset,refsetSCTID,moduleSCTID
											, tgtLangCode,  bwd,reportFileWriter);

									I_ExtendByRef extension = Terms.get().getExtension(descs.get(desc).getNid());
									I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);

									exportLanguage(lastPart, desc, languageRefset, moduleSCTID, refsetSCTID, bwl,reportFileWriter);
								}
							}
						}
					}
				}
				if (exportStatedRelationship(languageRefset,refsetSCTID, moduleSCTID, bws)){
					if (!exportRelationship(languageRefset,refsetSCTID, moduleSCTID, bwr)){
						reportFileWriter.append("The refset concept " + languageRefset.toUserString()  + " has not inferred relationships.\r\n" );
					}
				}else{
					reportFileWriter.append("The refset concept " + languageRefset.toUserString()  + " has not stated relationships.\r\n" );
				}

			}
			if( bCreateModuleConcept){
				exportConcept(moduleConcept, moduleSCTID,moduleSCTID, bwc);
				HashMap<I_DescriptionTuple, RefexChronicleBI> descs=getDescriptions(moduleConcept,languageRefset.getNid());
				if (descs.size()==0){
					reportFileWriter.append("The  module concept " + moduleConcept.toUserString()  + " has not descriptions on language refset.\r\n" );

				}else{
					boolean FSNExists=false;
					for (I_DescriptionTuple description:descs.keySet()){
						exportDescription(description, moduleConcept,
								moduleSCTID, moduleSCTID, tgtLangCode,  bwd,reportFileWriter);

						if (description.getTypeNid()==FSN)
							FSNExists=true;
						I_ExtendByRef extension = Terms.get().getExtension(description.getNid());
						I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);

						exportLanguage(lastPart, description, moduleConcept, moduleSCTID, refsetSCTID, bwl,reportFileWriter);
					}
					if (!FSNExists && completeFSNNotTranslated){
						//description will be exported with its decriptionid from source and moduleid from extension g
						descs=getDescriptions( moduleConcept,sourceRefset.getNid()); 
						if (descs.size()>0){
							for (I_DescriptionTuple desc:descs.keySet()){
								if (desc.getTypeNid()==FSN && desc.getStatusNid()==activeValue.getNid()){

									exportDescription(desc,moduleConcept,refsetSCTID,moduleSCTID
											, tgtLangCode,  bwd,reportFileWriter);

									I_ExtendByRef extension = Terms.get().getExtension(descs.get(desc).getNid());
									I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);

									exportLanguage(lastPart, desc, moduleConcept, moduleSCTID, refsetSCTID, bwl,reportFileWriter);
								}
							}
						}
					}
				}
				if (exportStatedRelationship(moduleConcept, moduleSCTID, moduleSCTID,  bws)){
					if (!exportRelationship(moduleConcept, moduleSCTID, moduleSCTID, bwr)){
						reportFileWriter.append("The module concept " + moduleConcept.toUserString()  + " has not inferred relationships.\r\n" );
					}
				}else {
					reportFileWriter.append("The module concept " + moduleConcept.toUserString()  + " has not stated relationships.\r\n" );
				}

			}
			if (bCreateRefsetConcept || bCreateModuleConcept){
				bwc.close();
				bwr.close();
				bws.close();
				bwc=null;
				bwr=null;
				bws=null;
			}
			System.gc();


			//write final files


		}

	}

	@SuppressWarnings("unchecked")
	private HashMap<I_DescriptionTuple,RefexChronicleBI> getDescriptions(
			I_GetConceptData concept, int refsetNid) throws IOException {
		List<? extends I_DescriptionTuple> descriptions =  concept.getDescriptionTuples(allSnomedStatus, 
				allDescTypes, releaseConfig.getViewPositionSetReadOnly(), 
				Precedence.PATH, releaseConfig.getConflictResolutionStrategy());

		HashMap<I_DescriptionTuple,RefexChronicleBI> descTMap=new HashMap<I_DescriptionTuple,RefexChronicleBI>();
		for (I_DescriptionTuple descT:descriptions){
			for (RefexChronicleBI desc:descT.getAnnotations()){
				if (desc.getCollectionNid()==refsetNid){
					descTMap.put(descT, desc);
					break;
				}
			}
		}
		return descTMap ;
	}

	public void postExportProcess() throws Exception{


		File outFolder=new File (expFolderFile,"preAssign");
		if (!outFolder.exists())
			outFolder.mkdirs();
		File rf2FullFolder=new File( previousRF2Folder);
		if (!rf2FullFolder.exists())
			rf2FullFolder.mkdirs();

		RF2ArtifactPostExportImpl pExp=null;
		if (bCreateRefsetConcept || bCreateModuleConcept){
			pExp=new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_CONCEPT,  rf2FullFolder,
					conceptTmpFile, outFolder, new File (expFolder),
					previousReleaseDate, releaseDate);
			pExp.postProcess();
			pExp=null;

			System.gc();

			pExp=new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_RELATIONSHIP, rf2FullFolder,
					relationshipTmpFile, outFolder,  new File (expFolder),
					previousReleaseDate, releaseDate);
			pExp.postProcess();
			pExp=null;

			System.gc();

			pExp=new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_STATED_RELATIONSHIP,  rf2FullFolder,
					statedRelationshipTmpFile,  outFolder,  new File (expFolder),
					previousReleaseDate, releaseDate);
			pExp.postProcess();
			pExp=null;

			System.gc();

		}

		pExp=new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_DESCRIPTION, rf2FullFolder,
				descriptionTmpFile, outFolder,  new File (expFolder),
				previousReleaseDate, releaseDate);
		pExp.postProcess();
		pExp=null;

		System.gc();

		pExp=new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_LANGUAGE_REFSET ,rf2FullFolder,
				languageTmpFile, outFolder,  new File (expFolder),
				previousReleaseDate, releaseDate);
		pExp.postProcess();
		pExp=null;

		System.gc();

		pExp=new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_ATTRIBUTE_VALUE , rf2FullFolder,
				inactDescTmpFile, outFolder,  new File (expFolder),
				previousReleaseDate, releaseDate);
		pExp.postProcess();
		pExp=null;

		System.gc();

	}

	public void idAssignmentProcess(boolean idInsert) throws Exception{

		Rf2FileProvider fProv=new Rf2FileProvider();
		Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/idGenerator.xml");
		config.setReleaseDate(releaseDate);		
		config.setFlushCount(10000);
		config.setInvokeDroolRules("false");
		config.setFileExtension("txt");
		config.setUsername(username);
		config.setPassword(password);
		config.setEndPoint(endpointURL);
		config.setDestinationFolder(expFolder);
		ArrayList<RF2IdentifierFile> rf2Files=new ArrayList<RF2IdentifierFile>();


		File preFolder=new File (expFolderFile,"preAssign");
		if (!preFolder.exists())
			preFolder.mkdirs();
		File rf2FullPrevFolder=new File(preFolder.getAbsolutePath() + "/" + fProv.getFullOutputFolder() );
		if (!rf2FullPrevFolder.exists()){
			rf2FullPrevFolder.mkdir();
		}
		File rf2DeltaPrevFolder=new File(preFolder.getAbsolutePath() + "/" + fProv.getDeltaOutputFolder() );
		if (!rf2DeltaPrevFolder.exists()){
			rf2DeltaPrevFolder.mkdir();
		}
		File rf2SnapshotPrevFolder=new File(preFolder.getAbsolutePath() + "/" + fProv.getSnapshotOutputFolder() );
		if (!rf2SnapshotPrevFolder.exists()){
			rf2SnapshotPrevFolder.mkdir();
		}

		File rf2FullOutputFolder=new File(expFolderFile.getAbsolutePath() + "/" + fProv.getFullOutputFolder() );
		if (!rf2FullOutputFolder.exists()){
			rf2FullOutputFolder.mkdir();
		}
		File rf2DeltaOutputFolder=new File(expFolderFile.getAbsolutePath() + "/" + fProv.getDeltaOutputFolder() );
		if (!rf2DeltaOutputFolder.exists()){
			rf2DeltaOutputFolder.mkdir();
		}
		File rf2SnapshotOutputFolder=new File(expFolderFile.getAbsolutePath() + "/" + fProv.getSnapshotOutputFolder() );
		if (!rf2SnapshotOutputFolder.exists()){
			rf2SnapshotOutputFolder.mkdir();
		}

		File fullPrevFile;
		File deltaPrevFile;
		File snapshotPrevFile;

		File fullFinalFile;
		File deltaFinalFile;
		File snapshotFinalFile;

		

		/*	<RF2IdentifierFile>
		<fileName>${project.outputFolder}/Full/sct2_Concept_Full_INT_${project.releaseDate}.txt</fileName>
		<sctIdFileName>${project.destinationFolder}/Full/sct2_Concept_Full_INT_${project.releaseDate}.txt</sctIdFileName>
		<key>
			<keyOrdinals>
				<param>0</param>
				<param>4</param>
			</keyOrdinals>
			<effectiveTimeOrdinal>1</effectiveTimeOrdinal>
		</key>
		<sctidparam>
			<namespaceId>0</namespaceId>
			<partitionId>00</partitionId>
			<releaseId>20120131</releaseId>
			<executionId>Daily Build</executionId>
			<moduleId>Concept (Core Component)</moduleId>
			<componentType>Concept Full (Core Component)</componentType>
			<idSaveTolist>true</idSaveTolist>
			<idType>RF2_CONCEPT</idType>
			<idColumnIndex>0</idColumnIndex>
			<idMapFile>${project.destinationFolder}/IdMap/Concepts_Uuid_Id.txt</idMapFile>
		</sctidparam>
	</RF2IdentifierFile>
		 */

		Date et=new Date();
		String execId="WB-" + Terms.get().getActiveAceFrameConfig().getDbConfig().getUsername().substring(0,5) + "-" + ExportUtil.DATEFORMAT.format(et);
		int etOrd=1;
		ArrayList<String>ordin=null;

		String componentType="";
		String idSaveTolist="";
		String idType="";
		String idColumnIndex="";
		String idMapFile="";
		String partitionId="";
		RF2IdentifierFile idFile=null;
		if (bCreateRefsetConcept || bCreateModuleConcept){
			fullPrevFile=fProv.getFullOutputFile(rf2FullPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP ,releaseDate);
			deltaPrevFile=fProv.getDeltaOutputFile(rf2DeltaPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP,releaseDate, previousReleaseDate);
			snapshotPrevFile=fProv.getSnapshotOutputFile(rf2SnapshotPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP,releaseDate);

			fullFinalFile=fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP ,releaseDate);
			deltaFinalFile=fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP,releaseDate, previousReleaseDate);
			snapshotFinalFile=fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP,releaseDate);

			ordin=new ArrayList<String>();
			ordin.add("0");
			componentType="Relationship full";
			idSaveTolist="true";
			idType="RF2_RELATIONSHIP";
			idColumnIndex="0";
			idMapFile=idMapPath + "/Relationships_Uuid_Id.txt";

			hashIdMap.put("RF2_RELATIONSHIP", new File(idMapFile)); 
			partitionId="";
			if (nspNr==0)
				partitionId="02";
			else
				partitionId="12";

			idFile=getIdentifierFile(fullPrevFile, fullFinalFile, etOrd, ordin, execId,
					componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
					partitionId);
			rf2Files.add(idFile);


			ordin=new ArrayList<String>();
			ordin.add("0");
			componentType="Relationship delta";
			idSaveTolist="false";
			idType="RF2_RELATIONSHIP";
			idColumnIndex="-1";
			idMapFile="";
			partitionId="";
			if (nspNr==0)
				partitionId="02";
			else
				partitionId="12";

			idFile=getIdentifierFile(deltaPrevFile, deltaFinalFile, etOrd, ordin, execId,
					componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
					partitionId);

			rf2Files.add(idFile);

			ordin=new ArrayList<String>();
			ordin.add("0");
			componentType="Relationship snapshot";
			idSaveTolist="false";
			idType="RF2_RELATIONSHIP";
			idColumnIndex="-1";
			idMapFile="";
			partitionId="";
			if (nspNr==0)
				partitionId="02";
			else
				partitionId="12";

			idFile=getIdentifierFile(snapshotPrevFile, snapshotFinalFile, etOrd, ordin, execId,
					componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
					partitionId);

			rf2Files.add(idFile);

			fullPrevFile=fProv.getFullOutputFile(rf2FullPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP ,releaseDate);
			deltaPrevFile=fProv.getDeltaOutputFile(rf2DeltaPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP,releaseDate, previousReleaseDate);
			snapshotPrevFile=fProv.getSnapshotOutputFile(rf2SnapshotPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP,releaseDate);

			fullFinalFile=fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP ,releaseDate);
			deltaFinalFile=fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP,releaseDate, previousReleaseDate);
			snapshotFinalFile=fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_STATED_RELATIONSHIP,releaseDate);


			ordin=new ArrayList<String>();
			ordin.add("0");
			componentType="Stated Relationship full";
			idSaveTolist="true";
			idType="RF2_STATED_RELATIONSHIP";
			idColumnIndex="0";
			idMapFile=idMapPath + "/StatedRelationships_Uuid_Id.txt";

			hashIdMap.put("RF2_STATED_RELATIONSHIP", new File(idMapFile)); 
			partitionId="";
			if (nspNr==0)
				partitionId="02";
			else
				partitionId="12";

			idFile=getIdentifierFile(fullPrevFile, fullFinalFile, etOrd, ordin, execId,
					componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
					partitionId);
			rf2Files.add(idFile);


			ordin=new ArrayList<String>();
			ordin.add("0");
			componentType="Stated Relationship delta";
			idSaveTolist="false";
			idType="RF2_STATED_RELATIONSHIP";
			idColumnIndex="-1";
			idMapFile="";
			partitionId="";
			if (nspNr==0)
				partitionId="02";
			else
				partitionId="12";

			idFile=getIdentifierFile(deltaPrevFile, deltaFinalFile, etOrd, ordin, execId,
					componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
					partitionId);

			rf2Files.add(idFile);

			ordin=new ArrayList<String>();
			ordin.add("0");
			componentType="Stated Relationship snapshot";
			idSaveTolist="false";
			idType="RF2_STATED_RELATIONSHIP";
			idColumnIndex="-1";
			idMapFile="";
			partitionId="";
			if (nspNr==0)
				partitionId="02";
			else
				partitionId="12";

			idFile=getIdentifierFile(snapshotPrevFile, snapshotFinalFile, etOrd, ordin, execId,
					componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
					partitionId);

			rf2Files.add(idFile);
		}
		fullPrevFile=fProv.getFullOutputFile(rf2FullPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION,releaseDate);
		deltaPrevFile=fProv.getDeltaOutputFile(rf2DeltaPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION,releaseDate, previousReleaseDate);
		snapshotPrevFile=fProv.getSnapshotOutputFile(rf2SnapshotPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION,releaseDate);

		fullFinalFile=fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION ,releaseDate);
		deltaFinalFile=fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION,releaseDate, previousReleaseDate);
		snapshotFinalFile=fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_DESCRIPTION,releaseDate);

		ordin=new ArrayList<String>();
		ordin.add("0");
		componentType="Description full";
		idSaveTolist="true";
		idType="RF2_DESCRIPTION";
		idColumnIndex="0";
		idMapFile=idMapPath + "/Descriptions_Uuid_Id.txt";
		hashIdMap.put("RF2_DESCRIPTION", new File(idMapFile)); 
		partitionId="";
		if (nspNr==0)
			partitionId="01";
		else
			partitionId="11";

		idFile=getIdentifierFile(fullPrevFile, fullFinalFile, etOrd, ordin, execId,
				componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
				partitionId);
		rf2Files.add(idFile);


		ordin=new ArrayList<String>();
		ordin.add("0");
		componentType="Description delta";
		idSaveTolist="false";
		idType="RF2_DESCRIPTION";
		idColumnIndex="-1";
		idMapFile="";
		partitionId="";
		if (nspNr==0)
			partitionId="01";
		else
			partitionId="11";

		idFile=getIdentifierFile(deltaPrevFile, deltaFinalFile, etOrd, ordin, execId,
				componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
				partitionId);

		rf2Files.add(idFile);

		ordin=new ArrayList<String>();
		ordin.add("0");
		componentType="Description snapshot";
		idSaveTolist="false";
		idType="RF2_DESCRIPTION";
		idColumnIndex="-1";
		idMapFile="";
		partitionId="";
		if (nspNr==0)
			partitionId="01";
		else
			partitionId="11";

		idFile=getIdentifierFile(snapshotPrevFile, snapshotFinalFile, etOrd, ordin, execId,
				componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
				partitionId);

		rf2Files.add(idFile);

		fullPrevFile=fProv.getFullOutputFile(rf2FullPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET ,releaseDate);
		deltaPrevFile=fProv.getDeltaOutputFile(rf2DeltaPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET,releaseDate, previousReleaseDate);
		snapshotPrevFile=fProv.getSnapshotOutputFile(rf2SnapshotPrevFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET,releaseDate);

		fullFinalFile=fProv.getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET ,releaseDate);
		deltaFinalFile=fProv.getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET,releaseDate, previousReleaseDate);
		snapshotFinalFile=fProv.getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_LANGUAGE_REFSET,releaseDate);

		ordin=new ArrayList<String>();
		ordin.add("5");
		componentType="Language full";
		idSaveTolist="false";
		idType="RF2_DESCRIPTION";
		idColumnIndex="-1";
		idMapFile="";
		partitionId="";
		if (nspNr==0)
			partitionId="01";
		else
			partitionId="11";

		idFile=getIdentifierFile(fullPrevFile, fullFinalFile, etOrd, ordin, execId,
				componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
				partitionId);
		rf2Files.add(idFile);


		ordin=new ArrayList<String>();
		ordin.add("5");
		componentType="Language delta";
		idSaveTolist="false";
		idType="RF2_DESCRIPTION";
		idColumnIndex="-1";
		idMapFile="";
		partitionId="";
		if (nspNr==0)
			partitionId="01";
		else
			partitionId="11";

		idFile=getIdentifierFile(deltaPrevFile, deltaFinalFile, etOrd, ordin, execId,
				componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
				partitionId);

		rf2Files.add(idFile);

		ordin=new ArrayList<String>();
		ordin.add("5");
		componentType="Language snapshot";
		idSaveTolist="false";
		idType="RF2_DESCRIPTION";
		idColumnIndex="-1";
		idMapFile="";
		partitionId="";
		if (nspNr==0)
			partitionId="01";
		else
			partitionId="11";

		idFile=getIdentifierFile(snapshotPrevFile, snapshotFinalFile, etOrd, ordin, execId,
				componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
				partitionId);

		rf2Files.add(idFile);


		config.setRf2Files(rf2Files);
		//		config.setUpdateWbSctId(updateWbSctId);

		RF2IdListGeneratorFactory factory = new RF2IdListGeneratorFactory(config);
		factory.export();		

		factory=null;
		System.gc();

		if (idInsert){
			updateWb();
		}
	}

	private void updateWb() {

		try {
			if (bCreateRefsetConcept ){
				insertConceptPair(languageRefset.getUids().iterator().next().toString(), refsetSCTID);
			}
			if (bCreateModuleConcept){ 
				insertConceptPair(moduleConcept.getUids().iterator().next().toString(), moduleSCTID);
			} 

		}
		catch (IOException e) {
			e.printStackTrace();
		}
		for(String key:hashIdMap.keySet()){
			File fMap=hashIdMap.get(key);
			if (fMap.exists()){
				if (key.equals("RF2_CONCEPT")){
					insertConceptIds(fMap);
					break;
				}

				if (key.equals("RF2_DESCRIPTION")){
					insertDescriptionIds(fMap);
					break;
				}

				if (key.equals("RF2_RELATIONSHIP")){
					insertRelationshipIds(fMap);
					break;
				}

				if (key.equals("RF2_STATED_RELATIONSHIP")){
					insertRelationshipIds(fMap);
					break;
				}
			}
		}

	}

	private void insertRelationshipIds(File fMap) {
		FileInputStream ifis;
		try {
			ifis = new FileInputStream(fMap);
			InputStreamReader iisr = new InputStreamReader(ifis,"UTF-8");
			BufferedReader ibr = new BufferedReader(iisr);
			String line;
			String[] splitLine;
			UUID uuid;
			String sctid;
			while ((line=ibr.readLine())!=null){
				splitLine=line.split("\t",-1);
				uuid=UUID.fromString(splitLine[0]);
				sctid=splitLine[1];

				int componentNid = Terms.get().uuidToNative(uuid);
				I_Identify i_Identify = Terms.get().getId(componentNid);	
				I_GetConceptData commitedConcept = Terms.get().getConceptForNid(componentNid);
				i_Identify.addLongId(Long.parseLong(sctid), ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid(), activeValue.getNid(), releaseConfig.getViewPositionSet().iterator().next().getPath().getConceptNid(), Long.MAX_VALUE);
				Terms.get().addUncommitted(commitedConcept);

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void insertDescriptionIds(File fMap) {

		FileInputStream ifis;
		try {
			ifis = new FileInputStream(fMap);
			InputStreamReader iisr = new InputStreamReader(ifis,"UTF-8");
			BufferedReader ibr = new BufferedReader(iisr);
			String line;
			String[] splitLine;
			UUID uuid;
			String sctid;
			while ((line=ibr.readLine())!=null){
				splitLine=line.split("\t",-1);
				uuid=UUID.fromString(splitLine[0]);
				sctid=splitLine[1];

				I_Identify i_Identify = Terms.get().getId(uuid);
				i_Identify.addLongId(Long.parseLong(sctid), ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid(), activeValue.getNid(), releaseConfig.getViewPositionSet().iterator().next().getPath().getConceptNid(), Long.MAX_VALUE);
				I_DescriptionVersioned description = Terms.get().getDescription(Terms.get().uuidToNative(uuid));
				I_GetConceptData concept=Terms.get().getConcept (description.getConceptNid());
				Terms.get().addUncommitted(concept);

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void insertConceptIds(File fMap) {

		FileInputStream ifis;
		try {
			ifis = new FileInputStream(fMap);
			InputStreamReader iisr = new InputStreamReader(ifis,"UTF-8");
			BufferedReader ibr = new BufferedReader(iisr);
			String line;
			String[] splitLine;
			String uuid;
			String sctid;
			while ((line=ibr.readLine())!=null){
				splitLine=line.split("\t",-1);
				uuid=splitLine[0];
				sctid=splitLine[1];

				insertConceptPair(uuid,sctid);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void insertConceptPair(String uuid, String sctid) {
		I_GetConceptData concept;
		try {
			concept = Terms.get().getConcept(UUID.fromString(uuid));
			I_Identify i_Identify =concept.getIdentifier();

			i_Identify.addLongId(Long.parseLong(sctid), ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid(), activeValue.getNid(), releaseConfig.getViewPositionSet().iterator().next().getPath().getConceptNid(), Long.MAX_VALUE);

			Terms.get().addUncommitted(concept);
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private RF2IdentifierFile getIdentifierFile(File fullPrevFile, File fullFinalFile,
			int etOrd, ArrayList<String> ordin, String execId,
			String componentType, String idSaveTolist, String idType,
			String idColumnIndex, String idMapFile, String partitionId) {
		RF2IdentifierFile ident=new RF2IdentifierFile();
		Key key=new Key();
		key.effectiveTimeOrdinal=etOrd;
		key.keyOrdinals=ordin;
		ident.key=key;
		ident.fileName=fullPrevFile.getAbsolutePath();
		ident.sctIdFileName=fullFinalFile.getAbsolutePath();
		SctIDParam spar=new SctIDParam();
		spar.namespaceId= String.valueOf(nspNr);
		spar.partitionId=partitionId;
		spar.releaseId=releaseDate;
		spar.executionId=execId;
		spar.moduleId=moduleSCTID;
		spar.componentType=componentType;
		spar.idSaveTolist=idSaveTolist;
		spar.idType=idType;
		spar.idColumnIndex=idColumnIndex;
		spar.idMapFile=idMapFile;
		ident.sctidparam=spar;
		return ident;
	}

	private void writeHeader(BufferedWriter bw, String header) throws IOException {
		bw.append(header);
		bw.append("\r\n");

	}

	@Override
	public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher)
	throws Exception {
		
		I_GetConceptData concept = Terms.get().getConcept(cNid);
		Long sctid=getExistentConceptSCTID(concept);
		String uuid=concept.getUUIDs().iterator().next().toString();
//		if (uuid.equals("4be3f62e-28d5-3bb4-a424-9aa7856a1790")){
//			boolean bstop=true;
//		}
		if (sctid!=null){
			String conceptSCTID=String.valueOf(sctid);
			boolean FSNExists=false;
			HashMap<I_DescriptionTuple, RefexChronicleBI> descriptions=getDescriptions( concept,languageRefset.getNid()); 
			if (descriptions.size()>0){
				for (I_DescriptionTuple desc:descriptions.keySet()){

					if (desc.getTypeNid()==FSN)
						FSNExists=true;
					exportDescription(desc,concept,conceptSCTID,moduleSCTID
							, tgtLangCode,  bwd,reportFileWriter);

					exportInactDescription(desc, moduleSCTID, I_Constants.DESCRIPTION_INACTIVATION_REFSET_ID, bwi);

					I_ExtendByRef extension = Terms.get().getExtension(descriptions.get(desc).getNid());
					I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);

					exportLanguage(lastPart, desc, concept, moduleSCTID, refsetSCTID, bwl,reportFileWriter);
				}
				if (!FSNExists && completeFSNNotTranslated){
					//description will be exported with its decriptionid from source and moduleid from extension g
					descriptions=getDescriptions( concept,sourceRefset.getNid()); 
					if (descriptions.size()>0){
						for (I_DescriptionTuple desc:descriptions.keySet()){
							if (desc.getTypeNid()==FSN && desc.getStatusNid()==activeValue.getNid()){

								exportDescription(desc,concept,conceptSCTID,moduleSCTID
										, tgtLangCode,  bwd,reportFileWriter);

								I_ExtendByRef extension = Terms.get().getExtension(descriptions.get(desc).getNid());
								I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);

								exportLanguage(lastPart, desc, concept, moduleSCTID, refsetSCTID, bwl,reportFileWriter);
							}
						}
					}
				}
			}
		}else{
			HashMap<I_DescriptionTuple, RefexChronicleBI> descriptions=getDescriptions( concept,languageRefset.getNid()); 
			if (descriptions.size()>0){
				reportFileWriter.append("The concept "  + concept.toUserString() + " has not SCTID and it cannot be exported." + "\r\n");				
			}
		}
	}
	public I_ExtendByRefPartCid getLastLangExtensionPart(I_ExtendByRef extension) throws TerminologyException, IOException {
		long lastVersion = Long.MIN_VALUE;
		I_ExtendByRefPartCid extensionPart=null;
		for (I_ExtendByRefVersion loopTuple : extension.getTuples(allStatusSet,releaseConfig.getViewPositionSetReadOnly(),
				Precedence.PATH,releaseConfig.getConflictResolutionStrategy())) {

			if (loopTuple.getTime() >= lastVersion) {
				lastVersion = loopTuple.getTime();
				extensionPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
			}
		}
		return extensionPart;
	}
	class Rf2FileProvider extends RF2ArtifactPostExportAbst{

	}
	public void closeFiles(){

		try {
			bwd.close();
			bwi.close();
			bwl.close();
			bwd=null;
			bwi=null;
			bwl=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getLog() {
		try {
			reportFileWriter.flush();
		
			reportFileWriter.close();
			FileInputStream fis = new FileInputStream(reportFile);

			return readStream(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String readStream(FileInputStream is) {
		StringBuilder sb = new StringBuilder(1024);
		try {
			Reader r = new InputStreamReader(is, "UTF-8");
			int c = 0;
			while (c != -1) {
				c = r.read();
				sb.append((char) c);
			}
			r.close();
			System.gc();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return sb.toString();
	}

}