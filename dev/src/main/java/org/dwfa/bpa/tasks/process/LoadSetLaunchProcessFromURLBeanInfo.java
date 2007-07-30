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

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class LoadSetLaunchProcessFromURLBeanInfo  extends SimpleBeanInfo {

    public LoadSetLaunchProcessFromURLBeanInfo() {
        super();
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(LoadSetLaunchProcessFromURL.class);
           bd.setDisplayName("<html><center><font color='blue'>Load, Set, Launch<br>Process From Property");
        return bd;
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor processPropName =
                new PropertyDescriptor("processPropName", LoadSetLaunchProcessFromURL.class);
            processPropName.setBound(true);
            processPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            processPropName.setDisplayName("process property");
            processPropName.setShortDescription("A process property that contains a marshalled process. It is unmarshalled, then executed.");

            PropertyDescriptor rv[] =
                {processPropName};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
}
