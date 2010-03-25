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
package org.dwfa.ace.task.refset.migrate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.refset.MemberRefsetHelper;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.tapi.SuppressDataChecks;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@AllowDataCheckSuppression
@BeanList(specs = { @Spec(directory = "tasks/ide/refset/migrate", type = BeanType.TASK_BEAN) })
public class RetireAllMarkedParents extends AbstractTask {

    private static final long serialVersionUID = 7859118015824964201L;

    protected I_TermFactory termFactory;

    protected HashMap<String, I_GetConceptData> concepts = new HashMap<String, I_GetConceptData>();

    public RetireAllMarkedParents() {
    }

    public void init() throws Exception {
        termFactory = LocalVersionedTerminology.get();
        if (termFactory == null) {
            throw new RuntimeException("The LocalVersionedTerminology is not available. Please check the database.");
        }

        concepts.put("CURRENT", termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()));
        concepts.put("RETIRED", termFactory.getConcept(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid()));
        concepts.put("PARENT_MARKER", termFactory.getConcept(ConceptConstants.PARENT_MARKER.localize().getNid()));
    }

    private void retireExistingMarkedParentMembers(I_GetConceptData memberRefsetConcept) throws Exception {

        int refsetId = memberRefsetConcept.getConceptId();

        List<I_ThinExtByRefVersioned> extVersions = termFactory.getRefsetExtensionMembers(refsetId);

        for (I_ThinExtByRefVersioned thinExtByRefVersioned : extVersions) {

            List<I_ThinExtByRefTuple> extensions = thinExtByRefVersioned.getTuples(null, null, true, false);

            for (I_ThinExtByRefTuple thinExtByRefTuple : extensions) {
                if (thinExtByRefTuple.getRefsetId() == refsetId) {

                    I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) thinExtByRefTuple.getPart();
                    if (part.getC1id() == concepts.get("PARENT_MARKER").getConceptId()
                        && part.getStatusId() == concepts.get("CURRENT").getConceptId()) {

                        I_ThinExtByRefPart clone = part.duplicate();
                        clone.setStatusId(concepts.get("RETIRED").getConceptId());
                        clone.setVersion(Integer.MAX_VALUE);
                        thinExtByRefVersioned.addVersion(clone);

                        String subject = termFactory.getConcept(thinExtByRefTuple.getComponentId()).getInitialText();
                        getLogger().info("Retiring current parent member extension on concept :" + subject);
                        getLogger().info("\t" + part.toString());

                        termFactory.addUncommittedNoChecks(thinExtByRefVersioned);
                    }
                }
            }
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
    }

    @SuppressDataChecks
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            init();

            for (Integer memberRefsetId : MemberRefsetHelper.getMemberRefsets()) {
                I_GetConceptData memberRefsetConcept = termFactory.getConcept(memberRefsetId);
                retireExistingMarkedParentMembers(memberRefsetConcept);
            }

            return Condition.CONTINUE;

        } catch (Exception ex) {
            throw new TaskFailedException("Unable to migrate specification refsets", ex);
        }
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

}
