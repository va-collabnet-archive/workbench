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

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;

public class ToQueueTwoCriterionBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ToQueueTwoCriterionBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor queueType =
                new PropertyDescriptor("queueType", ToQueueTwoCriterion.class);
            queueType.setBound(true);
            queueType.setPropertyEditorClass(ConceptLabelEditor.class);
            queueType.setDisplayName("Queue type 1:");
            queueType.setShortDescription("The first criterion for the type of queue to place this process into.");

            PropertyDescriptor queueType2 =
                new PropertyDescriptor("queueType2", ToQueueTwoCriterion.class);
            queueType2.setBound(true);
            queueType2.setPropertyEditorClass(ConceptLabelEditor.class);
            queueType2.setDisplayName("Queue type 2:");
            queueType2.setShortDescription("The second criterion for the type of queue to place this process into.");

            PropertyDescriptor rv[] = { queueType, queueType2 };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ToQueueTwoCriterion.class);
        bd.setDisplayName("<html><font color='green'><center>To Queue<br>Two Criterion");
        return bd;
    }
}
