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
package org.dwfa.ace.task.refset.members;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JList;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpMemberRefsets;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/refset/membership", type = BeanType.TASK_BEAN) })
public class RefreshMemberRefsetUsingListViewConcepts extends AbstractTask {

    private static final long serialVersionUID = -1488580246193922770L;

    private static final int dataVersion = 1;

    /** the refset we are adding to */
    private String refsetConceptPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();

    /** the value to be given to the new concept extension */
    private String conceptExtValuePropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.refsetConceptPropName);
        out.writeObject(this.conceptExtValuePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.refsetConceptPropName = (String) in.readObject();
            this.conceptExtValuePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_GetConceptData refset = (I_GetConceptData) process.getProperty(refsetConceptPropName);
            I_GetConceptData value = (I_GetConceptData) process.getProperty(conceptExtValuePropName);

            if (refset == null) {
                throw new TerminologyException("A working refset has not been selected.");
            }

            if (value == null) {
                throw new TerminologyException("No concept extension value selected.");
            }

            getLogger().info(
                "Adding concepts from list view to refset '" + refset.getInitialText() + "' with a value '"
                    + value.getInitialText() + "'.");

            I_TermFactory termFactory = Terms.get();
            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            JList conceptList = config.getBatchConceptList();
            I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();

            I_ShowActivity computeRefsetActivityPanel = termFactory.newActivityPanel(true,
                termFactory.getActiveAceFrameConfig(), "Computing refset: " + refset.getInitialText(), true);
            computeRefsetActivityPanel.setProgressInfoUpper("Computing refset: " + refset.getInitialText());
            computeRefsetActivityPanel.setProgressInfoLower("<html>" + "1) Creating refset spec query.   "
                + "<font color='red'>COMPLETE.<br><font color='black'>"

                + "2) Executing refset spec query over database.   "
                + "<font color='red'>COMPLETE.<br><font color='black'>"

                + "3) Adding new members to member refset.   "
                + "<font color='green'>Executing.<br><font color='black'>"

                + "4) Calculating concepts for parent refset removal.   "
                + "<font color='green'> <br><font color='black'>");

            // add concepts from list view to the refset
            // this will skip any that already exist as current members of the
            // refset
            Set<I_GetConceptData> newMembers = new HashSet<I_GetConceptData>();
            for (int i = 0; i < model.getSize(); i++) {
                newMembers.add(model.getElementAt(i));
            }

            I_HelpMemberRefsets helper = Terms.get().getMemberRefsetHelper(config, refset.getConceptNid(), value.getConceptNid());
            helper.addAllToRefset(newMembers, "Adding new members to member refset");

            computeRefsetActivityPanel.setProgressInfoLower("<html>" + "1) Creating refset spec query.   "
                + "<font color='red'>COMPLETE.<br><font color='black'>"

                + "2) Executing refset spec query over database.   "
                + "<font color='red'>COMPLETE.<br><font color='black'>"

                + "3) Adding new members to member refset.   " + "<font color='red'>COMPLETE.<br><font color='black'>"

                + "4) Calculating concepts for parent refset removal.   "
                + "<font color='green'>Executing.<br><font color='black'>");

            getLogger().info("Calculating which concepts need to be removed from member refset.");

            // remove any concepts from member refset who are no longer in the
            // list
            Iterator<I_GetConceptData> conceptIterator = termFactory.getConceptIterator();
            int conceptCount = 0;
            Set<I_GetConceptData> oldMembers = new HashSet<I_GetConceptData>();
            I_GetConceptData currentConcept = null;
            int cleanupCount = 0;

            while (conceptIterator.hasNext()) {

                while (conceptIterator.hasNext()) {
                    currentConcept = conceptIterator.next();
                    if (!newMembers.contains(currentConcept)) {
                        oldMembers.add(currentConcept);
                    }

                    conceptCount++;
                    if (conceptCount % 10000 == 0) {
                        getLogger().info("Scanned " + conceptCount + " concepts for member refset cleanup.");
                        computeRefsetActivityPanel.setProgressInfoLower("<html>" + "1) Creating refset spec query.   "
                            + "<font color='red'>COMPLETE.<br><font color='black'>"

                            + "2) Executing refset spec query over database.   "
                            + "<font color='red'>COMPLETE.<br><font color='black'>"

                            + "3) Adding new members to member refset.   "
                            + "<font color='red'>COMPLETE.<br><font color='black'>"

                            + "4) Calculating concepts for parent refset removal.   "
                            + "<font color='green'>Executing.<br><font color='black'>" + "   Scanned " + conceptCount
                            + " concepts for member refset cleanup.");
                    }

                    if (oldMembers.size() > 100000) {
                        break;
                    }
                }

                helper.removeAllFromRefset(oldMembers, "Cleaning up old members from member refset");
                oldMembers = new HashSet<I_GetConceptData>();
                cleanupCount++;
            }
            computeRefsetActivityPanel.setProgressInfoLower("<html>" + "1) Creating refset spec query.   "
                + "<font color='red'>COMPLETE.<br><font color='black'>"

                + "2) Executing refset spec query over database.   "
                + "<font color='red'>COMPLETE.<br><font color='black'>"

                + "3) Adding new members to member refset.   " + "<font color='red'>COMPLETE.<br><font color='black'>"

                + "4) Calculating concepts for parent refset removal.   "
                + "<font color='red'>COMPLETE.<br><font color='black'>"
                + "   Final number of scanned concepts for member refset cleanup: " + conceptCount);
            computeRefsetActivityPanel.complete();
            getLogger().info("Number of cleanup executions=" + cleanupCount);

            return Condition.CONTINUE;

        } catch (Exception e) {
            throw new TaskFailedException("Unable to add concept to refset. " + e.getMessage(), e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getRefsetConceptPropName() {
        return refsetConceptPropName;
    }

    public void setRefsetConceptPropName(String refsetConceptPropName) {
        this.refsetConceptPropName = refsetConceptPropName;
    }

    public String getConceptExtValuePropName() {
        return conceptExtValuePropName;
    }

    public void setConceptExtValuePropName(String conceptExtValuePropName) {
        this.conceptExtValuePropName = conceptExtValuePropName;
    }

}
