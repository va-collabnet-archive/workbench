package org.dwfa.ace.task.profile;

import java.beans.IntrospectionException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/profile", type = BeanType.TASK_BEAN) })
public class SaveProfile extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String profileDir = "profiles/users";

    private String usernamePropName = ProcessAttachmentKeys.USERNAME.getAttachmentKey();
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profileDir);
        out.writeObject(usernamePropName);
        out.writeObject(profilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            profileDir = (String) in.readObject();
            usernamePropName = (String) in.readObject();
            profilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ImplementTermFactory termFactory = (I_ImplementTermFactory) LocalVersionedTerminology.get();
            I_ConfigAceDb aceConfig = termFactory.newAceDbConfig();
            String username = (String) process.readProperty(usernamePropName);
            I_ConfigAceFrame profile = (I_ConfigAceFrame) process.readProperty(profilePropName);
            aceConfig.getAceFrames().add(profile);
            File userDir = new File("profiles" + File.separator + "users" + File.separator + username);
            File changeSetRoot = new File(userDir, "changesets");
            changeSetRoot.mkdirs();
            aceConfig.setChangeSetRoot(changeSetRoot);
            aceConfig.setChangeSetWriterFileName(username + ".#0#" + UUID.randomUUID().toString() + ".jcs");
            
            File profileFile = new File(userDir, username + ".ace");
            profileFile.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(profileFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(aceConfig);
            oos.close();

            File startupFolder = new File(userDir, "startup");
            startupFolder.mkdirs();
            
            File shutdownFolder = new File(userDir, "shutdown");
            shutdownFolder.mkdirs();
         
            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (FileNotFoundException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getProfileDir() {
        return profileDir;
    }

    public void setProfileDir(String profileDir) {
        this.profileDir = profileDir;
    }

    public String getUsernamePropName() {
        return usernamePropName;
    }

    public void setUsernamePropName(String usernamePropName) {
        this.usernamePropName = usernamePropName;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }
}
