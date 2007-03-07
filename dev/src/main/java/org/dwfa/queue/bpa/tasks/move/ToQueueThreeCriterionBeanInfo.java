/*
 * Created on Apr 5, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class ToQueueThreeCriterionBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ToQueueThreeCriterionBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor queueType =
                new PropertyDescriptor("queueType", ToQueueThreeCriterion.class);
            queueType.setBound(true);
            queueType.setPropertyEditorClass(QueueTypeEditor.class);
            queueType.setDisplayName("Queue type 1:");
            queueType.setShortDescription("The first criterion for the type of queue to place this process into.");

            PropertyDescriptor queueType2 =
                new PropertyDescriptor("queueType2", ToQueueThreeCriterion.class);
            queueType2.setBound(true);
            queueType2.setPropertyEditorClass(QueueTypeEditor.class);
            queueType2.setDisplayName("Queue type 2:");
            queueType2.setShortDescription("The second criterion for the type of queue to place this process into.");

            PropertyDescriptor queueType3 =
                new PropertyDescriptor("queueType3", ToQueueThreeCriterion.class);
            queueType3.setBound(true);
            queueType3.setPropertyEditorClass(QueueTypeEditor.class);
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
