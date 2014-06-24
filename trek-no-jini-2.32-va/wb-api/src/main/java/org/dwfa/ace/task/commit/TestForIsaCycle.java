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
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.classify.SnoTable;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/precommit/priority", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/commit/priority", type = BeanType.TASK_BEAN) })
public class TestForIsaCycle extends AbstractConceptTest {

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
            ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
//            THIS WAS THE OLDER VERSION
//            Collection<? extends I_RelVersioned> usrl = (Collection<? extends I_RelVersioned>) concept.getSourceRels();
            /*
             * I tried to use this line:
             *  if(rt.isActive(Terms.get().getActiveAceFrameConfig().getViewCoordinate())){
             * to see if the rel was active. I realized that I was probably not looking at the rel as whole,
             * but rather at only that particular version.
             * 
             * I changed to getting the concept version, and then getting the active rels,
             * which seemed less complicated and gave me the right result.
             */
            ConceptVersionBI cv = Ts.get().getConceptVersion(
                    Terms.get().getActiveAceFrameConfig().getViewCoordinate(), concept.getConceptNid());
            Collection<? extends RelationshipVersionBI> activeRels = cv.getRelationshipsOutgoingActive();
            
            if (Terms.get().getActiveAceFrameConfig() == null || Terms.get().getActiveAceFrameConfig().getEditingPathSet().isEmpty()) {
                return alertList;
            }

            boolean foundCycle = false;
            String error = SnoTable.updatePrefs(false);
            // TODO MARC to fix up after re-implementing the classifier. 
            /* if (error != null) {
                alertList.add(new AlertToDataConstraintFailure(AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
                    "<html>" + error, concept));
                
            } else { */
            
//                 THIS WAS THE OLDER VERSION
//                for (I_RelVersioned rv : usrl) {
//                    List<? extends I_RelTuple> rvtl = rv.getTuples();
//                    for (I_RelTuple rt : rvtl) {
//                        try {
//                            if(rt.isActive(Terms.get().getActiveAceFrameConfig().getViewCoordinate())){
//                                boolean test = SnoTable.findIsaCycle(rt.getC1Id(), rt.getTypeNid(), rt.getC2Id(), true);
//                                if (test)
//                                    foundCycle = true;
//                            }
//                        } catch (TerminologyException e) {
//                            AceLog.getAppLog().alertAndLogException(e);
//                        } catch (IOException e) {
//                            AceLog.getAppLog().alertAndLogException(e);
//                        }
//                    }
//                }
                
                for (RelationshipVersionBI rv : activeRels) {
                    try{
                          boolean test = SnoTable.findIsaCycle(rv.getSourceNid(), rv.getTypeNid(), rv.getTargetNid(), true);
                          if (test)
                               foundCycle = true;
                        } catch (TerminologyException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        } catch (IOException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        }
                }

                if (foundCycle)
                    alertList.add(new AlertToDataConstraintFailure(AlertToDataConstraintFailure.ALERT_TYPE.OMG,
                        "<html>Added IS_A relationship will create a cycle. ", concept));
            return alertList;
        } catch (Exception e) {
           throw new TaskFailedException(e);
        }
    }

}
