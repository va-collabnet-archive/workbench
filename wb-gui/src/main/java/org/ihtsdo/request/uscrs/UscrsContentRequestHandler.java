package org.ihtsdo.request.uscrs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.ihtsdo.request.uscrs.USCRSBatchTemplate.COLUMN;
import org.ihtsdo.request.uscrs.USCRSBatchTemplate.PICKLIST_Characteristic_Type;
import org.ihtsdo.request.uscrs.USCRSBatchTemplate.PICKLIST_Refinability;
import org.ihtsdo.request.uscrs.USCRSBatchTemplate.PICKLIST_Relationship_Type;
import org.ihtsdo.request.uscrs.USCRSBatchTemplate.PICKLIST_Semantic_Tag;
import org.ihtsdo.request.uscrs.USCRSBatchTemplate.PICKLIST_Source_Terminology;
import org.ihtsdo.request.uscrs.USCRSBatchTemplate.SHEET;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.id.LongIdBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * USCRS implementation of a {@link ContentRequestHandler}.
 *
 * @author bcarlsenca
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

public class UscrsContentRequestHandler 
{
	/** The request id counter. */
	private static AtomicInteger globalRequestCounter = new AtomicInteger(1);

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(UscrsContentRequestHandler.class);
	
	private static ThreadLocal<Integer> currentRequestId = new ThreadLocal<>();
	private ViewCoordinate vc;
	
