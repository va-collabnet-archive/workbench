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
package org.dwfa.bpa.tasks.prop;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AppendPropertyToPropertyBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor seperatorTextPropertyName = new PropertyDescriptor("seperatorText",
                getBeanDescriptor().getBeanClass());
            seperatorTextPropertyName.setBound(true);
            seperatorTextPropertyName.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            seperatorTextPropertyName.setDisplayName("<html><font color='blue'>Seperator text:");
            seperatorTextPropertyName.setShortDescription("Text to append between Properties. ");

            PropertyDescriptor destinationPropName = new PropertyDescriptor("destinationPropName",
                getBeanDescriptor().getBeanClass());
            destinationPropName.setBound(true);
            destinationPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            destinationPropName.setDisplayName("<html><font color='blue'>Destination property:");
            destinationPropName.setShortDescription("Name of the property to append to. ");

            PropertyDescriptor sourcePropName = new PropertyDescriptor("sourcePropName",
                    getBeanDescriptor().getBeanClass());
            sourcePropName.setBound(true);
            sourcePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            sourcePropName.setDisplayName("<html><font color='blue'>Source property:");
            sourcePropName.setShortDescription("Name of the property to append from. ");

                
                PropertyDescriptor rv[] = { destinationPropName, seperatorTextPropertyName, sourcePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AppendPropertyToProperty.class);
        bd.setDisplayName("<html><center>Append Property<br>To Property");
        return bd;
    }
}

