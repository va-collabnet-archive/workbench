/*
 * Created on Jun 1, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;


/**
 * @author kec
 *
 */
public class SetDestinationBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SetDestinationBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor destination =
                new PropertyDescriptor("destination", SetDestination.class);
            destination.setBound(true);
            destination.setPropertyEditorClass(JTextFieldEditor.class);
            destination.setDisplayName("destination");
            destination.setShortDescription("An electronic address to which this process is to be delivered.");


            PropertyDescriptor rv[] =
                {destination};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetDestination.class);
        bd.setDisplayName("<html><font color='green'><center>Set Destination");
        return bd;
    }

}
