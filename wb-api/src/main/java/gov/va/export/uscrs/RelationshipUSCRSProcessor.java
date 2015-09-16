package gov.va.export.uscrs;

import gov.va.export.uscrs.USCRSBatchTemplate.COLUMN;
import gov.va.export.uscrs.USCRSBatchTemplate.PICKLIST_Characteristic_Type;
import gov.va.export.uscrs.USCRSBatchTemplate.PICKLIST_Refinability;
import gov.va.export.uscrs.USCRSBatchTemplate.PICKLIST_Relationship_Type;
import gov.va.export.uscrs.USCRSBatchTemplate.SHEET;

import java.io.IOException;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelationshipUSCRSProcessor extends USCRSProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(RelationshipUSCRSProcessor.class);
	
	/**
	 * Handle Add Parent spreadsheet tab
	 *
	 * @param concept the concept
	 * @param getBt() the wb
	 * @throws Exception the exception
	 */
	void handleNewParent(RelationshipVersionBI<?> rel) throws Exception
	{
		ConceptVersionBI sourceCon = Ts.get().getConceptVersion(vc, rel.getConceptNid());
		ConceptVersionBI targetCon = Ts.get().getConceptVersion(vc, rel.getTargetNid());
		
		if (activeStatusNids.contains(rel.getStatusNid()) && rel.getTypeNid() == Snomed.IS_A.getLenient().getNid()) 
		{
			if (rel.getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid()) {
				LOG.info("New Parent Handler");
				getBt().selectSheet(SHEET.Add_Parent);
				getBt().addRow();
				for (COLUMN column : getBt().getColumnsOfSheet(SHEET.Add_Parent))
				{
					switch (column)
					{
						case Topic:
							getBt().addStringCell(column, getTopic(sourceCon));
							break;
						case Source_Terminology: 
							getBt().addStringCell(column, getTerminology(sourceCon));
							break;
						case Child_Concept_Id:
							getBt().addStringCell(column, getConSctId(sourceCon));
							break;
						case Destination_Terminology:
							getBt().addStringCell(column, getTerminology(targetCon));
							break;
						case Parent_Concept_Id:  
							getBt().addStringCell(column, getConSctId(targetCon));
							break;
						case Justification:
							getBt().addStringCell(column, getJustification());
							break;
						case Note:
							getCompSctId(rel);
							break;
						default :
							throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Add_Parent);
					}
				}
			}
		}
	}
	
	/**
	 * Handle new rels spreadsheet tab
	 *
	 * @param concept the concept
	 * @throws Exception the exception
	 */	
	void handleNewRel(RelationshipVersionBI<?> rel) throws Exception {
		if (rel.getTypeNid() != Snomed.IS_A.getLenient().getNid()) 
		{
			if (rel.getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid()) {
				ConceptVersionBI sourceCon = Ts.get().getConceptVersion(vc, rel.getConceptNid());
				ConceptVersionBI targetCon = Ts.get().getConceptVersion(vc, rel.getTargetNid());
				ConceptVersionBI typeCon = Ts.get().getConceptVersion(vc, rel.getTypeNid());
	
				LOG.info("New Relationship Handler");
				getBt().selectSheet(SHEET.New_Relationship);
				getBt().addRow();
				for (COLUMN column : getBt().getColumnsOfSheet(SHEET.New_Relationship)) {
					switch (column) {
						case Topic:
							getBt().addStringCell(column, getTopic(sourceCon));
							break;
						case Source_Terminology:
							getBt().addStringCell(column, getTerminology(sourceCon));
							break;
						case Source_Concept_Id:
							getBt().addStringCell(column, getConSctId(sourceCon));
							break;
						case Relationship_Type:
							getBt().addStringCell(column, getRelType(typeCon));
							break;
						case Destination_Terminology:
							getBt().addStringCell(column, getTerminology(targetCon));
							break;
						case Destination_Concept_Id:
							getBt().addStringCell(column, getConSctId(targetCon));
							break;
						case Characteristic_Type:
							getBt().addStringCell(column, getCharType(rel.getCharacteristicNid()));
							break;
						case Refinability:
							getBt().addStringCell(column, getRefinability(rel.getRefinabilityNid()));
							break;
						case Relationship_Group:
							getBt().addNumericCell(column, rel.getGroup());
							break;
						case Justification:
							getBt().addStringCell(column, getJustification());
							break;
						case Note:
							getCompSctId(rel);
							break;
						default :
							throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Relationship);
					}
				}
			}
		}
	}
	
	/**
	 * Handle Change parent spreadsheet tab
	 *
	 * @param concept the concept
	 * @throws Exception the exception
	 */
	void handleChangeParent(RelationshipVersionBI<?> rel) throws Exception
	{	
		ConceptVersionBI sourceCon = Ts.get().getConceptVersion(vc, rel.getConceptNid());
		ConceptVersionBI targetCon = Ts.get().getConceptVersion(vc, rel.getTargetNid());

		if (rel.getTypeNid() == Snomed.IS_A.getLenient().getNid()) 
		{
			if (rel.getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid()) {
				getBt().selectSheet(SHEET.Change_Parent);
				getBt().addRow();
				for (COLUMN column : getBt().getColumnsOfSheet(SHEET.Change_Parent)) {
					switch(column)
					{
						case Topic:
							getBt().addStringCell(column, getTopic(sourceCon));
							break;
						case Source_Terminology:
							getBt().addStringCell(column, getTerminology(sourceCon));
							break;
						case Concept_Id:
							getBt().addStringCell(column, getConSctId(sourceCon));
							break;
						case New_Parent_Concept_Id:
							getBt().addStringCell(column, getConSctId(targetCon));
							break;
						case New_Parent_Terminology:
							getBt().addStringCell(column, getTerminology(targetCon));
							break;
						case Justification:
							getBt().addStringCell(column, getJustification());
							break;
						case Note:
							getCompSctId(rel);
							break;
						default :
							throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Change_Parent);
					}
				}
			}
		}
	}
	
	/**
	 * Handle Change Relationships spreadsheet tab
	 *
	 * @param concept the concept
	 * @param getBt() the wb
	 * @throws Exception the exception
	 */
	void handleChangeRels(RelationshipVersionBI<?> rel) throws Exception
	{
		ConceptVersionBI sourceCon = Ts.get().getConceptVersion(vc, rel.getConceptNid());
		ConceptVersionBI targetCon = Ts.get().getConceptVersion(vc, rel.getTargetNid());
		ConceptVersionBI typeCon = Ts.get().getConceptVersion(vc, rel.getTypeNid());

		if (rel.getTypeNid() != Snomed.IS_A.getLenient().getNid()) 
		{
			if (rel.getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid()) {
				getBt().selectSheet(SHEET.Change_Relationship);
				getBt().addRow();
				for (COLUMN column : getBt().getColumnsOfSheet(SHEET.Change_Relationship))
				{
					switch (column)
					{
						case Topic:
							getBt().addStringCell(column, getTopic(sourceCon ));
							break;
						case Source_Concept_Id:
							getBt().addStringCell(column, getConSctId(sourceCon ));
							break;
						case Relationship_Id:  
							getBt().addStringCell(column, getCompSctId(rel));
							break;
						case Relationship_Type: 
							getBt().addStringCell(column, getRelType(typeCon));
							break;
						case Source_Terminology:
							getBt().addStringCell(column, getTerminology(sourceCon));
							break;
						case Destination_Concept_Id:
							getBt().addStringCell(column, getConSctId(targetCon));
							break;
						case Destination_Terminology:
							getBt().addStringCell(column, getTerminology(targetCon));
							break;
						case Characteristic_Type:
							getBt().addStringCell(column, getCharType(rel.getCharacteristicNid()));
							break;
						case Refinability:
							getBt().addStringCell(column, getRefinability(rel.getRefinabilityNid()));
							break;
						case Relationship_Group:
							getBt().addNumericCell(column, rel.getGroup());
							break;
						case Justification:
							getBt().addStringCell(column, getJustification());
							break;
						case Note:
							getCompSctId(rel);
							break;
						default :
							throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Change_Relationship);
					}
				}
			}
		}
	}
	
	/**
	 * Handle Retire Relationship spreadsheet tab
	 *
	 * @param concept the concept
	 * @param getBt() the wb
	 * @throws Exception the exception
	 */
	void handleRetireRelationship(RelationshipVersionBI<?> rel) throws Exception
	{
		ConceptVersionBI sourceCon = Ts.get().getConceptVersion(vc, rel.getConceptNid());
		ConceptVersionBI targetCon = Ts.get().getConceptVersion(vc, rel.getTargetNid());
		ConceptVersionBI typeCon = Ts.get().getConceptVersion(vc, rel.getTypeNid());
		
		getBt().selectSheet(SHEET.Retire_Relationship);
		getBt().addRow();
		for (COLUMN column : getBt().getColumnsOfSheet(SHEET.Retire_Relationship))
		{
			if (rel.getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid()) {
				switch (column)
				{
					case Topic:
						getBt().addStringCell(column, getTopic(sourceCon));
						break;
					case Source_Terminology:
						getBt().addStringCell(column, getTerminology(sourceCon));
						break;
					case Source_Concept_Id:
						getBt().addStringCell(column, getConSctId(sourceCon ));
						break;
					case Relationship_Id:  
						getBt().addStringCell(column, getCompSctId(rel));
						break;
					case Destination_Terminology:
						getBt().addStringCell(column, getTerminology(targetCon));
						break;
					case Destination_Concept_Id:
						getBt().addStringCell(column, getConSctId(targetCon));
						break;
					case Relationship_Type:
						getBt().addStringCell(column, getRelType(typeCon));
						break;
					case Justification:
						getBt().addStringCell(column, getJustification());
						break;
					case Note:
						getCompSctId(rel);
						break;
					default :
						throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Retire_Relationship);
				}
			}
		}
	}
	
	private String getRelType(ConceptVersionBI typeCon) throws IOException, ContradictionException {
		try {
			return PICKLIST_Relationship_Type.find(typeCon.getDescriptionPreferred().getText()).toString();	
		} catch(EnumConstantNotPresentException ecnpe) {
			LOG.error("USCRS PICKLIST API Missing Relationship Type Value " + ecnpe.constantName());
			return "";
		} catch(Exception e) {
			LOG.error("USCRS Rel Type Error");
			return "";
		}
	}
	
    private String getCharType(int characteristicNid) throws IOException, ContradictionException {
		try {
			String characteristic = Ts.get().getConceptVersion(vc, characteristicNid).getDescriptionPreferred().getText(); 
			if(characteristic.equalsIgnoreCase("stated")) {
				return "Stated";
				//return PICKLIST_Characteristic_Type.Defining_relationship.toString(); 
			} else if(characteristic.equalsIgnoreCase("other-term")) {
				return PICKLIST_Characteristic_Type.Qualifying_relationship.toString(); //TOODO: Map Correct
			} else if(characteristic.equalsIgnoreCase("other-term")) {
				return PICKLIST_Characteristic_Type.Additional_relationship.toString(); //TOODO: Map Correct
			}
			return characteristic; //But this works temporarily
		} catch(EnumConstantNotPresentException ecnpe) {
			LOG.error("USCRS PICKLIST API Missing Characteristic Type Value " + ecnpe.constantName());
			return "";
		} catch(Exception e) {
			LOG.error("USCRS Characteristic Type Error");
			return "";
		}
	}

    private String getRefinability(int refinabilityNid) throws IOException, ContradictionException {
    	try {
    		String desc = Ts.get().getConceptVersion(vc, refinabilityNid).getDescriptionPreferred().getText();
			String descToPicklist = desc;
			
			//Map the words optional and mandatory to their equal ENUMS b/c of API limitations
			if(desc.equals("Optional refinability")) {
				descToPicklist = "Optional";
			} else if(desc.equals("Mandatory refinability")) {
				descToPicklist = "Mandatory";
			} else {
				descToPicklist = desc;
			}
				
			return PICKLIST_Refinability.find(descToPicklist).toString();
		} catch(EnumConstantNotPresentException ecnpe) {
			LOG.error("USCRS PICKLIST API Missing Refinability Value " + ecnpe.constantName());
			return "";
		} catch(Exception e) {
			LOG.error("USCRS Refinability Type Error");
			return "";
		}	}


}
