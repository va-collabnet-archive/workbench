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
package org.dwfa.ace.task.refset.spec.status;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.api.PathBI;

/**
 * Takes a refset spec as input and updates the refset's meta data and member
 * refsets to the specified status.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class UpdateRefsetSpecStatusTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String refsetSpecUuidPropName = ProcessAttachmentKeys.REFSET_SPEC_UUID.getAttachmentKey();
    private TermEntry statusTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids());
    private I_TermFactory termFactory;
    private boolean includeMetaDataConcepts;
    private boolean includeMetaDataRelationships;
    private boolean includeMetaDataDescriptions;
    private boolean includeSpecExtensions;
    private boolean includeMemberExtensions;
    private boolean includeParentExtensions;
    private boolean includeCommentExtensions;
    private boolean includePromotionExtensions;

    private int retiredStatusId;

    private transient Exception ex = null;
    private transient Condition returnCondition = Condition.ITEM_COMPLETE;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(refsetSpecUuidPropName);
        out.writeObject(statusTermEntry);
        out.writeBoolean(includeMetaDataConcepts);
        out.writeBoolean(includeMetaDataRelationships);
        out.writeBoolean(includeMetaDataDescriptions);
        out.writeBoolean(includeSpecExtensions);
        out.writeBoolean(includeMemberExtensions);
        out.writeBoolean(includeParentExtensions);
        out.writeBoolean(includeCommentExtensions);
        out.writeBoolean(includePromotionExtensions);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            refsetSpecUuidPropName = (String) in.readObject();
            statusTermEntry = (TermEntry) in.readObject();
            includeMetaDataConcepts = (Boolean) in.readBoolean();
            includeMetaDataRelationships = (Boolean) in.readBoolean();
            includeMetaDataDescriptions = (Boolean) in.readBoolean();
            includeSpecExtensions = (Boolean) in.readBoolean();
            includeMemberExtensions = (Boolean) in.readBoolean();
            includeParentExtensions = (Boolean) in.readBoolean();
            includeCommentExtensions = (Boolean) in.readBoolean();
            includePromotionExtensions = (Boolean) in.readBoolean();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {

        try {
            ex = null;
            if (SwingUtilities.isEventDispatchThread()) {
                doRun(process, worker);
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        doRun(process, worker);
                    }
                });
            }
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
        if (ex != null) {
            throw new TaskFailedException(ex);
        }
        return returnCondition;
    }

    public void doRun(final I_EncodeBusinessProcess process, I_Work worker) {

        try {

            termFactory = Terms.get();
            termFactory.commit();

            retiredStatusId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();

            UUID refsetSpecUuid = (UUID) process.getProperty(refsetSpecUuidPropName);
            I_GetConceptData statusConcept = termFactory.getConcept(statusTermEntry.getIds());
            I_GetConceptData refsetSpecConcept = termFactory.getConcept(new UUID[] { refsetSpecUuid });
            //TODO use other than termFactory.getActiveAceFrameConfig();

            RefsetSpec refsetSpec = new RefsetSpec(refsetSpecConcept, Terms.get().getActiveAceFrameConfig());

            I_GetConceptData memberRefsetConcept = refsetSpec.getMemberRefsetConcept();
            I_GetConceptData markedParentConcept = refsetSpec.getMarkedParentRefsetConcept();
            I_GetConceptData commentsConcept = refsetSpec.getCommentsRefsetConcept();
            I_GetConceptData promotionConcept = refsetSpec.getPromotionRefsetConcept();

            Set<I_GetConceptData> concepts = new HashSet<I_GetConceptData>();
            concepts.add(refsetSpecConcept);
            concepts.add(memberRefsetConcept);
            concepts.add(markedParentConcept);
            concepts.add(commentsConcept);
            concepts.add(promotionConcept);

            if (includeMetaDataConcepts) {
                updateStatusOfConcepts(concepts, statusConcept);
            }

            if (includeMetaDataRelationships) {
                updateStatusOfRelationships(concepts, statusConcept);
            }

            if (includeMetaDataDescriptions) {
                updateStatusOfDescriptions(concepts, statusConcept);
            }

            if (includeSpecExtensions) {
                updateStatusOfExtensions(refsetSpecConcept, statusConcept);
            }

            if (includeMemberExtensions) {
                updateStatusOfExtensions(memberRefsetConcept, statusConcept);
            }

            if (includeParentExtensions) {
                updateStatusOfExtensions(markedParentConcept, statusConcept);
            }

            if (includeCommentExtensions) {
                updateStatusOfExtensions(commentsConcept, statusConcept);
            }

            if (includePromotionExtensions) {
                updateStatusOfExtensions(promotionConcept, statusConcept);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                "Refset wizard cannot be completed. Error updating refset spec status: " + e.getMessage(), "",
                JOptionPane.ERROR_MESSAGE);
            returnCondition = Condition.ITEM_CANCELED;
            return;
        }

        returnCondition = Condition.ITEM_COMPLETE;
    }

    private void updateStatusOfExtensions(I_GetConceptData extensionConcept, I_GetConceptData statusConcept)
            throws Exception {
        Collection<? extends I_ExtendByRef> extensions =
                termFactory.getRefsetExtensionMembers(extensionConcept.getConceptNid());

        for (I_ExtendByRef extension : extensions) {
            // get the latest version
            I_ExtendByRefPart latestPart = null;
            for (I_ExtendByRefPart part : extension.getMutableParts()) {
                if ((latestPart == null) || (part.getVersion() >= latestPart.getVersion())) {
                    latestPart = part;
                }
            }

            if (latestPart != null && latestPart.getStatusId() != retiredStatusId) {

                for (PathBI editPath : termFactory.getActiveAceFrameConfig().getPromotionPathSet()) {
                    latestPart.makeAnalog(statusConcept.getConceptNid(), editPath
                                .getConceptNid(), Long.MAX_VALUE);
                    termFactory.addUncommittedNoChecks(extension);
                }
            }
        }
    }

    private void updateStatusOfDescriptions(Set<I_GetConceptData> concepts, I_GetConceptData statusConcept)
            throws IOException, TerminologyException {
        for (I_GetConceptData currentConcept : concepts) {
            Collection<? extends I_DescriptionVersioned> descs = currentConcept.getDescriptions();
            for (I_DescriptionVersioned descVersioned : descs) {
                for (PathBI editPath : termFactory.getActiveAceFrameConfig().getPromotionPathSet()) {
                    I_DescriptionPart templatePart = descVersioned.getLastTuple().getMutablePart();
                    if (templatePart.getStatusId() != retiredStatusId) {
                        templatePart.makeAnalog(statusConcept.getConceptNid(), editPath.getConceptNid(), Long.MAX_VALUE);
                    }
                }
            }
            termFactory.addUncommittedNoChecks(currentConcept);
        }
    }

    //TODO change algorithm for finding latest parts to use version computer...
    private void updateStatusOfRelationships(Set<I_GetConceptData> concepts, I_GetConceptData statusConcept)
            throws IOException, TerminologyException {
        for (I_GetConceptData currentConcept : concepts) {
            Collection<? extends I_RelVersioned> rels = currentConcept.getSourceRels();
            for (I_RelVersioned relVersioned : rels) {
                for (PathBI editPath : termFactory.getActiveAceFrameConfig().getPromotionPathSet()) {
                    I_RelPart newPart = relVersioned.getLastTuple().getMutablePart();
                    if (newPart.getStatusId() != retiredStatusId) {
                        relVersioned.addVersion((I_RelPart) newPart.makeAnalog(statusConcept.getConceptNid(), 
                        		editPath.getConceptNid(), Long.MAX_VALUE));
                    }
                }
            }
            termFactory.addUncommittedNoChecks(currentConcept);
        }
    }

    //TODO change algorithm for finding latest parts to use version computer...
   private void updateStatusOfConcepts(Set<I_GetConceptData> concepts, I_GetConceptData statusConcept)
            throws IOException, TerminologyException {
        for (I_GetConceptData currentConcept : concepts) {
            I_ConceptAttributeVersioned v = currentConcept.getConceptAttributes();
            List<? extends I_ConceptAttributeTuple> tuples = v.getTuples();
            for (PathBI editPath : termFactory.getActiveAceFrameConfig().getPromotionPathSet()) {
                tuples.get(tuples.size() - 1).makeAnalog(
                                statusConcept.getConceptNid(), editPath.getConceptNid(), Long.MAX_VALUE);
            }
            termFactory.addUncommittedNoChecks(currentConcept);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public String getRefsetSpecUuidPropName() {
        return refsetSpecUuidPropName;
    }

    public void setRefsetSpecUuidPropName(String refsetSpecUuidPropName) {
        this.refsetSpecUuidPropName = refsetSpecUuidPropName;
    }

    public TermEntry getStatusTermEntry() {
        return statusTermEntry;
    }

    public void setStatusTermEntry(TermEntry statusTermEntry) {
        this.statusTermEntry = statusTermEntry;
    }

    public boolean isIncludeMetaDataConcepts() {
        return includeMetaDataConcepts;
    }

    public void setIncludeMetaDataConcepts(boolean includeMetaDataConcepts) {
        this.includeMetaDataConcepts = includeMetaDataConcepts;
    }

    public boolean isIncludeMetaDataRelationships() {
        return includeMetaDataRelationships;
    }

    public void setIncludeMetaDataRelationships(boolean includeMetaDataRelationships) {
        this.includeMetaDataRelationships = includeMetaDataRelationships;
    }

    public boolean isIncludeMetaDataDescriptions() {
        return includeMetaDataDescriptions;
    }

    public void setIncludeMetaDataDescriptions(boolean includeMetaDataDescriptions) {
        this.includeMetaDataDescriptions = includeMetaDataDescriptions;
    }

    public boolean isIncludeSpecExtensions() {
        return includeSpecExtensions;
    }

    public void setIncludeSpecExtensions(boolean includeSpecExtensions) {
        this.includeSpecExtensions = includeSpecExtensions;
    }

    public boolean isIncludeMemberExtensions() {
        return includeMemberExtensions;
    }

    public void setIncludeMemberExtensions(boolean includeMemberExtensions) {
        this.includeMemberExtensions = includeMemberExtensions;
    }

    public boolean isIncludeParentExtensions() {
        return includeParentExtensions;
    }

    public void setIncludeParentExtensions(boolean includeParentExtensions) {
        this.includeParentExtensions = includeParentExtensions;
    }

    public boolean isIncludeCommentExtensions() {
        return includeCommentExtensions;
    }

    public void setIncludeCommentExtensions(boolean includeCommentExtensions) {
        this.includeCommentExtensions = includeCommentExtensions;
    }

    public boolean isIncludePromotionExtensions() {
        return includePromotionExtensions;
    }

    public void setIncludePromotionExtensions(boolean includePromotionExtensions) {
        this.includePromotionExtensions = includePromotionExtensions;
    }

}
