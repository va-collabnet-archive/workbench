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
package org.dwfa.ace.task.search;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.ihtsdo.tk.api.ContradictionException;

public abstract class AbstractSearchTest extends AbstractTask implements I_TestSearchResults {

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
     * Attribute that indicates if the search criteria should be inverted
     */
    protected boolean inverted = false;

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

    @Override
    public final void complete(I_EncodeBusinessProcess bp, I_Work worker) throws TaskFailedException {
        // nothing to do..
    }

    @Override
    public final Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException, ContradictionException {

        try {
            I_AmTermComponent component = (I_AmTermComponent) process.getProperty(componentPropName);
            I_ConfigAceFrame profile = (I_ConfigAceFrame) process.getProperty(profilePropName);
            if (profile == null) {
                profile = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            }
            if (test(component, profile)) {
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
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.ace.task.search.I_TestSearchResults#test(org.dwfa.ace.api.
     * I_AmTermComponent)
     */
    @Override
    public abstract boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException, ContradictionException;

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    @Override
    public Collection<Condition> getConditions() {
        return CONDITIONAL_TEST_CONDITIONS_REVERSE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    @Override
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

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    /**
     * Applies the inversion attribute of this class to the specified result.
     * 
     * @param b
     * @return if inverted==true then the inverse of the passed boolean value,
     *         otherwise the unmodified passed boolean value
     */
    protected boolean applyInversion(boolean b) {
        if (inverted) {
            return !b;
        } else {
            return b;
        }
    }

}
