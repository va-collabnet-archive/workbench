/*
 * Created on Apr 7, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class LoadSetLaunchProcessFromURLBeanInfo  extends SimpleBeanInfo {

    public LoadSetLaunchProcessFromURLBeanInfo() {
        super();
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(LoadSetLaunchProcessFromURL.class);
           bd.setDisplayName("<html><center><font color='blue'>Load, Set, Launch<br>Process From URL");
        return bd;
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor processURLString =
                new PropertyDescriptor("processURLString", getBeanDescriptor().getBeanClass());
            processURLString.setBound(true);
            processURLString.setPropertyEditorClass(JTextFieldEditor.class);
            processURLString.setDisplayName("process URL");
            processURLString.setShortDescription("A URL from which a process is loaded.");

            PropertyDescriptor rv[] =
                {processURLString};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
}
