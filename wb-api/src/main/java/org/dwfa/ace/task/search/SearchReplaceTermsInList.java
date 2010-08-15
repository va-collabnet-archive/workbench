/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.task.search;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
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
import org.ihtsdo.tk.api.PathBI;

@BeanList(specs = { @Spec(directory = "tasks/ide/listview", type = BeanType.TASK_BEAN) })
public class SearchReplaceTermsInList extends AbstractTask {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    private static int objDataVersion = -1;

    private String searchStringPropName = ProcessAttachmentKeys.FIND_TEXT.getAttachmentKey();
    private String replaceStringPropName = ProcessAttachmentKeys.REPLACE_TEXT.getAttachmentKey();
    private String caseSensitivePropName = ProcessAttachmentKeys.CASE_SENSITIVITY.getAttachmentKey();
    private String searchAllPropName = ProcessAttachmentKeys.SEARCH_ALL.getAttachmentKey();
    private String searchFsnPropName = ProcessAttachmentKeys.SEARCH_FSN.getAttachmentKey();
    private String searchPftPropName = ProcessAttachmentKeys.SEARCH_PT.getAttachmentKey();
    private String searchSynonymPropName = ProcessAttachmentKeys.SEARCH_SYNONYM.getAttachmentKey();
    private String retireAsStatusPropName = ProcessAttachmentKeys.RETIRE_AS_STATUS.getAttachmentKey();

    private List<I_GetConceptData> statuses;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(searchStringPropName);
        out.writeObject(replaceStringPropName);
        out.writeObject(caseSensitivePropName);
        out.writeObject(searchAllPropName);
        out.writeObject(searchFsnPropName);
        out.writeObject(searchPftPropName);
        out.writeObject(searchSynonymPropName);
        out.writeObject(retireAsStatusPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            searchStringPropName = (String) in.readObject();
            replaceStringPropName = (String) in.readObject();
            caseSensitivePropName = (String) in.readObject();
            searchAllPropName = (String) in.readObject();
            searchFsnPropName = (String) in.readObject();
            searchPftPropName = (String) in.readObject();
            searchSynonymPropName = (String) in.readObject();
            retireAsStatusPropName = ProcessAttachmentKeys.RETIRE_AS_STATUS.getAttachmentKey();
        } else if (objDataVersion >= 2) {
            searchStringPropName = (String) in.readObject();
            replaceStringPropName = (String) in.readObject();
            caseSensitivePropName = (String) in.readObject();
            searchAllPropName = (String) in.readObject();
            searchFsnPropName = (String) in.readObject();
            searchPftPropName = (String) in.readObject();
            searchSynonymPropName = (String) in.readObject();
            retireAsStatusPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {

            I_TermFactory termFactory = Terms.get();

            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            int retiredConceptId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids()
                .iterator()
                .next());
            I_GetConceptData FSN_UUID = termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
            I_GetConceptData PFT_UUID = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
            I_GetConceptData SYNONYM_UUID = termFactory.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());

            String searchString = "" + process.getProperty(searchStringPropName);
            String replaceString = "" + process.getProperty(replaceStringPropName);
            boolean caseSensitive = Boolean.valueOf("" + process.getProperty(caseSensitivePropName));
            boolean searchAll = Boolean.valueOf("" + process.getProperty(searchAllPropName));
            boolean searchFsn = Boolean.valueOf("" + process.getProperty(searchFsnPropName));
            boolean searchPft = Boolean.valueOf("" + process.getProperty(searchPftPropName));
            boolean searchSynonym = Boolean.valueOf("" + process.getProperty(searchSynonymPropName));
            int retireAsStatus = Integer.valueOf("" + process.getProperty(retireAsStatusPropName));

            // create list of IDs of description types to check (e.g. preferred
            // term)
            I_IntSet descriptionTypesToCheck = termFactory.newIntSet();
            if (searchFsn) {
                descriptionTypesToCheck.add(FSN_UUID.getConceptNid());
            }
            if (searchPft) {
                descriptionTypesToCheck.add(PFT_UUID.getConceptNid());
            }
            if (searchSynonym) {
                descriptionTypesToCheck.add(SYNONYM_UUID.getConceptNid());
            }

            JList conceptList = config.getBatchConceptList();
            I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
            List<SearchReplaceDescription> searchReplaceDescriptions = new ArrayList<SearchReplaceDescription>();

            // Basically, no replace string was entered... which is a valid
            // expression of replace matches with an empty string
            if (replaceString == null) {
                replaceString = "";
            }

