package org.dwfa.ace.task.refset.spec.importexport;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

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

    private static final int dataVersion = 2;

    private String inputFilePropName = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();
    private String outputFilePropName = ProcessAttachmentKeys.OUTPUT_FILE.getAttachmentKey();
    private String pathUuidPropName = ProcessAttachmentKeys.PATH_UUID.getAttachmentKey();

    public String getInputFilePropName() {
        return inputFilePropName;
    }

    public void setInputFilePropName(String inputFilePropName) {
        this.inputFilePropName = inputFilePropName;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(inputFilePropName);
        out.writeObject(outputFilePropName);
        out.writeObject(pathUuidPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= 1) {
            inputFilePropName = (String) in.readObject();
        } else if (objDataVersion == 2) {
            inputFilePropName = (String) in.readObject();
            outputFilePropName = (String) in.readObject();
            pathUuidPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        // initialise the progress panel
        I_ShowActivity activityPanel = LocalVersionedTerminology.get().newActivityPanel(true);
        try {
            String importFileName = (String) process.readProperty(inputFilePropName);
            String outputFileName = (String) process.readProperty(outputFilePropName);
            Object pathObj = process.readProperty(pathUuidPropName);
            UUID pathUuid;
            if (pathObj == null) {
                pathUuid = null;
            } else {
                pathUuid = (UUID) pathObj;
            }

            activityPanel.setIndeterminate(true);
            activityPanel.setProgressInfoUpper("Importing refset spec from file : " + importFileName);
            activityPanel.setProgressInfoLower("<html><font color='black'> In progress.");

            TupleFileUtil tupleImporter = new TupleFileUtil();
            tupleImporter.importFile(new File(importFileName), new File(outputFileName), pathUuid);

            LocalVersionedTerminology.get().commit();

            activityPanel.setProgressInfoUpper("Importing refset spec from file : " + importFileName);
            activityPanel.setProgressInfoLower("<html><font color='red'> COMPLETE. <font color='black'>");

            activityPanel.complete();

            return Condition.CONTINUE;
        } catch (Exception ex) {
            try {
                activityPanel.complete();
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

    public String getOutputFilePropName() {
        return outputFilePropName;
    }

    public void setOutputFilePropName(String outputFilePropName) {
        this.outputFilePropName = outputFilePropName;
    }

    public String getPathUuidPropName() {
        return pathUuidPropName;
    }

    public void setPathUuidPropName(String pathUuidPropName) {
        this.pathUuidPropName = pathUuidPropName;
    }
}
