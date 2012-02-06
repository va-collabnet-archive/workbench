/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.rf2.identifier.factory.RF2IdListGeneratorFactory;
import org.ihtsdo.rf2.identifier.mojo.Key;
import org.ihtsdo.rf2.identifier.mojo.RF2IdentifierFile;
import org.ihtsdo.rf2.identifier.mojo.SctIDParam;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;
import org.ihtsdo.tk.api.KindOfCacheBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class RF1DataExport.
 */
public class RF1DataExport implements I_ProcessConcepts{
	
	/** The output desc file writer. */
	BufferedWriter outputDescFileWriter;
	
	/** The report file writer. */
	BufferedWriter reportFileWriter;
	
	/** The line count. */
	int lineCount;
	
	/** The refset concept. */
	I_GetConceptData refsetConcept;
	
	/** The refset uuid. */
	UUID refsetUUID;
	
	/** The refset helper. */
	I_HelpRefsets refsetHelper ;
	
	/** The term factory. */
	I_TermFactory termFactory;
	
	/** The id. */
	I_Identify id;
	
	/** The formatter. */
	SimpleDateFormat formatter;
	
	/** The output subs file writer. */
	private BufferedWriter outputSubsFileWriter;
	
	/** The FSN. */
	private int FSN;
	
	/** The PREFERRED. */
	private int PREFERRED;
	//	private RefsetUtilImpl rUtil;
	/** The sep. */
	private String sep;
	
	/** The beg end. */
	private String begEnd;
	
	/** The desc line count. */
	private long descLineCount;
	
	/** The subs line count. */
	private long subsLineCount;
	
	/** The export desc file. */
	private File exportDescFile;
	
	/** The export subs file. */
	private File exportSubsFile;
	
	/** The complete with core tems. */
	private boolean completeWithCoreTems;
	
	/** The promo refset. */
	private I_GetConceptData promoRefset;
	
	/** The release config. */
	private I_ConfigAceFrame releaseConfig;
	
	/** The snomed root. */
	private I_GetConceptData snomedRoot;
	
	/** The source refset. */
	private I_GetConceptData sourceRefset;
	
	/** The subset id. */
	private Long subsetId;
	
	/** The snomed int id. */
	private int snomedIntId;
	
	/** The active value. */
	private I_GetConceptData activeValue;
	
	/** The inactive value. */
	private I_GetConceptData inactiveValue;
	
	/** The all snomed status. */
	private NidSetBI allSnomedStatus;
	
	/** The allowed dest rel types. */
	private I_IntSet allowedDestRelTypes;
	
	/** The base config. */
	private I_ConfigAceFrame baseConfig;
	
	/** The all desc types. */
	private NidSetBI allDescTypes;
	
	/** The tgt lang code. */
	private String tgtLangCode;
	
	/** The all status set. */
	private I_IntSet allStatusSet;
	
	/** The hash id map. */
	private HashMap<String, File> hashIdMap;
	
	/** The release date. */
	private String releaseDate;
	
	/** The namespace. */
	private String namespace;
	
	/** The report file. */
	private File reportFile;
	
	/** The my static is a cache. */
	public static KindOfCacheBI myStaticIsACache;
	
	/** The my static is a cache refset spec. */
	public static KindOfCacheBI myStaticIsACacheRefsetSpec;
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(RF1DataExport.class);

	/**
	 * Instantiates a new r f1 data export.
	 *
	 * @param config the config
	 * @param exportDescFile the export desc file
	 * @param exportSubsFile the export subs file
	 * @param reportFile the report file
	 * @param refsetConcept the refset concept
	 * @param newSctId the new sct id
	 * @param sourceRefset the source refset
	 * @param completeWithCoreTems the complete with core tems
	 * @throws Exception the exception
	 */
	public RF1DataExport(I_ConfigAceFrame config ,File exportDescFile, 
			File exportSubsFile, File reportFile, I_GetConceptData refsetConcept, 
			Long newSctId, I_GetConceptData sourceRefset, boolean completeWithCoreTems) throws Exception{
		termFactory = Terms.get();
		this.releaseConfig=config;
		this.baseConfig=Terms.get().getActiveAceFrameConfig();
		this.sourceRefset=sourceRefset;
		//		termFactory.setActiveAceFrameConfig(config);
		formatter=new SimpleDateFormat("yyyyMMdd");
		this.exportDescFile=exportDescFile;
		this.exportSubsFile=exportSubsFile;
		this.refsetConcept=refsetConcept;
		this.subsetId=newSctId;
		this.completeWithCoreTems=completeWithCoreTems;
		this.reportFile=reportFile;
		try {
			FSN=SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();
			//TODO change logic for detect preferred from language refset
			PREFERRED=SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid();
			activeValue = Terms.get().getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
			inactiveValue = Terms.get().getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());

			allowedDestRelTypes =  Terms.get().newIntSet();
			allowedDestRelTypes.add(Terms.get().uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
			snomedRoot = Terms.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));

