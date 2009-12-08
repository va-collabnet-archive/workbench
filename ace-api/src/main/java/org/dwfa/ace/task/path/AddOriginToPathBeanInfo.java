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

public class AddOriginToPathBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor pathConceptPropName = new PropertyDescriptor("pathConceptPropName",
                getBeanDescriptor().getBeanClass());
            pathConceptPropName.setBound(true);
            pathConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            pathConceptPropName.setDisplayName("<html><font color='green'>path concept property:");
            pathConceptPropName.setShortDescription("The path to be modified with an additional origin");

            PropertyDescriptor originPathConceptPropName = new PropertyDescriptor("originPathConceptPropName",
                getBeanDescriptor().getBeanClass());
            originPathConceptPropName.setBound(true);
            originPathConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            originPathConceptPropName.setDisplayName("<html><font color='green'>origin path concept property:");
            originPathConceptPropName.setShortDescription("The path to be added as an origin");

            PropertyDescriptor originPositionStr = new PropertyDescriptor("originPositionStr",
                getBeanDescriptor().getBeanClass());
            originPositionStr.setBound(true);
            originPositionStr.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            originPositionStr.setDisplayName("<html><font color='green'>origin position:");
            originPositionStr.setShortDescription("The version as a string. Expressed as \"latest\" or yyyy-MM-dd HH:mm:ss.");

            PropertyDescriptor rv[] = { pathConceptPropName, originPathConceptPropName, originPositionStr };
            return rv;

        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddOriginToPath.class);
        bd.setDisplayName("<html><font color='green'><center>add origin to path");
        return bd;
    }
}
