package org.dwfa.ace.task.profile;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;
import org.dwfa.queue.bpa.tasks.move.QueueTypeEditor;

public class EditPopupAddBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor conceptToAdd =
                new PropertyDescriptor("conceptToAdd", getBeanDescriptor().getBeanClass());
            conceptToAdd.setBound(true);
            conceptToAdd.setPropertyEditorClass(QueueTypeEditor.class);
            conceptToAdd.setDisplayName("<html><font color='green'>concept to add:");
            conceptToAdd.setShortDescription("The concept to add to the selected type edit popup...");

            PropertyDescriptor type =
                new PropertyDescriptor("type", getBeanDescriptor().getBeanClass());
            type.setBound(true);
            type.setPropertyEditorClass(EditPopupTypeEditor.class);
            type.setDisplayName("<html><font color='green'>type:");
            type.setShortDescription("The popup type to add the concept to..");

            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile.");

            PropertyDescriptor rv[] =
                { conceptToAdd, type, profilePropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(EditPopupAdd.class);
        bd.setDisplayName("<html><font color='green'><center>Add to Edit Popup");
        return bd;
    }
}
