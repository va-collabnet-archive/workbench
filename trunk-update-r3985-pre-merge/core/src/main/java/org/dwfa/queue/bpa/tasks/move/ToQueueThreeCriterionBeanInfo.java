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
 * Created on Apr 5, 2006
 */
package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;

public class ToQueueThreeCriterionBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ToQueueThreeCriterionBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor queueType = new PropertyDescriptor("queueType", ToQueueThreeCriterion.class);
            queueType.setBound(true);
            queueType.setPropertyEditorClass(ConceptLabelEditor.class);
            queueType.setDisplayName("Queue type 1:");
            queueType.setShortDescription("The first criterion for the type of queue to place this process into.");

            PropertyDescriptor queueType2 = new PropertyDescriptor("queueType2", ToQueueThreeCriterion.class);
            queueType2.setBound(true);
            queueType2.setPropertyEditorClass(ConceptLabelEditor.class);
            queueType2.setDisplayName("Queue type 2:");
            queueType2.setShortDescription("The second criterion for the type of queue to place this process into.");

            PropertyDescriptor queueType3 = new PropertyDescriptor("queueType3", ToQueueThreeCriterion.class);
            queueType3.setBound(true);
            queueType3.setPropertyEditorClass(ConceptLabelEditor.class);
            queueType3.setDisplayName("Queue type 3:");
            queueType3.setShortDescription("The third criterion for the type of queue to place this process into.");

            PropertyDescriptor rv[] = { queueType, queueType2, queueType3 };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ToQueueThreeCriterion.class);
        bd.setDisplayName("<html><font color='green'><center>To Queue<br>Three Criterion");
        return bd;
    }
}
