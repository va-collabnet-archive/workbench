package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
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
 * A Task that exports all reference sets to a directory specified in the <code>directoryKey</code> property.
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/refset/membership", type = BeanType.TASK_BEAN) })
public final class WriteRefsetDescriptions extends AbstractTask {

    private static final long serialVersionUID  = 1;
    private static final int dataVersion        = 1;

    private String directoryKey;
    private transient I_TermFactory termFactory;
    private transient CleanableProcessExtByRefBuilder cleanableProcessExtByRefBuilder;

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
     * 1. We have a compile-time check for this constructor (as opposed to reflection which checks only @ runtime)
     * 2. We don't expose setters that could be used to modify the object post-construction. This could lead to nasty
     * side-effects.
     * 3. We could legitimately use this constructor if we desired to.
     *
     * @param directoryKey The name of the key used to store the default directory.
     * @param termFactory The <code>I_TermFactory</code> instance to use.
     * @param cleanableProcessExtByRefBuilder Builder used to create the <code>CleanableProcessExtByRef</code> instance.
     */
    @ForTesting
    WriteRefsetDescriptions(final String directoryKey, final I_TermFactory termFactory,
                            final CleanableProcessExtByRefBuilder cleanableProcessExtByRefBuilder) {
        this.directoryKey = directoryKey;
        this.termFactory = termFactory;
        this.cleanableProcessExtByRefBuilder = cleanableProcessExtByRefBuilder;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(directoryKey);
     }

    private void readObject(final ObjectInputStream in) throws IOException,
            ClassNotFoundException {
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
            taskLogger.logInfo("reading directoryKey ->"  + directoryKey);
            selectedDirectory = (File) process.readProperty(directoryKey);
            taskLogger.logInfo("Export to path --> " + selectedDirectory);

            CleanableProcessExtByRef refsetDescriptionWriter = cleanableProcessExtByRefBuilder.
                                                                withTermFactory(termFactory).
                                                                withLogger(taskLogger).
                                                                withSelectedDir(selectedDirectory).
                                                                build();
            try {
                termFactory.iterateExtByRefs(refsetDescriptionWriter);
            } finally {
                //if any writing fails close the open connections.
                refsetDescriptionWriter.clean();
            }

            return  Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException("The task failed with a path of -> " + selectedDirectory, e);
        }
    }

    public void complete(final I_EncodeBusinessProcess i_encodeBusinessProcess, final I_Work i_work) {
        //do nothing on completion.
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    @Override
    public int[] getDataContainerIds() {
        return new int[] {  };
    }

    public String getDirectoryKey() {
        return directoryKey;
    }

    public void setDirectoryKey(final String directoryKey) {
        this.directoryKey = directoryKey;
    }

    private void setTransientProperties() {
        termFactory = LocalVersionedTerminology.get();
        cleanableProcessExtByRefBuilder = new WriteRefsetDescriptionsProcessExtByRefBuilder();
    }
}
