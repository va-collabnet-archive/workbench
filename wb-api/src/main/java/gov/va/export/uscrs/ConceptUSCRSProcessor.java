package gov.va.export.uscrs;

import gov.va.export.uscrs.USCRSBatchTemplate.COLUMN;
import gov.va.export.uscrs.USCRSBatchTemplate.PICKLIST_Semantic_Tag;
import gov.va.export.uscrs.USCRSBatchTemplate.SHEET;

import java.util.ArrayList;
import java.util.LinkedList;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConceptUSCRSProcessor extends USCRSProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConceptUSCRSProcessor.class);

	/**
	 * Creates a new row in the "New Concept" tab of the workbook passed in. It returns the ISA relationships
	 * if there are more than 3.It also returns all non ISA relationships
	 *
	 * @param concept the concept
	 * @param previousExportTime 
	 * @param getBt() the wb
	 * @return ArrayList<RelationshipVersionBI> extra relationships (if more than 3 ISA, those are returned), plus all non ISA
	 * @throws Exception the exception
	 */
	ArrayList<RelationshipVersionBI<?>> handleNewConcept(ConceptChronicleBI concept, long previousExportTime) throws Exception
	{
		ArrayList<RelationshipVersionBI<?>> extraRels = new ArrayList<RelationshipVersionBI<?>>();
		// PARENTS
		LinkedList<Integer> parentNids = new LinkedList<Integer>();
		LinkedList<String> parentsTerms = new LinkedList<String>();

		int count = 0;
		for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing())
		{
			RelationshipVersionBI<?> relVersion = rel.getVersion(vc);
			
			if(relVersion != null) {
				if(activeStatusNids.contains(relVersion.getStatusNid())) 
				{
					if ((relVersion.getTypeNid() == Snomed.IS_A.getLenient().getNid())) 
					{
						if (relVersion.getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid()) {
							int relDestNid = relVersion.getTargetNid();
							parentNids.add(count, relDestNid);
							ComponentVersionBI compVer = Ts.get().getComponentVersion(vc, relDestNid);
							ConceptVersionBI destConVer = Ts.get().getConceptVersion(vc, compVer.getConceptNid());
							
							parentsTerms.add(count, getTerminology(destConVer));
							
							if(count > 2 && relVersion != null) {
								extraRels.add(relVersion);
							}
							count++;
						}
					} else {
						extraRels.add(relVersion);
					}
				}
			}
		}
		
		getBt().selectSheet(SHEET.New_Concept);
		getBt().addRow();
		ConceptVersionBI conVer = concept.getVersion(vc);

		for (COLUMN column : getBt().getColumnsOfSheet(SHEET.New_Concept))
		{
			switch (column)
			{
				case Request_Id:
					int reqId = Integer.parseInt(getConSctId(conVer));
					if (reqId > 999999) 
					{
						getBt().addStringCell(column, "");
					} else {
						if(previousExportTime < 0) {
							//throw new RuntimeException("We appear to have found an SCTID when we only expected a generated sequence ID");
						}
						newConceptRequestIds.add(reqId);
						getBt().addNumericCell(column, reqId);
					}
					break;
				case Topic:
					getBt().addStringCell(column, getTopic(concept));
					break;
				case Local_Code:
					getBt().addStringCell(column, concept.getPrimUuid().toString());
					break;
				case Local_Term: 
					getBt().addStringCell(column, getPreferredTerm(conVer, vc));
					break;
				case Fully_Specified_Name:
					getBt().addStringCell(column, getFsnWithoutSemTag(conVer));
					break;
				case Semantic_Tag:
					getBt().addStringCell(column, getSemanticTag(conVer));
					break;
				case Preferred_Term:
					getBt().addStringCell(column, getPreferredTerm(conVer, vc));
					break;
				//Note that this logic is fragile, and will break, if we encounter a parentConcept column before the corresponding terminology column....
				//but we should be processing them in order, as far as I know.
				case Terminology_1_:
				case Terminology_2_:
				case Terminology_3_:
					if (parentNids.size() >= 1)
					{
						getBt().addStringCell(column, getTerminology(Ts.get().getConceptVersion(vc, parentNids.get(0))));
					}
					else
					{
						getBt().addStringCell(column, "");
					}
					break;
				case Parent_Concept_Id_1_:
				case Parent_Concept_Id_2_:
				case Parent_Concept__Id_3_:
					if(parentNids.size() >= 1) 
					{
						 ConceptVersionBI parCon = Ts.get().getConceptVersion(vc, parentNids.remove(0));
						getBt().addStringCell(column, getConSctId(parCon));
						
					} else 
					{
						getBt().addStringCell(column, "");
					}
					break;
				case UMLS_CUI:
					getBt().addStringCell(column, ""); //Not in API
					break;
				case Definition:
					getBt().addStringCell(column, "Needed for VA purposes");
					break;
				case Proposed_Use:
					getBt().addStringCell(column, ""); //User Input
					break;
				case Justification:
					getBt().addStringCell(column, getJustification());
					break;
				case Note:
					getBt().addStringCell(column, getNote(conVer));
					break;
				case Synonym:
					getBt().addStringCell(column, "");
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Concept);
			}
		}
		return extraRels;
	}

	/**
	 * Returns the FSN, with the sematic tag removed.
	 * 
	 * @param concept
	 * @return FSN with-out the semantic tag
	 * @throws Exception
	 */
	private String getFsnWithoutSemTag(ConceptVersionBI concept) throws Exception {
		String fsn = concept.getDescriptionFullySpecified().getText();
		
		String fsnOnly;
		if(fsn == null) {
			throw new Exception("FSN Could not be retreived");
		} else {
		
			fsnOnly = fsn;
			if (fsn.indexOf('(') != -1)
			{
				fsnOnly = fsn.substring(0, fsn.lastIndexOf('(') - 1);
			}
		}
		return fsnOnly;
	}

	/**
	 * Takes a concept and it returns the semantic tag, pulled from the FSN, 
	 * and selected from the PICKLIST (todo: enable PICKLIST selection)
	 * @param concept
	 * @return the Semantic tag from the FSN
	 * @throws Exception
	 */
	private String getSemanticTag(ConceptVersionBI concept) throws Exception {
		String fsn = concept.getDescriptionFullySpecified().getText();

		if (fsn.indexOf('(') != -1)
		{
			String st = fsn.substring(fsn.lastIndexOf('(') + 1, fsn.lastIndexOf(')'));
			try {
				return PICKLIST_Semantic_Tag.find(st).toString();
			} catch(Exception e) {
				LOG.error("Error choosing Semtantic Tag for " + fsn + " from PICKLIST USCRS Batch Templte for Concept UUID: " + concept.getPrimUuid());
			}
			return st; //T0DO** FIX Task: The picklist does not support all the various semantic tags that the API returns
		} else {
			return null;
		}
	}

	/**
	 * Handle Retire Concept spreadsheet tab
	 *
	 * @param concept the concept
	 * @param getBt() the wb
	 * @throws Exception the exception
	 */
	void handleRetireConcept(ConceptVersionBI concept) throws Exception
	{
		getBt().selectSheet(SHEET.Retire_Concept);
		getBt().addRow();
		for (COLUMN column : getBt().getColumnsOfSheet(SHEET.Retire_Concept))
		{
			switch (column)
			{
				case Topic:
					getBt().addStringCell(column, this.getTopic(concept));
					break;
				case Terminology:
					getBt().addStringCell(column, getTerminology(concept));
					break;
				case Concept_Id:
					getBt().addStringCell(column, getConSctId(concept));
					break;
				case Change_Concept_Status_To: 
					getBt().addStringCell(column, USCRSBatchTemplate.PICKLIST_Change_Concept_Status_To.Retired.toString());
					break;
				case Duplicate_Concept_Id: 
					getBt().addStringCell(column, "");
					break;
				case Justification:
					getBt().addStringCell(column, getJustification());
					break;
				case Note:
					getBt().addStringCell(column, getNote(concept));
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Retire_Concept);
			}
		}
	}

}
