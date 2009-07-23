package org.dwfa.ace.task.refset.spec.importexport;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.file.TupleFileUtil;
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
 * Imports the refset currently in the refset spec panel to the specified file.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/export", type = BeanType.TASK_BEAN) })
public class ImportRefsetSpecTask extends AbstractTask {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String inputFilePropName = ProcessAttachmentKeys.DEFAULT_FILE
            .getAttachmentKey();

    public String getInputFilePropName() {
        return inputFilePropName;
    }

    public void setInputFilePropName(String inputFilePropName) {
        this.inputFilePropName = inputFilePropName;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(inputFilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            // Nothing to do
            inputFilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

        try {
            String fileName = (String) process.readProperty(inputFilePropName);

            // initialise the progress panel
            I_ShowActivity activityPanel = LocalVersionedTerminology.get()
                    .newActivityPanel(true);
            activityPanel.setIndeterminate(true);
            activityPanel
                    .setProgressInfoUpper("Importing refset spec from file : "
                            + fileName);
            activityPanel
                    .setProgressInfoLower("<html><font color='black'> In progress.");

            TupleFileUtil tupleImporter = new TupleFileUtil();
            tupleImporter.importFile(new File(fileName));

            LocalVersionedTerminology.get().commit();

            activityPanel
                    .setProgressInfoUpper("Importing refset spec from file : "
                            + fileName);
            activityPanel
                    .setProgressInfoLower("<html><font color='red'> COMPLETE. <font color='black'>");

            activityPanel.complete();

            return Condition.CONTINUE;
        } catch (Exception ex) {
            try {
                LocalVersionedTerminology.get().cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
            throw new TaskFailedException(ex);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }
}
