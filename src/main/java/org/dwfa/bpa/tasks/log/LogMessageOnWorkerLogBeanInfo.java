/*
 * Created on Jun 1, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.log;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * @author kec
 *
 */
public class LogMessageOnWorkerLogBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public LogMessageOnWorkerLogBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor message =
                new PropertyDescriptor("message", LogMessageOnWorkerLog.class);
            message.setBound(true);
            message.setPropertyEditorClass(JTextFieldEditor.class);
            message.setDisplayName("log message");
            message.setShortDescription("A message to write to the worker log.");


            PropertyDescriptor rv[] =
                {message};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(LogMessageOnWorkerLog.class);
        bd.setDisplayName("<html><font color='green'><center>Log on Worker Log");
        return bd;
    }
}
