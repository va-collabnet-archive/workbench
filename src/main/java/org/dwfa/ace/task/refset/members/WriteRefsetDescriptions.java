package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.TaskLogger;
import org.dwfa.ace.task.util.LogMill;
import org.dwfa.ace.task.util.SimpleLogMill;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

@BeanList(specs = { @Spec(directory = "tasks/ide/refset/membership", type = BeanType.TASK_BEAN) })
public final class WriteRefsetDescriptions extends AbstractTask {

    private static final long serialVersionUID  = 1;
    private static final int dataVersion        = 1;

    private String directoryKey;
    private LogMill logMill;

    public WriteRefsetDescriptions() {
        directoryKey = ProcessAttachmentKeys.WORKING_DIR.getAttachmentKey();
        logMill = new SimpleLogMill();
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(directoryKey);
        out.writeObject(logMill);
     }

    private void readObject(final ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            directoryKey = in.readUTF();
            logMill = (LogMill) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        File selectedDirectory = null;
        try {
            TaskLogger taskLogger = new TaskLogger(worker);
            logMill.logInfo(taskLogger, "Exporting reference sets as description files");
            logMill.logInfo(taskLogger, "reading directoryKey ->"  + directoryKey);
            selectedDirectory = (File) process.readProperty(directoryKey);
            logMill.logInfo(taskLogger, "Export to path --> " + selectedDirectory);

            CleanableProcessExtByRef refsetDescriptionWriter = new WriteRefsetDescriptionsProcessExtByRef(taskLogger,
                    selectedDirectory);
            LocalVersionedTerminology.get().iterateExtByRefs(refsetDescriptionWriter);
            refsetDescriptionWriter.clean();

        } catch (Exception e) {
            throw new TaskFailedException("The task failed with a path of -> " + selectedDirectory, e);
        }

        return  Condition.CONTINUE;
    }

    public void complete(final I_EncodeBusinessProcess i_encodeBusinessProcess, final I_Work i_work) throws TaskFailedException {
        //do nothing on completion.
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    @Override
    public int[] getDataContainerIds() {
        return new int[] {  };
    }

    public String getDirectoryKey() {
        return directoryKey;
    }

    public void setDirectoryKey(final String directoryKey) {
        this.directoryKey = directoryKey;
    }
}
