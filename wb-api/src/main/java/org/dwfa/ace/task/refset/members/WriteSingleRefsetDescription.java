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
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
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
 * Task that exports a single specified referenceset.
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/refset/membership", type = BeanType.TASK_BEAN) })
public final class WriteSingleRefsetDescription extends AbstractTask {

    private static final long serialVersionUID = 484045781156873929L;
    private static final int dataVersion = 1;

    private transient CleanableProcessExtByRefBuilder cleanableProcessBuilder;
    private transient PropertyValidator propertyValidator;
    private transient LocalVersionedTerminologyWrapper terminologyWrapper;

    private String selectedRefsetKey;
    private String directoryKey;

    public WriteSingleRefsetDescription() {
        // default contructor for annotated beans.
        selectedRefsetKey = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
        directoryKey = ProcessAttachmentKeys.WORKING_DIR.getAttachmentKey();
        setTransientProperties();
    }

    @ForTesting
    WriteSingleRefsetDescription(final String selectedRefsetKey,
            final CleanableProcessExtByRefBuilder cleanableProcessBuilder, final String directoryKey,
            final PropertyValidator propertyValidator, final LocalVersionedTerminologyWrapper terminologyWrapper) {
        this.selectedRefsetKey = selectedRefsetKey;
        this.cleanableProcessBuilder = cleanableProcessBuilder;
        this.directoryKey = directoryKey;
        this.propertyValidator = propertyValidator;
        this.terminologyWrapper = terminologyWrapper;
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            File outputDirecotry = (File) process.getProperty(directoryKey);
            propertyValidator.validate(outputDirecotry, "output directory");
            I_GetConceptData refset = (I_GetConceptData) process.getProperty(selectedRefsetKey);
            propertyValidator.validate(refset, "selected refset");

            I_TermFactory termFactory = terminologyWrapper.get();
            CleanableProcessExtByRef processor = cleanableProcessBuilder.withTermFactory(termFactory).withLogger(
                new TaskLogger(worker)).withSelectedDir(outputDirecotry).build();

            try {
                Collection<? extends I_ExtendByRef> extensions = termFactory.getRefsetExtensionMembers(refset.getConceptId());
                for (I_ExtendByRef extension : extensions) {
                    processor.processExtensionByReference(extension);
                }
            } finally {
                processor.clean();
            }

            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public void complete(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        // do nothing.
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(directoryKey);
        out.writeUTF(selectedRefsetKey);
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            directoryKey = in.readUTF();
            selectedRefsetKey = in.readUTF();
            setTransientProperties();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private void setTransientProperties() {
        try {
            cleanableProcessBuilder = new WriteRefsetDescriptionsProcessExtByRefBuilder();
            propertyValidator = new PropertyValidatorImpl();
            terminologyWrapper = new LocalVersionedTerminologyWrapperImpl();
        } catch (Exception e) {
            throw new IllegalStateException("Error when initializing transient variables.", e);
        }
    }

    @Override
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    @ForJavaBeans
    public String getSelectedRefsetKey() {
        return selectedRefsetKey;
    }

    @ForJavaBeans
    public String getDirectoryKey() {
        return directoryKey;
    }

    @ForJavaBeans
    public void setSelectedRefsetKey(final String selectedRefsetKey) {
        this.selectedRefsetKey = selectedRefsetKey;
    }

    @ForJavaBeans
    public void setDirectoryKey(final String directoryKey) {
        this.directoryKey = directoryKey;
    }
}
