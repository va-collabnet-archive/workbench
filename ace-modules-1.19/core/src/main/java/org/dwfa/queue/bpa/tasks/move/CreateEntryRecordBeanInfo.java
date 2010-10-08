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
 * Created on Mar 8, 2006
 */
package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CreateEntryRecordBeanInfo extends SimpleBeanInfo {

    public CreateEntryRecordBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor queueType = new PropertyDescriptor("queueType", CreateEntryRecord.class);
            queueType.setBound(true);
            queueType.setPropertyEditorClass(ConceptLabelEditor.class);
            queueType.setDisplayName("Queue type:");
            queueType.setShortDescription("The type of queue to generate the entry record for.");

            PropertyDescriptor localPropertyName = new PropertyDescriptor("localPropName", CreateEntryRecord.class);
            localPropertyName.setBound(true);
            localPropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            localPropertyName.setDisplayName("<html><font color='blue'>Entry record:");
            localPropertyName.setShortDescription("Name of the local property that holds the entry record. ");

            PropertyDescriptor rv[] = { queueType, localPropertyName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateEntryRecord.class);
        bd.setDisplayName("<html><font color='green'><center>Create Entry Record");
        return bd;
    }
}
