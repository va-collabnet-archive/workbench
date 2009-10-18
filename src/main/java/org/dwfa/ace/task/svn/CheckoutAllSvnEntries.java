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
public class CheckoutAllSvnEntries extends AbstractAllSvnEntriesTask {

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

    protected void doSvnTask(I_ConfigAceFrame config, SubversionData svd, String taskKey) throws TaskFailedException {
		try {
			SPECIAL_SVN_ENTRIES entry = SPECIAL_SVN_ENTRIES.valueOf(taskKey);
			BundleType bundleType = config.getBundleType();
			switch (entry) {
			case PROFILE_CSU:
				switch (bundleType) {
				case STAND_ALONE:
					// nothing to do...
					break;

				case CHANGE_SET_UPDATE:
					config.svnCheckout(svd);
					break;
				default:
					throw new TaskFailedException("Can't handle: " + bundleType);
				}
				
				break;
			default:
				throw new TaskFailedException("Don't know how to handle: " + entry);
			}
		} catch (IllegalArgumentException e) {
            config.svnCheckout(svd);			
		}
    }

}
