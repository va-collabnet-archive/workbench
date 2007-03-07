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

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.ProcessDataIdEditor;

public class LoadProcessFromURLBeanInfo extends SimpleBeanInfo {

    public LoadProcessFromURLBeanInfo() {
        super();
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(LoadProcessFromURL.class);
           bd.setDisplayName("<html><center><font color='green'>Load Process<br>From URL");
        return bd;
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor processURL =
                new PropertyDescriptor("processURLString", LoadProcessFromURL.class);
            processURL.setBound(true);
            processURL.setPropertyEditorClass(JTextFieldEditor.class);
            processURL.setDisplayName("process URL");
            processURL.setShortDescription("A URL from which a process is loaded.");

            PropertyDescriptor process =
                new PropertyDescriptor("processDataId", LoadProcessFromURL.class);
            process.setBound(true);
            process.setPropertyEditorClass(ProcessDataIdEditor.class);
            process.setDisplayName("<html><font color='green'>Process");
            process.setShortDescription("A data id for the process container to load. Only data containers that contain I_EncodeBusinessProcess objects can be dropped. ");

            PropertyDescriptor rv[] =
                {processURL, process};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
}
