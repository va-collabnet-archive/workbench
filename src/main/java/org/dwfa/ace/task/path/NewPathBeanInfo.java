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

public class NewPathBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile property:");
            profilePropName.setShortDescription("The property containing the profile which will be set to view and edit the newly created path. Leave blank to ignore.");

            PropertyDescriptor parentPathTermEntry = new PropertyDescriptor("parentPathTermEntry",
                getBeanDescriptor().getBeanClass());
            parentPathTermEntry.setBound(true);
            parentPathTermEntry.setPropertyEditorClass(ConceptLabelPropEditor.class);
            parentPathTermEntry.setDisplayName("<html><font color='green'>path parent:");
            parentPathTermEntry.setShortDescription("The parent for the new editing path.");

            PropertyDescriptor pathDescription = new PropertyDescriptor("PathDescription",
                getBeanDescriptor().getBeanClass());
            pathDescription.setBound(true);
            pathDescription.setPropertyEditorClass(PropertyNameLabelEditor.class);
            pathDescription.setDisplayName("<html><font color='green'>Path Description");
            pathDescription.setShortDescription("Description For NewPath");

            PropertyDescriptor newConceptPropName = new PropertyDescriptor("newConceptPropName",
                getBeanDescriptor().getBeanClass());
            newConceptPropName.setBound(true);
            newConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            newConceptPropName.setDisplayName("<html><font color='green'>new path concept property:");
            newConceptPropName.setShortDescription("The property which will be set to contain the new path concept");

            PropertyDescriptor rv[] = { parentPathTermEntry, profilePropName, pathDescription, newConceptPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(NewPath.class);
        bd.setDisplayName("<html><font color='green'><center>new path");
        return bd;
    }
}
