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
package org.dwfa.ace.task.refset.spec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;

public abstract class AbstractAddRefsetSpecTask extends AbstractTask {

    private String activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT_UUID.getAttachmentKey();
    private String activeDescriptionPropName = ProcessAttachmentKeys.ACTIVE_DESCRIPTION_UUID.getAttachmentKey();
    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 4;

    private Boolean clauseIsTrue = true;
    private transient Exception ex = null;
    private transient Condition returnCondition = Condition.CONTINUE;
    protected I_DescriptionVersioned c3Description;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeBoolean(clauseIsTrue);
        out.writeObject(activeConceptPropName);
        out.writeObject(activeDescriptionPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion < 2) {
                clauseIsTrue = true;
            } else {
                clauseIsTrue = in.readBoolean();
            }
            if (objDataVersion == 3) {
                activeConceptPropName = (String) in.readObject();
            } else {
                activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT_UUID.getAttachmentKey();
            }
            if (objDataVersion >= 4) {
                activeDescriptionPropName = (String) in.readObject();
            } else {
                activeDescriptionPropName = ProcessAttachmentKeys.ACTIVE_DESCRIPTION_UUID.getAttachmentKey();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public final void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public final Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker)
            throws TaskFailedException {

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
        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        }
        if (ex != null) {
            throw new TaskFailedException(ex);
        }
        return returnCondition;
    }

    private void doRun(final I_EncodeBusinessProcess process, final I_Work worker) {
        try {
            I_ConfigAceFrame configFrame = LocalVersionedTerminology.get().getActiveAceFrameConfig();
            if (configFrame.getEditingPathSet().size() == 0) {
                String msg = "Unable to add spec. Editing path set is empty.";
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), msg);
                throw new TaskFailedException(msg);
            }

            UUID descriptionUuid = (UUID) process.readAttachement(activeDescriptionPropName);
            if (descriptionUuid != null) {
                c3Description = LocalVersionedTerminology.get().getDescription(descriptionUuid.toString());
            }

            I_GetConceptData refsetSpec = configFrame.getRefsetSpecInSpecEditor();
            if (refsetSpec != null) {
                JTree specTree = configFrame.getTreeInSpecEditor();

                int refsetId = refsetSpec.getConceptId();
                int componentId = refsetId;

                TreePath selection = specTree.getSelectionPath();
                DefaultMutableTreeNode selectedNode = null;
                boolean canAdd = true;
                if (selection != null) {
                    canAdd = false;
                    selectedNode = (DefaultMutableTreeNode) selection.getLastPathComponent();
                    I_ThinExtByRefVersioned selectedSpec = (I_ThinExtByRefVersioned) selectedNode.getUserObject();
                    componentId = selectedSpec.getMemberId();
                    if (selectedSpec.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize()
                        .getNid()) {
                        canAdd = true;
                    }
                }

                if (canAdd) {
                    I_TermFactory tf = LocalVersionedTerminology.get();
                    int typeId = getRefsetPartTypeId();
                    int memberId =
                            tf.uuidToNativeWithGeneration(UUID.randomUUID(),
                                ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), configFrame
                                    .getEditingPathSet(), Integer.MAX_VALUE);

                    I_ThinExtByRefVersioned ext = tf.newExtension(refsetId, memberId, componentId, typeId);
                    for (I_Path p : configFrame.getEditingPathSet()) {
                        I_ThinExtByRefPart specPart = createAndPopulatePart(tf, p, configFrame);
                        ext.addVersion(specPart);
                    }
                    tf.addUncommitted(ext);
                    configFrame.fireRefsetSpecChanged(ext);
                } else {
                    String msg = "Unable to add spec. Selected parent must be a branching spec.";
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), msg);
                    throw new TaskFailedException(msg);
                }
            } else {
                throw new TaskFailedException("Unable to complete operation. Refset is null.");
            }
            returnCondition = Condition.CONTINUE;
        } catch (Exception e) {
            ex = e;
        }
    }

    protected abstract I_ThinExtByRefPart createAndPopulatePart(I_TermFactory tf, I_Path p, I_ConfigAceFrame configFrame)
            throws IOException, TerminologyException;

    protected abstract int getRefsetPartTypeId() throws IOException, TerminologyException;

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public Boolean getClauseIsTrue() {
        if (clauseIsTrue == null) {
            clauseIsTrue = true;
        }
        return clauseIsTrue;
    }

    public void setClauseIsTrue(Boolean clauseIsTrue) {
        this.clauseIsTrue = clauseIsTrue;
    }

    public String getActiveConceptPropName() {
        return activeConceptPropName;
    }

    public void setActiveConceptPropName(String activeConceptPropName) {
        this.activeConceptPropName = activeConceptPropName;
    }

    public String getActiveDescriptionPropName() {
        return activeDescriptionPropName;
    }

    public void setActiveDescriptionPropName(String activeDescriptionPropName) {
        this.activeDescriptionPropName = activeDescriptionPropName;
    }

}
