package org.dwfa.ace.task.profile;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SaveProfileBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor profileDir =
                new PropertyDescriptor("profileDir", getBeanDescriptor().getBeanClass());
            profileDir.setBound(true);
            profileDir.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            profileDir.setDisplayName("<html><font color='green'>profile dir:");
            profileDir.setShortDescription("The directory to write the profile to.");

            PropertyDescriptor usernamePropName =
                new PropertyDescriptor("usernamePropName", getBeanDescriptor().getBeanClass());
            usernamePropName.setBound(true);
            usernamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            usernamePropName.setDisplayName("<html><font color='green'>username prop:");
            usernamePropName.setShortDescription("The property that contains the username.");

            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile to write to disk.");

            PropertyDescriptor rv[] =
                { profileDir, usernamePropName, profilePropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SaveProfile.class);
        bd.setDisplayName("<html><font color='green'><center>save profile");
        return bd;
    }
}
