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

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN) })
public class TestForInactiveConceptInExtension extends AbstractExtensionTest {

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

    @Override
    public List<AlertToDataConstraintFailure> test(I_ThinExtByRefVersioned extension, boolean forCommit)
            throws TaskFailedException {
        List<AlertToDataConstraintFailure> alerts = new ArrayList<AlertToDataConstraintFailure>();
        try {
            I_ConfigAceFrame activeProfile = LocalVersionedTerminology.get().getActiveAceFrameConfig();
            for (I_ThinExtByRefPart part : extension.getVersions()) {
                testPart(part, activeProfile, alerts, forCommit);
            }
            if (LocalVersionedTerminology.get().hasConcept(extension.getComponentId())) {
                checkForInactive(activeProfile, alerts, extension.getComponentId());
            }
            checkForInactive(activeProfile, alerts, extension.getRefsetId());
            checkForInactive(activeProfile, alerts, extension.getTypeId());
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
        return alerts;
    }

    private void testPart(I_ThinExtByRefPart part, I_ConfigAceFrame activeProfile,
            List<AlertToDataConstraintFailure> alerts2, boolean forCommit) throws IOException, TerminologyException {
        if (part.getVersion() == Integer.MAX_VALUE) {
            for (int nid : part.getPartComponentNids().toArray()) {
                if (LocalVersionedTerminology.get().hasConcept(nid)) {
                    checkForInactive(activeProfile, alerts2, nid);
                }
            }
        }
    }

    private void checkForInactive(I_ConfigAceFrame activeProfile, List<AlertToDataConstraintFailure> alerts2, int nid)
            throws TerminologyException, IOException {
        I_GetConceptData concept = LocalVersionedTerminology.get().getConcept(nid);
        List<I_ConceptAttributeTuple> attributes = concept.getConceptAttributeTuples(activeProfile.getAllowedStatus(),
            activeProfile.getViewPositionSet());
        if (attributes == null || attributes.size() == 0) {
            String alertString = "<html>Inactive concept in refset:<br> <font color='blue'>" + concept.toString()
                + "</font><br>If appropriate, please change prior to commit...";
            if (concept.getDescTuple(activeProfile.getLongLabelDescPreferenceList(), activeProfile) != null) {
                alertString = "<html>Inactive concept in refset:<br> <font color='blue'>"
                    + concept.getDescTuple(activeProfile.getLongLabelDescPreferenceList(), activeProfile).getText()
                    + "</font><br>If appropriate, please<br>change prior to commit...";
            }
            AlertToDataConstraintFailure.ALERT_TYPE alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
            AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(alertType, alertString, concept);
            alerts2.add(alert);

        }
    }

}
