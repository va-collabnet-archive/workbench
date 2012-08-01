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
package org.dwfa.ace.task.file;

import java.awt.Frame;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.dwfa.ace.log.AceLog;

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
 * Opens a Swing file dialog so that user can choose a file location. The user
 * is allowed to cancel, causing the task to return cancel condition.
 * 
 * @author Christine Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/file", type = BeanType.TASK_BEAN) })
public class ChooseTxtFileCancelOrCompleteTask extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    /**
     * The input file path.
     */
    private String fileName = "C:/Concepts_modified.txt";

    /**
     * The key used by file attachment.
     */
    private String fileKey = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();

    private String message = "Please select a file";
    protected transient boolean done;
    protected transient Condition returnCondition;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(fileName);
        out.writeObject(fileKey);
        out.writeObject(message);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            fileName = (String) in.readObject();
            fileKey = (String) in.readObject();
            message = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                doRun(process, worker);
            } else {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    try {
                        doRun(process, worker);
                    } catch (IntrospectionException ex) {
                        AceLog.getAppLog().alertAndLogException(ex);
                    } catch (IllegalAccessException ex) {
                        AceLog.getAppLog().alertAndLogException(ex);
                    } catch (InvocationTargetException ex) {
                         AceLog.getAppLog().alertAndLogException(ex);
                    }
                    
                }
            });
            }
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }

            return returnCondition;

            
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch(InterruptedException e){
            throw new TaskFailedException(e);
        }
    }
    
    private void doRun(I_EncodeBusinessProcess process, I_Work worker) 
            throws IntrospectionException, IllegalAccessException, InvocationTargetException{
        JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle(message);
            fileChooser.setFileFilter(new TxtFileFilter());
            int returnValue = fileChooser.showDialog(new Frame(), "Save");
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                fileName = fileChooser.getSelectedFile().getPath();
                if (!fileName.endsWith(".txt")) {
                    fileName = fileName + ".txt";
                }
                System.out.println(fileName);
                if (worker.getLogger().isLoggable(Level.INFO)) {
                    worker.getLogger().info(("Selected file: " + fileName));
                }
                process.setProperty(this.fileKey, fileName);

                returnCondition =  Condition.ITEM_COMPLETE;
                done = true;
                synchronized (ChooseTxtFileCancelOrCompleteTask.this) {
                    ChooseTxtFileCancelOrCompleteTask.this.notifyAll();
                }
            } else {
                returnCondition =  Condition.ITEM_CANCELED;
                done = true;
                synchronized (ChooseTxtFileCancelOrCompleteTask.this) {
                    ChooseTxtFileCancelOrCompleteTask.this.notifyAll();
                }
            }
    }

    private class TxtFileFilter extends FileFilter {
        @Override
        public boolean accept(File arg0) {
            return arg0.isDirectory() || arg0.getName().toLowerCase().endsWith(".txt");
        }

        @Override
        public String getDescription() {
            return ".txt";
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
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
    
    protected void waitTillDone(Logger l) {
        while (!this.isDone()) {
            try {
                wait();
            } catch (InterruptedException e) {
                l.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    protected void notifyTaskDone() {
        synchronized (ChooseTxtFileCancelOrCompleteTask.this) {
            ChooseTxtFileCancelOrCompleteTask.this.notifyAll();
        }
    }
    
    public boolean isDone() {
        return this.done;
    }
}
