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
package org.dwfa.ace.task.refset.spec.wf;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Checks if the associated promotion refset has any items with promotion
 * status=unreviewed (new addition or new deletion).
 * If so, it returns true, else it returns false.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class HasUnreviewedItemsTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String promotionUuidPropName = ProcessAttachmentKeys.PROMOTION_UUID.getAttachmentKey();

    private transient I_TermFactory termFactory;
    private transient I_EncodeBusinessProcess process;
    private transient I_ConfigAceFrame config;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(promotionUuidPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            promotionUuidPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            this.process = process;
            termFactory = Terms.get();
            //TODO use other than termFactory.getActiveAceFrameConfig();
            config = Terms.get().getActiveAceFrameConfig();

            if (hasUnreviewedItems()) {
                return Condition.TRUE;
            } else {
                return Condition.FALSE;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new TaskFailedException(e.getMessage());
        }
    }

    private boolean hasUnreviewedItems() throws Exception {
        UUID promotionRefsetUuid = (UUID) process.getProperty(promotionUuidPropName);
        if (promotionRefsetUuid == null) {
            RefsetSpec spec = new RefsetSpec(termFactory.getActiveAceFrameConfig().getRefsetSpecInSpecEditor(), 
                Terms.get().getActiveAceFrameConfig());
            promotionRefsetUuid = spec.getPromotionRefsetConcept().getUids().iterator().next();

        }
        I_GetConceptData promotionRefsetConcept = termFactory.getConcept(new UUID[] { promotionRefsetUuid });
        I_GetConceptData unreviewedAdditionConcept =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION.getUids());
        I_GetConceptData unreviewedDeletionConcept =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_DELETION.getUids());

        for (I_ExtendByRef r: promotionRefsetConcept.getExtensions()) {
            List<? extends I_ExtendByRefVersion> versions = r.getTuples(config.getAllowedStatus(), null, 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            if (versions.size() == 0) {
                break;
            } else if (versions.size() > 1) {
                throw new Exception("Unresolved conflict in promotion set. versions: " + versions + " member: " + r);
            }
            I_ExtendByRefVersion v = versions.get(0);
            I_ExtendByRefPartCid promotionPart = (I_ExtendByRefPartCid) v.getMutablePart();
            if (unreviewedAdditionConcept.getNid() == promotionPart.getC1id()) {
                return true;
            }
            if (unreviewedDeletionConcept.getNid() == promotionPart.getC1id()) {
                return true;
            }
            
        }
        return false;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONDITIONAL_TEST_CONDITIONS;
    }

    public String getPromotionUuidPropName() {
        return promotionUuidPropName;
    }

    public void setPromotionUuidPropName(String promotionUuidPropName) {
        this.promotionUuidPropName = promotionUuidPropName;
    }
}
