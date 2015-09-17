package gov.va.export.uscrs;

import gov.va.export.uscrs.USCRSBatchTemplate.COLUMN;
import gov.va.export.uscrs.USCRSBatchTemplate.PICKLIST_Case_Significance;
import gov.va.export.uscrs.USCRSBatchTemplate.SHEET;

import java.io.IOException;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DescriptionUSCRSProcessor extends USCRSProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(DescriptionUSCRSProcessor.class);

	/**
	 * Handle new Synonyms spreadsheet tab
	 *
	 * @param concept the concept
	 * @param getBt() the wb
	 * @throws Exception the exception
	 */
    void handleNewSyn(DescriptionVersionBI<?> descVersion) throws Exception
	{	
		ConceptVersionBI conVersion = Ts.get().getConceptVersion(vc, descVersion.getConceptNid());

		LOG.info("Creating new Synonym (Description)");
		getBt().selectSheet(SHEET.New_Synonym);
		getBt().addRow();
		
		for (COLUMN column : getBt().getColumnsOfSheet(SHEET.New_Synonym)) {
			switch(column)
			{
			case Topic:
				getBt().addStringCell(column, getTopic(conVersion));
				break;
			case Terminology:
				getBt().addStringCell(column, getTerminology(conVersion));
				break;
			case Concept_Id:
				getBt().addStringCell(column, getConSctId(conVersion));
				break;
			case Term:
				getBt().addStringCell(column, descVersion.getText());
				break;
			case Case_Significance:
				getBt().addStringCell(column, getCaseSig(descVersion.isInitialCaseSignificant()));
				break;
			case Justification:
				getBt().addStringCell(column, getJustification());
				break;
			case Note: 
				getBt().addStringCell(column, getNote(descVersion));
				break;
			default :
				throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Synonym);
			}
		}
	}		


	/**
	 * Pass in an ArrayList of description versions and a workbook and a new row will be created for each description 
	 * in the corresponding notebook
	 *
	 * @param ArrayList<DescriptionVersionBI<?>> descVersion an ArrayList of DescriptionVersions that will be added
	 * @param getBt() the wb
	 * @throws Exception the exception
	 */
	void handleChangeDesc(DescriptionVersionBI<?> d) throws Exception
	{
		ConceptVersionBI conVersion = Ts.get().getConceptVersion(vc, d.getConceptNid());

		getBt().selectSheet(SHEET.Change_Description);
		getBt().addRow();
		for (COLUMN column : getBt().getColumnsOfSheet(SHEET.Change_Description))
		{
			switch (column)
			{
				case Topic:
					try {
						getBt().addStringCell(column, getTopic(conVersion));
					} catch(Exception e) {
						LOG.error("Error Creating Desc Topic", e);
					}
					break;
				case Terminology:
					getBt().addStringCell(column, getTerminology(conVersion));
					break;
				case Concept_Id:
					getBt().addStringCell(column, getConSctId(conVersion));
					break;
				case Description_Id:
					getBt().addStringCell(column, getCompSctId(d));
					break;
				case Term: 
					getBt().addStringCell(column, d.getText());
					break;
				case Case_Significance:
					getBt().addStringCell(column, getCaseSig(d.isInitialCaseSignificant()));
					break;
				case Justification:
					getBt().addStringCell(column, getJustification());
					break;
				case Note:
					getBt().addStringCell(column, getNote(d));
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Change_Description);
			}
		}
	}
	
	/**
	 * Handle Retire Description spreadsheet tab
	 *
	 * @param concept the concept
	 * @param getBt() the wb
	 * @throws Exception the exception
	 */
	void handleRetireDescription(DescriptionVersionBI<?> d) throws Exception
	{
		ConceptVersionBI conVersion = Ts.get().getConceptVersion(vc, d.getConceptNid());
		
		getBt().selectSheet(SHEET.Retire_Description);
		getBt().addRow();
		for (COLUMN column : getBt().getColumnsOfSheet(SHEET.Retire_Description))
		{
			switch (column)
			{
				case Topic:
					getBt().addStringCell(column, getTopic(conVersion));
					break;
				case Terminology:
					getBt().addStringCell(column, getTerminology(conVersion));
					break;
				case Concept_Id:
					getBt().addStringCell(column, getConSctId(conVersion));
					break;
				case Description_Id:
					getBt().addStringCell(column, getCompSctId(d));
					break;
				case Change_Description_Status_To:
					getBt().addStringCell(column, USCRSBatchTemplate.PICKLIST_Change_Concept_Status_To.Retired.toString());
					break;
				case Justification:
					getBt().addStringCell(column, getJustification());
					break;
				case Note:
					getBt().addStringCell(column, getNote(d));
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.Retire_Description);
			}
		}
	}

	
	private String getCaseSig(boolean caseSig) {
		try {
			if(caseSig) {
				return PICKLIST_Case_Significance.Entire_term_case_sensitive.toString();
			} else {
				return PICKLIST_Case_Significance.Entire_term_case_insensitive.toString();
			}
		} catch(EnumConstantNotPresentException ecnpe) {
			LOG.error("USCRS PICKLIST API Missing Case Sifnificance " + ecnpe.constantName());
			return "";
		} catch(Exception e) {
			LOG.error("USCRS Case Sifnificance Error");
			return "";
		}
	}

	public boolean isSynonym(ConceptVersionBI conceptVersionBI, DescriptionVersionBI<?> d) throws ValidationException, IOException, ContradictionException {
		return d != null &&
			   conceptVersionBI.getDescriptionFullySpecified().getNid() != d.getNid() &&
			   !isPreferredTerm(d) &&
			   d.getTypeNid() != SnomedMetadataRf2.DEFINITION_RF2.getLenient().getNid();
	}
}

