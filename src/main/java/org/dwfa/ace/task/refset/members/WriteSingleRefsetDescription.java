package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

/**
 * Task that exports a single specified referenceset.
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/refset/membership", type = BeanType.TASK_BEAN) })
public final class WriteSingleRefsetDescription extends AbstractTask {

    private static final long serialVersionUID  = 484045781156873929L;
    private static final int dataVersion        = 1;

    private transient CleanableProcessExtByRefBuilder cleanableProcessBuilder;
    private transient PropertyValidator propertyValidator;
    private transient LocalVersionedTerminologyWrapper terminologyWrapper;

    private String selectedRefsetKey;
    private String directoryKey;

    public WriteSingleRefsetDescription() {
        //default contructor for annotated beans.
        selectedRefsetKey = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
        directoryKey = ProcessAttachmentKeys.WORKING_DIR.getAttachmentKey();
        setTransientProperties();
    }

    @ForTesting
    WriteSingleRefsetDescription(final String selectedRefsetKey,
        final CleanableProcessExtByRefBuilder cleanableProcessBuilder, final String directoryKey,
        final PropertyValidator propertyValidator,
        final LocalVersionedTerminologyWrapper terminologyWrapper) {
        this.selectedRefsetKey = selectedRefsetKey;
        this.cleanableProcessBuilder = cleanableProcessBuilder;
        this.directoryKey = directoryKey;
        this.propertyValidator = propertyValidator;
        this.terminologyWrapper = terminologyWrapper;
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            File outputDirecotry = (File) process.readProperty(directoryKey);
            propertyValidator.validate(outputDirecotry, "output directory");
            I_ThinExtByRefVersioned refset = (I_ThinExtByRefVersioned) process.readProperty(selectedRefsetKey);
            propertyValidator.validate(refset, "selected refset");

            CleanableProcessExtByRef processor = cleanableProcessBuilder.
                                                    withTermFactory(terminologyWrapper.get()).
                                                    withLogger(new TaskLogger(worker)).
                                                    withSelectedDir(outputDirecotry).
                                                    build();

            try {
                processor.processExtensionByReference(refset);
            } finally {
                processor.clean();
            }

            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public void complete(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        //do nothing.
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(directoryKey);
        out.writeUTF(selectedRefsetKey);
     }

    private void readObject(final ObjectInputStream in) throws IOException,
            ClassNotFoundException {
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
