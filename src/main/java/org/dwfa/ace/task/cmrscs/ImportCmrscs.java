package org.dwfa.ace.task.cmrscs;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.logging.Logger;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.task.cs.ChangeSetImporter;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/cmrscs", type = BeanType.TASK_BEAN) })
public class ImportCmrscs extends AbstractTask {

    private String rootDirStr = "profiles";

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(rootDirStr);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
                                                                 ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            rootDirStr = (String) in.readObject();
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

        importAllChangeSets(worker.getLogger());

        return Condition.CONTINUE;
    }

	public void importAllChangeSets(Logger logger) throws TaskFailedException {
		LocalVersionedTerminology.get().suspendChangeSetWriters();
        ChangeSetImporter csi = new ChangeSetImporter() {
			@Override
			public I_ReadChangeSet getChangeSetReader(File csf) {
				return new CmrscsReader(csf);
			}
        };
        csi.importAllChangeSets(logger, null, rootDirStr, false, ".cmrscs");

        LocalVersionedTerminology.get().resumeChangeSetWriters();
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
}