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
package org.dwfa.ace.task.refset.members;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.TaskLogger;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * A Task that exports all reference sets to a directory specified in the
 * <code>directoryKey</code> property.
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/refset/membership", type = BeanType.TASK_BEAN) })
public final class WriteRefsetDescriptions extends AbstractTask {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    private transient LocalVersionedTerminologyWrapper terminologyWrapper;
    private transient CleanableProcessExtByRefBuilder cleanableProcessExtByRefBuilder;
    private String directoryKey;

    public WriteRefsetDescriptions() {
        directoryKey = ProcessAttachmentKeys.WORKING_DIR.getAttachmentKey();
        setTransientProperties();
    }

    /**
     * This is used only for testing. This is the lesser of 3 evils. We could:
     * 1. Use setter injection.
     * 2. Use reflection.
     * 3. Use constructor injection.
     * 
     * to set dependencies.
     * 
     * Why is constructor injection better?
     * 
     * 1. We have a compile-time check for this constructor (as opposed to
     * reflection which checks only @ runtime)
     * 2. We don't expose setters that could be used to modify the object
     * post-construction. This could lead to nasty
     * side-effects.
     * 3. We could legitimately use this constructor if we desired to.
     * 
     * @param directoryKey The name of the key used to store the default
     *            directory.
     * @param terminologyWrapper The <code>I_TermFactory</code> wrapper to to
     *            use.
     * @param cleanableProcessExtByRefBuilder Builder used to create the
     *            <code>CleanableProcessExtByRef</code> instance.
     */
    @ForTesting
    WriteRefsetDescriptions(final String directoryKey, final LocalVersionedTerminologyWrapper terminologyWrapper,
            final CleanableProcessExtByRefBuilder cleanableProcessExtByRefBuilder) {
        this.directoryKey = directoryKey;
        this.terminologyWrapper = terminologyWrapper;
        this.cleanableProcessExtByRefBuilder = cleanableProcessExtByRefBuilder;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(directoryKey);
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            directoryKey = in.readUTF();
            setTransientProperties();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        File selectedDirectory = null;
        try {
            TaskLogger taskLogger = new TaskLogger(worker);
            taskLogger.logInfo("Exporting reference sets as description files");
            taskLogger.logInfo("reading directoryKey ->" + directoryKey);
            selectedDirectory = (File) process.getProperty(directoryKey);
            taskLogger.logInfo("Export to path --> " + selectedDirectory);

            I_TermFactory termFactory = terminologyWrapper.get();
            CleanableProcessExtByRef refsetDescriptionWriter = cleanableProcessExtByRefBuilder.withTermFactory(
                termFactory).withLogger(taskLogger).withSelectedDir(selectedDirectory).build();
            try {
                termFactory.iterateExtByRefs(refsetDescriptionWriter);
            } finally {
                // if any writing fails close the open connections.
                refsetDescriptionWriter.clean();
            }

            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException("The task failed with a path of -> " + selectedDirectory, e);
        }
    }

    public void complete(final I_EncodeBusinessProcess i_encodeBusinessProcess, final I_Work i_work) {
        // do nothing on completion.
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    @Override
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    @ForJavaBeans
    public String getDirectoryKey() {
        return directoryKey;
    }

    @ForJavaBeans
    public void setDirectoryKey(final String directoryKey) {
        this.directoryKey = directoryKey;
    }

    private void setTransientProperties() {
        terminologyWrapper = new LocalVersionedTerminologyWrapperImpl();
        cleanableProcessExtByRefBuilder = new WriteRefsetDescriptionsProcessExtByRefBuilder();
    }
}
