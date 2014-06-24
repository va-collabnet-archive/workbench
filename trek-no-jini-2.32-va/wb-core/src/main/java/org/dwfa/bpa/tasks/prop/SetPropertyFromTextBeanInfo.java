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
/*
 * Created on Mar 7, 2006
 */
package org.dwfa.bpa.tasks.prop;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetPropertyFromTextBeanInfo extends SimpleBeanInfo {

    public SetPropertyFromTextBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor valueTextPropertyName = new PropertyDescriptor("valueText", SetPropertyFromText.class);
            valueTextPropertyName.setBound(true);
            valueTextPropertyName.setPropertyEditorClass(JTextFieldEditor.class);
            valueTextPropertyName.setDisplayName("<html><font color='green'>New value:");
            valueTextPropertyName.setShortDescription("Text representation of the new property value. ");

            PropertyDescriptor localPropertyName = new PropertyDescriptor("localPropName", SetPropertyFromText.class);
            localPropertyName.setBound(true);
            localPropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            localPropertyName.setDisplayName("<html><font color='blue'>Local property:");
            localPropertyName.setShortDescription("Name of the local property to set. ");

            PropertyDescriptor rv[] = { localPropertyName, valueTextPropertyName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetPropertyFromText.class);
        bd.setDisplayName("<html>Set Property");
        return bd;
    }

}
