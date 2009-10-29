package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ShowPreferencesForProfileBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile.");
        	
            PropertyDescriptor visible =
                new PropertyDescriptor("visible", getBeanDescriptor().getBeanClass());
            visible.setBound(true);
            visible.setPropertyEditorClass(CheckboxEditor.class);
            visible.setDisplayName("<html><font color='green'>Show preferences view");
            visible.setShortDescription("Choose whether to show preferences view.");

            PropertyDescriptor rv[] = { profilePropName, visible };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowPreferencesForProfile.class);
        bd.setDisplayName("<html><font color='green'><center>Show or Hide<br>"
                + "Preferences View<br>for Profile");
        return bd;
    }

}
