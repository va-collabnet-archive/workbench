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
import org.dwfa.bpa.tasks.editor.LevelEditor;

/**
 * @author kec
 *
 */
public class LogMessageBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public LogMessageBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor level =
                new PropertyDescriptor("level", LogMessage.class);
            level.setBound(true);
            level.setPropertyEditorClass(LevelEditor.class);
            level.setDisplayName("level");
            level.setShortDescription("Logging level for the message.");
            
            PropertyDescriptor log =
                new PropertyDescriptor("log", LogMessage.class);
            log.setBound(true);
            log.setPropertyEditorClass(JTextFieldEditor.class);
            log.setDisplayName("log");
            log.setShortDescription("Log upon which to write the message.");

            PropertyDescriptor message =
                new PropertyDescriptor("message", LogMessage.class);
            message.setBound(true);
            message.setPropertyEditorClass(JTextFieldEditor.class);
            message.setDisplayName("log message");
            message.setShortDescription("A message to write to the worker log.");


            PropertyDescriptor rv[] =
                {message, log, level};
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
        bd.setDisplayName("<html><font color='green'><center>Log on Specified Log");
        return bd;
    }
}
