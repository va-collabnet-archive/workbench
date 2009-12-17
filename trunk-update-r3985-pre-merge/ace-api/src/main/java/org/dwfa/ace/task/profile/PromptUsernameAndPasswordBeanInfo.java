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

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class PromptUsernameAndPasswordBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor promptMessage = new PropertyDescriptor("promptMessage",
                getBeanDescriptor().getBeanClass());
            promptMessage.setBound(true);
            promptMessage.setPropertyEditorClass(JTextFieldEditor.class);
            promptMessage.setDisplayName("<html><font color='green'>prompt message:");
            promptMessage.setShortDescription("The prompt message.");

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

            PropertyDescriptor rv[] = { promptMessage, usernamePropName, passwordPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(PromptUsernameAndPassword.class);
        bd.setDisplayName("<html><font color='green'><center>prompt<br>username/password");
        return bd;
    }
}
