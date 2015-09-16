package gov.va.export.uscrs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSet;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class USCRSInitiator {
	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(USCRSInitiator.class);
	private List<RelationshipChronicleBI> exportRelsUnFiltered;
	private ViewCoordinate vc;
	private ConceptUSCRSProcessor conceptHandler = new ConceptUSCRSProcessor();
	private DescriptionUSCRSProcessor descriptionHandler = new DescriptionUSCRSProcessor();
	private RelationshipUSCRSProcessor relationshipHandler = new RelationshipUSCRSProcessor();
	private int ACTIVE_STATUS_NID;
	private boolean conceptCreated;


	USCRSInitiator(ViewCoordinate viewCoordinate) throws ValidationException, IOException {
		vc = viewCoordinate;
		ACTIVE_STATUS_NID = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
		exportRelsUnFiltered =new ArrayList<RelationshipChronicleBI>();
	}
	
	
	boolean examineConcept(I_GetConceptData concept, long previousExportTime) throws Exception {
		exportRelsUnFiltered.clear();
		if (previousExportTime < 0) {
			return noDateExamination(concept.getVersion(vc));
		} else {
			return dateExamination(concept, previousExportTime);
		}
	}

	private boolean dateExamination(I_GetConceptData concept, long previousExportTime) throws Exception {
		boolean changeMade = false;
		ViewCoordinate vcPreviousRelease = new ViewCoordinate(vc);
		conceptCreated = false;
	
		// Create Initial VC's Position 
		PositionBI[] currentPosSet = vc.getPositionSet().getPositionArray();
		PositionBI viewPos = currentPosSet[0];

		Set<PositionBI> pSet = new HashSet<PositionBI>();
		PositionBI newPos = Terms.get().newPosition(viewPos.getPath(), previousExportTime);
        pSet.add(newPos);
        PositionSetBI posSet = new PositionSet(pSet);
        vcPreviousRelease.setPositionSet(posSet);

		// Update Initial VC's Allowed Statuses
		vcPreviousRelease.setAllowedStatusNids(null);

		// Examine Content
		changeMade = handleDateExamCon(changeMade, concept, previousExportTime, vcPreviousRelease);
			
		// If concept was created & retired prior to previous release, don't need to export anything else
		if (!inactiveNeverReleased(concept, vcPreviousRelease)) {
			changeMade = handleDateExamDescs(changeMade, concept, previousExportTime, vcPreviousRelease);
			
			changeMade = handleDateExamRels(changeMade, concept, previousExportTime, vcPreviousRelease);
		}
		
		return changeMade;		
	}

	private boolean inactiveNeverReleased(I_GetConceptData concept, ViewCoordinate vcPreviousRelease) throws IOException, ContradictionException {
		ConceptAttributeChronicleBI cac = concept.getVersion(vc).getConceptAttributes();
		ConceptAttributeVersionBI<?> caLatest = cac.getVersion(vc);
		ConceptAttributeVersionBI<?> caInitial = cac.getVersion(vcPreviousRelease);

		if (caInitial == null && caLatest == null) {
			return true;
		} else {
			return false;
		}
	}


	private boolean handleDateExamCon(boolean changeMade,
			I_GetConceptData concept, long previousExportTime,
			ViewCoordinate vcPreviousRelease) throws Exception {

		try {
			ConceptAttributeChronicleBI cac = concept.getVersion(vc).getConceptAttributes();
			ConceptAttributeVersionBI<?> caLatest = cac.getVersion(vc);
			ConceptAttributeVersionBI<?> caInitial = cac.getVersion(vcPreviousRelease);
	
			if(caInitial != null) {
				if(caLatest != null && caLatest.getStatusNid() == ACTIVE_STATUS_NID) {
					ConceptAttributeVersionBI<?> thisCaLatest = caLatest;
					ConceptAttributeVersionBI<?> thisCaInitial = caInitial;
					
					boolean conceptIsChanged = false;
					if(thisCaInitial.isDefined() != thisCaLatest.isDefined()) {
						conceptIsChanged = true;
					}
					if(conceptIsChanged) {
						//TODO: Handle Concept Changes
						//conceptCreated = true;
					} else {
						//noop
					}
				} else {
					changeMade = true;
					conceptHandler.handleRetireConcept(concept.getVersion(vc));
				}
			} else {
				if(caLatest != null) {
					ViewCoordinate vcPrActiveInactive = new ViewCoordinate(vcPreviousRelease);
					vcPrActiveInactive.setAllowedStatusNids(null);
					
					ConceptAttributeVersionBI<?> cavRetiredCheck = cac.getVersion(vcPrActiveInactive);
	
					if(cavRetiredCheck != null) {
						// Place in the edit concept tab (un-retired)
					} else {
						exportRelsUnFiltered.addAll(conceptHandler.handleNewConcept(concept));
						changeMade = true;
						conceptCreated = true;
					}
				} else {
					//noop
				}
			}
			
		} catch (Exception e) {
			throw new Exception("Error getting concept " + concept.getConceptNid() + " attributes for date / time comparison", e);
		}
		
		return changeMade;
	}


	private boolean handleDateExamDescs(boolean changeMade,
			I_GetConceptData concept, long previousExportTime,
			ViewCoordinate vcPreviousRelease) throws IOException, Exception {
		//Export Descriptions
		for (DescriptionChronicleBI d : concept.getDescriptions()) {
			try {
				DescriptionVersionBI<?> dvLatest = d.getVersion(vc);
				DescriptionVersionBI<?> dvInitial = d.getVersion(vcPreviousRelease);

				if(dvInitial != null) {
					if(dvLatest != null && dvLatest.getStatusNid() == ACTIVE_STATUS_NID){

						boolean hasChange = false;
						if(!dvLatest.getLang().equals(dvInitial.getLang())) {
							hasChange = true;
						} else if(!dvLatest.getText().equals(dvInitial.getText())) {
							hasChange = true;
						} else if (dvLatest.isInitialCaseSignificant() != dvInitial.isInitialCaseSignificant()) {
							hasChange = true;
						}
						if(hasChange) {
							descriptionHandler.handleChangeDesc(dvLatest);
							changeMade = true;
						}
					} else {
						descriptionHandler.handleRetireDescription(dvInitial);
						changeMade = true;
					}
				} else {
					if(dvLatest != null) {
						ViewCoordinate vcPrActiveInactive = new ViewCoordinate(vcPreviousRelease);
						vcPrActiveInactive.setAllowedStatusNids(null);
						DescriptionVersionBI<?> dvCheckRetired = d.getVersion(vcPrActiveInactive);

						if(dvCheckRetired != null) {
							descriptionHandler.handleChangeDesc(dvCheckRetired);
							changeMade = true;
						} else {
							DescriptionVersionBI<?> dvLatestG = dvLatest;
							if(isSynonym(concept.getVersion(vc), dvLatestG)) {
								descriptionHandler.handleNewSyn(dvLatestG);
								changeMade = true;
							}
						}
					} else {
						//noop
					}
				}
			} catch (Exception e) {
				throw new Exception("Description Export Error on Desc: " + d.getNid() + " on con: " + d.getConceptNid(), e);
			}
		}
		
		return changeMade;
	}


	private boolean handleDateExamRels(boolean changeMade, I_GetConceptData concept, long previousExportTime, ViewCoordinate vcPreviousRelease) throws Exception {
		//Export Relationships
		if(conceptCreated) {
			LOG.debug("USCRS Handler - Concept was already created, handeling components accordingly (skip first 3 ISA relationships");
		} else {
			LOG.debug("Concept NOT previously created - generating relationships now instead");
			Collection<? extends RelationshipChronicleBI> outgoingRels = concept.getRelationshipsOutgoing();
				
			for (RelationshipChronicleBI rel : outgoingRels) {
				try {
					RelationshipVersionBI<?> rv = null;
					try {
						ViewCoordinate vcas = USCRSProcessor.vcAllStatus;
						rv = rel.getVersion(vcas);
					} catch (Exception e) {
						throw new Exception("Error getting relationship version on the proper View Coordinate", e);
					}
					
					if(rv != null && rv.getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid()) {
						exportRelsUnFiltered.add(rv);
					}
				
				} catch (Exception e) {
					throw new Exception("Error retreiving the incoming relationships on Rel: " + rel.getNid() + " on con: " + rel.getConceptNid(), e);
				}
			}		
		}
		
		for(RelationshipChronicleBI r : exportRelsUnFiltered) {
			try {
				RelationshipVersionBI<?> rvLatest = r.getVersion(vc);
				RelationshipVersionBI<?> rvInitial = r.getVersion(vcPreviousRelease);
	
		        if(rvInitial != null) {
					if(rvLatest != null && rvLatest.getStatusNid() == ACTIVE_STATUS_NID){
						
						boolean hasRelAttrChange = false;
						if(rvLatest.getCharacteristicNid() != rvInitial.getCharacteristicNid()) {
							hasRelAttrChange = true;
						} else if(rvLatest.getGroup() != rvInitial.getGroup()) {
							hasRelAttrChange = true;
						} else if (rvLatest.getRefinabilityNid() != rvInitial.getRefinabilityNid()) {
							hasRelAttrChange = true;
						} else if (rvLatest.isInferred() != rvInitial.isInferred()) {
							hasRelAttrChange = true;
						} else if (rvLatest.isStated() != rvInitial.isStated()) {
							hasRelAttrChange = true;
						}
						
						if(hasRelAttrChange) {
							if (rvLatest.getTypeNid() == Snomed.IS_A.getLenient().getNid()) {
								relationshipHandler.handleChangeParent(rvLatest);
								changeMade = true;
							} else {
								relationshipHandler.handleChangeRels(rvLatest);
								changeMade = true;
							}
						}
					} else {
						relationshipHandler.handleRetireRelationship(rvInitial);
						changeMade = true;
					}
				} else {
					if (rvLatest != null) {
						if (! vc.getAllowedStatusNids().contains(rvLatest.getStatusNid())) {
							if (rvLatest.getTypeNid() == Snomed.IS_A.getLenient().getNid()) {
								relationshipHandler.handleChangeParent(rvLatest);
								changeMade = true;
							} else {
								relationshipHandler.handleChangeRels(rvLatest);
								changeMade = true;
							}
						} else {
							if (rvLatest.getTypeNid() == Snomed.IS_A.getLenient().getNid()) {
								relationshipHandler.handleNewParent(rvLatest);
								changeMade = true;
							} else {
								relationshipHandler.handleNewRel(rvLatest);  
								changeMade = true;
							}
						}
					} else {
						//noop
					}
				}
			} catch (Exception e) {
				throw new Exception("Error retreiving the incoming relationships on Rel: " + r.getNid() + " on con: " + r.getConceptNid(), e);
			}
		}		
		return changeMade;
	}

	private boolean noDateExamination(ConceptVersionBI con) throws Exception {
		//No date filter, process everything that way
		LOG.info("USCRS Handler -Filter Set. Not a Date Filter. Exporting all concepts.");
		
		try {
			if (con.getStatusNid() == ACTIVE_STATUS_NID) {
				exportRelsUnFiltered.addAll(conceptHandler.handleNewConcept(con));
			}
		} catch (Exception e) {
			throw new Exception("Could not export concept " + con.getConceptNid(), e);
		} 

		for (RelationshipChronicleBI rc : exportRelsUnFiltered)
		{
			try {
				RelationshipVersionBI<?> relVer = rc.getVersion(vc);
				
				if (relVer.getStatusNid() == ACTIVE_STATUS_NID) {
					relationshipHandler.handleNewParent(relVer);
					relationshipHandler.handleNewRel(relVer);
				}
			} catch (Exception e) {
				throw new Exception("Could not export rel: " + rc.getNid() + " on concept " + con.getConceptNid(), e);
			} 
		}
		
		Collection<? extends DescriptionChronicleBI> descriptions = con.getDescriptions();
		
		for(DescriptionChronicleBI d : descriptions) {
			try {
				DescriptionVersionBI<?> dv = d.getVersion(vc);

				if(dv.getStatusNid() == ACTIVE_STATUS_NID && isSynonym(con, dv)) {
					descriptionHandler.handleNewSyn(dv);
				}
			} catch (Exception e) {
				throw new Exception("Could not export desc: " + d.getNid() + " on concept " + con.getConceptNid(), e);
			} 
		}
						
		
		return true;
	}

	
	private boolean isSynonym(ConceptVersionBI conceptVersionBI, DescriptionVersionBI<?> d) throws ValidationException, IOException, ContradictionException {
		return d != null &&
			   conceptVersionBI.getDescriptionFullySpecified().getNid() != d.getNid() &&
			   conceptVersionBI.getDescriptionPreferred().getNid() != d.getNid() &&
			   d.getTypeNid() != SnomedMetadataRf2.DEFINITION_RF2.getLenient().getNid();
	}
}
