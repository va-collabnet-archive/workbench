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

public class SetEditPathForProfileBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile property:");
            profilePropName.setShortDescription("The property containing the profile to change.");

            PropertyDescriptor editPathEntry = new PropertyDescriptor("editPathEntry",
                getBeanDescriptor().getBeanClass());
            editPathEntry.setBound(true);
            editPathEntry.setPropertyEditorClass(ConceptLabelPropEditor.class);
            editPathEntry.setDisplayName("<html><font color='green'>editing path:");
            editPathEntry.setShortDescription("The property that contains the editing path.");

            PropertyDescriptor rv[] = { profilePropName, editPathEntry };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetEditPathForProfile.class);
        bd.setDisplayName("<html><font color='green'><center>add edit path<br>to profile");
        return bd;
    }
}
