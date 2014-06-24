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
package org.dwfa.ace.task.wfpanel;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SelectInactiveParentBeanInfo extends SimpleBeanInfo {

    /**
     * Constructor - calls the parent constructor
     */
    public SelectInactiveParentBeanInfo() {
        super();
    }

    /**
     * Returns a list of property descriptors for this task.
     * 
     * @return Returns a PropertyDescriptor array containing the properties of
     *         this task
     * @exception Error Thrown when an exception happens during Introspection
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
        	 PropertyDescriptor relParentPropName = new PropertyDescriptor("relParentPropName", SelectInactiveParent.class);
             relParentPropName.setBound(true);
             relParentPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             relParentPropName.setDisplayName("<html><font color='green'>Rel parent:");
             relParentPropName.setShortDescription("The property containing the new parent value for the relationship.");

            // Property Description: instruction
            PropertyDescriptor instruction = new PropertyDescriptor("instruction", SelectInactiveParent.class);
            instruction.setBound(true);
            instruction.setPropertyEditorClass(JTextFieldEditor.class);
            instruction.setDisplayName("<html><font color='green'>Instruction:");
            instruction.setShortDescription("Instructions to present to the user in the workflow panel. ");

            PropertyDescriptor rv[] = {instruction, relParentPropName};
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * Return the descriptor for this JavaBean which contains a reference to the
     * JavaBean
     * that implements this task as well as the display name of the task along
     * with formating information.
     * 
     * @see java.beans.BeanInfo#getBeanDescriptor()
     * @return Returns the BeanDescriptor for this task
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SelectInactiveParent.class);
        bd.setDisplayName("<html><font color='green'><center>Select Inactive<br>Parent");
        return bd;
    }
}
