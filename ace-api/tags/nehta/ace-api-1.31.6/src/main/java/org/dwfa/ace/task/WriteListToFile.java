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
import java.io.File;
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
    public static final String DEFAULT_MESSAGE_KEY = ProcessAttachmentKeys.DLG_MSG.getAttachmentKey();
    public static final String DEFAULT_FILE_WRITTEN_MESSAGE = "Output file was written. ";
    public static final String DEFAULT_FILE_NOT_WRITTEN_MESSAGE = "Output file not was written. ";
    private static final int DATA_VERSION = 0;
    private String objectListPropertyName;
    private String outputFile;
    private String messageKey;
    private String fileWrittenOutputMessage;
    private String fileNotWrittenOutputMessage;

    /**
     * Creates an instance of the {@code WriteListToFile} task with the specified filename;
     * @param fileName the fileName to write to.
     * @param objectListPropertyName the name of the property holding the object list.
     * @param messageKey the name of the property to hold messages.
     * @param fileWrittenOutputMessage a message to for when an output file is written.
     * @param fileNotWrittenOutputMessage a message to for when an output file is NOT written.
     */
    public WriteListToFile(String fileName, String objectListPropertyName, String messageKey,
            String fileWrittenOutputMessage, String fileNotWrittenOutputMessage) {
        this.outputFile = fileName;
        this.objectListPropertyName = objectListPropertyName;
        this.messageKey = messageKey;
        this.fileWrittenOutputMessage = fileWrittenOutputMessage;
        this.fileNotWrittenOutputMessage = fileNotWrittenOutputMessage;
    }

    /**
     * Default no-args constructor. Initialises file name to the default of {@code listOutput.txt} and the object list
     * property name to the default of {@code A: OBJECTS_LIST} as defined by
     * {@link ProcessAttachmentKeys#getAttachmentKey()}
     */
    public WriteListToFile() {
        this(DEFAULT_FILE_NAME, DEFAULT_PROPERTY_NAME, DEFAULT_MESSAGE_KEY, DEFAULT_FILE_WRITTEN_MESSAGE,
                DEFAULT_FILE_NOT_WRITTEN_MESSAGE);
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
        out.writeObject(messageKey);
        out.writeObject(fileWrittenOutputMessage);
        out.writeObject(fileNotWrittenOutputMessage);

    }

    /**
     * Deserialises an input Stream into this {@code WriteListToFile} task.
     * <p>
     * When {@code DATA_VERSION} == 0, <br>
     * {@code outputFileName} is initialised to the serialized value or the default value of {@code listOutputFile.txt}.
     * <br>
     * {@code objectListPropertyName} is initialised to the serialized value or the default value of
     * {@code A: OBJECTS_LIST} as defined by {@link ProcessAttachmentKeys#getAttachmentKey()}
     * <br>
     * {@code messageKey} is initialised to the serialized value or the default value of
     * {@code A: DLG_MSG} as defined by {@link ProcessAttachmentKeys#getAttachmentKey()}
     * <br>
     * {@code fileWrittenOutputMessage} is initialised to the serialized value or the default value of
     * {@code DEFAULT_FILE_WRITTEN_MESSAGE}.
     * <br>
     * {@code fileNotWrittenOutputMessage} is initialised to the serialized value or the default value of
     * {@code DEFAULT_FILE_NOT_WRITTEN_MESSAGE}.
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

                String inMessageKey = (String) in.readObject();
                messageKey = inMessageKey == null ? DEFAULT_MESSAGE_KEY
                        : inMessageKey;

                String inFileWrittenOutputMessage = (String) in.readObject();
                fileWrittenOutputMessage = inFileWrittenOutputMessage == null ? DEFAULT_FILE_WRITTEN_MESSAGE
                        : inFileWrittenOutputMessage;

                String inFileNotWrittenOutputMessage = (String) in.readObject();
                fileNotWrittenOutputMessage = inFileNotWrittenOutputMessage == null ? DEFAULT_FILE_NOT_WRITTEN_MESSAGE
                        : inFileNotWrittenOutputMessage;
                break;
            default:
                throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            Object inProperty = process.readProperty(objectListPropertyName);
            if (inProperty instanceof List) {
                List<Object> list = (List<Object>) inProperty;
                if (list != null && !list.isEmpty()) {
                    this.writeList(list);
                    this.appendMessage(process, String.format("%1$s%2$s", fileWrittenOutputMessage, new File(
                            outputFile).getAbsolutePath()));
                }
            } else {
                this.appendMessage(process, String.format("%1$s", fileNotWrittenOutputMessage));
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
            Logger.getLogger(this.getClass().getName()).info(String.format("Output file written to '%1$s'.", new File(
                    outputFile).getAbsolutePath()));
        } finally {
            writer.close();
        }
    }

    /**
     * Convenience method to encapsulate adding error messages to existing error messages.
     * @param process the current Business Process.
     * @throws Exception if there is an {@link java.beans.IntrospectionException}, {@link IllegalAccessException} or
     * {@link java.lang.reflect.InvocationTargetException} whilst accessing properties from the
     * {@link I_EncodeBusinessProcess}.
     */
    private void appendMessage(I_EncodeBusinessProcess process, String messageToAppend) throws Exception {
        StringBuilder messages = new StringBuilder();
        String previousMessages = (String) process.readProperty(messageKey);

        if (previousMessages != null && !previousMessages.isEmpty()) {
            messages.append(previousMessages).append("\n");
        }

        messages.append(messageToAppend);

        process.setProperty(messageKey, messages.toString());
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

    /**
     * Returns the value of ${code messageKey}
     * @return messageKey - the name of the property that messages will be placed into.
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * Sets the value of {@code messageKey}
     * @param messageKey - the value to assign to {@code messageKey}
     */
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * Returns the value of {@code fileNotWrittenOutputMessage}
     * @return fileNotWrittenOutputMessage -the text to add to a message when displaying a message about not writing
     * the file.
     */
    public String getFileNotWrittenOutputMessage() {
        return fileNotWrittenOutputMessage;
    }

    /**
     * Sets the value of {@code fileNotWrittenOutputMessage}
     * @param fileNotWrittenOutputMessage - the value to assign to {@code fileNotWrittenOutputMessage}
     */
    public void setFileNotWrittenOutputMessage(String fileNotWrittenOutputMessage) {
        this.fileNotWrittenOutputMessage = fileNotWrittenOutputMessage;
    }

    /**
     * Returns the value of {@code fileWrittenOutputMessage}
     * @return fileWrittenOutputMessage -the text to add to a message when displaying a message about writing the file.
     */
    public String getFileWrittenOutputMessage() {
        return fileWrittenOutputMessage;
    }

    /**
     * Sets the value of {@code fileWrittenOutputMessage}
     * @param fileWrittenOutputMessage - the value to assign to {@code fileWrittenOutputMessage}
     */
    public void setFileWrittenOutputMessage(String fileWrittenOutputMessage) {
        this.fileWrittenOutputMessage = fileWrittenOutputMessage;
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
        if ((this.messageKey == null) ? (other.messageKey != null) : !this.messageKey.equals(other.messageKey)) {
            return false;
        }
        if ((this.fileWrittenOutputMessage == null) ? (other.fileWrittenOutputMessage != null)
                : !this.fileWrittenOutputMessage.equals(other.fileWrittenOutputMessage)) {
            return false;
        }
        if ((this.fileNotWrittenOutputMessage == null) ? (other.fileNotWrittenOutputMessage != null)
                : !this.fileNotWrittenOutputMessage.equals(other.fileNotWrittenOutputMessage)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.objectListPropertyName != null ? this.objectListPropertyName.hashCode() : 0);
        hash = 89 * hash + (this.outputFile != null ? this.outputFile.hashCode() : 0);
        hash = 89 * hash + (this.messageKey != null ? this.messageKey.hashCode() : 0);
        hash = 89 * hash + (this.fileWrittenOutputMessage != null ? this.fileWrittenOutputMessage.hashCode() : 0);
        hash = 89 * hash + (this.fileNotWrittenOutputMessage != null ? this.fileNotWrittenOutputMessage.hashCode() : 0);
        return hash;
    }
}
