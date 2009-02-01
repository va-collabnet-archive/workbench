package org.dwfa.ace.task.prop.set;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;

import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;


/**
 * This task takes a set as input and checks if it contains one item.
 * @author Christine Hill
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/prop/set", type = BeanType.TASK_BEAN) })
public class OneItemInSet extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String inputPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(inputPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            inputPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            HashSet set = (HashSet) process.readProperty(inputPropName);
            if (set != null && set.size() == 1) {
                return Condition.TRUE;
            }
            return Condition.FALSE;

        } catch (IntrospectionException e) {
            return Condition.FALSE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return CONDITIONAL_TEST_CONDITIONS;
    }

    public String getInputPropName() {
        return inputPropName;
    }

    public void setInputPropName(String inputPropName) {
        this.inputPropName = inputPropName;
    }
}
