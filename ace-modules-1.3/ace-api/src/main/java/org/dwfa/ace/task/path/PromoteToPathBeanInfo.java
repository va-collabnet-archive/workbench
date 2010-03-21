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

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class PromoteToPathBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor pathConceptPropName = new PropertyDescriptor("pathConceptPropName",
                getBeanDescriptor().getBeanClass());
            pathConceptPropName.setBound(true);
            pathConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            pathConceptPropName.setDisplayName("<html><font color='green'>path concept property:");
            pathConceptPropName.setShortDescription("The path to be updated");

            PropertyDescriptor destRelTypeConceptPropName = new PropertyDescriptor("destRelTypeConceptPropName",
                getBeanDescriptor().getBeanClass());
            destRelTypeConceptPropName.setBound(true);
            destRelTypeConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            destRelTypeConceptPropName.setDisplayName("<html><font color='green'>dest rel type:");
            destRelTypeConceptPropName.setShortDescription("Origins (paths) of this destination relationship type will be copied to the selected path.");

            PropertyDescriptor rv[] = { pathConceptPropName, destRelTypeConceptPropName };
            return rv;

        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(PromoteToPath.class);
        bd.setDisplayName("<html><font color='green'><center>promote updates to path");
        return bd;
    }
}