            String processedDescriptions = "";
            String signpostOutput = "";
            if ((searchString != null && !searchString.equals(""))
                && (searchAll || searchFsn || searchPft || searchSynonym)) {
                // For each concept in the list view
                for (int i = 0; i < model.getSize(); i++) {

                    // Get the current concept
                    I_GetConceptData child = model.getElementAt(i);

                    PositionSetReadOnly positionsToCheck = config.getViewPositionSetReadOnly();

                    // get latest descriptions
                    List<? extends I_DescriptionTuple> descriptionTuples = child.getDescriptionTuples(config.getAllowedStatus(),
                        (searchAll ? null : descriptionTypesToCheck), positionsToCheck, config.getPrecedence(), 
                        config.getConflictResolutionStrategy());

                    // For the current description of this concept
                    for (I_DescriptionTuple description : descriptionTuples) {

                        if (!processedDescriptions.contains(":" + description.getDescId() + ":")) {
                            // If it contains the search string
                            if ((caseSensitive && description.getText().contains(searchString))
                                || (!caseSensitive && description.getText().toUpperCase().contains(
                                    searchString.toUpperCase()))) {

                                // If not case sensitive, create regex
                                // prepending text
                                String searchPrepend = "";
                                if (!caseSensitive) {
                                    searchPrepend = "(?i)";
                                }

                                String origDesc = description.getText();
                                String finalDesc = description.getText().replaceAll(searchPrepend + searchString,
                                    replaceString);
                                String origDescHtml = description.getText().replaceAll(searchPrepend + searchString,
                                    "<font color='red'>" + searchString + "</font>");
                                String finalDescHtml = description.getText().replaceAll(searchPrepend + searchString,
                                    "<font color='green'>" + replaceString + "</font>");

                                String descType = termFactory.getConcept(description.getTypeId()).getInitialText();

                                // Add to our list of descriptions that will
                                // require change
                                SearchReplaceDescription srDesc = new SearchReplaceDescription(origDesc, finalDesc,
                                    origDescHtml, finalDescHtml, descType);
                                searchReplaceDescriptions.add(srDesc);

                                Set<PathBI> paths = config.getEditingPathSet();

                                // Create a new, cloned, description
                                I_DescriptionVersioned newDesc = termFactory.newDescription(UUID.randomUUID(), child,
                                    description.getLang(), finalDesc, termFactory.getConcept(description.getTypeId()),
                                    config);

                                for (PathBI path : paths) {
                                    // retire the existing description
                                    I_DescriptionPart newRetiredPart;
                                    
                                    if (retireAsStatus != -1) {
                                    	newRetiredPart = (I_DescriptionPart) description.makeAnalog(retireAsStatus, path.getConceptNid(), Long.MAX_VALUE);
                                    } else {
                                    	newRetiredPart = (I_DescriptionPart) description.makeAnalog(retiredConceptId, path.getConceptNid(), Long.MAX_VALUE);
                                    }
                                    description.getDescVersioned().addVersion(newRetiredPart);
                                    termFactory.addUncommitted(child);
                                }
                            }
                            processedDescriptions += ":" + description.getDescId() + ":";
                        }
                    }
                }

                signpostOutput = "<html>\n"
                    + "<TABLE width='100%' cellpadding='1' cellspacing='0' border='1' style='font-family:arial;font-size:10px;' bgcolor='white' bordercolor='gray'>\n"
                    + "<TR>\n" + "  <TD bgcolor='#00CCCC' align='center'>Update No.</TD>\n"
                    + "  <TD bgcolor='#00CCCC'>Description Type</TD>\n"
                    + "  <TD bgcolor='#00CCCC'>Original Value</TD>\n" + "  <TD bgcolor='#00CCCC'>Updated Value</TD>\n"
                    + "</TR>\n";

                int i = 0;
                // Output the original and final versions of the string
                for (SearchReplaceDescription desc : searchReplaceDescriptions) {

                    i++;
                    signpostOutput += "<TR>\n" + "  <TD align='center'>" + i + "/" + searchReplaceDescriptions.size()
                        + "</TD>\n" + "  <TD>" + desc.getDescType() + "</TD>\n" + "  <TD>" + desc.getOrigDescHtml()
                        + "</TD>\n" + "  <TD>" + desc.getFinalDescHtml() + "</TD>\n" + "</TR>\n";
                }

                if (searchReplaceDescriptions.isEmpty()) {
                    signpostOutput = "<html><h1>NO MATCHES FOUND</h1></html>";
                } else {
                    signpostOutput += "</TABLE>\n" + "</html>";
                }
            } else if (searchString == null || searchString.equals("")) {
                signpostOutput = "<html><h1>NO SEARCH STRING ENTERED</h1></html>";
            } else {
                signpostOutput = "<html><h1>NO DESCRIPTION TYPE SELECTED</h1></html>";
            }

            displayOutput(signpostOutput, config);

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException("Failed reading properties from process: ", e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException("Failed reading properties from process: ", e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException("Failed reading properties from process: ", e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException("Failed reading properties from process: ", e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
    }

    private void displayOutput(String signpostOutput, I_ConfigAceFrame config) {

        JPanel signpostPanel = config.getSignpostPanel();
        signpostPanel.setLayout(new BorderLayout());

        signpostPanel.removeAll();
        signpostPanel.validate();
        // JLabel outputLabel = new JLabel(signpostOutput, JLabel.CENTER);
        // JScrollPane scrollPane = new JScrollPane(outputLabel);
        // scrollPane.add(outputLabel);
        // scrollPane.setPreferredSize(new Dimension(800, 300));
        // signpostPanel.add(scrollPane);

        JEditorPane htmlPane = new JEditorPane("text/html", signpostOutput);
        htmlPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(htmlPane);
        signpostPanel.add(scrollPane, BorderLayout.CENTER);
        scrollPane.setPreferredSize(new Dimension(signpostPanel.getSize()));

        // signpostPanel.setPreferredSize(new Dimension(1200,300));
        // signpostPanel.validate();
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

    public String getRetireAsStatusPropName() {
        return retireAsStatusPropName;
    }

    public void setRetireAsStatusPropName(String retireAsStatusPropName) {
        this.retireAsStatusPropName = retireAsStatusPropName;
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }
}
