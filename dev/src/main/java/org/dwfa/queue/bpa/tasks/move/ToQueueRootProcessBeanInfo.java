/*
 * Created on Jun 13, 2005
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
public class ToQueueRootProcessBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor queueType =
                new PropertyDescriptor("queueType", ToQueueRootProcess.class);
            queueType.setBound(true);
            queueType.setPropertyEditorClass(QueueTypeEditor.class);
            queueType.setDisplayName("Queue type");
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
        BeanDescriptor bd = new BeanDescriptor(ToQueueRootProcess.class);
        bd.setDisplayName("<html><font color='green'><center>ROOT Process<br>To Queue");
        return bd;
    }

}
