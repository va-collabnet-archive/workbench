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

import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.LinkedList;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Creates an attachment list based on a previously selected file.
 * @author Christine Hill
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/ace", type = BeanType.TASK_BEAN) })
public class NewAttachmentListFromChosenFile extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;


    /**
     * The key used by file attachment.
     */
    private String fileKey = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();

    /**
     * The input file name.
     */
    private String fileName = "C:/working/au-ct/change-sets/target/classes/change-sets/Concepts_modified.txt";

    /**
     * The name/key of the attachment list.
     */
    private String listName = ProcessAttachmentKeys.DEFAULT_CONCEPT_LIST.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(fileName);
        out.writeObject(listName);
        out.writeObject(fileKey);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            fileName = (String) in.readObject();
            listName = (String) in.readObject();
            fileKey = (String) in.readObject();
        } else {
            throw new IOException(
                    "Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
                         throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
                                throws TaskFailedException {
        try {
            fileName = (String) process.readProperty(fileKey);

            ArrayList<Collection<UUID>> temporaryList
                    = new ArrayList<Collection<UUID>>();

            if (worker.getLogger().isLoggable(Level.INFO)) {
                worker.getLogger().info(("Reading in file: " + fileName));
            }

            BufferedReader in = new BufferedReader(new FileReader(fileName));

            // read file into list
            String currentLine = in.readLine();
            while (currentLine != null) {
                if (worker.getLogger().isLoggable(Level.FINE)) {
                    worker.getLogger().fine(("From file: " + currentLine));
                }
                LinkedList<UUID> list = new LinkedList<UUID>();
                list.add(UUID.fromString(currentLine));
                temporaryList.add(list);
                currentLine = in.readLine();
            }

            in.close();

            process.setProperty(this.listName, temporaryList);

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
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

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }
}
