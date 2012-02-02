package org.ihtsdo.project.dataexport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.coordinate.PositionSet;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

public abstract class RF2DataExport  implements ProcessUnfetchedConceptDataBI{

	private static final String CONCEPT_HEADER = "id	effectiveTime	active	moduleId	definitionStatusId	" ;
	private static final String DESCRIPTION_HEADER = "id	effectiveTime	active	moduleId	conceptId	languageCode	typeId	term	caseSignificanceId";
	private static final String RELATIONSHIP_HEADER = "id	effectiveTime	active	moduleId	sourceId	destinationId	relationshipGroup	typeId	characteristicTypeId	modifierId";
	private static final String LANGUAGE_HEADER = null;
	private static final String INACTDESCRIPTION_HEADER = null;
	public I_ConfigAceFrame releaseConfig;
	private int snomedIntId;
	private I_IntSet allowedDestRelTypes;
	private I_GetConceptData activeValue;
	private I_GetConceptData inactiveValue;
	public I_ConfigAceFrame baseConfig;
	private NidSetBI allSnomedStatus;
	protected String releaseDate;
	private I_GetConceptData snomedCTModelComponent;

	private static Logger logger = Logger.getLogger(RF2DataExport.class);
	protected String nullUuid; // null string to match with UUID.fromString("00000000-0000-0000-C000-000000000046")
	protected I_IntSet allStatusSet;
	private int ACCEPTABLE;
	private int PREFERRED;
	protected NidSetBI allDescTypes;




