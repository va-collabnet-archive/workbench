package org.dwfa.ace.task.svn;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.BundleType;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.api.I_ConfigAceFrame.SPECIAL_SVN_ENTRIES;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/svn", type = BeanType.TASK_BEAN) })
public class PurgeSvnEntry extends AbstractSvnEntryTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            //;
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }


    protected void doSvnTask(I_ConfigAceFrame config, SubversionData svd, String svnEntryKey) throws TaskFailedException {
		try {
			SPECIAL_SVN_ENTRIES entry = SPECIAL_SVN_ENTRIES.valueOf(svnEntryKey);
			switch (entry) {
			case BERKELEY_DB:
				// nothing to do
				break;
			case PROFILE_CSU:
				// nothing to do
				break;
			case PROFILE_DBU:
				// nothing to do

			default:
				throw new TaskFailedException("Don't know how to handle: " + entry);
			}
		} catch (IllegalArgumentException e) {
            config.svnPurge(svd);			
		}
	}

}
