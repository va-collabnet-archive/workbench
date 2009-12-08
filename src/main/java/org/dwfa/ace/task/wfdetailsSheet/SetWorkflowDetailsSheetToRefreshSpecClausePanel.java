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
package org.dwfa.ace.task.wfdetailsSheet;

import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.utypes.UniversalAcePosition;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/gui/workflow/detail sheet", type = BeanType.TASK_BEAN) })
public class SetWorkflowDetailsSheetToRefreshSpecClausePanel extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 2;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
    private String refsetPositionSetPropName = ProcessAttachmentKeys.POSITION_SET.getAttachmentKey();

    private String snomedPositionSetPropName = ProcessAttachmentKeys.POSITION_LIST.getAttachmentKey();
    private String clausesToUpdateMemberUuidPropName = ProcessAttachmentKeys.REFSET_MEMBER_UUID.getAttachmentKey();

    private transient Exception ex = null;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(refsetUuidPropName);
        out.writeObject(refsetPositionSetPropName);
        out.writeObject(snomedPositionSetPropName);
        out.writeObject(clausesToUpdateMemberUuidPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            profilePropName = (String) in.readObject();
            refsetUuidPropName = (String) in.readObject();
            refsetPositionSetPropName = (String) in.readObject();
            snomedPositionSetPropName = (String) in.readObject();
            if (objDataVersion == 1) {
                in.readObject();
            }
            clausesToUpdateMemberUuidPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
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
        return Condition.CONTINUE;
    }

    private void doRun(final I_EncodeBusinessProcess process, final I_Work worker) {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());
            ClearWorkflowDetailsSheet clear = new ClearWorkflowDetailsSheet();
            clear.setProfilePropName(getProfilePropName());
            clear.evaluate(process, worker);
            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
            int width = 750;
            int height = 300;
            workflowDetailsSheet.setSize(width, height);
            workflowDetailsSheet.setLayout(new GridLayout(1, 1));
            I_TermFactory tf = LocalVersionedTerminology.get();

            UUID refsetSpecUuid = (UUID) process.getProperty(refsetUuidPropName);
            Set<UniversalAcePosition> universalRefsetSpecVersionSet = (Set<UniversalAcePosition>) process.getProperty(refsetPositionSetPropName);
            Set<I_Position> refsetSpecVersionSet = new HashSet<I_Position>();
            for (UniversalAcePosition univPos : universalRefsetSpecVersionSet) {
                I_Path path = tf.getPath(univPos.getPathId());
                I_Position thinPos = tf.newPosition(path, tf.convertToThinVersion(univPos.getTime()));
                refsetSpecVersionSet.add(thinPos);
            }

            Set<UniversalAcePosition> universalSourceTerminologyVersionSet = (Set<UniversalAcePosition>) process.getProperty(snomedPositionSetPropName);
            Set<I_Position> sourceTerminologyVersionSet = new HashSet<I_Position>();
            for (UniversalAcePosition univPos : universalSourceTerminologyVersionSet) {
                I_Path path = tf.getPath(univPos.getPathId());
                I_Position thinPos = tf.newPosition(path, tf.convertToThinVersion(univPos.getTime()));
                sourceTerminologyVersionSet.add(thinPos);
            }

            I_ConfigAceFrame frameConfig = (I_ConfigAceFrame) process.getProperty(getProfilePropName());
            List<Collection<UUID>> clausesToUpdate = (List<Collection<UUID>>) process.getProperty(clausesToUpdateMemberUuidPropName);

            I_GetConceptData refsetSpec = LocalVersionedTerminology.get().getConcept(refsetSpecUuid);
            workflowDetailsSheet.add(new RefreshSpecClausePanel(refsetSpec, refsetSpecVersionSet,
                sourceTerminologyVersionSet, clausesToUpdate, frameConfig));
        } catch (Exception e) {
            ex = e;
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getRefsetUuidPropName() {
        return refsetUuidPropName;
    }

    public void setRefsetUuidPropName(String refsetUuidPropName) {
        this.refsetUuidPropName = refsetUuidPropName;
    }

    public String getRefsetPositionSetPropName() {
        return refsetPositionSetPropName;
    }

    public void setRefsetPositionSetPropName(String refsetPositionSetPropName) {
        this.refsetPositionSetPropName = refsetPositionSetPropName;
    }

    public String getSnomedPositionSetPropName() {
        return snomedPositionSetPropName;
    }

    public void setSnomedPositionSetPropName(String snomedPositionSetPropName) {
        this.snomedPositionSetPropName = snomedPositionSetPropName;
    }

    public String getClausesToUpdateMemberUuidPropName() {
        return clausesToUpdateMemberUuidPropName;
    }

    public void setClausesToUpdateMemberUuidPropName(String clauseToUpdateMemberUuidPropName) {
        this.clausesToUpdateMemberUuidPropName = clauseToUpdateMemberUuidPropName;
    }
}
