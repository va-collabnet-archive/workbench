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
package org.dwfa.ace.task.prop;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetPropertyToConceptFromPropertyBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor conceptSpecPropName = new PropertyDescriptor("conceptSpecPropName",
                getBeanDescriptor().getBeanClass());
            conceptSpecPropName.setBound(true);
            conceptSpecPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptSpecPropName.setDisplayName("<html><font color='green'>spec prop:");
            conceptSpecPropName.setShortDescription("The property that contains the spec (uuid, list<uuid>, term entry) for the concept.");

            PropertyDescriptor conceptPropName = new PropertyDescriptor("conceptPropName",
                getBeanDescriptor().getBeanClass());
            conceptPropName.setBound(true);
            conceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptPropName.setDisplayName("<html><font color='green'>concept prop:");
            conceptPropName.setShortDescription("The property to hold the concept.");

            PropertyDescriptor rv[] = { conceptSpecPropName, conceptPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetPropertyToConceptFromProperty.class);
        bd.setDisplayName("<html><font color='green'><center>set property<br>to concept<br>from spec");
        return bd;
    }
}
