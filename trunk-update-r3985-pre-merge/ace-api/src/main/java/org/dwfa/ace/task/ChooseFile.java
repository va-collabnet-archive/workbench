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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
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
 * Opens a file dialog so that user can choose a file location.
 * 
 * @author Christine Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/file", type = BeanType.TASK_BEAN) })
public class ChooseFile extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 3;

    /**
     * The input file path.
     */
    private String fileName = "C:/working/au-ct/change-sets/target/classes/change-sets/Concepts_modified.txt";

    /**
     * The key used by file attachment.
     */
    private String fileKey = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();

    private String message = "Please select a file";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private int mode = FileDialog.LOAD;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(fileName);
        out.writeObject(fileKey);
        out.writeInt(mode);
        out.writeObject(message);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            fileName = (String) in.readObject();
            fileKey = (String) in.readObject();
            if (objDataVersion == 2) {
                mode = in.readInt();
                message = "Please select a file";
            } else if (objDataVersion >= 3) {
                mode = in.readInt();
                message = (String) in.readObject();
            } else {
                mode = FileDialog.LOAD;
                message = "Please select a file";
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            // prompt for location of file
            FileDialog dialog = new FileDialog(new Frame(), message, mode);
            dialog.setVisible(true);
            fileName = dialog.getDirectory() + dialog.getFile();
            if (mode == FileDialog.LOAD) {
                fileName = new File(fileName).toURI().toURL().toExternalForm();
            }

            if (fileName == null) {
                throw new TaskFailedException("User failed to select a file.");
            }

            if (worker.getLogger().isLoggable(Level.INFO)) {
                worker.getLogger().info(("Selected file: " + fileName));
            }

            process.setProperty(this.fileKey, fileName);

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (MalformedURLException e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public Boolean isLoadMode() {
        return mode == FileDialog.LOAD;
    }

    public void setLoadMode(Boolean load) {
        if (load) {
            mode = FileDialog.LOAD;
        } else {
            mode = FileDialog.SAVE;
        }
    }
}
