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
package org.dwfa.ace.task.profile.cap;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class NewCapDefaultProfileBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that will contain the profile this task creates.");

            PropertyDescriptor fullNamePropName = new PropertyDescriptor("fullNamePropName",
                getBeanDescriptor().getBeanClass());
            fullNamePropName.setBound(true);
            fullNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            fullNamePropName.setDisplayName("<html><font color='green'>full name prop:");
            fullNamePropName.setShortDescription("The property that contains the user's full name.");

            PropertyDescriptor usernamePropName = new PropertyDescriptor("usernamePropName",
                getBeanDescriptor().getBeanClass());
            usernamePropName.setBound(true);
            usernamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            usernamePropName.setDisplayName("<html><font color='green'>username prop:");
            usernamePropName.setShortDescription("The property that contains the username.");

            PropertyDescriptor passwordPropName = new PropertyDescriptor("passwordPropName",
                getBeanDescriptor().getBeanClass());
            passwordPropName.setBound(true);
            passwordPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            passwordPropName.setDisplayName("<html><font color='green'>password prop:");
            passwordPropName.setShortDescription("The property that contains the password.");

            PropertyDescriptor adminUsernamePropName = new PropertyDescriptor("adminUsernamePropName",
                getBeanDescriptor().getBeanClass());
            adminUsernamePropName.setBound(true);
            adminUsernamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            adminUsernamePropName.setDisplayName("<html><font color='green'>admin username prop:");
            adminUsernamePropName.setShortDescription("The property that contains the admin username.");

            PropertyDescriptor adminPasswordPropName = new PropertyDescriptor("adminPasswordPropName",
                getBeanDescriptor().getBeanClass());
            adminPasswordPropName.setBound(true);
            adminPasswordPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            adminPasswordPropName.setDisplayName("<html><font color='green'>admin password prop:");
            adminPasswordPropName.setShortDescription("The property that contains the admin password.");

            PropertyDescriptor rv[] = { profilePropName, fullNamePropName, usernamePropName, passwordPropName,
                                       adminUsernamePropName, adminPasswordPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(NewCapDefaultProfile.class);
        bd.setDisplayName("<html><font color='green'><center>new cap default profile");
        return bd;
    }
}
