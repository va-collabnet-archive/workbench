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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestDescGt256Bytes extends AbstractConceptTest {

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
    public List<AlertToDataConstraintFailure> test(I_GetConceptData concept, boolean forCommit)
            throws TaskFailedException {
        try {
            I_TermFactory termFactory = Terms.get();

            I_ConfigAceFrame activeProfile = termFactory.getActiveAceFrameConfig();

            PositionSetReadOnly allPositions = getPositions(termFactory);

            ArrayList<I_DescriptionVersioned> descriptions = new ArrayList<I_DescriptionVersioned>();
            for (I_DescriptionTuple desc : concept.getDescriptionTuples(activeProfile.getAllowedStatus(), null,
                allPositions, activeProfile.getPrecedence(), activeProfile.getConflictResolutionStrategy())) {
                descriptions.add(desc.getDescVersioned());
            }

            return testDescriptions(concept, descriptions, forCommit);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private List<AlertToDataConstraintFailure> testDescriptions(I_GetConceptData concept,
            ArrayList<I_DescriptionVersioned> descriptions, boolean forCommit) throws Exception {
        ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
        I_TermFactory termFactory = Terms.get();
        I_IntSet typesToCheck = termFactory.newIntSet();
        I_GetConceptData fsn_type = getConceptSafe(termFactory,
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
        I_GetConceptData pt_type = getConceptSafe(termFactory,
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
        I_GetConceptData syn_type = getConceptSafe(termFactory,
            ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
        if (fsn_type == null) {
            return alertList;
        }
        typesToCheck.add(fsn_type.getConceptNid());
        typesToCheck.add(pt_type.getConceptNid());
        typesToCheck.add(syn_type.getConceptNid());
        for (I_DescriptionVersioned desc : descriptions) {
            for (I_DescriptionPart part : desc.getMutableParts()) {
                if (part.getVersion() == Integer.MAX_VALUE) {
                    if (typesToCheck.contains(part.getTypeId())) {
                        Charset utf8 = Charset.forName("UTF-8");
                        
                        if (part.getText() != null) {
                            ByteBuffer bytes = utf8.encode(part.getText());
                            if (bytes.limit() > 255) {
                                I_GetConceptData typeBean = termFactory.getConcept(part.getTypeId());
                                I_DescriptionTuple typeDesc = typeBean.getDescTuple(termFactory.getActiveAceFrameConfig().getTableDescPreferenceList(), termFactory.getActiveAceFrameConfig());
                                alertList.add(new AlertToDataConstraintFailure(
                                        (forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
                                        : AlertToDataConstraintFailure.ALERT_TYPE.WARNING), "<html>"
                                        + typeDesc.getText() + ":&nbsp;&nbsp;<font color=blue>"
                                        + part.getText().substring(0, 40) + "</font>..."
                                        + "<br>exceeds the 255 byte limit by  " + (bytes.limit() - 255) + " bytes.",
                                        concept));
                            }
                        }
                    }
                }
            }
        }
        
        // TEST IF Preferred or Synonym DESCRIPTION EXCEEDS 80 CHARACTERS
        // :NOTE:MEC: Global restriction added per Kaiser Pilot requirement.
        /* typesToCheck.clear();
        typesToCheck.add(pt_type.getConceptNid());
        typesToCheck.add(syn_type.getConceptNid());
        for (I_DescriptionVersioned desc : descriptions) {
            for (I_DescriptionPart part : desc.getMutableParts()) {
                if (part.getVersion() == Integer.MAX_VALUE) {
                    if (typesToCheck.contains(part.getTypeId())) {
                        int len = part.getText().length();
                        if (len > 80) {
                            I_GetConceptData typeBean = termFactory.getConcept(part.getTypeId());
                            I_DescriptionTuple typeDesc = typeBean.getDescTuple(termFactory.getActiveAceFrameConfig()
                                .getTableDescPreferenceList(), termFactory.getActiveAceFrameConfig());
                            alertList.add(new AlertToDataConstraintFailure(
                                (forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
                                          : AlertToDataConstraintFailure.ALERT_TYPE.WARNING), "<html>"
                                    + typeDesc.getText() + ":&nbsp;&nbsp;<font color=blue>"
                                    + part.getText().substring(0, 40) + "</font>..."
                                    + "<br>exceeds the 80 character limit by  " + (len - 80) + " characters.",
                                concept));
                        }
                    }
                }
            }
        }
        */
        return alertList;
    }

}
