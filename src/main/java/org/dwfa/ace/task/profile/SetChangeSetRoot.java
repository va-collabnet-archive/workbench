package org.dwfa.ace.task.profile;

import java.beans.IntrospectionException;
import java.io.File;
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
 * Sets the change set root directory to a new root. This change will take effect the next time a change set is opened. If a change 
 * set is already open, it will not take effect until the log rolls over to a new file. As a result, if immediate changes are required,
 * the environment should be restarted. 
 * @author kec
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/profile", type = BeanType.TASK_BEAN) })
public class SetChangeSetRoot extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String rootDirName = "profiles/changesets";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(rootDirName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            profilePropName = (String) in.readObject();
            rootDirName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }
    

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            
            I_ConfigAceFrame profile = (I_ConfigAceFrame) process.readProperty(profilePropName);
            
            File currentChangeSetFile = new File(profile.getDbConfig().getChangeSetRoot(), 
            		profile.getDbConfig().getChangeSetWriterFileName());
            worker.getLogger().info("Current (old) change set file: " + currentChangeSetFile.getAbsolutePath());
            profile.getDbConfig().setChangeSetRoot(new File(rootDirName));
            File newChangeSetFile = new File(profile.getDbConfig().getChangeSetRoot(), 
            		profile.getDbConfig().getChangeSetWriterFileName());
            worker.getLogger().info("New change set file: " + newChangeSetFile.getAbsolutePath());
            return Condition.CONTINUE;
            
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

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

	public String getRootDirName() {
		return rootDirName;
	}

	public void setRootDirName(String rootDirName) {
		this.rootDirName = rootDirName;
	}

}
