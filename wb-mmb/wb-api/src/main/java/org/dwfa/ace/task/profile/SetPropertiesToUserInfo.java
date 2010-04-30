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
package org.dwfa.ace.task.profile;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * The SetPropertiesToUserInfo class is a Workflow task that takes the name of
 * the working profile for the current process and sets the associated username
 * and FullName attributes as properties of the current process.
 * 
 * @author Perry Reid
 * @version 1.0, October 2009
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/profile", type = BeanType.TASK_BEAN) })
public class SetPropertiesToUserInfo extends AbstractTask {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String usernamePropName = ProcessAttachmentKeys.USERNAME.getAttachmentKey();
    private String fullNamePropName = ProcessAttachmentKeys.FULLNAME.getAttachmentKey();

    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(usernamePropName);
        out.writeObject(fullNamePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            profilePropName = (String) in.readObject();
            usernamePropName = (String) in.readObject();
            fullNamePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * Handles actions required by the task after normal task completion (such
     * as moving a
     * process to another user's input queue).
     * 
     * @return void
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    /**
     * Performs the primary action of the task, which in this case is to expose
     * the username and FullName attributes of the current user's profile as
     * properties
     * of the current Workflow process.
     * 
     * @return The exit condition of the task
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {

            I_ConfigAceFrame config = (I_ConfigAceFrame) process.getProperty(profilePropName);

            // Set username
            process.setProperty(this.usernamePropName, config.getUsername());

            // Set FullName
            process.setProperty(this.fullNamePropName, config.getDbConfig().getFullName());

            return Condition.CONTINUE;

        } catch (NullPointerException e) {
            throw new TaskFailedException(e);
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

    /**
     * Returns the condition of the executing task... which normally
     * will be the CONTINUE_CONDITION.
     * 
     * @return The current condition of the task
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    /**
     * Get the current user's profile name
     * 
     * @return The name of the current user's profile.
     */
    public String getProfilePropName() {
        return profilePropName;
    }

    /**
     * Set the current user's profile name
     * 
     * @param profilePropName The name of the current profile.
     * @return void
     */
    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    /**
     * Get the current user's username
     * 
     * @return The username of the current user
     */
    public String getUsernamePropName() {
        return usernamePropName;
    }

    /**
     * Set the current user's username
     * 
     * @param usernamePropName The username of the current user
     */
    public void setUsernamePropName(String usernamePropName) {
        this.usernamePropName = usernamePropName;
    }

    /**
     * Get the current user's FullName
     * 
     * @return The FullName of the current user
     */
    public String getFullNamePropName() {
        return fullNamePropName;
    }

    /**
     * Set the current user's FullName
     * 
     * @param fullNamePropName The FullName of the current user
     */
    public void setFullNamePropName(String fullNamePropName) {
        this.fullNamePropName = fullNamePropName;
    }

}
