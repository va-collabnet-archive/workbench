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
package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure.ALERT_TYPE;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class VerifySingleMemberPerConceptForRefset extends AbstractExtensionTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private static I_IntSet singleConceptRefsets;

    @Override
    public List<AlertToDataConstraintFailure> test(I_ThinExtByRefVersioned extension, boolean forCommit)
            throws TaskFailedException {
        try {
            I_TermFactory tf = LocalVersionedTerminology.get();
            List<AlertToDataConstraintFailure> returnValues = new ArrayList<AlertToDataConstraintFailure>();
            if (singleConceptRefsets == null) {
                singleConceptRefsets = tf.newIntSet();
                singleConceptRefsets.add(RefsetAuxiliary.Concept.ALLERGY_RXN_INCLUSION_SPEC.localize().getNid());
                singleConceptRefsets.add(RefsetAuxiliary.Concept.DISCHARGE_INCLUSION_SPEC.localize().getNid());
                singleConceptRefsets.add(RefsetAuxiliary.Concept.DOCUMENT_SECTION_ORDER.localize().getNid());
                singleConceptRefsets.add(RefsetAuxiliary.Concept.PATHOLOGY_INCLUSION_SPEC.localize().getNid());
                // etc..
                // @TODO Need to gather this info from a machine readable
                // concept model in the future...
            }
            if (singleConceptRefsets.contains(extension.getRefsetId())) {
                List<I_ThinExtByRefVersioned> matches = new ArrayList<I_ThinExtByRefVersioned>();

                for (I_ThinExtByRefVersioned ext : tf.getAllExtensionsForComponent(extension.getComponentId(), true)) {
                    if (ext.getRefsetId() == extension.getRefsetId()) {
                        matches.add(ext);
                    }
                }

                if (matches.size() > 1) {
                    I_GetConceptData conceptWithDuplicate = tf.getConcept(extension.getComponentId());
                    I_GetConceptData refsetIdentity = tf.getConcept(extension.getRefsetId());
                    if (forCommit) {
                        AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(ALERT_TYPE.ERROR,
                            "<html>Duplicate refset entries of identity: " + refsetIdentity.getInitialText()
                                + "<br>for concept: " + conceptWithDuplicate.getInitialText(), conceptWithDuplicate);
                        returnValues.add(alert);
                    } else {
                        // if not for commit, give option of rollback
                        AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(ALERT_TYPE.WARNING,
                            "<html>Duplicate refset entries of identity: " + refsetIdentity.getInitialText()
                                + "<br>for concept: " + conceptWithDuplicate.getInitialText(), conceptWithDuplicate);
                        returnValues.add(alert);
                        AbortExtension abortFixup = new AbortExtension(extension, "don't add new refset member");
                        alert.getFixOptions().add(abortFixup);
                    }
                }
            }
            return returnValues;
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
    }

}
