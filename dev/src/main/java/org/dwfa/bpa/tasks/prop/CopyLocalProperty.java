package org.dwfa.bpa.tasks.prop;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Collection;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.PropertyDescriptorWithTarget;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;

/**
 * Copy an object from one process property to another. 
 * @author kec
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/processes/copy tasks", type = BeanType.TASK_BEAN)})
public class CopyLocalProperty extends AbstractTask {

    private String originalPropName = "";
    private String remotePropName = "";
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(originalPropName);
        out.writeObject(remotePropName);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            originalPropName = (String) in.readObject();
            remotePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }
    public CopyLocalProperty() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            PropertyDescriptor[] propertyDescriptors = process.getAllPropertiesBeanInfo().getPropertyDescriptors();
            PropertyDescriptorWithTarget originalDescriptor = null;
            PropertyDescriptorWithTarget copyDescriptor = null;
            for (PropertyDescriptor d: propertyDescriptors) {
                if (PropertyDescriptorWithTarget.class.isAssignableFrom(d.getClass())) {
                    PropertyDescriptorWithTarget dwt = (PropertyDescriptorWithTarget) d;
                    if (dwt.getLabel().equals(this.remotePropName)) {
                    	copyDescriptor = dwt;
                        break;
                    }
                } 
            }
            for (PropertyDescriptor d: propertyDescriptors) {
                if (PropertyDescriptorWithTarget.class.isAssignableFrom(d.getClass())) {
                    PropertyDescriptorWithTarget dwt = (PropertyDescriptorWithTarget) d;
                    if (dwt.getLabel().equals(this.originalPropName)) {
                    	originalDescriptor = dwt;
                        break;
                    }
                } 
            }
            Method writeMethod = copyDescriptor.getWriteMethod();
            Method readMethod = originalDescriptor.getReadMethod();
            Object remoteVal = readMethod.invoke(originalDescriptor.getTarget());
            writeMethod.invoke(copyDescriptor.getTarget(), new Object[] { remoteVal });
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
        return Condition.CONTINUE;
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        //Nothing to do...
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] { };
    }


    /**
     * @return Returns the localPropName.
     */
    public String getLocalPropName() {
        return originalPropName;
    }

    /**
     * @param localPropName The localPropName to set.
     */
    public void setLocalPropName(String localPropName) {
        this.originalPropName = localPropName;
    }

    /**
     * @return Returns the remotePropName.
     */
    public String getRemotePropName() {
        return remotePropName;
    }

    /**
     * @param remotePropName The remotePropName to set.
     */
    public void setRemotePropName(String remotePropName) {
        this.remotePropName = remotePropName;
    }

}
