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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.refset.MarkedParentRefsetHelper;
import org.dwfa.ace.refset.MemberRefsetHelper;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.tapi.SuppressDataChecks;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@AllowDataCheckSuppression
@BeanList(specs = { @Spec(directory = "tasks/ide/refset/migrate", type = BeanType.TASK_BEAN) })
public class RegenerateMarkedParents extends AbstractTask {

    private static final long serialVersionUID = -8787270417774804851L;

    public final String PARENT_MEMBER_HIERARCHY_NAME = "parent members";

    public final String PARENT_MEMBER_REFSET_PURPOSE_NAME = "marked parent membership";

    public final String PARENT_MEMBER_REFSET_RELATIONSHIP_NAME = ConceptConstants.INCLUDES_MARKED_PARENTS_REL_TYPE.getDescription();

    private String memberConceptPropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();

    protected I_GetConceptData memberConcept;

    protected I_TermFactory termFactory;

    protected HashMap<String, I_GetConceptData> concepts = new HashMap<String, I_GetConceptData>();

    public RegenerateMarkedParents() throws Exception {
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

    private void regenerateMarkedParentMembers(I_GetConceptData memberRefsetConcept) throws Exception {

        int refsetId = memberRefsetConcept.getConceptId();

        Set<Integer> normalMemberIds = new HashSet<Integer>();
        List<I_ThinExtByRefVersioned> extVersions = termFactory.getRefsetExtensionMembers(refsetId);

        for (I_ThinExtByRefVersioned thinExtByRefVersioned : extVersions) {

            List<I_ThinExtByRefTuple> extensions = thinExtByRefVersioned.getTuples(null, null, true, false);

            for (I_ThinExtByRefTuple thinExtByRefTuple : extensions) {
                if (thinExtByRefTuple.getRefsetId() == refsetId) {

                    I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) thinExtByRefTuple.getMutableIdPart();
                    if (part.getConceptId() == memberConcept.getConceptId()) {
                        normalMemberIds.add(thinExtByRefTuple.getComponentId());
                    }
                }
            }
        }

        new MarkedParentRefsetHelper(refsetId, memberConcept.getConceptId()).addParentMembers(normalMemberIds.toArray(new Integer[] {}));
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
    }

    @SuppressDataChecks
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            init();

            memberConcept = (I_GetConceptData) process.readProperty(memberConceptPropName);

            if (memberConcept == null) {
                throw new TerminologyException("No member concept selected.");
            }

            for (Integer memberRefsetId : MemberRefsetHelper.getMemberRefsets()) {
                I_GetConceptData memberRefsetConcept = termFactory.getConcept(memberRefsetId);
                regenerateMarkedParentMembers(memberRefsetConcept);
            }

            return Condition.CONTINUE;

        } catch (Exception ex) {
            throw new TaskFailedException("Unable to migrate specification refsets", ex);
        }
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getMemberConceptPropName() {
        return memberConceptPropName;
    }

    public void setMemberConceptPropName(String memberConceptPropName) {
        this.memberConceptPropName = memberConceptPropName;
    }

}