	public RF2DataExport(I_ConfigAceFrame releaseConfig,String releaseDate,I_ConfigAceFrame baseConfig) {
		this.releaseDate=releaseDate;
		this.releaseConfig=releaseConfig;
		this.baseConfig=baseConfig;
		this.nullUuid="00000000-0000-0000-c000-000000000046";

		try {
			PREFERRED=SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid();
			ACCEPTABLE=SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid();

			snomedIntId = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());
			snomedCTModelComponent = Terms.get().getConcept(UUID.fromString("a60bd881-9010-3260-9653-0c85716b4391"));

			allowedDestRelTypes =  Terms.get().newIntSet();
			allowedDestRelTypes.add(Terms.get().uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
			activeValue = Terms.get().getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
			inactiveValue = Terms.get().getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());

			allSnomedStatus=getSnomedStatuses();
			allDescTypes=getAllDescTypes();

			this.allStatusSet=Terms.get().newIntSet();
			this.allStatusSet.addAll(allSnomedStatus.getSetValues());

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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

	protected void exportConcept(I_GetConceptData concept,
			String conceptSCTID, String moduleId, BufferedWriter bwc) {
		String effectiveTime = "";
		Date et = null;
		String conceptStatus = "";
		String active = "";
		String definitionStatusId = "";

		try {

			List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
					allSnomedStatus, 
					releaseConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, releaseConfig.getConflictResolutionStrategy());

			if (conceptAttributes != null && !conceptAttributes.isEmpty()) {
				I_ConceptAttributeTuple attributes = conceptAttributes.iterator().next();


				if (attributes.isDefined()) {
					definitionStatusId = I_Constants.FULLY_DEFINED;
				} else {
					definitionStatusId = I_Constants.PRIMITIVE;
				}
				conceptStatus = ExportUtil.getStatusType(attributes.getStatusNid());
				et = new Date(attributes.getTime());
				effectiveTime = ExportUtil.DATEFORMAT.format(et);
				// Before Jan 31, 2010, then conceptstatus 0 & 6 means current concept (Active)
				// After Jan 31, 2010 , then conceptstatus 0 means current but 6 means retired
				if (conceptStatus.equals("0")) {
					active = "1";
				} else if (releaseDate.compareTo(I_Constants.limited_policy_change)<0 && conceptStatus.equals("6")) {
					active = "1";
				} else {
					active = "0";
				}

				if ((conceptSCTID==null || conceptSCTID.equals("") || conceptSCTID.equals("0"))){
					conceptSCTID=concept.getUUIDs().iterator().next().toString();
				}
				if(active.equals("1") && moduleId.equals(I_Constants.CORE_MODULE_ID)){
					//moduleId = getConceptMetaModuleID(concept , getConfig().getReleaseDate());
					moduleId = computeModuleId(concept);	
				}

				bwc.append(conceptSCTID);
				bwc.append("\t");
				bwc.append(effectiveTime);
				bwc.append("\t");
				bwc.append(active);
				bwc.append("\t");
				bwc.append(moduleId);
				bwc.append("\t"); 
				bwc.append(definitionStatusId);
				bwc.append("\r\n");
			}
		}catch (NullPointerException ne) {
			ne.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private String computeModuleId(I_GetConceptData concept) throws IOException, TerminologyException {
		String moduleid = I_Constants.CORE_MODULE_ID;	
		if (snomedCTModelComponent.isParentOf(concept, 
				baseConfig.getAllowedStatus(),
				baseConfig.getDestRelTypes(), 
				baseConfig.getViewPositionSetReadOnly(), 
				baseConfig.getPrecedence(), 
				baseConfig.getConflictResolutionStrategy())) {
			moduleid = I_Constants.META_MOULE_ID;
		}else if(snomedCTModelComponent.equals(concept)){
			moduleid = I_Constants.META_MOULE_ID;
		}

		return moduleid;
	}

	@Override
	public NidBitSetBI getNidSet() throws IOException {
		NidBitSetBI cons = Ts.get().getAllConceptNids();
		return cons;
	}

	@Override
	public abstract void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher)
	throws Exception;

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

	public void exportDescription(I_DescriptionTuple description, I_GetConceptData concept,
			String conceptSCTID, String moduleId,  String tgtLangCode, BufferedWriter bwd, BufferedWriter reportFileWriter) {
		String effectiveTime = "";
		String descriptionid=null ;
		String active = "";
		String caseSignificanceId = "";
		String typeId = "";
		String languageCode="";

		try {

			String sDescType = ExportUtil.getSnomedDescriptionType(description.getTypeNid());
			if (!sDescType.equals("4") ) { 
				typeId = ExportUtil.getTypeId(sDescType);

				Date descriptionEffectiveDate = new Date(Terms.get().convertToThickVersion(description.getVersion()));
				effectiveTime = ExportUtil.DATEFORMAT.format(descriptionEffectiveDate);

				//&& !effectiveTime.contains("1031") && !effectiveTime.contains("0430")) {
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
				String descriptionstatus = ExportUtil.getStatusType(description.getStatusNid());

				if (descriptionstatus.equals("0") || descriptionstatus.equals("6") || descriptionstatus.equals("8"))
					active = "1";
				else
					active = "0";

				if (description.isInitialCaseSignificant()) {
					caseSignificanceId = I_Constants.SENSITIVE_CASE;
				} else {
					caseSignificanceId = I_Constants.INITIAL_INSENSITIVE;
				}

				if(active.equals("1") && moduleId.equals(I_Constants.CORE_MODULE_ID)){
					//moduleId = getConceptMetaModuleID(concept , getConfig().getReleaseDate());
					moduleId = computeModuleId(concept);	
				}

				if (conceptSCTID==null || conceptSCTID.equals("") || conceptSCTID.equals("0")){
					conceptSCTID=concept.getUids().iterator().next().toString();
				}

				if ((descriptionid==null || descriptionid.equals("") ) && active.equals("0")){
					reportFileWriter.append("The description " + description.getText() + "- of concept SCTID#" + conceptSCTID  + " was not exported, it has inactive status and it has not SCTID.\r\n" );
					
					logger.info("Unplublished Retired Description: " + description.getUUIDs().iterator().next().toString());
				}else{

					bwd.append(descriptionid);
					bwd.append("\t");
					bwd.append(effectiveTime);
					bwd.append("\t");
					bwd.append(active);
					bwd.append("\t");
					bwd.append(moduleId);
					bwd.append("\t");  
					bwd.append(conceptSCTID);
					bwd.append("\t");
					bwd.append(languageCode);
					bwd.append("\t"); 
					bwd.append(typeId);
					bwd.append("\t");
					bwd.append(term);
					bwd.append("\t");
					bwd.append(caseSignificanceId);
					bwd.append("\r\n");
				}
			}
		}catch (NullPointerException ne) {
			logger.error("NullPointerException: " + ne.getMessage());
			logger.error(" NullPointerException " + conceptSCTID);
			logger.error(" NullPointerException " + descriptionid);
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			logger.error("IOExceptions: " + conceptSCTID);
			logger.error("IOExceptions: " + descriptionid);
		} catch (Exception e) {
			logger.error("Exceptions in exportDescription: " + e.getMessage());
			logger.error("Exceptions in exportDescription: " +conceptSCTID);
			logger.error("Exceptions in exportDescription: " +descriptionid);
		}
	}

	public NidSetBI getAllDescTypes() throws TerminologyException, IOException {
		NidSetBI allDescTypes = new NidSet();
		allDescTypes.add(Terms.get().uuidToNative(UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf")));
		allDescTypes.add(Terms.get().uuidToNative(UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2")));
		allDescTypes.add(Terms.get().uuidToNative(UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09")));
		return allDescTypes;
	}
	public boolean exportRelationship(I_GetConceptData concept,
			String conceptSCTID, String moduleId, BufferedWriter bwr) throws IOException {

		boolean bRet=false;
		try {
			String effectiveTime = "";
			String relationshipId = "";
			String destinationId = "";
			String relTypeId = "";
			String active = "";
			String characteristicTypeId = "";
			String modifierId = I_Constants.SOMEMODIFIER; 
			int relationshipStatusId=0;

			List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(allSnomedStatus, null, 
					baseConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, baseConfig.getConflictResolutionStrategy());

			for (I_RelTuple rel : relationships) {
				characteristicTypeId="";

				I_Identify charId = Terms.get().getId(rel.getCharacteristicId());
				List<? extends I_IdPart> idParts = charId.getVisibleIds(baseConfig.getViewPositionSetReadOnly(), 
						snomedIntId);
				if (idParts != null) {
					Object denotation = getLastCurrentVisibleId(idParts, baseConfig.getViewPositionSetReadOnly(), 
							RelAssertionType.INFERRED_THEN_STATED);
					if (denotation instanceof Long) {
						Long c = (Long) denotation;
						if (c != null)  characteristicTypeId = c.toString();
					}
				}

				if (characteristicTypeId.equals(I_Constants.INFERRED) || characteristicTypeId.equals(I_Constants.ADDITIONALRELATION)){
					destinationId = "";
					I_Identify id = Terms.get().getId(rel.getC2Id());
					if (id != null) {
						idParts = id.getVisibleIds(baseConfig.getViewPositionSetReadOnly(), 
								snomedIntId);
						if (idParts != null) {
							Object denotation = getLastCurrentVisibleId(idParts, baseConfig.getViewPositionSetReadOnly(), 
									RelAssertionType.INFERRED_THEN_STATED);
							if (denotation instanceof Long) {
								Long c = (Long) denotation;
								if (c != null)  destinationId = c.toString();
							}
						}
					}

					relTypeId = "";

					id = Terms.get().getId(rel.getTypeNid());
					if (id != null) {
						idParts = Terms.get().getId(rel.getTypeNid()).getVisibleIds(baseConfig.getViewPositionSetReadOnly(), 
								snomedIntId);
						if (idParts != null) {
							Object denotation = getLastCurrentVisibleId(idParts, baseConfig.getViewPositionSetReadOnly(), 
									RelAssertionType.INFERRED_THEN_STATED);
							if (denotation instanceof Long) {
								Long c = (Long) denotation;
								if (c != null)  relTypeId = c.toString();
							}
						}
					}

					if (relTypeId.equals(I_Constants.ISA)) {
						if (destinationId.equals(I_Constants.DUPLICATE_CONCEPT) || destinationId.equals(I_Constants.AMBIGUOUS_CONCEPT) ||
								destinationId.equals(I_Constants.OUTDATED_CONCEPT) || destinationId.equals(I_Constants.ERRONEOUS_CONCEPT ) ||
								destinationId.equals(I_Constants.LIMITED_CONCEPT) || destinationId.equals(I_Constants.REASON_NOT_STATED_CONCEPT) ||
								destinationId.equals(I_Constants.MOVED_ELSEWHERE_CONCEPT)){
							continue;
						}
					} 

					relationshipId = "";

					id = Terms.get().getId(rel.getNid());
					if (id != null) {
						idParts = Terms.get().getId(rel.getNid()).getVisibleIds(baseConfig.getViewPositionSetReadOnly(), 
								snomedIntId);
						if (idParts != null) {
							Object denotation =getLastCurrentVisibleId(idParts, baseConfig.getViewPositionSetReadOnly(), 
									RelAssertionType.INFERRED_THEN_STATED);
							if (denotation instanceof Long) {
								Long c = (Long) denotation;
								if (c != null)  relationshipId = c.toString();
							}
						}
					}

					relationshipStatusId = rel.getStatusNid();
					if (moduleId.equals(I_Constants.CORE_MODULE_ID)){
						if (relationshipStatusId == activeValue.getNid()) {               
							active = "1";
							moduleId = ExportUtil.getConceptMetaModuleID(concept,releaseDate);
						} else if (relationshipStatusId == inactiveValue.getNid()) {               
							active = "0";
							Long lastActiveDate=ExportUtil.getLatestActivePart(rel.getFixedPart().getMutableParts());

							if (lastActiveDate!=null){
								moduleId = ExportUtil.getConceptMetaModuleID(concept,
										ExportUtil.DATEFORMAT.format(new Date(lastActiveDate)));
							}else{
								moduleId = ExportUtil.getConceptMetaModuleID(concept,
										ExportUtil.DATEFORMAT.format(new Date(rel.getTime())));
							}
						}
					}else if (relationshipStatusId == activeValue.getNid()) {               
						active = "1";
					}else {               
						active = "0";
					}
					effectiveTime = ExportUtil.DATEFORMAT.format(new Date(rel.getTime()));

					String relationshipGroup = String.valueOf(rel.getGroup());

					if (relTypeId==null || relTypeId.equals("")){
						relTypeId=Terms.get().getUids(rel.getTypeNid()).iterator().next().toString();
					}

					if (destinationId==null || destinationId.equals("")){
						Collection<UUID> Uids=Terms.get().getUids(rel.getC2Id());
						if (Uids==null  ){
							continue;
						}
						destinationId=Uids.iterator().next().toString();
						if (destinationId.equals(nullUuid)){
							continue;
						}
					}

					if ((relationshipId==null || relationshipId.equals("")) && active.equals("1")){
						relationshipId=rel.getUUIDs().iterator().next().toString();						
					}

					if (relationshipId==null || relationshipId.equals("")){
						logger.info("Unplublished Retired Relationship: " + rel.getUUIDs().iterator().next().toString());
					}else{

						bwr.append(relationshipId);
						bwr.append("\t");
						bwr.append(effectiveTime);
						bwr.append("\t");
						bwr.append(active);
						bwr.append("\t");
						bwr.append(moduleId);
						bwr.append("\t");  
						bwr.append(conceptSCTID);
						bwr.append("\t");
						bwr.append(destinationId);
						bwr.append("\t"); 
						bwr.append(relationshipGroup);
						bwr.append("\t");
						bwr.append(relTypeId);
						bwr.append("\t");
						bwr.append(characteristicTypeId);
						bwr.append("\t");
						bwr.append(modifierId);
						bwr.append("\r\n");

						bRet=true;
					}
				}
			}
		}catch (NullPointerException ne) {
			logger.error("NullPointerException: " + ne.getMessage());
			logger.error(" NullPointerException " + conceptSCTID);
			bRet=false;
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			logger.error("IOExceptions: " + conceptSCTID);
			bRet=false;
		} catch (Exception e) {
			logger.error("Exceptions in exportInfRelationship: " + e.getMessage());
			logger.error("Exceptions in exportInfRelationship: " +conceptSCTID);
			bRet=false;
		}
		return bRet;
	}

	@Override
	public boolean continueWork() {
			return true;
	}

	public Object getLastCurrentVisibleId(List<? extends I_IdPart> parts, PositionSet viewpointSet,
			RelAssertionType relAssertionType) {
		Object data = null;
		if (getLastCurrentVisibleIdPart(parts, viewpointSet, relAssertionType) != null) {
			data = getLastCurrentVisibleIdPart(parts, viewpointSet, relAssertionType).getDenotation();
		}
		return data;
	}

	public I_IdPart getLastCurrentVisibleIdPart(List<? extends I_IdPart> parts, PositionSet viewpointSet,
			RelAssertionType relAssertionType) {
		//		System.out.println("Parts Size: " + parts.size());
		I_ConfigAceFrame config = null;
		int currentId = Integer.MIN_VALUE;
		int activeId = Integer.MIN_VALUE;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			currentId = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			activeId = Terms.get().uuidToNative(UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f"));

		} catch (TerminologyException e) {
			//
		} catch (IOException e) {
			//
		}
		I_IdPart currentVisiblePart = null;

		Long lastTime = Long.MIN_VALUE;
		Long maxTime = Long.MIN_VALUE;

		for (PositionBI viewpoint : viewpointSet) {
			if (viewpoint.getTime() > maxTime) {
				maxTime = viewpoint.getTime();
			}
		}
		RelAssertionType lastAssertionType = null;
		int classifierNid = config.getClassifierConcept().getNid();
		//		System.out.println("Current: " + currentId + " ClassifierNid: " + currentId);
		for (I_IdPart loopPart : parts) {
			I_IdPart loopTempPart = null;
			RelAssertionType loopAssertionType = null;
			//			System.out.println(loopPart.getTime() + "|" + loopPart.getStatusNid() + "|" + String.valueOf(loopPart.getDenotation())
			//					+ "|" + loopPart.getAuthorNid());
			if (loopPart.getTime() > lastTime && loopPart.getTime() <= maxTime) {
				if (relAssertionType == RelAssertionType.INFERRED && 
						loopPart.getAuthorNid() == classifierNid) {
					loopTempPart = loopPart;
					loopAssertionType = RelAssertionType.INFERRED;
				} else if (relAssertionType == RelAssertionType.INFERRED_THEN_STATED) {
					if (loopPart.getAuthorNid() == classifierNid) {
						loopTempPart = loopPart;
						loopAssertionType = RelAssertionType.INFERRED;
					} else if (lastAssertionType == null || lastAssertionType == RelAssertionType.STATED) {
						loopTempPart = loopPart;
						loopAssertionType = RelAssertionType.STATED;
					}
				} else if (relAssertionType == RelAssertionType.STATED && 
						loopPart.getAuthorNid() != classifierNid) {
					loopTempPart = loopPart;
					loopAssertionType = RelAssertionType.STATED;
				}

				if (loopTempPart != null) {
					if (loopTempPart.getStatusNid() == currentId || loopTempPart.getStatusNid()== activeId) {
						currentVisiblePart = loopTempPart;
						lastTime = loopPart.getTime();
						lastAssertionType = loopAssertionType;
					} else if (loopTempPart.getStatusNid() != currentId  
							&& loopTempPart.getStatusNid()!= activeId 
							&& currentVisiblePart != null) {
						if (loopTempPart.getDenotation().equals(currentVisiblePart.getDenotation())) {
							currentVisiblePart = null;
							lastTime = Long.MIN_VALUE;
							lastAssertionType = null;
						}
					}

				}

				// && loopPart.getStatusNid() == currentId
			}
		}
		return currentVisiblePart;
	}


	public void exportLanguage(I_ExtendByRefPartCid languageExtension, I_DescriptionTuple description, I_GetConceptData concept,
			String moduleId, String refsetSCTID, BufferedWriter bwl, BufferedWriter reportFileWriter) throws IOException {
		String effectiveTime = "";
		String descriptionid = "";
		String active = "";
		int extensionStatusId = 0;
		String acceptabilityId = "";

		try {


			extensionStatusId = languageExtension.getStatusNid();
			int acceptabilityNid = languageExtension.getC1id();
			Long descSCTID=getExistentDescriptionSCTID(description);
			if (descSCTID!=null){
				descriptionid=String.valueOf(descSCTID);
			}else if ((descSCTID==null || descSCTID==0) && active.equals("1")){
				descriptionid=description.getUUIDs().iterator().next().toString();
			}
			String status = ExportUtil.getStatusType(extensionStatusId);
			if (status.equals("0")){ 
				active = "1";
			} else if (status.equals("1")) {
				active = "0";
			} else {
				I_GetConceptData con=Terms.get().getConcept(extensionStatusId);
				logger.error("unknown extensionStatusId =====>" + extensionStatusId + "con : " + con.toString());
			}
			String descriptionstatus = ExportUtil.getStatusType(description.getStatusNid());

			if (active.equals("1") && !(descriptionstatus.equals("0") || descriptionstatus.equals("6") || descriptionstatus.equals("8"))){
				reportFileWriter.append("The description " + description.getText() + "- of concept " + concept.toUserString()  + " has inactive status and it has active reference on language refset.\r\n" );
			}
			if (acceptabilityNid == PREFERRED) { // preferred
				acceptabilityId = I_Constants.PREFERRED;
			} else if (acceptabilityNid == ACCEPTABLE) { 
				acceptabilityId = I_Constants.ACCEPTABLE;
			} else {
				logger.error("unknown acceptabilityId =====>" + acceptabilityNid + "conceptid  =====>" + concept.getUUIDs().iterator().next().toString() + " descriptionid ===>" + descriptionid);
			}

			if ((descriptionid==null || descriptionid.equals("")) && active.equals("1")){
				descriptionid=description.getUUIDs().iterator().next().toString();
			}

			if (descriptionid==null || descriptionid.equals("")){
				logger.error("Unplublished Retired description: "  + description.getUUIDs().iterator().next().toString() + " of concept : " + concept.getUUIDs().iterator().next().toString());
			}else {

				UUID refsetuuid = languageExtension.getPrimUuid(); 

				effectiveTime = ExportUtil.DATEFORMAT.format(new Date(languageExtension.getTime()));


				bwl.append(refsetuuid.toString());
				bwl.append("\t");
				bwl.append(effectiveTime);
				bwl.append("\t");
				bwl.append(active);
				bwl.append("\t");
				bwl.append(moduleId);
				bwl.append("\t");  
				bwl.append(refsetSCTID);
				bwl.append("\t");
				bwl.append(descriptionid);
				bwl.append("\t"); 
				bwl.append(acceptabilityId);
				bwl.append("\r\n");

			}

		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exceptions in exportLanguage: " + e.getMessage());
			logger.error(description.toUserString());
			e.printStackTrace();
			System.exit(0);
		}
	}
	public void exportInactDescription(I_DescriptionTuple description, 
			String moduleId, String refsetSCTID, BufferedWriter bwi) throws IOException {
		try {
			String effectiveTime = "";
			String valueId = "";
			String descriptionid="";
			String sDescType = ExportUtil.getSnomedDescriptionType(description.getTypeNid());
			Date descriptionEffectiveDate = new Date(Terms.get().convertToThickVersion(description.getVersion()));
			effectiveTime = ExportUtil.DATEFORMAT.format(descriptionEffectiveDate);

			if (!sDescType.equals("4"))  { // Ignore text-defination

				Long descSCTID=getExistentDescriptionSCTID(description);
				if (descSCTID!=null){
					descriptionid=String.valueOf(descSCTID);
				}else if (descSCTID==null || descSCTID==0){
					descriptionid=description.getUUIDs().iterator().next().toString();
				}

				UUID uuid = Type5UuidFactory.get(refsetSCTID + descriptionid);

				valueId = ExportUtil.getDescInactivationValueId(description.getStatusNid());

				String status="0";
				if ( !valueId.equals("XXX")) {
					status="1";
				} else {
					status="0";
					valueId="";
				} 

				bwi.append(uuid.toString());
				bwi.append("\t");
				bwi.append(effectiveTime);
				bwi.append("\t");
				bwi.append(status);
				bwi.append("\t");
				bwi.append(moduleId);
				bwi.append("\t");  
				bwi.append(refsetSCTID);
				bwi.append("\t");
				bwi.append(descriptionid);
				bwi.append("\t"); 
				bwi.append(valueId);
				bwi.append("\r\n");
			}
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exceptions in export inactive description: " + e.getMessage());
			logger.error("==========description==========" + description);
			e.printStackTrace();
			System.exit(0);
		}
	}

	public boolean exportStatedRelationship(I_GetConceptData concept,
			String conceptSCTID, String moduleId, BufferedWriter bws) throws IOException {
		boolean bRet=false;
		try {
			String effectiveTime = "";
			String relationshipId = "";
			String destinationId = "";
			String relTypeId = "";
			String active = "";
			String characteristicTypeId = "";
			String modifierId = I_Constants.SOMEMODIFIER;
			int relationshipStatusId=0;

			//Change this to come from config
			Date PREVIOUSRELEASEDATE = ExportUtil.DATEFORMAT.parse(I_Constants.inactivation_policy_change);

			List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(allSnomedStatus, null, 
					baseConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, baseConfig.getConflictResolutionStrategy());

			for (I_RelTuple rel : relationships) {
				characteristicTypeId="";
				I_Identify charId = Terms.get().getId(rel.getCharacteristicId());

				List<? extends I_IdPart> idParts = charId.getVisibleIds(baseConfig.getViewPositionSetReadOnly(), 
						snomedIntId);
				if (idParts != null) {
					Object denotation = getLastCurrentVisibleId(idParts, baseConfig.getViewPositionSetReadOnly(), 
							RelAssertionType.INFERRED_THEN_STATED);
					if (denotation instanceof Long) {
						Long c = (Long) denotation;
						if (c != null)  characteristicTypeId = c.toString();
					}
				}
				if (characteristicTypeId.equals(I_Constants.STATED) ){

					destinationId = "";
					I_Identify id = Terms.get().getId(rel.getC2Id());
					if (id != null) {
						idParts = id.getVisibleIds(baseConfig.getViewPositionSetReadOnly(), 
								snomedIntId);
						if (idParts != null) {
							Object denotation = getLastCurrentVisibleId(idParts, baseConfig.getViewPositionSetReadOnly(), 
									RelAssertionType.INFERRED_THEN_STATED);
							if (denotation instanceof Long) {
								Long c = (Long) denotation;
								if (c != null)  destinationId = c.toString();
							}
						}
					}

					relTypeId = "";

					id = Terms.get().getId(rel.getTypeNid());
					if (id != null) {
						idParts = Terms.get().getId(rel.getTypeNid()).getVisibleIds(baseConfig.getViewPositionSetReadOnly(), 
								snomedIntId);
						if (idParts != null) {
							Object denotation = getLastCurrentVisibleId(idParts, baseConfig.getViewPositionSetReadOnly(), 
									RelAssertionType.INFERRED_THEN_STATED);
							if (denotation instanceof Long) {
								Long c = (Long) denotation;
								if (c != null)  relTypeId = c.toString();
							}
						}
					}

					if (relTypeId.equals(I_Constants.ISA)) {
						if (destinationId.equals(I_Constants.DUPLICATE_CONCEPT) || destinationId.equals(I_Constants.AMBIGUOUS_CONCEPT) ||
								destinationId.equals(I_Constants.OUTDATED_CONCEPT) || destinationId.equals(I_Constants.ERRONEOUS_CONCEPT ) ||
								destinationId.equals(I_Constants.LIMITED_CONCEPT) || destinationId.equals(I_Constants.REASON_NOT_STATED_CONCEPT) ||
								destinationId.equals(I_Constants.MOVED_ELSEWHERE_CONCEPT)){
							continue;
						}
					} 

					relationshipId = "";

					id = Terms.get().getId(rel.getNid());
					if (id != null) {
						idParts = Terms.get().getId(rel.getNid()).getVisibleIds(baseConfig.getViewPositionSetReadOnly(), 
								snomedIntId);
						if (idParts != null) {
							Object denotation = getLastCurrentVisibleId(idParts, baseConfig.getViewPositionSetReadOnly(), 
									RelAssertionType.STATED);
							if (denotation instanceof Long) {
								Long c = (Long) denotation;
								if (c != null)  relationshipId = c.toString();
							}
						}
					}

					Date et = new Date(rel.getTime());
					effectiveTime = ExportUtil.DATEFORMAT.format(et);

					relationshipStatusId = rel.getStatusNid();
					if (moduleId.equals(I_Constants.CORE_MODULE_ID)){

						if (relationshipStatusId == activeValue.getNid()) {               
							active = "1";
							moduleId = ExportUtil.getConceptMetaModuleID(concept,releaseDate);
						} else if (relationshipStatusId == inactiveValue.getNid()) {               
							active = "0";
							Long lastActiveDate=ExportUtil.getLatestActivePart(rel.getFixedPart().getMutableParts());

							if (lastActiveDate!=null){
								moduleId = ExportUtil.getConceptMetaModuleID(concept,
										ExportUtil.DATEFORMAT.format(new Date(lastActiveDate)));
							}else{
								moduleId = ExportUtil.getConceptMetaModuleID(concept,
										ExportUtil.DATEFORMAT.format(new Date(rel.getTime())));
							}
						}
					}else if (relationshipStatusId == activeValue.getNid()) {               
						active = "1";
					}else {               
						active = "0";
					}

					String relationshipGroup = String.valueOf( rel.getGroup());

					if (relTypeId==null || relTypeId.equals("")){
						relTypeId=Terms.get().getUids(rel.getTypeNid()).iterator().next().toString();
					}
					if (destinationId==null || destinationId.equals("")){
						Collection<UUID> Uids=Terms.get().getUids(rel.getC2Id());
						if (Uids==null  ){
							continue;
						}
						destinationId=Uids.iterator().next().toString();
						if (destinationId.equals(nullUuid)){
							continue;
						}
					}

					if ((relationshipId==null || relationshipId.equals("")) && active.equals("1")){
						relationshipId=rel.getUUIDs().iterator().next().toString();
					}

					if (relationshipId==null || relationshipId.equals("")){
						logger.info("Unplublished Retired Stated Relationship: " + rel.getUUIDs().iterator().next().toString());
					}else {

						bws.append(relationshipId);
						bws.append("\t");
						bws.append(effectiveTime);
						bws.append("\t");
						bws.append(active);
						bws.append("\t");
						bws.append(moduleId);
						bws.append("\t");  
						bws.append(conceptSCTID);
						bws.append("\t");
						bws.append(destinationId);
						bws.append("\t"); 
						bws.append(relationshipGroup);
						bws.append("\t");
						bws.append(relTypeId);
						bws.append("\t");
						bws.append(characteristicTypeId);
						bws.append("\t");
						bws.append(modifierId);
						bws.append("\r\n");
						bRet=true;
					}
				}
			}

		}catch (NullPointerException ne) {
			logger.error("NullPointerException: " + ne.getMessage());
			logger.error(" NullPointerException " + conceptSCTID);
			bRet=false;
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			logger.error("IOExceptions: " + conceptSCTID);
			bRet=false;
		} catch (Exception e) {
			logger.error("Exceptions in exportStatedRelationship: " + e.getMessage());
			logger.error("Exceptions in exportStatedRelationship: " + conceptSCTID);
			bRet=false;
		}
		return bRet;
	}
	public static String getConceptHeader() {
		return CONCEPT_HEADER;
	}

	public static String getDescriptionHeader() {
		return DESCRIPTION_HEADER;
	}

	public static String getRelationshipHeader() {
		return RELATIONSHIP_HEADER;
	}

	public static String getLanguageHeader() {
		return LANGUAGE_HEADER;
	}

	public static String getInactdescriptionHeader() {
		return INACTDESCRIPTION_HEADER;
	}
}
