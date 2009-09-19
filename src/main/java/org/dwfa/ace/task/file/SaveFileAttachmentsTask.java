package org.dwfa.ace.task.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JFileChooser;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.tasks.util.FileContent;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Prompts the user to save all of the BP's file attachments.
 * 
 * @author Christine Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/file", type = BeanType.TASK_BEAN) })
public class SaveFileAttachmentsTask extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {

            Collection<String> keys = process.getAttachmentKeys();

            for (String key : keys) {
                Object attachment = process.readAttachement(key);
                if (attachment instanceof FileContent) {
                    FileContent fileContent = (FileContent) attachment;

                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Save attachment: " + fileContent.getFilename());
                    fileChooser.setSelectedFile(new File(fileContent.getFilename()));

                    if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File newFile = fileChooser.getSelectedFile();

                        FileWriter fw = new FileWriter(newFile);
                        for (byte b : fileContent.getContents()) {
                            fw.write((int) b);
                        }
                        fw.flush();
                        fw.close();
                    }
                }
            }

            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }
}
