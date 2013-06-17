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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpMemberRefsets;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.file.ConceptListReader;
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
import org.ihtsdo.tk.query.helper.SpecRefsetHelper;

/**
 * Adds a single concept as a member of the working refset
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/refset", type = BeanType.TASK_BEAN) })
public class ImportRefsetFromFile extends AbstractTask {

    private static final long serialVersionUID = -2883696709930614625L;

    private static final int dataVersion = 1;

    /** the refset we are adding to */
    private String refsetConceptPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();

    /** the name of the file to be imported */
    private String importFileName = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();

    /** the value to be given to the new concept extension */
    private String conceptExtValuePropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.refsetConceptPropName);
        out.writeObject(this.importFileName);
        out.writeObject(this.conceptExtValuePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            this.refsetConceptPropName = (String) in.readObject();
            this.importFileName = (String) in.readObject();
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
            String filename = (String) process.getProperty(importFileName);
            I_GetConceptData value = (I_GetConceptData) process.getProperty(conceptExtValuePropName);

            if (refset == null) {
                throw new TerminologyException("A working refset has not been selected.");
            }

            if (filename == null) {
                throw new TerminologyException("No file selected.");
            }

            if (value == null) {
                throw new TerminologyException("No concept extension value selected.");
            }

            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null,
                "The existing refset will be replaced by the contents of the file. Do you wish to continue?",
                "Confirmation", JOptionPane.YES_NO_OPTION)) {
                return Condition.PROCESS_COMPLETE;
            }

            I_TermFactory termFactory = Terms.get();

            ConceptListReader reader = new ConceptListReader();
            reader.setSourceFile(new File(filename));
            reader.setHasHeader(true);

            // Load in all concepts from the import file
            HashMap<Integer, I_GetConceptData> importConcepts = new HashMap<Integer, I_GetConceptData>();
            for (I_GetConceptData concept : reader) {
                importConcepts.put(concept.getConceptNid(), concept);
            }

//            I_HelpMemberRefsets refsetHelper = Terms.get().getMemberRefsetHelper(Terms.get().getActiveAceFrameConfig(),
//                    refset.getConceptNid(), value.getConceptNid());
            SpecRefsetHelper refsetHelper = new SpecRefsetHelper(Terms.get().getActiveAceFrameConfig().getViewCoordinate(),
                    Terms.get().getActiveAceFrameConfig().getEditCoordinate());

            // Find existing members of the refset that are not in the import
            // set. These need to be retired.
            HashSet<Integer> membersToRemove = new HashSet<Integer>();
            for (Integer existingMemberId : refset.getRefsetMemberNidsActive(Terms.get().getActiveAceFrameConfig().getViewCoordinate())) {
                if (!importConcepts.containsKey(existingMemberId)) {
                    membersToRemove.add(existingMemberId);
                }
            }
            getLogger().info("Importing new refset members from file");
            for(I_GetConceptData c : importConcepts.values()){
                refsetHelper.newRefsetExtension(refset.getConceptNid(), c.getNid(), value.getConceptNid());
            }
            getLogger().info("Removing existing members absent from file");
            for(int nid : membersToRemove){
                refsetHelper.retireRefsetExtension(refset.getConceptNid(), nid, value.getConceptNid());
            }

            return Condition.CONTINUE;

        } catch (Exception e) {
            throw new TaskFailedException("Unable to import refset from file. " + e.getMessage(), e);
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

    public String getImportFileName() {
        return importFileName;
    }

    public void setImportFileName(String importFileName) {
        this.importFileName = importFileName;
    }

}
