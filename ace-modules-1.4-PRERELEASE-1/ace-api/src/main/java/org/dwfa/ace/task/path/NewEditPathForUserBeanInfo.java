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
package org.dwfa.ace.task.path;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class NewEditPathForUserBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile property:");
            profilePropName.setShortDescription("The property containing the profile to set the editing path to the created path.");

            PropertyDescriptor parentPathTermEntry = new PropertyDescriptor("parentPathTermEntry",
                getBeanDescriptor().getBeanClass());
            parentPathTermEntry.setBound(true);
            parentPathTermEntry.setPropertyEditorClass(ConceptLabelPropEditor.class);
            parentPathTermEntry.setDisplayName("<html><font color='green'>path parent:");
            parentPathTermEntry.setShortDescription("The parent for the new editing path.");

            PropertyDescriptor originTime = new PropertyDescriptor("originTime", getBeanDescriptor().getBeanClass());
            originTime.setBound(true);
            originTime.setPropertyEditorClass(PropertyNameLabelEditor.class);
            originTime.setDisplayName("<html><font color='green'>origin time:");
            originTime.setShortDescription("The origin time in yyyy.MM.dd HH:mm:ss format or 'latest' for the latest change on the path.");

            PropertyDescriptor userPropName = new PropertyDescriptor("userPropName", getBeanDescriptor().getBeanClass());
            userPropName.setBound(true);
            userPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            userPropName.setDisplayName("<html><font color='green'>username key:");
            userPropName.setShortDescription("The key for the username.");

            PropertyDescriptor rv[] = { profilePropName, parentPathTermEntry, userPropName, originTime };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(NewEditPathForUser.class);
        bd.setDisplayName("<html><font color='green'><center>new edit path<br>for user");
        return bd;
    }
}
