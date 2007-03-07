/*
 * Created on Mar 8, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ProcessDataIdEditor;

public class LaunchProcessFromContainerBeanInfo extends SimpleBeanInfo {

    public LaunchProcessFromContainerBeanInfo() {
        super();
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(LaunchProcessFromContainer.class);
           bd.setDisplayName("<html><center><font color='blue'>Launch Process<br>From Container");
        return bd;
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  

            PropertyDescriptor process =
                new PropertyDescriptor("processDataId", LaunchProcessFromContainer.class);
            process.setBound(true);
            process.setPropertyEditorClass(ProcessDataIdEditor.class);
            process.setDisplayName("<html><font color='green'>Process");
            process.setShortDescription("A data id for the process container to launch. Only data containers that contain I_EncodeBusinessProcess objects can be dropped. ");

            PropertyDescriptor rv[] =
                { process };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
}
