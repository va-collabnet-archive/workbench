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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

@BeanList(specs = { @Spec(directory = "tasks/ide/refset", type = BeanType.TASK_BEAN) })
public final class WriteRefsetDescriptions extends AbstractTask {

    private static final long serialVersionUID  = 1;
    private static final int dataVersion        = 1;

    private String filePropertyName;
    private LogMill logMill;

    public WriteRefsetDescriptions() {
        filePropertyName = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();
        logMill = new SimpleLogMill();
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(filePropertyName);
        out.writeObject(logMill);
     }

    private void readObject(final ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            filePropertyName = in.readUTF();
            logMill = (LogMill) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        String outputDirectoryPath = "unknown";
        try {
            TaskLogger taskLogger = new TaskLogger(worker);
            logMill.logInfo(taskLogger, "Exporting reference sets as description files");
            String filePath = (String) process.readProperty(filePropertyName);
            logMill.logInfo(taskLogger, "Export to path --> " + filePath);

            LocalVersionedTerminology.get().iterateExtByRefs(
                    new WriteRefsetDescriptionsProcessExtByRef(taskLogger, filePath));

        } catch (Exception e) {
            throw new TaskFailedException("The task failed with a path of -> " + outputDirectoryPath, e);
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
}
