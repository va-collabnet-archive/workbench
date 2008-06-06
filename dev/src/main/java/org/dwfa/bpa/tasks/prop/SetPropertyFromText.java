/*
 * Created on Mar 7, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.prop;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
/**
 * @author kec<p>
 * Sets the specified property to the text provided in the task.
 * 
 * */
@BeanList(specs = 
{ @Spec(directory = "tasks/property tasks", type = BeanType.TASK_BEAN)})
public class SetPropertyFromText extends AbstractTask {

    private String localPropName = "";

    private String valueText = "";

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(localPropName);
        out.writeObject(valueText);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            localPropName = (String) in.readObject();
            valueText = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public SetPropertyFromText() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
        	process.setProperty(localPropName, valueText);

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
        return Condition.CONTINUE;
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do...
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /**
     * @return Returns the localPropName.
     */
    public String getLocalPropName() {
        return localPropName;
    }

    /**
     * @param localPropName
     *            The localPropName to set.
     */
    public void setLocalPropName(String localPropName) {
        this.localPropName = localPropName;
    }

    /**
     * @return Returns the valueText.
     */
    public String getValueText() {
        return valueText;
    }

    /**
     * @param valueText
     *            The valueText to set.
     */
    public void setValueText(String remotePropName) {
        this.valueText = remotePropName;
    }

}
