package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ShowQueueViewerForProfileBeanInfo extends SimpleBeanInfo {


    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
        	
            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile.");
        	
        	PropertyDescriptor show =
                new PropertyDescriptor("show", getBeanDescriptor().getBeanClass());
            show.setBound(true);
            show.setPropertyEditorClass(CheckboxEditor.class);
            show.setDisplayName("Show Queue Viewer:");
            show.setShortDescription("Select to show the queue viewer to the user.");

            PropertyDescriptor rv[] =
                { profilePropName, show };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowQueueViewerForProfile.class);
        bd.setDisplayName("<html><font color='green'><center>Show Queue Viewer<br>for Profile");
        return bd;
    }

}