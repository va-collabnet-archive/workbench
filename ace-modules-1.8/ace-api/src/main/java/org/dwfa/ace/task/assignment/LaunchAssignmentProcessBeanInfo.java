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

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info to LaunchAssignmentProcess class.
 * 
 * @author Susan Castillo
 * 
 */
public class LaunchAssignmentProcessBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor selectedAddressesPropName = new PropertyDescriptor("selectedAddressesPropName",
                getBeanDescriptor().getBeanClass());
            selectedAddressesPropName.setBound(true);
            selectedAddressesPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            selectedAddressesPropName.setDisplayName("<html><font color='green'>Assignment Addresses:");
            selectedAddressesPropName.setShortDescription("Addresses");

            PropertyDescriptor conceptUuidPropName = new PropertyDescriptor("conceptUuidPropName",
                getBeanDescriptor().getBeanClass());
            conceptUuidPropName.setBound(true);
            conceptUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptUuidPropName.setDisplayName("<html><font color='green'>Uuid List:");
            conceptUuidPropName.setShortDescription("Uuid List");

            PropertyDescriptor processFileNamePropName = new PropertyDescriptor("processFileNamePropName",
                getBeanDescriptor().getBeanClass());
            processFileNamePropName.setBound(true);
            processFileNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            processFileNamePropName.setDisplayName("<html><font color='green'>Process File Name:");
            processFileNamePropName.setShortDescription("The file name of the process to loadset, launch");

            PropertyDescriptor rv[] = { selectedAddressesPropName, conceptUuidPropName, processFileNamePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(LaunchAssignmentProcess.class);
        bd.setDisplayName("<html><font color='green'><center>Launch Assignment<br>Process");
        return bd;
    }

}
