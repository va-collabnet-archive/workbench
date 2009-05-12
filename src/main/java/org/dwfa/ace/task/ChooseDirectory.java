package org.dwfa.ace.task;

import org.dwfa.ace.task.refset.TaskLogger;
import org.dwfa.ace.task.util.Logger;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import javax.swing.JFileChooser;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

@BeanList(specs = { @Spec(directory = "tasks/ide/file", type = BeanType.TASK_BEAN) })
public final class ChooseDirectory extends AbstractTask {

    private static final long serialVersionUID = 960853732663625974L;
    private static final int dataVersion = 1;
    
    private String directoryKey;

    public ChooseDirectory() {
        directoryKey =  ProcessAttachmentKeys.WORKING_DIR.getAttachmentKey();
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(directoryKey);
    }

    private void readObject(final ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            directoryKey = in.readUTF();        
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {

        final JFileChooser chooser = new JFileChooser(".");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(null)) {
            throw new TaskFailedException("User failed to select a directory.");
        }

        final File selectedDirectory = chooser.getSelectedFile();
        final Logger logger = new TaskLogger(worker);
    
        logger.logInfo(("Selected directory: " + selectedDirectory.getName()));

        try {
            process.setProperty(directoryKey, selectedDirectory);
            logger.logInfo(directoryKey + " --> " + process.readProperty(directoryKey));
        } catch (Exception e) {
            logger.logWarn("Could not set set directory " + selectedDirectory.getName() + ", to key "+ directoryKey);
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    @Override
    public void complete(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        //do nothing.
    }

    @Override
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getDirectoryKey() {
        return directoryKey;
    }

    public void setDirectoryKey(final String directoryKey) {
        this.directoryKey = directoryKey;
    }
}
