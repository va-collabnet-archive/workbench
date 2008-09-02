package org.dwfa.ace.task.search;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.awt.*;

import javax.swing.*;

import org.dwfa.ace.api.*;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/listview", type = BeanType.TASK_BEAN) })
public class SearchReplaceTermsInList extends AbstractTask {
	
	private I_TermFactory termFactory;
	private I_ConfigAceFrame config;
	
	private I_GetConceptData FSN_UUID;
	private I_GetConceptData PFT_UUID;
	private I_GetConceptData SYNONYM_UUID;

	// Variables from the UI
	private String searchString = "";
	private String replaceString = "";
	private boolean caseSensitive = false;
	private boolean searchAll = true;
	private boolean searchFsn = true;
	private boolean searchPft = true;
	private boolean searchSynonym = true;
	private String signpostOutput = "";
	
	private String searchStringPropName = ProcessAttachmentKeys.FIND_TEXT.getAttachmentKey();
	private String replaceStringPropName = ProcessAttachmentKeys.REPLACE_TEXT.getAttachmentKey();
	private String caseSensitivePropName = ProcessAttachmentKeys.CASE_SENSITIVITY.getAttachmentKey();
	private String searchAllPropName = ProcessAttachmentKeys.SEARCH_ALL.getAttachmentKey();
	private String searchFsnPropName = ProcessAttachmentKeys.SEARCH_FSN.getAttachmentKey();
	private String searchPftPropName = ProcessAttachmentKeys.SEARCH_PT.getAttachmentKey();
	private String searchSynonymPropName = ProcessAttachmentKeys.SEARCH_SYNONYM.getAttachmentKey();

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do...

	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		try {
			
			termFactory = LocalVersionedTerminology.get();
			
			config = (I_ConfigAceFrame) worker
					.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
			
			int currentUnreviewedId = termFactory.getConcept(
                    ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids()).getConceptId();
			FSN_UUID = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
			PFT_UUID = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
			SYNONYM_UUID = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
			
			try {
				searchString = "" + process.readProperty(searchStringPropName);
				replaceString = "" + process.readProperty(replaceStringPropName);
				caseSensitive = Boolean.valueOf("" + process.readProperty(caseSensitivePropName));
				searchAll = Boolean.valueOf("" + process.readProperty(searchAllPropName));
				searchFsn = Boolean.valueOf("" + process.readProperty(searchFsnPropName));
				searchPft = Boolean.valueOf("" + process.readProperty(searchPftPropName));
				searchSynonym = Boolean.valueOf("" + process.readProperty(searchSynonymPropName));
			} catch (IllegalArgumentException e) {
				throw new TaskFailedException("Failed reading properties from process: ", e);
			} catch (IntrospectionException e) {
				throw new TaskFailedException("Failed reading properties from process: ", e);
			} catch (IllegalAccessException e) {
				throw new TaskFailedException("Failed reading properties from process: ", e);
			} catch (InvocationTargetException e) {
				throw new TaskFailedException("Failed reading properties from process: ", e);
			}
			
			// create list of IDs of description types to check (e.g. preferred term)
            I_IntSet descriptionTypesToCheck = termFactory.newIntSet();            
            if (searchFsn) {
            	descriptionTypesToCheck.add(FSN_UUID.getConceptId());
            }
            if (searchPft) {
            	descriptionTypesToCheck.add(PFT_UUID.getConceptId());
            }
            if (searchSynonym) {
            	descriptionTypesToCheck.add(SYNONYM_UUID.getConceptId());
            }
            
			JList conceptList = config.getBatchConceptList();
			I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
			List<SearchReplaceDescription> searchReplaceDescriptions = new ArrayList<SearchReplaceDescription>();
			
			// Basically, no replace string was entered... which is a valid expression of replace matches with an empty string
			if (replaceString == null) {
				replaceString = "";
			}

            String processedDescriptions = "";

            if ((searchString != null && !searchString.equals("")) &&
					(searchAll || searchFsn || searchPft || searchSynonym)) {
				// For each concept in the list view
				for (int i = 0; i < model.getSize(); i++) {
					
					// Get the current concept
					I_GetConceptData child = model.getElementAt(i);
					
		            Set<I_Position> positionsToCheck = config.getViewPositionSet();
		            
		            // get latest descriptions
		            List<I_DescriptionTuple> descriptionTuples =
		                child.getDescriptionTuples(null, (searchAll ? null : descriptionTypesToCheck), positionsToCheck);
					
		            // For the current FSN and PT of this concept
					for (I_DescriptionTuple description : descriptionTuples) {

                        if (!processedDescriptions.contains(":" + description.getDescId() + ":")) {
                            // If it contains the search string
                            if ((caseSensitive && description.getText().contains(searchString)) ||
                                    (!caseSensitive && description.getText().toUpperCase().contains(searchString.toUpperCase()))) {

                                // If not case sensitive, create regex prepending text
                                String searchPrepend = "";
                                if (!caseSensitive) {
                                    searchPrepend = "(?i)";
                                }

                                String origDesc = description.getText();
                                String finalDesc = description.getText().replaceAll(searchPrepend + searchString, replaceString);
                                String origDescHtml = description.getText().replaceAll(searchPrepend + searchString, "<font color='red'>" + searchString + "</font>");
                                String finalDescHtml = description.getText().replaceAll(searchPrepend + searchString, "<font color='green'>" + replaceString + "</font>");

                                String descType = termFactory.getConcept(description.getTypeId()).getInitialText();

                                // Add to our list of descriptions that will require change
                                SearchReplaceDescription srDesc = new SearchReplaceDescription(origDesc, finalDesc, origDescHtml, finalDescHtml, descType);
                                searchReplaceDescriptions.add(srDesc);

                                Set<I_Path> paths = config.getEditingPathSet();

                                for (I_Path path : paths) {

                                    // update the description with the search/replace alternative
                                    I_DescriptionPart newPart = description.duplicatePart();
                                    newPart.setPathId(path.getConceptId());
                                    newPart.setText(finalDesc);
                                    newPart.setStatusId(currentUnreviewedId);
                                    newPart.setVersion(Integer.MAX_VALUE);
                                    description.getDescVersioned().addVersion(newPart);

                                    termFactory.addUncommitted(child);
                                }
                            }
                            processedDescriptions += ":" + description.getDescId() + ":";
                        }
                    }
                }
				
				signpostOutput = "<html>\n" +
 					  "<TABLE width='100%' cellpadding='1' cellspacing='0' border='1' style='font-family:arial;font-size:10px;' bgcolor='white' bordercolor='gray'>\n" +
					  "<TR>\n" +
					  "  <TD bgcolor='#00CCCC' align='center'>Update No.</TD>\n" +
					  "  <TD bgcolor='#00CCCC'>Description Type</TD>\n" +
					  "  <TD bgcolor='#00CCCC'>Original Value</TD>\n" +
					  "  <TD bgcolor='#00CCCC'>Updated Value</TD>\n" +
					  "</TR>\n";					  
				
				int i = 0;
				// Output the original and final versions of the string
				for (SearchReplaceDescription desc : searchReplaceDescriptions) {
					
					i++;
					signpostOutput += "<TR>\n" +
					  "  <TD align='center'>" + i + "/" + searchReplaceDescriptions.size() + "</TD>\n" +
					  "  <TD>" + desc.getDescType() + "</TD>\n" +
					  "  <TD>" + desc.getOrigDescHtml() + "</TD>\n" +
					  "  <TD>" + desc.getFinalDescHtml() + "</TD>\n" +
					  "</TR>\n";
				}
				
				if (searchReplaceDescriptions.isEmpty()) {
					signpostOutput = "<html><h1>NO MATCHES FOUND</h1></html>";
				} else {
					signpostOutput += "</TABLE>\n" +
						"</html>";
				}
			} else if (searchString == null || searchString.equals("")) {
				signpostOutput = "<html><h1>NO SEARCH STRING ENTERED</h1></html>";
			} else {
				signpostOutput = "<html><h1>NO DESCRIPTION TYPE SELECTED</h1></html>";
			}
			
			displayOutput();

			return Condition.CONTINUE;
		} catch (IOException e) {
			throw new TaskFailedException(e);
		} catch (TerminologyException e) {
			throw new TaskFailedException(e);
		}
	}

	private void displayOutput() {
		
		JPanel signpostPanel = config.getSignpostPanel();
		signpostPanel.removeAll();
        signpostPanel.validate();
//        JLabel outputLabel = new JLabel(signpostOutput, JLabel.CENTER);
//		JScrollPane scrollPane = new JScrollPane(outputLabel);
//        scrollPane.add(outputLabel);
//        scrollPane.setPreferredSize(new Dimension(800, 300));
//        signpostPanel.add(scrollPane);

        JEditorPane htmlPane = new JEditorPane("text/html", signpostOutput);
        htmlPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(htmlPane);
        signpostPanel.add(scrollPane, BorderLayout.CENTER);
        scrollPane.setPreferredSize(new Dimension(900, 180));

//        signpostPanel.setPreferredSize(new Dimension(1200,300));
//        signpostPanel.validate();
    }

	public String getSearchStringPropName() {
		return searchStringPropName;
	}

	public void setSearchStringPropName(String searchStringPropName) {
		this.searchStringPropName = searchStringPropName;
	}

	public String getReplaceStringPropName() {
		return replaceStringPropName;
	}

	public void setReplaceStringPropName(String replaceStringPropName) {
		this.replaceStringPropName = replaceStringPropName;
	}

	public String getCaseSensitivePropName() {
		return caseSensitivePropName;
	}

	public void setCaseSensitivePropName(String caseSensitivePropName) {
		this.caseSensitivePropName = caseSensitivePropName;
	}

	public String getSearchAllPropName() {
		return searchAllPropName;
	}

	public void setSearchAllPropName(String searchAllPropName) {
		this.searchAllPropName = searchAllPropName;
	}

	public String getSearchFsnPropName() {
		return searchFsnPropName;
	}

	public void setSearchFsnPropName(String searchFsnPropName) {
		this.searchFsnPropName = searchFsnPropName;
	}

	public String getSearchPftPropName() {
		return searchPftPropName;
	}

	public void setSearchPftPropName(String searchPftPropName) {
		this.searchPftPropName = searchPftPropName;
	}

	public String getSearchSynonymPropName() {
		return searchSynonymPropName;
	}

	public void setSearchSynonymPropName(String searchSynonymPropName) {
		this.searchSynonymPropName = searchSynonymPropName;
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}
}
