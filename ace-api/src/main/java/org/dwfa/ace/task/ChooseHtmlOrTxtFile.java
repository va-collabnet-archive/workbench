/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task;

import java.awt.FileDialog;
import java.awt.Frame;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Opens a file dialog and displays only .txt or html files for selection and
 * sets the directory property based
 * on the location of the file selected.
 * 
 * @author Susan Castillo
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/assignments", type = BeanType.TASK_BEAN) })
public class ChooseHtmlOrTxtFile extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String defaultDir = "luceneDups/dupPotMatchResults";

    /**
     * The key used by file attachment.
     */
    private String instructionFileNamePropName = ProcessAttachmentKeys.INSTRUCTION_FILENAME.getAttachmentKey();
    private String directoryPropName = ProcessAttachmentKeys.DETAIL_HTML_DIR.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(directoryPropName);
        out.writeObject(instructionFileNamePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            directoryPropName = (String) in.readObject();
            instructionFileNamePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            // prompt to find file
            FileDialog dialog = new FileDialog(new Frame(), "Select Instruction File");
            dialog.setDirectory(defaultDir);
            dialog.setFilenameFilter(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".html") || name.endsWith(".txt");
                }
            });
            dialog.setVisible(true);
            if (dialog.getFile() == null) {
                return Condition.ITEM_CANCELED;
            }

            File selectedFile = new File(dialog.getDirectory(), dialog.getFile());
            String selectedDir = dialog.getDirectory() + "details" + File.separator;

            worker.getLogger().info("Directory is: " + selectedDir);

            if (worker.getLogger().isLoggable(Level.INFO)) {
                worker.getLogger().info(("Selected file: " + selectedFile));
            }

            process.setProperty(this.instructionFileNamePropName, selectedFile.getAbsoluteFile().toString());
            process.setProperty(directoryPropName, selectedDir);

            return Condition.ITEM_COMPLETE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public String getDefaultDir() {
        return defaultDir;
    }

    public void setDefaultDir(String defaultDir) {
        this.defaultDir = defaultDir;
    }

    public String getDirectoryPropName() {
        return directoryPropName;
    }

    public void setDirectoryPropName(String directoryPropName) {
        this.directoryPropName = directoryPropName;
    }

    public String getInstructionFileNamePropName() {
        return instructionFileNamePropName;
    }

    public void setInstructionFileNamePropName(String instructionFileNamePropName) {
        this.instructionFileNamePropName = instructionFileNamePropName;
    }

}
