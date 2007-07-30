package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class LoadSetLaunchProcessFromAttachmentBeanInfo extends SimpleBeanInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(LoadSetLaunchProcessFromAttachment.class);
           bd.setDisplayName("<html><center><font color='blue'>Load, Set, Launch<br>Process From Attachment");
        return bd;
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor message =
                new PropertyDescriptor("processURLString", getBeanDescriptor().getBeanClass());
            message.setBound(true);
            message.setPropertyEditorClass(JTextFieldEditor.class);
            message.setDisplayName("process URL");
            message.setShortDescription("A URL from which a process is downloaded, then executed.");

            PropertyDescriptor rv[] =
                {message};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
}
