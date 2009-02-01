package org.dwfa.ace.task.svn;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/svn", type = BeanType.TASK_BEAN) })
public class AddSubversionEntryForUser extends AddSubversionEntry {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
    private String usernameProperty = ProcessAttachmentKeys.USERNAME.getAttachmentKey();
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(usernameProperty);
     }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            usernameProperty = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }
    @Override
    protected void addUserInfo(I_EncodeBusinessProcess process, I_ConfigAceFrame config) throws TaskFailedException {
        String username;
		try {
			username = (String) process.readProperty(usernameProperty);
		} catch (Exception e) {
			throw new TaskFailedException(e);
		} 
        String repoUrl = this.getRepoUrl();
        if (repoUrl.endsWith("/")) {
            this.setRepoUrl(repoUrl + username);
        } else {
            this.setRepoUrl(repoUrl + "/" + username);
        }
        String workingCopy = this.getWorkingCopy();
        if (workingCopy.endsWith("/")) {
            this.setWorkingCopy(workingCopy + username);
        } else {
            this.setWorkingCopy(workingCopy + "/" + username);
        }
    }

    public String getUsernameProperty() {
        return usernameProperty;
    }

    public void setUsernameProperty(String usernameProperty) {
        this.usernameProperty = usernameProperty;
    }

}
