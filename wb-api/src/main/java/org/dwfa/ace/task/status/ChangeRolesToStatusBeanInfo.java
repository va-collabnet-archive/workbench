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
package org.dwfa.ace.task.status;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ChangeRolesToStatusBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ChangeRolesToStatusBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
         
            PropertyDescriptor newStatus = new PropertyDescriptor("newStatus", ChangeRolesToStatus.class);
            newStatus.setBound(true);
            newStatus.setPropertyEditorClass(ConceptLabelPropEditor.class);
            newStatus.setDisplayName("New status:");
            newStatus.setShortDescription("The new status value for the concept.");

            PropertyDescriptor activeConceptPropName = new PropertyDescriptor("activeConceptPropName",
                ChangeRolesToStatus.class);
            activeConceptPropName.setBound(true);
            activeConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            activeConceptPropName.setDisplayName("<html><font color='green'>Concept property:");
            activeConceptPropName.setShortDescription("Name of the property containing the concept to change the status of. ");
            
            PropertyDescriptor uuidListListPropName = new PropertyDescriptor("uuidListListPropName",
                    getBeanDescriptor().getBeanClass());
                uuidListListPropName.setBound(true);
                uuidListListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
                uuidListListPropName.setDisplayName("<html><font color='green'>Uuid List:");
                uuidListListPropName.setShortDescription("Uuid list.");

            PropertyDescriptor rv[] = {newStatus, activeConceptPropName, uuidListListPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChangeRolesToStatus.class);
        bd.setDisplayName("<html><font color='green'><center>Change Roles to Status");
        return bd;
    }
}
