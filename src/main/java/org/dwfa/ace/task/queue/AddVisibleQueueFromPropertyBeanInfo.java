package org.dwfa.ace.task.queue;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddVisibleQueueFromPropertyBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor visibleQueuePropName =
                new PropertyDescriptor("visibleQueuePropName", AddVisibleQueueFromProperty.class);
            visibleQueuePropName.setBound(true);
            visibleQueuePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            visibleQueuePropName.setDisplayName("<html><font color='green'>Visible queue prop:");
            visibleQueuePropName.setShortDescription("Enter the property name of the queue address to be added.");

            PropertyDescriptor rv[] = { visibleQueuePropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddVisibleQueueFromProperty.class);
        bd.setDisplayName("<html><font color='green'><center>Add Visible Queue<br>From Property");
        return bd;
    }

}
