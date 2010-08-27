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
package org.dwfa.ace.task.view;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetViewPositionToChildrenFromPropertyBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor profilePropName = 
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile property:");
            profilePropName.setShortDescription("The property containing the profile to change.");

            PropertyDescriptor positionStr = 
                new PropertyDescriptor("positionStr", getBeanDescriptor().getBeanClass());
            positionStr.setBound(true);
            positionStr.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            positionStr.setDisplayName("<html><font color='green'>position:");
            positionStr.setShortDescription("The version as a string. Expressed as \"latest\" or yyyy-MM-dd HH:mm:ss.");

            PropertyDescriptor viewPathConceptPropName = 
                new PropertyDescriptor("viewPathConceptPropName", getBeanDescriptor().getBeanClass());
            viewPathConceptPropName.setBound(true);
            viewPathConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            viewPathConceptPropName.setDisplayName("<html><font color='green'>view path:");
            viewPathConceptPropName.setShortDescription("The property that contains the parent concept that identifies the view paths as children.");

            PropertyDescriptor keepExistingViewPaths = 
                new PropertyDescriptor("keepExistingViewPaths", getBeanDescriptor().getBeanClass());
            keepExistingViewPaths.setBound(true);
            keepExistingViewPaths.setPropertyEditorClass(CheckboxEditor.class);
            keepExistingViewPaths.setDisplayName("<html><font color='green'>Keep existing view paths");
            keepExistingViewPaths.setShortDescription("Uncheck to view just the specified paths");

            PropertyDescriptor rv[] = { profilePropName, positionStr, viewPathConceptPropName, keepExistingViewPaths };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetViewPositionToChildrenFromProperty.class);
        bd.setDisplayName("<html><font color='green'><center>add view position<br>to children of<br>concept property");
        return bd;
    }
}
