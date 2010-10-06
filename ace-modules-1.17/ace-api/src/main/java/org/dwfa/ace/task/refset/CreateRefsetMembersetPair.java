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
package org.dwfa.ace.task.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/refset", type = BeanType.TASK_BEAN) })
public class CreateRefsetMembersetPair extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    private I_ConfigAceFrame config;
    private I_TermFactory termFactory;

    // private String newConceptName = "newConcpt";
    private I_GetConceptData newRefsetConcept = null;
    private I_GetConceptData newMembersetConcept = null;

    private I_GetConceptData selectedConcept = null;
    private I_GetConceptData isAConcept = null;

    private enum SetType {
        REFSET, MEMBERSET
    };

    private String propName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
    private String altIsA = ProcessAttachmentKeys.CONCEPT_UUID.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(propName);
        out.writeObject(altIsA);
    }// End method writeObject

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            propName = (String) in.readObject();
            altIsA = (String) in.readObject();
        } else if (objDataVersion == 1) {
            propName = (String) in.readObject();
            altIsA = null;
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }// End method readObject

    public String getPropName() {
        return propName;
    }

    public void setPropName(String propName) {
        this.propName = propName;
    }

    public String getAltIsA() {
        return altIsA;
    }

    public void setAltIsA(String altIsA) {
        this.altIsA = altIsA;
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }// End method complete

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        termFactory = LocalVersionedTerminology.get();

        config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

        try {
            selectedConcept = config.getHierarchySelection();
            if (altIsA != null) {
                String altIsAUuid = (String) process.readProperty(altIsA);
                if (altIsAUuid != null) {
                    this.isAConcept = termFactory.getConcept(new UUID[] { UUID.fromString(altIsAUuid) });
                }
            }

            if (this.isAConcept == null) {
                this.isAConcept = termFactory.getConcept(ConceptConstants.SNOMED_IS_A.localize().getNid()); // rel
                                                                                                            // type
            }

            if (isValidParent()) {
                /*
                 * Create a new concept for the Memberset
                 */

                String name = (String) process.readProperty(propName) + " member reference set";
                newMembersetConcept = createNewConcept(name);
                createMembersetRels(newMembersetConcept);

                termFactory.addUncommitted(newMembersetConcept);
                termFactory.commit();

                /*
                 * Create a new concept for the Refset specification
                 */
                name = (String) process.readProperty(propName) + " specification reference set";
                newRefsetConcept = createNewConcept(name);
                createRefsetRels(newRefsetConcept);

                termFactory.addUncommitted(newRefsetConcept);
                termFactory.commit();
            } else {
                /*
                 * Notify user that invalid concept has been selected
                 */
                JFrame frame = new JFrame("Invalid concept selection");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                JOptionPane.showMessageDialog(
                    frame,
                    "Unable to create a refset under the selected concept in the heirachy. Please select a refset concept and try again.",
                    "Invalid Selection", JOptionPane.WARNING_MESSAGE);

            }

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;

    }// End method evaluate

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }// End method getConditions

    public int[] getDataContainerIds() {
        return new int[] {};
    }// End method getDataContainerIds

    private boolean isValidParent() throws TaskFailedException {
        try {

            UUID[] ids = new UUID[1];
            ids[0] = UUID.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da");
            I_GetConceptData refsetConcept = termFactory.getConcept(ids);

            /*
             * Check if selected concept is the refset concept.
             */
            if (selectedConcept == refsetConcept) {
                return true;
            }

            /*
             * Check if selected concept has more than one source relationship
             */
            if (selectedConcept.getSourceRelTargets(null, null, null, false).size() > 1) {
                return false;
            }

            /*
             * only has one source rel
             * Check if the source concept is refset concept
             */
            I_GetConceptData parentConcept = selectedConcept.getSourceRelTargets(null, null, null, false)
                .iterator()
                .next();
            if (selectedConcept == parentConcept) {
                return true;
            }

            Set<I_GetConceptData> grandParentConcepts = parentConcept.getSourceRelTargets(null, null, null, false);
            while (!grandParentConcepts.isEmpty()) {

                if (grandParentConcepts.size() > 1) {
                    return false;
                }

                parentConcept = grandParentConcepts.iterator().next();
                if (parentConcept == refsetConcept) {
                    return true;
                }
            }// End while loop

            return false;

        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
    }// End method isValidParent

    private I_GetConceptData createNewConcept(String conceptName) throws TaskFailedException {
        I_GetConceptData newConcept = null;
        try {

            newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

            termFactory.newDescription(UUID.randomUUID(), newConcept, "en", conceptName,
                ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), config);

            termFactory.newDescription(UUID.randomUUID(), newConcept, "en", conceptName,
                ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), config);

            return newConcept;
        } catch (IOException e) {
            undoEdits(newConcept, LocalVersionedTerminology.get());
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            undoEdits(newConcept, LocalVersionedTerminology.get());
            throw new TaskFailedException(e);
        } catch (Exception e) {
            undoEdits(newConcept, LocalVersionedTerminology.get());
            throw new TaskFailedException(e);
        }
    }// End method createNewConcept

    private void createMembersetRels(I_GetConceptData concept) throws TaskFailedException {
        try {

            I_GetConceptData relChar = termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.localize()
                .getNid());
            I_GetConceptData refinability = termFactory.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            I_GetConceptData relStat = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize()
                .getNid());

            /*
             * Create default "is a" relationship
             */
            termFactory.newRelationship(UUID.randomUUID(), concept, this.isAConcept // rel
                                                                                    // type
                , selectedConcept // dest concept
                , relChar, refinability, relStat, 0, config);

            /*
             * Create default "refset type rel" relationship
             */

            termFactory.newRelationship(UUID.randomUUID(), concept,
                termFactory.getConcept(ConceptConstants.REFSET_TYPE_REL.localize().getNid()) // rel
                                                                                             // type
                , termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid()) // dest
                                                                                                        // concept
                , relChar, refinability, relStat, 0, config);

        } catch (IOException e) {
            undoEdits(concept, LocalVersionedTerminology.get());
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            undoEdits(concept, LocalVersionedTerminology.get());
            throw new TaskFailedException(e);
        } catch (Exception e) {
            undoEdits(concept, LocalVersionedTerminology.get());
            throw new TaskFailedException(e);
        }
    }// End method createMembersetRels

    private void createRefsetRels(I_GetConceptData concept) throws TaskFailedException {
        try {

            UUID uuid = UUID.randomUUID();

            I_GetConceptData relChar = termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.localize()
                .getNid());
            I_GetConceptData refinability = termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());
            I_GetConceptData relStat = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize()
                .getNid());

            /*
             * Create default "is a" relationship
             */
            termFactory.newRelationship(UUID.randomUUID(), concept, this.isAConcept // rel
                                                                                    // type
                , selectedConcept // dest concept
                , relChar, refinability, relStat, 0, config);

            /*
             * Create default "refset type rel" relationship
             */
            termFactory.newRelationship(UUID.randomUUID(), concept,
                termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_RELATIONSHIP.REFSET_TYPE_REL.localize().getNid()) // rel
                                                                                                                        // type
                , termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid()) // dest
                                                                                                        // concept
                , relChar, refinability, relStat, 0, config);

            /*
             * Create default "refset purpose rel" relationship
             */
            termFactory.newRelationship(UUID.randomUUID(), concept,
                termFactory.getConcept(ConceptConstants.REFSET_PURPOSE_REL.localize().getNid()) // rel
                                                                                                // type
                , termFactory.getConcept(RefsetAuxiliary.Concept.INCLUSION_SPECIFICATION.localize().getNid()) // dest
                                                                                                              // concept
                , relChar, refinability, relStat, 0, config);

            /*
             * Create default "generates" relationship
             */

            termFactory.newRelationship(UUID.randomUUID(), concept,
                termFactory.getConcept(ConceptConstants.GENERATES_REL.localize().getNid()) // rel
                                                                                           // type
                , newMembersetConcept // dest concept
                , relChar, refinability, relStat, 0, config);

        } catch (IOException e) {
            undoEdits(concept, LocalVersionedTerminology.get());
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            undoEdits(concept, LocalVersionedTerminology.get());
            throw new TaskFailedException(e);
        } catch (Exception e) {
            undoEdits(concept, LocalVersionedTerminology.get());
            throw new TaskFailedException(e);
        }
    }// End method createRefsetRels

    private void undoEdits(I_GetConceptData newConcept, I_TermFactory termFactory) {

        System.out.println("undo edits.........................");
        if (termFactory != null) {
            if (newConcept != null) {
                termFactory.forget(newConcept);
            }
        }
    }// End method undoEdits

}// End class CreateRefsetMembersetPair
