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

import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.task.util.ListUtil;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Reads list of UUID's from URL/File
 * @author Susan Castillo
 *
 */
@BeanList(specs = {@Spec(directory = "tasks/ide/assignments", type = BeanType.TASK_BEAN)})
public class ReadUuidListListFromUrl extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 3;
    private String uuidListListPropName = ProcessAttachmentKeys.UUID_LIST_LIST.getAttachmentKey();
    private String uuidFileNamePropName = ProcessAttachmentKeys.UUID_LIST_FILENAME.getAttachmentKey();
    private boolean continueOnError;
    private List<String> invalidUuids;
    private String uuidErrorMessage;

    public ReadUuidListListFromUrl() {
        uuidErrorMessage = "";
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(uuidListListPropName);
        out.writeObject(uuidFileNamePropName);
        out.writeBoolean(continueOnError);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();

        switch (objDataVersion) {
            case 0:
            case 1:
                uuidListListPropName = (String) in.readObject();
                uuidFileNamePropName = ProcessAttachmentKeys.UUID_LIST_FILENAME.getAttachmentKey();
                break;
            case 2:
                uuidListListPropName = (String) in.readObject();
                uuidFileNamePropName = (String) in.readObject();
                break;
            case 3:
                uuidListListPropName = (String) in.readObject();
                uuidFileNamePropName = (String) in.readObject();
                continueOnError = in.readBoolean();
                break;
            default:
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

            invalidUuids = new ArrayList<String>();
            List<List<UUID>> uuidListOfLists = new ArrayList<List<UUID>>();

            String uuidLineStr;

            String uuidFileName = (String) process.readProperty(uuidFileNamePropName);
            BufferedReader br = new BufferedReader(new FileReader(uuidFileName));

            while ((uuidLineStr = br.readLine()) != null) {
                List<UUID> uuidList = new ArrayList<UUID>();
                for (String uuidStr : uuidLineStr.split("\t")) {
                    worker.getLogger().fine("uuidStrs: " + uuidStr);
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        uuidList.add(uuid);
                    } catch (IllegalArgumentException iae) {
                        if (!continueOnError) {
                            throw iae;
                        }
                        getLogger().log(Level.WARNING, "Invalid UUID: " + uuidStr);
                        invalidUuids.add(uuidStr);
                        if (uuidErrorMessage == null || uuidErrorMessage.isEmpty()) {
                            uuidErrorMessage = iae.getMessage();
                        }
                    }
                }
                if (!uuidList.isEmpty()) {
                    uuidListOfLists.add(uuidList);
                }
            }


            process.setProperty(this.uuidListListPropName, uuidListOfLists);
            if (continueOnError && !invalidUuids.isEmpty()) {
                appendErrorMessages(process);
                appendErrorObjects(process);
            }

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (FileNotFoundException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getUuidListListPropName() {
        return uuidListListPropName;
    }

    public void setUuidListListPropName(String potDupUuidList) {
        this.uuidListListPropName = potDupUuidList;
    }

    public String getUuidFileNamePropName() {
        return uuidFileNamePropName;
    }

    public void setUuidFileNamePropName(String dupPotFileName) {
        this.uuidFileNamePropName = dupPotFileName;
    }

    public boolean isContinueOnError() {
        return continueOnError;
    }

    public void setContinueOnError(boolean isContinueOnError) {
        this.continueOnError = isContinueOnError;
    }

    /**
     * Convenience method to encapsulate adding error messages to existing error messages.
     * @param process
     * @throws IntrospectionException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void appendErrorMessages(I_EncodeBusinessProcess process) throws IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        StringBuilder errorMessages = new StringBuilder();

        String previousMessages = (String) process.readProperty(ProcessAttachmentKeys.ERROR_MESSAGE.getAttachmentKey());

        if (previousMessages != null && !previousMessages.isEmpty()) {
            errorMessages.append(previousMessages).append("\n");
        }

        errorMessages.append(uuidErrorMessage);

        process.setProperty(ProcessAttachmentKeys.ERROR_MESSAGE.getAttachmentKey(), errorMessages.toString());
    }

    /**
     * Convenience method to encapsulate appending objects to the Object List for use with the {@link WriteToFile} task.
     * @param process
     * @throws IntrospectionException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void appendErrorObjects(I_EncodeBusinessProcess process) throws IntrospectionException,
            IllegalAccessException, InvocationTargetException {

        List<Object> objects = new ArrayList<Object>();
        Object inProperty = process.readProperty(ProcessAttachmentKeys.OBJECTS_LIST.getAttachmentKey());
        if (inProperty instanceof List) {
            objects.addAll((List<Object>) inProperty);
        }
        objects.addAll(invalidUuids);
        process.setProperty(ProcessAttachmentKeys.OBJECTS_LIST.getAttachmentKey(), objects);
    }
}
