package org.dwfa.ace.task.svn;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Modify the config for each SVN entry 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/svn", type = BeanType.TASK_BEAN) })
public class ModifyAllSvnEntries extends AbstractAllSvnEntriesTask {

	private static final long serialVersionUID = 5226090220893610305L;

	protected static Logger logger = Logger.getLogger(ModifyAllSvnEntries.class.getName());

	private static final int dataVersion = 1;
	
	private String repoUrl = 
		"https://au-ct.au-ct.org/svn/au-ct/branches/test-1.0/au-ct-edit-bundle/src/main/profiles/users";
	
	@Override
	protected void doSvnTask(I_ConfigAceFrame config, SubversionData svd, String taskKey) {
		String existingRepoUrl = svd.getRepositoryUrlStr();
		
		logger.info("The existing repo url in the profile is " + existingRepoUrl);
		logger.info("Setting repo url in the profile to " + getRepoUrl());
		
		svd.setRepositoryUrlStr(getRepoUrl());		
	}

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(repoUrl);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            repoUrl = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }	

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }
    
	public String getRepoUrl() {
		return repoUrl;
	}

	public void setRepoUrl(String repoUrl) {
		this.repoUrl = repoUrl;
	}

}
