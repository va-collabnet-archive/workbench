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
package org.dwfa.bpa.tasks.file;

import java.awt.FileDialog;
import java.awt.Frame;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/file", type = BeanType.TASK_BEAN) })
public class SpecifyOutputFile extends AbstractTask {

    private String outputFilePropName = "A: OUTPUT_FILE";
    private String prompt = "Select output file: ";
    private String extension = ".jcs";

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(outputFilePropName);
        out.writeObject(prompt);
        out.writeObject(extension);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            outputFilePropName = (String) in.readObject();
            prompt = (String) in.readObject();
            extension = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private transient Exception ex;

    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        ex = null;

        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    FileDialog dialog = new FileDialog(new Frame(), prompt);
                    dialog.setMode(FileDialog.SAVE);
                    dialog.setDirectory(System.getProperty("user.dir"));
                    try {
                        String defaultFileName = (String) process.readProperty(outputFilePropName);
                        if (defaultFileName != null && defaultFileName.length() > 1) {
                            File defaultFile = new File(defaultFileName);
                            dialog.setDirectory(defaultFile.getParent());
                            dialog.setFile(defaultFile.getName());
                        }
                    } catch (IllegalArgumentException e1) {
                        ex = e1;
                    } catch (IntrospectionException e1) {
                        ex = e1;
                    } catch (IllegalAccessException e1) {
                        ex = e1;
                    } catch (InvocationTargetException e1) {
                        ex = e1;
                    }
                    dialog.setVisible(true);
                    if (dialog.getFile() == null) {
                        ex = new TaskFailedException("User did not select file...");
                    } else {
                        try {
                            if (dialog.getFile().toLowerCase().endsWith(extension) == false) {
                                dialog.setFile(dialog.getFile() + extension);
                            }
                            process.setProperty(outputFilePropName, dialog.getDirectory() + File.separator
                                + dialog.getFile());
                        } catch (IntrospectionException e) {
                            ex = e;
                        } catch (IllegalAccessException e) {
                            ex = e;
                        } catch (InvocationTargetException e) {
                            ex = e;
                        }
                    }
                }
            });
        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        }

        if (ex != null) {
            throw new TaskFailedException(ex);
        }
        return Condition.CONTINUE;
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public String getOutputFilePropName() {
        return outputFilePropName;
    }

    public void setOutputFilePropName(String outputFilePropName) {
        this.outputFilePropName = outputFilePropName;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

}
