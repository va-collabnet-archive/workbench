package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

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
            PropertyDescriptor processPropName =
                new PropertyDescriptor("processPropName", getBeanDescriptor().getBeanClass());
            processPropName.setBound(true);
            processPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            processPropName.setDisplayName("process prop");
            processPropName.setShortDescription("A property containing a process which is loaded, set, then launched.");

            PropertyDescriptor rv[] =
                {processPropName};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
}
