package org.dwfa.ace.task.profile;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class EditPopupClearBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor type =
                new PropertyDescriptor("type", getBeanDescriptor().getBeanClass());
            type.setBound(true);
            type.setPropertyEditorClass(EditDefaultTypeEditor.class);
            type.setDisplayName("<html><font color='green'>type:");
            type.setShortDescription("The type to add the concept to..");

            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile.");

            PropertyDescriptor rv[] =
                { type, profilePropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(EditPopupClear.class);
        bd.setDisplayName("<html><font color='green'><center>Clear Edit Popup");
        return bd;
    }
}
