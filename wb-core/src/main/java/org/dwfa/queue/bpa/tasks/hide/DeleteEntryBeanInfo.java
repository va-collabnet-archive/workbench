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
package org.dwfa.queue.bpa.tasks.hide;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class DeleteEntryBeanInfo extends SimpleBeanInfo {
    protected Class<DeleteEntry> getBeanClass() {
        return DeleteEntry.class;
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(getBeanClass());
        bd.setDisplayName("<html><font color='red'><center>Delete Entry");
        return bd;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor queueEntryProp = new PropertyDescriptor("queueEntryPropName", getBeanClass());
            queueEntryProp.setBound(true);
            queueEntryProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
            queueEntryProp.setDisplayName("Entry Data:");
            queueEntryProp.setShortDescription("A QueueEntryData object that fully specifies a queue entry.");

            PropertyDescriptor rv[] = { queueEntryProp };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }
}