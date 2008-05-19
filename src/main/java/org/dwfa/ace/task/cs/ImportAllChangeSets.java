package org.dwfa.ace.task.cs;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.cs.ComponentValidator;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * @author kec
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/ace/change sets", type = BeanType.TASK_BEAN) })
public class ImportAllChangeSets extends AbstractTask {

    private String rootDirStr = "profiles/";

    private Boolean validateChangeSets = true;
    
    private String validators = ComponentValidator.class.getName();

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 3;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(rootDirStr);
        out.writeBoolean(validateChangeSets);
        out.writeObject(validators);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
                                                                 ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            rootDirStr = (String) in.readObject();
            if (objDataVersion > 1) {
            	validateChangeSets = in.readBoolean();
            } else {
            	validateChangeSets = true;
            }
            
            if (objDataVersion > 2) {
            	validators = (String) in.readObject();
            } else {
            	validators = ComponentValidator.class.getName();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
    throws TaskFailedException {

        LocalVersionedTerminology.get().suspendChangeSetWriters();

        ChangeSetImporter csi = new ChangeSetImporter() {
			@Override
			public I_ReadChangeSet getChangeSetReader(File csf) {
				try {
					return LocalVersionedTerminology.get().newBinaryChangeSetReader(csf);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
        };
        csi.importAllChangeSets(worker.getLogger(), validators, rootDirStr, validateChangeSets, ".jcs");
        LocalVersionedTerminology.get().resumeChangeSetWriters();

        return Condition.CONTINUE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
        throws TaskFailedException {
        // Nothing to do.

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /**
     * @return Returns the message.
     */
    public String getRootDirStr() {
        return rootDirStr;
    }

    /**
     * @param message
     *            The message to set.
     */
    public void setRootDirStr(String rootDirStr) {
        this.rootDirStr = rootDirStr;
    }

	public Boolean getValidateChangeSets() {
		return validateChangeSets;
	}

	public void setValidateChangeSets(Boolean validateChangeSets) {
		this.validateChangeSets = validateChangeSets;
	}

	public String getValidators() {
		return validators;
	}

	public void setValidators(String validators) {
		this.validators = validators;
	}
}
