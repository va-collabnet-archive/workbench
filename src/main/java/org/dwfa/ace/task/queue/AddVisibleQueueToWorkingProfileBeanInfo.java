package org.dwfa.ace.task.queue;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddVisibleQueueToWorkingProfileBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor visibleQueuePropName =
                new PropertyDescriptor("visibleQueuePropName", getBeanDescriptor().getBeanClass());
            visibleQueuePropName.setBound(true);
            visibleQueuePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            visibleQueuePropName.setDisplayName("<html><font color='green'>Visible queue prop:");
            visibleQueuePropName.setShortDescription("Enter the property name of the queue address to be added.");

            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile to change.");

            PropertyDescriptor rv[] = { visibleQueuePropName, profilePropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddVisibleQueueToWorkingProfile.class);
        bd.setDisplayName("<html><font color='green'><center>Add Visible Queue<br>To Specified Profile<br>From Property");
        return bd;
    }

}
