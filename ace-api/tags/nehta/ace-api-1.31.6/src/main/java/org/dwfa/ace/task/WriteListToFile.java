/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.ace.task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Logger;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * The {@code WriteListToFile} class is a subclass of {@code AbstractTask} that writes a {@code List} of
 * objects to a file.
 *
 * If the {@code List} of Objects is empty or null, no file will be written.
 *
 * When this class is modified, the equals and hashCode methods should also be updated.
 *
 * @author Mattthew Edwards
 */
@BeanList(specs = {@Spec(directory = "tasks/file", type = BeanType.TASK_BEAN)})
public final class WriteListToFile extends AbstractTask {

    private static final long serialVersionUID = 7;
    public static final String DEFAULT_FILE_NAME = "listOutput.txt";
    public static final String DEFAULT_PROPERTY_NAME = ProcessAttachmentKeys.OBJECTS_LIST.getAttachmentKey();
    private static final int DATA_VERSION = 0;
    private String objectListPropertyName;
    private String outputFile;

    /**
     * Creates an instance of the {@code WriteListToFile} task with the specified filename;
     * @param fileName the fileName to write to.
     * @param objectListPropertyName the name of the property holding the object list.
     */
    public WriteListToFile(String fileName, String objectListPropertyName) {
        this.outputFile = fileName;
        this.objectListPropertyName = objectListPropertyName;
    }

    /**
     * Default no-args constructor. Initialises file name to the default of {@code listOutput.txt} and the object list
     * property name to the default of {@code A: OBJECTS_LIST} as defined by
     * {@link ProcessAttachmentKeys#getAttachmentKey()}
     */
    public WriteListToFile() {
        this(DEFAULT_FILE_NAME, DEFAULT_PROPERTY_NAME);
    }

    /**
     * Deserialises this {@code WriteListToFile} task ti an {@code ObjectOutputStream}into.
     * @param out the {@code ObjectOutputStream} to serialize the object to.
     * @throws IOException will occur if there are failed or interrupted I/O operations.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(DATA_VERSION);
        out.writeObject(outputFile);
        out.writeObject(objectListPropertyName);
    }

    /**
     * Deserialises an input Stream into this {@code WriteListToFile} task.
     * <p>
     * When {@code DATA_VERSION} == 0, <br>
     * {@code outputFileName} is initialised to the serialized value or the default value of {@code listOutputFile.txt}.
     * <br>
     * {@code objectListPropertyName} is initialised to the serialized value or the default value of
     * {@code A: OBJECTS_LIST} as defined by {@link ProcessAttachmentKeys#getAttachmentKey()}
     * </p>
     * @param in the {@code ObjectInputStream} containing the serialized object.
     * @throws IOException will occur if there are failed or interrupted I/O operations.
     * @throws ClassNotFoundException if no definition for the specified class could be found.
     */
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();

        switch (objDataVersion) {
            case 0:
                String inOutputFile = (String) in.readObject();
                outputFile = inOutputFile == null ? DEFAULT_FILE_NAME : inOutputFile;
                String inObjectListPropertyName = (String) in.readObject();
                objectListPropertyName = inObjectListPropertyName == null ? DEFAULT_PROPERTY_NAME
                        : inObjectListPropertyName;
                break;
            default:
                throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            List<Object> list = (List<Object>) process.readProperty(objectListPropertyName);
            if (list != null && !list.isEmpty()) {
                this.writeList(list);
            }
            return Condition.CONTINUE;
        } catch (Exception ex) {
            throw new TaskFailedException(ex);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        Logger.getLogger(this.getClass().getName()).fine(String.format("'%1$s' complete.", this.getClass().getName()));
    }

    /**
     * Accessor for returning the conditions for this task. Always returns the Continue Condition.
     * @return the task conditions.
     */
    public List<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * Writes a List of objects to {@code outputFile}. .
     * If an object is not a {@code String}, the value from the {@link Object#toString()} method will be written.
     * @param list -  the List of {@code Objects} to write.
     * @throws IOException will occur if there are failed or interrupted I/O operations.
     */
    private void writeList(final List list) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            for (Object line : list) {
                writer.write(line.toString());
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    /**
     * Returns the value of {@code objectListPropertyName}
     * @return objectListPropertyName - the value of {@code objectListPropertyName}
     */
    public String getObjectListPropertyName() {
        return objectListPropertyName;
    }

    /**
     * Sets the value of {@code objectListPropertyName}
     * @param objectListPropertyName the value to assign to {@code objectListPropertyName}
     */
    public void setObjectListPropertyName(String objectListPropertyName) {
        this.objectListPropertyName = objectListPropertyName;
    }

    /**
     * Returns the value of {@code outputFile}
     * @return outputFile - the value of {@code outputFile}
     */
    public String getOutputFile() {
        return outputFile;
    }

    /**
     * Sets the value of {@code outputFile}
     * @param outputFile the value to assign to {@code outputFile}
     */
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WriteListToFile other = (WriteListToFile) obj;
        if ((this.objectListPropertyName == null) ? (other.objectListPropertyName != null)
                : !this.objectListPropertyName.equals(other.objectListPropertyName)) {
            return false;
        }
        if ((this.outputFile == null) ? (other.outputFile != null) : !this.outputFile.equals(other.outputFile)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.objectListPropertyName != null ? this.objectListPropertyName.hashCode() : 0);
        hash = 29 * hash + (this.outputFile != null ? this.outputFile.hashCode() : 0);
        return hash;
    }
}
