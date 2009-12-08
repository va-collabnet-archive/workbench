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

/**
 * The SetPropertiesToUserInfoBeanInfo class describes the visible elements of
 * the
 * Workflow task SetPropertiesToUserInfo so that it can be displayed in the
 * Process Builder.
 * The SetPropertiesToUserInfo class takes the name of the working profile for
 * the current
 * process and sets the associated username and FullName attributes as
 * properties of the
 * current process.
 * 
 * @author Perry Reid
 * @version 1.0, October 2009
 */
public class SetPropertiesToUserInfoBeanInfo extends SimpleBeanInfo {

    /**
     * Returns a list of property descriptors for this task.
     * 
     * @return Returns a PropertyDescriptor array containing the properties of
     *         this task
     * @exception Error Thrown when an exception happens during Introspection
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that will contain the profile.");

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
            usernamePropName.setShortDescription("The property that contains the user's username.");

            PropertyDescriptor rv[] = { profilePropName, fullNamePropName, usernamePropName };
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
        BeanDescriptor bd = new BeanDescriptor(SetPropertiesToUserInfo.class);
        bd.setDisplayName("<html><font color='green'><center>set user info");
        return bd;
    }
}
