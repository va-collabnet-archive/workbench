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
package org.dwfa.ace.task.profile;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CreateUserPathAndQueuesBasedOnCreatorProfileBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor creatorProfilePropName = new PropertyDescriptor("creatorProfilePropName",
                getBeanDescriptor().getBeanClass());
            creatorProfilePropName.setBound(true);
            creatorProfilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            creatorProfilePropName.setDisplayName("<html><font color='green'>creator profile prop:");
            creatorProfilePropName.setShortDescription("The property that contains the creator's profile.");

            PropertyDescriptor newProfilePropName = new PropertyDescriptor("newProfilePropName",
                getBeanDescriptor().getBeanClass());
            newProfilePropName.setBound(true);
            newProfilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            newProfilePropName.setDisplayName("<html><font color='green'>new profile prop:");
            newProfilePropName.setShortDescription("The property that contains the new profile.");

            PropertyDescriptor errorsAndWarningsPropName = new PropertyDescriptor("errorsAndWarningsPropName",
                getBeanDescriptor().getBeanClass());
            errorsAndWarningsPropName.setBound(true);
            errorsAndWarningsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            errorsAndWarningsPropName.setDisplayName("<html><font color='green'>error & warn prop:");
            errorsAndWarningsPropName.setShortDescription("The property that contains errors and warnings found prior to commit.");

            PropertyDescriptor rv[] = { creatorProfilePropName, newProfilePropName, errorsAndWarningsPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateUserPathAndQueuesBasedOnCreatorProfile.class);
        bd.setDisplayName("<html><font color='green'><center>Create User Path,<br>User Concept, <br>and User Queues<br>Based on Creator");
        return bd;
    }
}
