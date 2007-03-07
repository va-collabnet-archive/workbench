/*
 * Created on Apr 22, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.deadline;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.RelativeTimeEditor;

/**
 * @author kec
 *
 */
public class SetDeadlineRelativeBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SetDeadlineRelativeBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor relativeTimeProp =
                new PropertyDescriptor("relativeTimeInMins", SetDeadlineRelative.class);
            relativeTimeProp.setBound(true);
            relativeTimeProp.setPropertyEditorClass(RelativeTimeEditor.class);
            relativeTimeProp.setDisplayName("Deadline");
            relativeTimeProp.setShortDescription("The amount of time to add to the execution date to compute the deadline for the process.");

            PropertyDescriptor rv[] = {relativeTimeProp};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetDeadlineRelative.class);
        bd.setDisplayName("<html><font color='green'><center>Set Deadline");
        return bd;
    }

}
