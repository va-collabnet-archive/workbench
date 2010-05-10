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
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.batch.Batch;
import org.dwfa.ace.file.ConceptListWriter;
import org.dwfa.ace.refset.MemberRefsetHelper;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Adds a single concept as a member of the working refset
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/refset", type = BeanType.TASK_BEAN) })
public class ExportRefsetToFile extends AbstractTask {

    private static final long serialVersionUID = -4933685427716835533L;

    private static final int dataVersion = 1;

    /** the refset we are adding to */
    private String refsetConceptPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();

    /** the name of the file to be imported */
    private String importFileName = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();

    /** the value to be given to the new concept extension */
    private String conceptExtValuePropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();

    private ConceptListWriter writer;

    private I_TermFactory termFactory;

    private MemberRefsetHelper refsetHelper;

    private Set<Integer> refsetMembers;

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
            I_GetConceptData refset = getProperty(process, I_GetConceptData.class, refsetConceptPropName);
            String filename = getProperty(process, String.class, importFileName);
            I_GetConceptData value = getProperty(process, I_GetConceptData.class, conceptExtValuePropName);

            termFactory = LocalVersionedTerminology.get();

            refsetHelper = new MemberRefsetHelper(refset.getConceptId(), value.getConceptId());

            writer = new ConceptListWriter();            
            writer.open(new File(filename), false);
            
            Batch<Integer> locateBatch = new Batch<Integer>(null, "Locating refset concepts for export", true) {
                @Override
                protected void process() throws Exception {
                    refsetMembers = refsetHelper.getExistingMembers();
                }
                
                @Override
                protected void onCancel() throws Exception {
                    refsetMembers = null;
                }
            };           
            locateBatch.run();
            
            if (refsetMembers != null) {
                
                Batch<Integer> exportBatch = new Batch<Integer>(refsetMembers, "Refset member export", true) {
                    
                    @Override
                    protected void processItem(Integer memberId) throws Exception {
                        writer.write(termFactory.getConcept(memberId));
                    }
                    
                    @Override
                    protected void onCancel() throws Exception {
                        writer.abort();
                    }
    
                    @Override
                    protected void onComplete() throws Exception {
                        writer.close();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {                    
                                JOptionPane.showMessageDialog(null, 
                                    "Completed export of " + items.size() + " refset member concepts.", 
                                    "Refset Export", JOptionPane.INFORMATION_MESSAGE);
                            }
                        });
                    }
                };
                exportBatch.run();
            }
            
            return Condition.CONTINUE;

        } catch (Exception e) {
            throw new TaskFailedException("Unable to export refset to file. " + e.getMessage(), e);
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
