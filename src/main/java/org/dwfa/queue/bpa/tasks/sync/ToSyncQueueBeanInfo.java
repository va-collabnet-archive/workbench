/*
 * Created on Mar 8, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.sync;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ToSyncQueueBeanInfo extends SimpleBeanInfo {

    public ToSyncQueueBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            
            PropertyDescriptor localPropertyName =
                new PropertyDescriptor("localPropName", ToSyncQueue.class);
            localPropertyName.setBound(true);
            localPropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            localPropertyName.setDisplayName("<html><font color='blue'>Sync property");
            localPropertyName.setShortDescription("Name of the local property that holds the syncronization record. ");

            PropertyDescriptor rv[] = { localPropertyName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(ToSyncQueue.class);
           bd.setDisplayName("<html>To Sync Queue");
        return bd;
    }

}
