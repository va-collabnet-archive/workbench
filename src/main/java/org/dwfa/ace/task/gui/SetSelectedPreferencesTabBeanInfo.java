package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetSelectedPreferencesTabBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile.");
        	
            PropertyDescriptor tabName =
                new PropertyDescriptor("tabName", getBeanDescriptor().getBeanClass());
            tabName.setBound(true);
            tabName.setPropertyEditorClass(JTextFieldEditor.class);
            tabName.setDisplayName("<html><font color='green'>Tab name");
            tabName.setShortDescription("Name of the preferences tab to select.");

            PropertyDescriptor rv[] = { profilePropName, tabName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetSelectedPreferencesTab.class);
        bd.setDisplayName("<html><font color='green'><center>Select<br>"
                + "Preferences Tab<br>for Profile");
        return bd;
    }

}
