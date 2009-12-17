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
package org.dwfa.ace.task.copy;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CopyTermEntryToPropertyBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor termEntry = new PropertyDescriptor("termEntry", getBeanDescriptor().getBeanClass());
            termEntry.setBound(true);
            termEntry.setPropertyEditorClass(ConceptLabelEditor.class);
            termEntry.setDisplayName("concept:");
            termEntry.setShortDescription("The concept to copy to the property.");

            PropertyDescriptor propertyName = new PropertyDescriptor("propertyName", getBeanDescriptor().getBeanClass());
            propertyName.setBound(true);
            propertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            propertyName.setDisplayName("<html><font color='green'>concept property:");
            propertyName.setShortDescription("Name of the property to copy the concept to to. ");

            PropertyDescriptor rv[] = { termEntry, propertyName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CopyTermEntryToProperty.class);
        bd.setDisplayName("<html><font color='green'><center>copy term entry<br>to property");
        return bd;
    }
}
