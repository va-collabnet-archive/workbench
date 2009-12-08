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

/**
 * Bean info for TakeFirstItemInAttachmentList class.
 * 
 * @author Christine Hill
 * 
 */
public class TakeFirstItemInAttachmentListBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public TakeFirstItemInAttachmentListBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor listName = new PropertyDescriptor("listName", TakeFirstItemInAttachmentList.class);
            listName.setBound(true);
            listName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            listName.setDisplayName("<html><font color='green'>Name of temporary list:");
            listName.setShortDescription("Name of the temporary list.");

            PropertyDescriptor conceptKey = new PropertyDescriptor("conceptKey", TakeFirstItemInAttachmentList.class);
            conceptKey.setBound(true);
            conceptKey.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptKey.setDisplayName("<html><font color='green'>Concept key:");
            conceptKey.setShortDescription("Concept key.");

            PropertyDescriptor rv[] = { listName, conceptKey };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TakeFirstItemInAttachmentList.class);
        bd.setDisplayName("<html><font color='green'><center>Take First Item<br>In Attachment List<br> Return Concept");
        return bd;
    }

}
