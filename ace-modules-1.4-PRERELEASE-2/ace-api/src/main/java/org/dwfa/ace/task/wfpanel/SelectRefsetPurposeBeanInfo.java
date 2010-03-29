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

/**
 * The SelectRefsetPurposeBeanInfo class describes the visible elements of the
 * Workflow task SelectRefsetPurpose so that it can be displayed in the Process
 * Builder.
 * The SelectRefsetPurpose task enables a user to select the "purpose" of a
 * refset
 * using a combo box. The user can only select one purpose per refset.
 * 
 * @author Perry Reid
 * @version 1.0, October 2009
 */
public class SelectRefsetPurposeBeanInfo extends SimpleBeanInfo {

    /**
     * Constructor - calls the parent constructor
     */
    public SelectRefsetPurposeBeanInfo() {
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

            // Property Description: profilePropName
            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that will contain the profile this task uses and/or modifies.");

            // Property Description: instruction
            PropertyDescriptor instruction = new PropertyDescriptor("instruction", getBeanDescriptor().getBeanClass());
            instruction.setBound(true);
            instruction.setPropertyEditorClass(JTextFieldEditor.class);
            instruction.setDisplayName("<html><font color='green'>Instruction:");
            instruction.setShortDescription("Instructions to present to the user in the workflow panel. ");

            // Property Description: refsetUuidPropName
            PropertyDescriptor refsetUuidPropName = new PropertyDescriptor("refsetUuidPropName",
                getBeanDescriptor().getBeanClass());
            refsetUuidPropName.setBound(true);
            refsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetUuidPropName.setDisplayName("<html><font color='green'>Refset UUID prop:");
            refsetUuidPropName.setShortDescription("The refset UUID prop.");

            PropertyDescriptor rv[] = { profilePropName, instruction, refsetUuidPropName };
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
        BeanDescriptor bd = new BeanDescriptor(SelectRefsetPurpose.class);
        bd.setDisplayName("<html><font color='green'><center>Select Refset<br>Purpose");
        return bd;
    }
}