	public UscrsContentRequestHandler(int conceptNid) throws IOException, TerminologyException
	{
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		vc = config.getViewCoordinate();
		ConceptVersionBI concept = Ts.get().getConceptVersion(vc, conceptNid);
		if (concept == null)
		{
			JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Unable to load concept for " + conceptNid,
					"USCRS Content Request", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try
		{
			if ((concept.getPathNid() != TermAux.SNOMED_CORE_PATH.getLenient().getNid())
					&& Arrays.binarySearch(config.getEditCoordinate().getEditPaths(), concept.getPathNid()) < 0)
			{
				int response = JOptionPane.showConfirmDialog(LogWithAlerts.getActiveFrame(null), "The concept path is neither Snomed CORE nor a configured edit path."
						+ "\nIt is recommended that you only submit concepts edited on one of these paths to USCRS.\n\nDo you want to continue?",
						"USCRS Content Request", JOptionPane.YES_NO_OPTION);
				
				if (response != JOptionPane.YES_OPTION)
				{
					return;
				}
			}
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Unable to load concepts for path comparison.", "USCRS Content Request",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		try
		{
			UscrsContentRequestTrackingInfo info = submitContentRequest(Arrays.asList(new ConceptVersionBI[] {concept}), null);
			if (info.isSuccessful())
			{
				JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
						"Content request submission successful.\n\nUpload " + info.getFile() + " to here: " + info.getUrl(),
						"USCRS Content Request",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
		catch (Exception e)
		{
			LOG.error("Unexpected error during submit", e);
			e.printStackTrace();//TODO remove this after logging is properly configured - slf4j logs aren't making it out at the moment
			JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Unexpected error trying to submit request:" + e,
					"USCRS Content Request", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	/**
	 * @param concept - only set up for 'newConcept at the moment, pass in the concept you want written as a 'new' concept
	 * @param outputFile - where to write the xls file, or null, to allow the user to be prompted for it.
	 * @return
	 * @throws Exception
	 */
	public static UscrsContentRequestTrackingInfo submitContentRequest(List<ConceptVersionBI> concepts, File outputFile) throws Exception
	{
		LOG.debug("Submit content Request");

		// Ideally this would connect to a request submission instance and dynamically create the request. In lieu
		// of that, we simply create a spreadsheet.

		// Create workbook
		USCRSBatchTemplate bt = new USCRSBatchTemplate(USCRSBatchTemplate.class.getResourceAsStream("/USCRS_Batch_Template-2015-01-27.xls"));
		
		for (ConceptVersionBI concept : concepts)
		{
			currentRequestId.set(globalRequestCounter.getAndIncrement());
			
			// Handle new concept
			handleNewConcept(concept, bt);
	
			// Handle non-isa relationships
			handleNewRels(concept, bt);
		}

		
		// Now determine
		UscrsContentRequestTrackingInfo info = new UscrsContentRequestTrackingInfo();
		info.setName("Batch of " + concepts.size() + " concepts");
		// Save the file
		if (outputFile == null)
		{
		
			LOG.info("Choose file to save");
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory())
					{
						return true;
					}
					else
					{
						if (pathname.getName().toLowerCase().endsWith(".xls"))
						{
							return true;
						}
						return false;
					}
				}
	
				@Override
				public String getDescription() {
					return "Excel files (*.xls)";
				}
			});
	
			// Set extension filter.
			fileChooser.setSelectedFile(new File("USCRS_Export.xls"));

			// Show save file dialog.
			int result = fileChooser.showSaveDialog(LogWithAlerts.getActiveFrame(null));
			if (result == JFileChooser.APPROVE_OPTION)
			{
				outputFile = fileChooser.getSelectedFile();
			}
		}
		
		LOG.info("  file = " + outputFile);
		if (outputFile != null)
		{
			bt.saveFile(outputFile);
			info.setIsSuccessful(true);
			info.setFile(outputFile.toString());
			info.setDetail("Batch USCRS submission spreadsheet successfully created.");
		}
		else
		{
			// Assume user cancelled
			info.setIsSuccessful(false);
			info.setDetail("Submission cancelled.");
		}
		return info;
	}

	/**
	 * Handle new concept spreadsheet tab.
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	private static void handleNewConcept(ConceptVersionBI concept, USCRSBatchTemplate bt) throws Exception
	{
		LOG.debug("  Handle new concept tab");

		LOG.debug("    Add data for " + concept.toUserString());

		String fsn = null;
		

		if (concept.getDescriptions() != null) 
		{
			for (DescriptionChronicleBI desc : concept.getDescriptions()) 
			{
				int versionCount = desc.getVersions().size();
				DescriptionVersionBI<?> descVer = desc.getVersions().toArray(new DescriptionVersionBI[versionCount])[versionCount - 1];
				if (descVer.getTypeNid() == SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid() 
						|| descVer.getTypeNid() == SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid()) 
				{
					if (descVer.getStatusNid() ==  SnomedMetadataRfx.getSTATUS_CURRENT_NID()) 
					{
						fsn = descVer.getText();
						break;
					}
				}
			}
		}

		// PARENTS
		List<Long> parentIds = new ArrayList<>();
		for (RelationshipVersionBI<?> relVersion : concept.getRelationshipsOutgoingActiveIsa())
		{
			parentIds.add(getSCTId(Ts.get().getConcept(relVersion.getTargetNid())));
		}
		LOG.debug("      parents = " + parentIds.size());
		if (parentIds.size() > 3)
		{
			throw new Exception("Cannot handle more than 3 parents");
		}

		//Synonyms
		List<String> synonyms = new ArrayList<>();
		for (DescriptionVersionBI<?> descVersion : concept.getDescriptionsActive())
		{
			// find active, non FSN descriptions not matching the preferred name
			if (descVersion.getTypeNid() != SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid() 
					&& descVersion.getTypeNid() != SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid()
					&& !descVersion.getText().equals(concept.getDescriptionPreferred().getText()))
			{
				synonyms.add(descVersion.getText());
			}
		}
		LOG.debug("      Synonym Count: {}", synonyms.size());

		bt.selectSheet(SHEET.New_Concept);
		bt.addRow();
		int colNumber = 0;
		for (COLUMN c : bt.getColumnsOfSheet(SHEET.New_Concept))
		{
			switch (c)
			{
				case Request_Id:
					bt.addNumericCell(colNumber++, currentRequestId.get());
					break;
				case Topic:
					// TODO: Topic - consider making the user enter this
					bt.addStringCell(colNumber++, "New concept");
					break;
				case Local_Code:
					bt.addStringCell(colNumber++, concept.getPrimUuid().toString());
					break;
				case Local_Term:
					bt.addStringCell(colNumber++, concept.getDescriptionPreferred().getText());
					break;
				case Fully_Specified_Name:
					// Fully Specified Name (without the semantic tag)
					String fsnOnly = fsn;
					if (fsn.indexOf('(') != -1)
					{
						fsnOnly = fsn.substring(0, fsn.lastIndexOf('(') - 1);
					}
					bt.addStringCell(colNumber++, fsnOnly);
					break;
				case Semantic_Tag:
					PICKLIST_Semantic_Tag tag = null;
					if (fsn.indexOf('(') != -1)
					{
						String st = fsn.substring(fsn.lastIndexOf('(') + 1, fsn.lastIndexOf(')'));
						tag = PICKLIST_Semantic_Tag.find(st.toLowerCase());
					}
					if (tag == null)
					{
						throw new Exception("Cannot submit a concept to USCRS without an FSN having a valid semantic tag.");
					}
					bt.addStringCell(colNumber++, tag.toString());
					break;
				case Preferred_Term:
					bt.addStringCell(colNumber++, concept.getDescriptionPreferred().getText());
					break;
				case Terminology_1_:
				case Terminology_2_:
				case Terminology_3_:
					if (parentIds.size() == 0)
					{
						bt.addStringCell(colNumber++, "");
					}
					else
					{
						bt.addStringCell(colNumber++, PICKLIST_Source_Terminology.SNOMED_CT_International.toString());  //TODO this isn't a safe bet
					}
					break;
				case Parent_Concept_Id_1_:
				case Parent_Concept_Id_2_:
				case Parent_Concept__Id_3_:
					if (parentIds.size() == 0)
					{
						bt.addStringCell(colNumber++, "");
					}
					else
					{
						bt.addNumericCell(colNumber++, parentIds.remove(0));
					}
					break;
				case UMLS_CUI:
					bt.addStringCell(colNumber++, "");
					break;
				case Definition:
					// TODO: Definition - consider making the user enter this 
					bt.addStringCell(colNumber++, "See logical definition in relationships");
					break;
				case Proposed_Use:
					// TODO: Proposed Use - consider making the user enter this 
					bt.addStringCell(colNumber++, "");
					break;
				case Justification:
					// TODO: Justification - consider making the user enter this - find out out to find namespace in WB
					bt.addStringCell(colNumber++, "Developed as part of extension namespace ");
					break;
				case Note:
					StringBuilder sb = new StringBuilder();
					if (concept.getConceptAttributesActive() != null && concept.getConceptAttributesActive().isDefined())
					{
						sb.append("NOTE: this concept is fully defined. ");
					}
					//First two synonyms have cols, any others go in Note
					if (synonyms.size() >2 )
					{
						sb.append("NOTE: this concept also has the following synonyms: ");
					}
					while (synonyms.size() > 2)
					{
						sb.append(synonyms.remove(0));
						if (synonyms.size() > 2)
						{
							sb.append(", ");
						}
					}
					bt.addStringCell(colNumber++, sb.toString());
					break;
				case Synonym:
					bt.addStringCell(colNumber++, (synonyms.size() > 0 ? synonyms.remove(0) : ""));
					break;
				default :
					throw new RuntimeException("Unexpected column type found in Sheet: " + c + " - " + SHEET.New_Concept);
			}
		}
	}

	/**
	 * Handle new rels spreadsheet tab
	 *
	 * @param concept the concept
	 * @param bt the wb
	 * @throws Exception the exception
	 */
	private static void handleNewRels(ConceptVersionBI concept, USCRSBatchTemplate bt) throws Exception
	{
		LOG.debug("  Handle non-ISA rels");

		bt.selectSheet(SHEET.New_Relationship);

		for (RelationshipVersionBI<?> relVersion : concept.getRelationshipsOutgoingActive())
		{
			// find active, non-ISA relationships
			// TODO should this be excluding inferred relationships?
			if (relVersion.getTypeNid() != Snomed.IS_A.getLenient().getNid())
			{
				bt.addRow();
				int colNumber = 0;
				for (COLUMN column : bt.getColumnsOfSheet(SHEET.New_Relationship))
				{
					LOG.debug("    Add rel " + relVersion.toUserString());
					switch (column)
					{
						case Topic:
							// TODO: Topic - consider making the user enter this
							bt.addStringCell(colNumber++, "See new concept request");
							break;
						case Source_Terminology:
							// Source Concept Id - aligns with Request Id from the new concept spreadsheet
							bt.addStringCell(colNumber++, PICKLIST_Source_Terminology.Current_Batch_Requests.toString());
							break;
						case Source_Concept_Id:
							bt.addNumericCell(colNumber++, currentRequestId.get());
							break;
						case Relationship_Type:
							//TODO fix this ugly hack on getting the description without referencing a VC
							bt.addStringCell(colNumber++, PICKLIST_Relationship_Type.find(
									Ts.get().getConcept(relVersion.getTypeNid()).getVersions().iterator().next().getDescriptionPreferred().getText()).toString());
							break;
						case Destination_Terminology:
							// Destination Termionlogy - TODO: here we're only supporting things linked to SNOMED, in the future we may need to link
							// to things that have been previously created, but we need tracking info integration to do that properly.
							bt.addStringCell(colNumber++, PICKLIST_Source_Terminology.SNOMED_CT_International.toString());
							break;
						case Destination_Concept_Id:
							bt.addNumericCell(colNumber++, getSCTId(Ts.get().getConcept(relVersion.getTargetNid())));
							break;
						case Characteristic_Type:
							bt.addStringCell(colNumber++, PICKLIST_Characteristic_Type.Defining_relationship.toString());
							break;
						case Refinability:
							bt.addStringCell(colNumber++, PICKLIST_Refinability.Not_refinable.toString());
							break;
						case Relationship_Group:
							bt.addNumericCell(colNumber++, relVersion.getGroup());
							break;
						case Justification:
							//TODO - find out out to find namespace in WB
							bt.addStringCell(colNumber++, "Developed as part of extension namespace");
							break;
						case Note:
							bt.addStringCell(colNumber++, "This is a defining relationship expressed for the corresponding new concept request in the other tab");
							break;
						default :
							throw new RuntimeException("Unexpected column type found in Sheet: " + column + " - " + SHEET.New_Relationship);
					}
				}
			}
		}
	}
	
	private static long getSCTId(ConceptChronicleBI conceptChronicle) throws IOException
	{
		for (IdBI id : conceptChronicle.getAllIds()) {
			if (id.getAuthorityNid() == Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids())) 
			{
				return((LongIdBI) id).getDenotation();
			}
		}
		return -1;
	}
}
