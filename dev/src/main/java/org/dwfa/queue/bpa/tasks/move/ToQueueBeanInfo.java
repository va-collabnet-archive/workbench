/*
 * Created on Apr 22, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * @author kec
 *
 */
public class ToQueueBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ToQueueBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor queueType =
                new PropertyDescriptor("queueType", ToQueue.class);
            queueType.setBound(true);
            queueType.setPropertyEditorClass(QueueTypeEditor.class);
            queueType.setDisplayName("Queue type:");
            queueType.setShortDescription("The type of queue to place this process into.");



            PropertyDescriptor rv[] =
                {queueType};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ToQueue.class);
        bd.setDisplayName("<html><font color='green'><center>To Queue");
        return bd;
    }
}
