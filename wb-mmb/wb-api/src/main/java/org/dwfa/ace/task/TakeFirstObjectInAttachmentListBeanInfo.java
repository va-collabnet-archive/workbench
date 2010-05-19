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
package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class TakeFirstObjectInAttachmentListBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor listPropName = new PropertyDescriptor("listPropName", getBeanDescriptor().getBeanClass());
            listPropName.setBound(true);
            listPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            listPropName.setDisplayName("<html><font color='green'>prop name of list:");
            listPropName.setShortDescription("Property name of the list.");

            PropertyDescriptor objectPropName = new PropertyDescriptor("objectPropName",
                getBeanDescriptor().getBeanClass());
            objectPropName.setBound(true);
            objectPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            objectPropName.setDisplayName("<html><font color='green'>object key:");
            objectPropName.setShortDescription("property name to place object taken from list.");

            PropertyDescriptor rv[] = { listPropName, objectPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TakeFirstObjectInAttachmentList.class);
        bd.setDisplayName("<html><font color='green'><center>Take First Object<br>In Attachment List");
        return bd;
    }

}
