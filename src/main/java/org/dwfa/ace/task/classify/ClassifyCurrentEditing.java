package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class ClassifyCurrentEditing extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        long startTime = System.currentTimeMillis();

        try {
            final Logger logger = worker.getLogger();
            
            // get uncommitted editing concept
            final I_HostConceptPlugins host =
                (I_HostConceptPlugins) worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());
            final I_GetConceptData concept = (I_GetConceptData) host.getTermComponent();
            
            if (null == concept) {
                throw new TaskFailedException("Could not find current concept.");
            }

            final I_SnorocketFactory rocket = (I_SnorocketFactory) process.readAttachement(ProcessKey.SNOROCKET.getAttachmentKey());

            // load the new concept into classifer
            logger.info("** Classifying: " + concept);

            new ClassifierUtil(worker.getLogger(), rocket).processConcept(concept, true);

            logger.info("init time: " + (System.currentTimeMillis() - startTime) + "s");

            rocket.classify();

            logger.info("Classified! " + (System.currentTimeMillis() - startTime) + "s");
        } catch (IOException e) {
            handleException(e);
        } catch (TerminologyException e) {
            handleException(e);
        }

        return Condition.CONTINUE;
    }

    private void handleException(Exception e) throws TaskFailedException {
        throw new TaskFailedException(e);
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
