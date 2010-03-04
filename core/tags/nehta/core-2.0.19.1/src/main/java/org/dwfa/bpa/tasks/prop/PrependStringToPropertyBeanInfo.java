/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.bpa.tasks.prop;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class PrependStringToPropertyBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor valueTextPropertyName =
                    new PropertyDescriptor("valueText", getBeanDescriptor().getBeanClass());
            valueTextPropertyName.setBound(true);
            valueTextPropertyName.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            valueTextPropertyName.setDisplayName("<html><font color='blue'>Prepend value:");
            valueTextPropertyName.setShortDescription("Text to prepend. ");

            PropertyDescriptor stringPropName =
                    new PropertyDescriptor("stringPropName",
                    getBeanDescriptor().getBeanClass());
            stringPropName.setBound(true);
            stringPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            stringPropName.setDisplayName("<html><font color='blue'>property:");
            stringPropName.setShortDescription("Name of the property to prepend to. ");

            PropertyDescriptor rv[] = {stringPropName, valueTextPropertyName};
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(PrependStringToProperty.class);
        bd.setDisplayName("<html><center>Prepend String<br>To Property");
        return bd;
    }
}
