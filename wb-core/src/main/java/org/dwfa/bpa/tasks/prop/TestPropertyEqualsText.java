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
/*
 * Created on Mar 8, 2006
 */
package org.dwfa.bpa.tasks.prop;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
 * * @author kec
 * <p>
 * Test to determine if the property equals the value provided in the task.
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/property tasks", type = BeanType.TASK_BEAN) })
public class TestPropertyEqualsText extends AbstractTask {

    private String localPropName = "";

    private String valueText = "";

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(localPropName);
        out.writeObject(valueText);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            localPropName = (String) in.readObject();
            valueText = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public TestPropertyEqualsText() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            PropertyDescriptorWithTarget descriptor = null;
            for (PropertyDescriptor d : process.getAllPropertiesBeanInfo().getPropertyDescriptors()) {
                if (PropertyDescriptorWithTarget.class.isAssignableFrom(d.getClass())) {
                    PropertyDescriptorWithTarget dwt = (PropertyDescriptorWithTarget) d;
                    if (dwt.getLabel().equals(this.localPropName)) {
                        descriptor = dwt;
                        break;
                    }
                }
            }
            Object value = descriptor.getReadMethod().invoke(descriptor.getTarget(), new Object[] {});
            boolean test = value.toString().equals(this.valueText);
            if (test) {
                return Condition.TRUE;
            }
            return Condition.FALSE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    public Collection<Condition> getConditions() {
        return CONDITIONAL_TEST_CONDITIONS;
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
