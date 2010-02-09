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
package org.dwfa.ace.task.assignment;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info to LaunchBatchGenAssignmentProcess class.
 * @author Susan Castillo	
 *
 */
public class LaunchBatchGenAssignmentProcessBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor batchGenAssigneePropName =
                    new PropertyDescriptor("batchGenAssigneePropName",
                        getBeanDescriptor().getBeanClass());
            batchGenAssigneePropName.setBound(true);
            batchGenAssigneePropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            batchGenAssigneePropName
                .setDisplayName("<html><font color='green'>WF Mngr Inbox:");
            batchGenAssigneePropName.setShortDescription("Workflow Mngr");

            PropertyDescriptor uuidListListPropName =
                    new PropertyDescriptor("uuidListListPropName",
                        getBeanDescriptor().getBeanClass());
            uuidListListPropName.setBound(true);
            uuidListListPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidListListPropName
                .setDisplayName("<html><font color='green'>uuid List List:");
            uuidListListPropName.setShortDescription("uuid List");

            PropertyDescriptor processFileStr =
                    new PropertyDescriptor("processFileStr",
                        getBeanDescriptor().getBeanClass());
            processFileStr.setBound(true);
            processFileStr.setPropertyEditorClass(JTextFieldEditor.class);
            processFileStr
                .setDisplayName("<html><font color='green'>File name:");
            processFileStr
                .setShortDescription("Name/location of the file to attach.");

            PropertyDescriptor processToAssignPropName =
                    new PropertyDescriptor("processToAssignPropName",
                        getBeanDescriptor().getBeanClass());
            processToAssignPropName.setBound(true);
            processToAssignPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            processToAssignPropName
                .setDisplayName("<html><font color='green'>Assignment Process:");
            processToAssignPropName.setShortDescription("Assign Name");

            PropertyDescriptor rv[] =
                    { batchGenAssigneePropName, uuidListListPropName,
                     processFileStr, processToAssignPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd =
                new BeanDescriptor(LaunchBatchGenAssignmentProcess.class);
        bd
            .setDisplayName("<html><font color='green'><center>Launch Generate <br>Assignment Process");
        return bd;
    }

}
