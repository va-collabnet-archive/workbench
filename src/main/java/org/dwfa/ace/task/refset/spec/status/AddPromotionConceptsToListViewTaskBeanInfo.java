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
package org.dwfa.ace.task.refset.spec.status;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddPromotionConceptsToListViewTaskBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public AddPromotionConceptsToListViewTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that will contain the profile this task uses and/or modifies.");

            PropertyDescriptor memberRefsetUuidPropName;
            memberRefsetUuidPropName = new PropertyDescriptor("memberRefsetUuidPropName",
                getBeanDescriptor().getBeanClass());
            memberRefsetUuidPropName.setBound(true);
            memberRefsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            memberRefsetUuidPropName.setDisplayName("<html><font color='green'>Member refset UUID prop:");
            memberRefsetUuidPropName.setShortDescription("The member refset UUID prop.");

            PropertyDescriptor statusUuidPropName;
            statusUuidPropName = new PropertyDescriptor("statusUuidPropName", getBeanDescriptor().getBeanClass());
            statusUuidPropName.setBound(true);
            statusUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            statusUuidPropName.setDisplayName("<html><font color='green'>Promotion status UUID prop:");
            statusUuidPropName.setShortDescription("The promotion status UUID prop.");

            PropertyDescriptor rv[] = { profilePropName, memberRefsetUuidPropName, statusUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddPromotionConceptsToListViewTask.class);
        bd.setDisplayName("<html><font color='green'><center>Add promotion concepts<br>to list view");
        return bd;
    }

}
