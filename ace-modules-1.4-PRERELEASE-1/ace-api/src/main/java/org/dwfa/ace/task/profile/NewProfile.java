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
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;

public abstract class NewProfile extends AbstractTask {

    /**
	    * 
	    */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private String usernamePropName = ProcessAttachmentKeys.USERNAME.getAttachmentKey();

    private String passwordPropName = ProcessAttachmentKeys.PASSWORD.getAttachmentKey();

    private String adminUsernamePropName = ProcessAttachmentKeys.ADMIN_USERNAME.getAttachmentKey();

    private String adminPasswordPropName = ProcessAttachmentKeys.ADMIN_PASSWORD.getAttachmentKey();

    private String fullNamePropName = ProcessAttachmentKeys.FULLNAME.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(usernamePropName);
        out.writeObject(passwordPropName);
        out.writeObject(adminUsernamePropName);
        out.writeObject(adminPasswordPropName);
        out.writeObject(fullNamePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            profilePropName = (String) in.readObject();
            usernamePropName = (String) in.readObject();
            passwordPropName = (String) in.readObject();
            adminUsernamePropName = (String) in.readObject();
            adminPasswordPropName = (String) in.readObject();
            if (objDataVersion >= 2) {
                fullNamePropName = (String) in.readObject();
            } else {
                fullNamePropName = ProcessAttachmentKeys.FULLNAME.getAttachmentKey();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            String fullname = (String) process.readProperty(fullNamePropName);
            String username = (String) process.readProperty(usernamePropName);
            String password = (String) process.readProperty(passwordPropName);
            String adminUsername = (String) process.readProperty(adminUsernamePropName);
            String adminPassword = (String) process.readProperty(adminPasswordPropName);
            I_ConfigAceFrame newProfile = setupNewProfile(fullname, username, password, adminUsername, adminPassword);
            if (username != null) {
                if (newProfile.getAddressesList().contains(username) == false) {
                    newProfile.getAddressesList().add(username);
                }
            }
            process.setProperty(profilePropName, newProfile);
            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }

    protected abstract I_ConfigAceFrame setupNewProfile(String fullName, String username, String password,
            String adminUsername, String adminPassword) throws TerminologyException, IOException;

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String address) {
        this.profilePropName = address;
    }

    public static void addIfNotNull(I_IntSet roots, I_ConceptualizeUniversally concept, I_TermFactory tf)
            throws TerminologyException, IOException {
        try {
            roots.add(tf.uuidToNative(concept.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }
    }

    public static void addIfNotNull(I_IntList roots, I_ConceptualizeUniversally concept, I_TermFactory tf)
            throws TerminologyException, IOException {
        try {
            roots.add(tf.uuidToNative(concept.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }
    }

    public String getAdminPasswordPropName() {
        return adminPasswordPropName;
    }

    public void setAdminPasswordPropName(String adminPasswordPropName) {
        this.adminPasswordPropName = adminPasswordPropName;
    }

    public String getAdminUsernamePropName() {
        return adminUsernamePropName;
    }

    public void setAdminUsernamePropName(String adminUsernamePropName) {
        this.adminUsernamePropName = adminUsernamePropName;
    }

    public String getPasswordPropName() {
        return passwordPropName;
    }

    public void setPasswordPropName(String passwordPropName) {
        this.passwordPropName = passwordPropName;
    }

    public String getUsernamePropName() {
        return usernamePropName;
    }

    public void setUsernamePropName(String usernamePropName) {
        this.usernamePropName = usernamePropName;
    }

    public String getFullNamePropName() {
        return fullNamePropName;
    }

    public void setFullNamePropName(String fullNamePropName) {
        this.fullNamePropName = fullNamePropName;
    }

}
