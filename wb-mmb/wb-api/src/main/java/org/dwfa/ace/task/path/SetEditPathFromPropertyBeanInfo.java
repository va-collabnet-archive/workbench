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

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetEditPathFromPropertyBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor editPathPropName = new PropertyDescriptor("editPathPropName",
                getBeanDescriptor().getBeanClass());
            editPathPropName.setBound(true);
            editPathPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            editPathPropName.setDisplayName("<html><font color='green'>editing path:");
            editPathPropName.setShortDescription("The property that contains the edit path concept");

            PropertyDescriptor keepExistingEditPaths = new PropertyDescriptor("keepExistingEditPaths",
                getBeanDescriptor().getBeanClass());
            keepExistingEditPaths.setBound(true);
            keepExistingEditPaths.setPropertyEditorClass(CheckboxEditor.class);
            keepExistingEditPaths.setDisplayName("<html><font color='green'>Keep existing edit paths");
            keepExistingEditPaths.setShortDescription("Uncheck to edit just the specified path");

            PropertyDescriptor rv[] = { editPathPropName, keepExistingEditPaths };
            return rv;

        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetEditPathFromProperty.class);
        bd.setDisplayName("<html><font color='green'><center>Set edit path<br/>from property");
        return bd;
    }
}
