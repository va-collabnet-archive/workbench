/*
 * Created on Feb 18, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ProcessTaskIdEditor;

public class LaunchProcessBeanInfo extends SimpleBeanInfo {

    public LaunchProcessBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor process =
                new PropertyDescriptor("processTaskId", LaunchProcess.class);
            process.setBound(true);
            process.setPropertyEditorClass(ProcessTaskIdEditor.class);
            process.setDisplayName("Process id");
            process.setShortDescription("A task id for the process to launch. Only tasks that implement the I_EncodeBusinessProcess interface can be dropped. ");


            PropertyDescriptor rv[] =
                {process};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(LaunchProcess.class);
           bd.setDisplayName("<html><center><font color='blue'>Launch Process<br>From Internal Task");
        return bd;
    }

}
