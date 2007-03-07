/*
 * Created on Mar 8, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.prop;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.PropertyDescriptorWithTarget;
import org.dwfa.bpa.data.ProcessContainer;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.worker.EditorGlueForWorker;
/**
 * * @author kec<p>
 * Set the specified remote property to the value provided in the task.
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/property", type = BeanType.TASK_BEAN)})
public class SetRemotePropertyFromText extends AbstractTask {

    private String remotePropertyName = "";

    private String valueText = "";

    private int processDataId = -1;

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(remotePropertyName);
        out.writeObject(valueText);
        out.writeInt(processDataId);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            remotePropertyName = (String) in.readObject();
            valueText = (String) in.readObject();
            processDataId = in.readInt();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public SetRemotePropertyFromText() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            
            ProcessContainer pc = (ProcessContainer) process
            .getDataContainer(processDataId);
            I_EncodeBusinessProcess p = (I_EncodeBusinessProcess) pc.getData();
            
            PropertyDescriptor[] remoteDescriptors = p.getAllPropertiesBeanInfo().getPropertyDescriptors();
            PropertyDescriptorWithTarget remoteDescriptor = null;
            for (PropertyDescriptor d : remoteDescriptors) {
                if (PropertyDescriptorWithTarget.class.isAssignableFrom(d
                        .getClass())) {
                    PropertyDescriptorWithTarget dwt = (PropertyDescriptorWithTarget) d;
                    if (dwt.getLabel().equals(this.remotePropertyName)) {
                        remoteDescriptor = dwt;
                        break;
                    }
                }
            }
            PropertyEditor editor = remoteDescriptor
                    .createPropertyEditor(remoteDescriptor.getTarget());
            editor.addPropertyChangeListener(new EditorGlueForWorker(editor,
                    remoteDescriptor.getWriteMethod(), remoteDescriptor.getTarget(), worker));
            editor.setAsText(this.valueText);

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
     * @return Returns the remotePropertyName.
     */
    public String getRemotePropertyName() {
        return remotePropertyName;
    }

    /**
     * @param remotePropertyName
     *            The remotePropertyName to set.
     */
    public void setRemotePropertyName(String localPropName) {
        this.remotePropertyName = localPropName;
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

    /**
     * @return Returns the processDataId.
     */
    public int getProcessDataId() {
        return processDataId;
    }

    /**
     * @param processDataId The processDataId to set.
     */
    public void setProcessDataId(int processDataId) {
        this.processDataId = processDataId;
    }
    /**
     * @param processDataId The processDataId to set.
     */
    public void setProcessDataId(Integer processDataId) {
        this.processDataId = processDataId;
    }

}
