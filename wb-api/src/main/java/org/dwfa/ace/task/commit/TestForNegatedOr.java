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

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Tests if the refset spec contains a negated OR. These aren't allowed due to user confusion (they generally lead to a
 * very large proportion of the database being added to the resulting refset).
 * 
 * @author Christine Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForNegatedOr extends AbstractExtensionTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    private AlertToDataConstraintFailure.ALERT_TYPE alertType;

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

    @Override
    public List<AlertToDataConstraintFailure> test(I_ExtendByRef extension, boolean forCommit)
            throws TaskFailedException {
        try {
            ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();

            I_TermFactory termFactory = Terms.get();
            // TODO use other than termFactory.getActiveAceFrameConfig();
            I_ConfigAceFrame configFrame = termFactory.getActiveAceFrameConfig();
            I_ConfigAceDb configDb = configFrame.getDbConfig();
            I_GetConceptData userTopHierarchy = termFactory.getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());

            I_GetConceptData activeUser = configDb.getUserConcept();
            if (activeUser == null || activeUser.equals(userTopHierarchy)) {
                return alertList;
            }

            I_GetConceptData refsetSpecConcept = termFactory.getConcept(extension.getRefsetId());
            I_GetConceptData specifiesRefsetRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
            I_GetConceptData memberRefset = getLatestRelationshipTarget(refsetSpecConcept, specifiesRefsetRel);
            if (memberRefset == null) { // not a refset spec being edited
                return alertList;
            }

            RefsetSpec specHelper = new RefsetSpec(refsetSpecConcept, configFrame);
            AlertToDataConstraintFailure.ALERT_TYPE alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
            if (forCommit) {
                alertType = AlertToDataConstraintFailure.ALERT_TYPE.ERROR;
            }

            if (extension.getMutableParts().get(0) instanceof I_ExtendByRefPartCidCid) {
                I_ExtendByRefPartCidCid part = (I_ExtendByRefPartCidCid) extension.getMutableParts().get(0);
                if (part.getC1id() == RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_FALSE.localize().getNid()
                    && part.getC2id() == RefsetAuxiliary.Concept.REFSET_OR_GROUPING.localize().getNid()) {
                    alertList.add(new AlertToDataConstraintFailure(alertType,
                        formatAlertMessage("Negated OR detected - please remove before committing."), specHelper
                            .getRefsetSpecConcept()));
                }
            } else {
                return alertList;
            }

            return alertList;

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private String formatAlertMessage(String originalAlertMessage) {
        int alertMessageMaximumLength = 40;
        String formattedAlertMessage = "";
        String[] words = originalAlertMessage.split(" ");
        int charCount = 0;
        for (String word : words) {

            if (charCount > alertMessageMaximumLength) {
                formattedAlertMessage = formattedAlertMessage + "<br>";
                charCount = word.length();
            } else {
                formattedAlertMessage = formattedAlertMessage + " ";
                charCount = charCount + word.length() + 1; // for the space
            }
            formattedAlertMessage = formattedAlertMessage + word;
        }

        return "<html>" + formattedAlertMessage.trim();
    }
}
