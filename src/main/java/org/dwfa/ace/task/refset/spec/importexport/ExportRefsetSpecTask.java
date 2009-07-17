package org.dwfa.ace.task.refset.spec.importexport;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.file.TupleFileUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Exports the refset currently in the refset spec panel to the specified file.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/export", type = BeanType.TASK_BEAN) })
public class ExportRefsetSpecTask extends AbstractTask {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String outputFilePropName = ProcessAttachmentKeys.DEFAULT_FILE
            .getAttachmentKey();

    public String getOutputFilePropName() {
        return outputFilePropName;
    }

    public void setOutputFilePropName(String outputFilePropName) {
        this.outputFilePropName = outputFilePropName;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(outputFilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            // Nothing to do
            outputFilePropName = (String) in.readObject();
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
            I_ConfigAceFrame configFrame = (I_ConfigAceFrame) worker
                    .readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
                            .name());
            String fileName = (String) process.readProperty(outputFilePropName);
            TupleFileUtil tupleExporter = new TupleFileUtil();

            if (configFrame.getRefsetSpecInSpecEditor() == null) {
                throw new TerminologyException(
                        "No refset spec found - the refset spec should have \n"
                                + "a src relationship of type 'specifies refset' to the \n "
                                + "member refset. Make sure the refset to be exported \n "
                                + "is in the refset spec panel.");
            }
            if (configFrame.getRefsetInSpecEditor() == null) {
                throw new TerminologyException(
                        "No member spec found. Please put the refset to \n "
                                + "be exported in the refset spec panel.");
            }
            File file = new File(fileName);

            tupleExporter.exportRefsetSpecToFile(file, configFrame
                    .getRefsetSpecInSpecEditor());

            return Condition.CONTINUE;
        } catch (Exception ex) {
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
