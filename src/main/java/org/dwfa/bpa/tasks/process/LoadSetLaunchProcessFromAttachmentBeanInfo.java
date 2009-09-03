package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
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

            PropertyDescriptor dataCheckingSuppressed = 
                new PropertyDescriptor("dataCheckingSuppressed", getBeanDescriptor().getBeanClass());
            dataCheckingSuppressed.setBound(true);
            dataCheckingSuppressed.setPropertyEditorClass(CheckboxEditor.class);
            dataCheckingSuppressed.setDisplayName("<html><font color='green'>Suppress data checks:");
            dataCheckingSuppressed.setShortDescription("Select if data checking are to be suppressed in supporting tasks");
            
            PropertyDescriptor rv[] =
                { processPropName, dataCheckingSuppressed };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
}