			allDescTypes=getAllDescTypes();
			snomedIntId = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());
			reportFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile),"UTF8"));
			outputDescFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportDescFile),"UTF8"));
			outputSubsFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportSubsFile),"UTF8"));

			allSnomedStatus=getSnomedStatuses();
			LanguageMembershipRefset tgtLangRefset=new LanguageMembershipRefset(refsetConcept, releaseConfig);
			tgtLangCode=tgtLangRefset.getLangCode(releaseConfig);

			this.allStatusSet=Terms.get().newIntSet();
			this.allStatusSet.addAll(allSnomedStatus.getSetValues());
			Set<? extends I_GetConceptData> promRefsets = termFactory.getRefsetHelper(config).getPromotionRefsetForRefset(refsetConcept, config);
			promoRefset = promRefsets.iterator().next();
			if (promoRefset==null){
				reportFileWriter.append("The promotion refset concept for target language refset " + refsetConcept + " doesn't exists." + "\r\n");
				throw new Exception("The promotion refset concept for target language refset " + refsetConcept + " doesn't exists.");
			}else{

				sep="\t";
				begEnd="";

				outputDescFileWriter.append(begEnd);
				outputDescFileWriter.append("DescriptionId");
				outputDescFileWriter.append(sep);
				outputDescFileWriter.append("DescriptionStatus");
				outputDescFileWriter.append(sep);
				outputDescFileWriter.append("ConceptId");
				outputDescFileWriter.append(sep);
				outputDescFileWriter.append("Term");
				outputDescFileWriter.append(sep);
				outputDescFileWriter.append("InitialCapitalStatus");
				outputDescFileWriter.append(sep);
				outputDescFileWriter.append("DescriptionType");
				outputDescFileWriter.append(sep);
				outputDescFileWriter.append("LanguageCode");
				outputDescFileWriter.append(begEnd + "\r\n");


				outputSubsFileWriter.append(begEnd);
				outputSubsFileWriter.append("SubsetId");
				outputSubsFileWriter.append(sep);
				outputSubsFileWriter.append("MemberId");
				outputSubsFileWriter.append(sep);
				outputSubsFileWriter.append("MemberStatus");
				outputSubsFileWriter.append(sep);
				outputSubsFileWriter.append("LinkedId");
				outputSubsFileWriter.append(begEnd + "\r\n");

				descLineCount = 0l;
				subsLineCount = 0l;

				//				myStaticIsACache = Ts.get().getCache(config.getViewCoordinate());


				//				rUtil=new RefsetUtilImpl();
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the all desc types.
	 *
	 * @return the all desc types
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NidSetBI getAllDescTypes() throws TerminologyException, IOException {
		NidSetBI allDescTypes = new NidSet();
		allDescTypes.add(Terms.get().uuidToNative(UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf")));
		allDescTypes.add(Terms.get().uuidToNative(UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2")));
		allDescTypes.add(Terms.get().uuidToNative(UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09")));
		return allDescTypes;
	}
	
	/**
	 * Gets the snomed statuses.
	 *
	 * @return the snomed statuses
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NidSetBI getSnomedStatuses() throws TerminologyException, IOException {
		NidSetBI allStatuses = new NidSet();
		Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();

		allStatuses.add(activeValue.getNid());
		descendants = getDescendants(descendants, activeValue );
		for (I_GetConceptData loopConcept : descendants) {
			allStatuses.add(loopConcept.getNid());
		}		
		allStatuses.add(inactiveValue.getNid());
		descendants = getDescendants(descendants, inactiveValue );
		for (I_GetConceptData loopConcept : descendants) {
			allStatuses.add(loopConcept.getNid());
		}
		return allStatuses;
	}
	
	/**
	 * Gets the results.
	 *
	 * @return the results
	 */
	public Long[] getResults() {
		return new Long[]{descLineCount,subsLineCount};
	}

	/**
	 * Gets the descendants.
	 *
	 * @param descendants the descendants
	 * @param concept the concept
	 * @return the descendants
	 */
	public  Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			childrenSet.addAll(concept.getDestRelOrigins(baseConfig.getAllowedStatus(), allowedDestRelTypes, 
					baseConfig.getViewPositionSetReadOnly(), baseConfig.getPrecedence(), baseConfig.getConflictResolutionStrategy()));
			descendants.addAll(childrenSet);
			for (I_GetConceptData loopConcept : childrenSet) {
				descendants = getDescendants(descendants, loopConcept);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return descendants;
	}
	
	/**
	 * Gets the descriptions.
	 *
	 * @param concept the concept
	 * @param refsetNid the refset nid
	 * @return the descriptions
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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
	
	/**
	 * Export description and subset.
	 *
	 * @param description the description
	 * @param languageExtension the language extension
	 * @param concept the concept
	 * @param conceptSCTID the concept sctid
	 */
	public void exportDescriptionAndSubset(I_DescriptionTuple description,I_ExtendByRefPartCid languageExtension, I_GetConceptData concept,
			String conceptSCTID) {
		String descriptionid=null ;
		String active = "";
		String dType="";
		String languageCode="";

		try {

			String sDescType = ExportUtil.getSnomedDescriptionType(description.getTypeNid());
			if (!sDescType.equals("4") ) { 
				int typeId = description.getTypeNid();

				Long descSCTID=getExistentDescriptionSCTID(description);
				languageCode=description.getLang();
				if (descSCTID==null && languageCode!=tgtLangCode){
					reportFileWriter.append("The description " + description.getText() + "- of concept SCTID#" + conceptSCTID  + " was not exported, it has not SCTID and it has different language code (" + languageCode + ")\r\n" );
					return;
				}
				if (descSCTID!=null){
					descriptionid=String.valueOf(descSCTID);
				}else if ((descSCTID==null || descSCTID==0) && active.equals("1")){
					descriptionid=description.getUUIDs().iterator().next().toString();
				}

				String term = description.getText();
				if (term!=null ){
					if (term.indexOf("\t")>-1){
						term=term.replaceAll("\t", "");
					}
					if (term.indexOf("\r")>-1){
						term=term.replaceAll("\r", "");
					}
					if (term.indexOf("\n")>-1){
						term=term.replaceAll("\n", "");
					}
					term=StringEscapeUtils.unescapeHtml(term);

				}
				int acceptId = languageExtension.getC1id();
				if (typeId==FSN){
					dType="3";
				}else{
					if (acceptId==PREFERRED){
						dType="1";
					}else{
						dType="2";
					}
				}
				String descriptionstatus = ExportUtil.getStatusType(description.getStatusNid());

				if ((descriptionid==null || descriptionid.equals("") ) && !descriptionstatus.equals("0")){
					reportFileWriter.append("The description " + description.getText() + "- of concept SCTID#" + conceptSCTID  + " was not exported, it has inactive status and it has not SCTID.\r\n" );

					logger.info("Unplublished Retired Description: " + description.getUUIDs().iterator().next().toString());
				}else{

					String ics= description.isInitialCaseSignificant()? "1":"0";
					outputDescFileWriter.append(begEnd + descriptionid + sep + descriptionstatus + sep + conceptSCTID + sep + term + sep + ics + sep + dType + sep + languageCode + begEnd + "\r\n");
					descLineCount++;

					int extensionStatusId = languageExtension.getStatusNid();
					if(extensionStatusId==activeValue.getNid()){
						if (!descriptionstatus.equals("0")){ 
							reportFileWriter.append("The description " + description.getText() + "- of concept " + concept.toUserString()  + " has inactive status and it has active reference on language refset.\r\n" );
						}
						outputSubsFileWriter.append(begEnd + subsetId + sep + descriptionid + sep + dType + sep + begEnd + "\r\n");

						subsLineCount++;
					}
				}

			}
		}catch (NullPointerException ne) {
			ne.printStackTrace();
			logger.error("NullPointerException: " + ne.getMessage());
			logger.error(" NullPointerException " + conceptSCTID);
			logger.error(" NullPointerException " + descriptionid);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("IOExceptions: " + e.getMessage());
			logger.error("IOExceptions: " + conceptSCTID);
			logger.error("IOExceptions: " + descriptionid);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exceptions in exportDescription: " + e.getMessage());
			logger.error("Exceptions in exportDescription: " +conceptSCTID);
			logger.error("Exceptions in exportDescription: " +descriptionid);
		}
	}
	//	private boolean writeTerms(I_GetConceptData concept, I_GetConceptData refset) {
	//		//		List<ContextualizedDescription> descriptions;
	//		boolean bwrite=false;
	//		String languageCode="";
	//		try {
	//			Long sctid=getExistentConceptSCTID(concept);
	//
	//			if (sctid!=null){
	//				String conceptSCTID=String.valueOf(sctid);
	//
	//				HashMap<I_DescriptionTuple, RefexChronicleBI> descriptions=getDescriptions( concept,refset.getNid()); 
	//				if (descriptions.size()>0){
	//					boolean FSNExists=false;
	//					for (I_DescriptionTuple cdescription:descriptions.keySet()){
	//
	//						if (cdescription.getTypeNid()==FSN)
	//							FSNExists=true;
	//						//			for (I_ContextualizeDescription cdescription : descriptions) {
	//						//				if (cdescription.getLanguageExtension()!=null){
	//						Long descSCTID=getExistentDescriptionSCTID(cdescription);
	//						languageCode=cdescription.getLang();
	//						if (descSCTID==null && languageCode!=tgtLangCode){
	//							reportFileWriter.append("The description " + cdescription.getText() + "- of concept SCTID#" + conceptId  + " was not exported, it has not SCTID and it has different language code (" + languageCode + ")\r\n" );
	//							return;
	//						}
	//						if (descSCTID!=null){
	//							descriptionid=String.valueOf(descSCTID);
	//						}else if ((descSCTID==null || descSCTID==0) && active.equals("1")){
	//							descriptionid=description.getUUIDs().iterator().next().toString();
	//						}
	//
	//						String term = description.getText();
	//						if (term!=null ){
	//							if (term.indexOf("\t")>-1){
	//								term=term.replaceAll("\t", "");
	//							}
	//							if (term.indexOf("\r")>-1){
	//								term=term.replaceAll("\r", "");
	//							}
	//							if (term.indexOf("\n")>-1){
	//								term=term.replaceAll("\n", "");
	//							}
	//							term=StringEscapeUtils.unescapeHtml(term);
	//
	//						}
	//						String descriptionstatus = ExportUtil.getStatusType(description.getStatusNid());
	//
	//						String did=rUtil.getSnomedId(cdescription.getDescId(),termFactory).toString();
	//						try{
	//							Long.parseLong(did);
	//						}catch(NumberFormatException e){
	//							did=cdescription.getUUIDs().iterator().next().toString();
	//							reportFileWriter.append("The description " + did + " has not Snomed Description ID, It will be replaced with its UUID." + "\r\n");
	//
	//						}
	//						String dStatus=ExportUtil.getStatusType(cdescription.getDescriptionStatusId());
	//
	//
	//						boolean bstop;
	//
	//						if (	dStatus.equals("8")){
	//							bstop=true;
	//						}else if (!dStatus.equals("0")){
	//							bstop=true;
	//						}
	//
	//
	//						String lang=cdescription.getLang();
	//
	//						int typeId = cdescription.getTypeNid();
	//						String dType="";
	//						if (typeId==FSN){
	//							dType="3";
	//						}else{
	//							int acceptId=cdescription.getAcceptabilityId();
	//							if (acceptId==PREFERRED){
	//								dType="1";
	//							}else{
	//								dType="2";
	//							}
	//						}
	//						String term=cdescription.toString();
	//
	//						String ics= cdescription.isInitialCaseSignificant()? "1":"0";
	//
	//
	//						outputDescFileWriter.append(begEnd + did + sep + dStatus + sep + conceptId + sep + term + sep + ics + sep + dType + sep + lang + begEnd + "\r\n");
	//
	//						descLineCount++;
	//
	//						if (dStatus=="0"){
	//							outputSubsFileWriter.append(begEnd + subsetId + sep + did + sep + dType + sep + begEnd + "\r\n");
	//
	//							subsLineCount++;
	//						}
	//						bwrite=true;
	//					}
	//
	//				}else{
	//					HashMap<I_DescriptionTuple, RefexChronicleBI> descriptions=getDescriptions( concept,languageRefset.getNid()); 
	//					if (descriptions.size()>0){
	//						reportFileWriter.append("The concept "  + concept.toUserString() + " has not SCTID and it cannot be exported." + "\r\n");				
	//					}
	//				}
	//			} catch (TerminologyException e) {
	//				e.printStackTrace();
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//			} catch (Exception e) {
	//				e.printStackTrace();
	//			}
	//			return bwrite;
	//		}

	/**
	 * Gets the existent concept sctid.
	 *
	 * @param concept the concept
	 * @return the existent concept sctid
	 */
	public Long getExistentConceptSCTID(I_GetConceptData concept){
		Long sctid = null;
		try {
			Collection<? extends IdBI> allIds = concept.getAllIds();
			if (allIds==null)
				return null;
			for (IdBI loopId : allIds) {
				if (loopId.getAuthorityNid() == snomedIntId) {
					sctid = (Long)loopId.getDenotation();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sctid;
	}

	/**
	 * Gets the last lang extension part.
	 *
	 * @param extension the extension
	 * @return the last lang extension part
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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
	
	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api.I_GetConceptData)
	 */
	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {
		Long sctid=getExistentConceptSCTID(concept);

		if (sctid!=null){
			String conceptSCTID=String.valueOf(sctid);
			boolean FSNExists=false;
			HashMap<I_DescriptionTuple, RefexChronicleBI> descriptions=getDescriptions( concept,refsetConcept.getNid()); 
			if (descriptions.size()>0){
				for (I_DescriptionTuple desc:descriptions.keySet()){

					if (desc.getTypeNid()==FSN)
						FSNExists=true;
					I_ExtendByRef extension = Terms.get().getExtension(descriptions.get(desc).getNid());
					I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);
					exportDescriptionAndSubset(desc,lastPart,concept,conceptSCTID);

				}
				if (!FSNExists && completeWithCoreTems){
					//description will be exported with its decriptionid from source and moduleid from extension g
					descriptions=getDescriptions( concept,sourceRefset.getNid()); 
					if (descriptions.size()>0){
						for (I_DescriptionTuple desc:descriptions.keySet()){
							if (desc.getTypeNid()==FSN && desc.getStatusNid()==activeValue.getNid()){

								I_ExtendByRef extension = Terms.get().getExtension(descriptions.get(desc).getNid());
								I_ExtendByRefPartCid lastPart = getLastLangExtensionPart(extension);
								exportDescriptionAndSubset(desc,lastPart,concept,conceptSCTID);

							}
						}
					}
				}
			}
		}else{
			HashMap<I_DescriptionTuple, RefexChronicleBI> descriptions=getDescriptions( concept,refsetConcept.getNid()); 
			if (descriptions.size()>0){
				reportFileWriter.append("The concept "  + concept.toUserString() + " has not SCTID and it cannot be exported." + "\r\n");				
			}
		}
	}

	/**
	 * Gets the existent description sctid.
	 *
	 * @param description the description
	 * @return the existent description sctid
	 */
	public Long getExistentDescriptionSCTID(I_DescriptionTuple description){
		Long sctid = null;
		try {
			Collection<? extends IdBI> allIds = description.getAllIds();
			if (allIds==null)
				return null;
			for (IdBI loopId : allIds) {
				if (loopId.getAuthorityNid() == snomedIntId) {
					sctid = (Long)loopId.getDenotation();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sctid;
	}
	
	/**
	 * Close files.
	 */
	public void closeFiles(){
		try {
			
			outputDescFileWriter.flush();
			outputDescFileWriter.close();
			outputSubsFileWriter.flush();
			outputSubsFileWriter.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Id assignment process.
	 *
	 * @param releaseDate the release date
	 * @param idInsert the id insert
	 * @param username the username
	 * @param password the password
	 * @param endpointURL the endpoint url
	 * @param namespace the namespace
	 * @throws Exception the exception
	 */
	public void idAssignmentProcess(String releaseDate,boolean idInsert, String username, String password, String endpointURL,String namespace) throws Exception{

		this.releaseDate=releaseDate;
		this.namespace=namespace;
		String expFolder=exportDescFile.getParent();

		Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/idGenerator.xml");
		config.setReleaseDate(releaseDate);		
		config.setFlushCount(10000);
		config.setInvokeDroolRules("false");
		config.setFileExtension("txt");
		config.setUsername(username);
		config.setPassword(password);
		config.setEndPoint(endpointURL);
		config.setDestinationFolder(expFolder);
		ArrayList<RF2IdentifierFile> rf1Files=new ArrayList<RF2IdentifierFile>();

		Integer nspNr=Integer.parseInt(namespace);

		hashIdMap=new HashMap<String, File>();
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

		String idMapPath=expFolder + "/idMap";
		File finalDesc=File.createTempFile("descrip_",".txt",new File(exportDescFile.getParent()));
		File finalSubs=File.createTempFile("subsetm_",".txt",new File(exportSubsFile.getParent()));


		ordin=new ArrayList<String>();
		ordin.add("0");
		componentType="RF1 Description";
		idSaveTolist="true";
		idType="RF1_DESCRIPTION";
		idColumnIndex="0";
		idMapFile=idMapPath + "/Descriptions_Uuid_Id.txt";
		hashIdMap.put("DESCRIPTION", new File(idMapFile)); 
		partitionId="";
		if (nspNr==0)
			partitionId="01";
		else
			partitionId="11";

		idFile=getIdentifierFile(exportDescFile, finalDesc, etOrd, ordin, execId,
				componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
				partitionId);
		rf1Files.add(idFile);


		ordin=new ArrayList<String>();
		ordin.add("1");
		componentType="Language subset";
		idSaveTolist="false";
		idType="RF1_DESCRIPTION";
		idColumnIndex="-1";
		idMapFile="";
		partitionId="";
		if (nspNr==0)
			partitionId="01";
		else
			partitionId="11";

		idFile=getIdentifierFile(exportSubsFile, finalSubs, etOrd, ordin, execId,
				componentType, idSaveTolist, idType, idColumnIndex, idMapFile,
				partitionId);
		rf1Files.add(idFile);

		config.setRf2Files(rf1Files);

		RF2IdListGeneratorFactory factory = new RF2IdListGeneratorFactory(config);
		factory.export();		

		factory=null;
		System.gc();

		if (idInsert){
			updateWb();
		}
	}
	
	/**
	 * Update wb.
	 */
	private void updateWb() {
		for(String key:hashIdMap.keySet()){
			File fMap=hashIdMap.get(key);

			if (fMap.exists()){
				//				if (key.equals("RF1_CONCEPT")){
				//					insertConceptIds(fMap);
				//					break;
				//				}

				if (key.equals("RF1_DESCRIPTION")){
					insertDescriptionIds(fMap);
					break;
				}

				//				if (key.equals("RF1_RELATIONSHIP")){
				//					insertRelationshipIds(fMap);
				//					break;
				//				}
				//
				//				if (key.equals("RF1_STATED_RELATIONSHIP")){
				//					insertRelationshipIds(fMap);
				//					break;
				//				}
			}
		}

	}
	
	/**
	 * Insert description ids.
	 *
	 * @param fMap the f map
	 */
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
	
	/**
	 * Gets the identifier file.
	 *
	 * @param fullPrevFile the full prev file
	 * @param fullFinalFile the full final file
	 * @param etOrd the et ord
	 * @param ordin the ordin
	 * @param execId the exec id
	 * @param componentType the component type
	 * @param idSaveTolist the id save tolist
	 * @param idType the id type
	 * @param idColumnIndex the id column index
	 * @param idMapFile the id map file
	 * @param partitionId the partition id
	 * @return the identifier file
	 */
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
		spar.namespaceId= String.valueOf(namespace);
		spar.partitionId=partitionId;
		spar.releaseId=releaseDate;
		spar.executionId=execId;
		spar.moduleId="rf1 " + namespace;
		spar.componentType=componentType;
		spar.idSaveTolist=idSaveTolist;
		spar.idType=idType;
		spar.idColumnIndex=idColumnIndex;
		spar.idMapFile=idMapFile;
		ident.sctidparam=spar;
		return ident;
	}

	/**
	 * Gets the log.
	 *
	 * @return the log
	 */
	public String getLog() {
		try {	
			reportFileWriter.append("Exported to description file " + exportDescFile.getName()  + " : " + descLineCount + " lines" + "\r\n");
			reportFileWriter.append("Exported to subset file " + exportSubsFile.getName()  + " : " + subsLineCount + " lines" + "\r\n");
			reportFileWriter.flush();
			reportFileWriter.close();

			FileInputStream fis = new FileInputStream(reportFile);

			return readStream(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Read stream.
	 *
	 * @param is the is
	 * @return the string
	 */
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