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
package org.dwfa.ace.task.refset.grouping;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 * Gets the data from the Request for change panel and verifies that the
 * required data has been filled in.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf/grouping", type = BeanType.TASK_BEAN) })
public class GetNewRefsetGroupingPanelDataTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private String groupingConceptUuidPropName = ProcessAttachmentKeys.CONCEPT_UUID.getAttachmentKey();
    private TermEntry statusTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());

    private I_TermFactory termFactory;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(groupingConceptUuidPropName);
        out.writeObject(statusTermEntry);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            groupingConceptUuidPropName = (String) in.readObject();
            statusTermEntry = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            termFactory = Terms.get();

            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
            for (Component c : workflowDetailsSheet.getComponents()) {
                if (NewRefsetGroupingPanel.class.isAssignableFrom(c.getClass())) {
                    NewRefsetGroupingPanel panel = (NewRefsetGroupingPanel) c;

                    I_GetConceptData parent = panel.getRefsetParent();
                    String refsetName = panel.getRefsetName();

                    if (parent == null) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must select a refset parent. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }
                    if (refsetName == null) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must enter a refset name. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }

                    I_GetConceptData fsnConcept =
                            termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
                                .getUids());
                    I_GetConceptData ptConcept =
                            termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
                    I_GetConceptData isA = termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
                    I_GetConceptData purposeRel =
                            termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
                    I_GetConceptData ancillaryPurpose =
                            termFactory.getConcept(RefsetAuxiliary.Concept.ANCILLARY_DATA.getUids());

                    I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());

                    // create concept
                    I_GetConceptData newRefsetGroupingConcept =
                            helper.newConcept(config, termFactory.getConcept(statusTermEntry.getIds()));

                    // create descriptions
                    helper.newDescription(newRefsetGroupingConcept, fsnConcept, refsetName, config, termFactory
                        .getConcept(statusTermEntry.getIds()));
                    helper.newDescription(newRefsetGroupingConcept, ptConcept, refsetName, config, termFactory
                        .getConcept(statusTermEntry.getIds()));

                    // create relationships
                    helper.newRelationship(newRefsetGroupingConcept, isA, parent, config, termFactory
                        .getConcept(statusTermEntry.getIds()));
                    helper.newRelationship(newRefsetGroupingConcept, purposeRel, ancillaryPurpose, config, termFactory
                        .getConcept(statusTermEntry.getIds()));

                    // save newly created concept UUID
                    process.setProperty(groupingConceptUuidPropName, newRefsetGroupingConcept.getUids().iterator()
                        .next());

                    return Condition.ITEM_COMPLETE;

                }
            }
            return Condition.ITEM_COMPLETE;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TaskFailedException(e.getMessage());
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public TermEntry getStatusTermEntry() {
        return statusTermEntry;
    }

    public void setStatusTermEntry(TermEntry statusTermEntry) {
        this.statusTermEntry = statusTermEntry;
    }

    public String getGroupingConceptUuidPropName() {
        return groupingConceptUuidPropName;
    }

    public void setGroupingConceptUuidPropName(String groupingConceptUuidPropName) {
        this.groupingConceptUuidPropName = groupingConceptUuidPropName;
    }
}
