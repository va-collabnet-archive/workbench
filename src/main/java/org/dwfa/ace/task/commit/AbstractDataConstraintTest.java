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

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;

public abstract class AbstractDataConstraintTest extends AbstractTask implements I_TestDataConstraints {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * Property name for the term component to test.
     */
    private String componentPropName = ProcessAttachmentKeys.SEARCH_TEST_ITEM.getAttachmentKey();

    /**
     * Profile to use for determining view paths, status values.
     */
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    /**
     * If true, shows an alert on failure, in addition to the message written to
     * the
     * worker's log, if false, failure messages are sent only to the worker's
     * log.
     */
    private Boolean showAlertOnFailure = false;

    /**
     * If true, the task does the data constraint test with the forCommit
     * paramater set to true.
     */
    private Boolean forCommit = true;

    private transient I_ConfigAceFrame frameConfig;

    public I_ConfigAceFrame getFrameConfig() {
        return frameConfig;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.componentPropName);
        out.writeObject(this.profilePropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.componentPropName = (String) in.readObject();
            this.profilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public final void complete(I_EncodeBusinessProcess bp, I_Work worker) throws TaskFailedException {
        // nothing to do..
    }

    public final Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            I_Transact component = (I_Transact) process.readProperty(componentPropName);
            List<AlertToDataConstraintFailure> alerts = test(component, forCommit);
            frameConfig = (I_ConfigAceFrame) process.readProperty(profilePropName);

            boolean noFailures = true;
            for (AlertToDataConstraintFailure failure : alerts) {
                if (showAlertOnFailure) {
                    Alerter alert = new Alerter(showAlertOnFailure, worker.getLogger(), failure);
                    alert.alert();
                }
                if (failure.getAlertType().equals(AlertToDataConstraintFailure.ALERT_TYPE.ERROR)) {
                    noFailures = false;
                }
            }
            if (noFailures) {
                return Condition.TRUE;
            }
            return Condition.FALSE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONDITIONAL_TEST_CONDITIONS_REVERSE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public String getComponentPropName() {
        return componentPropName;
    }

    public void setComponentPropName(String componentPropName) {
        this.componentPropName = componentPropName;
    }

    public abstract List<AlertToDataConstraintFailure> test(I_Transact component, boolean forCommit)
            throws TaskFailedException;

    public Boolean getShowAlertOnFailure() {
        return showAlertOnFailure;
    }

    public void setShowAlertOnFailure(Boolean showAlertOnFailure) {
        this.showAlertOnFailure = showAlertOnFailure;
    }

    public Boolean getForCommit() {
        return forCommit;
    }

    public void setForCommit(Boolean forCommit) {
        this.forCommit = forCommit;
    }

}
