package org.dwfa.queue.bpa.tasks.hide;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class DeleteEntryBeanInfo extends SimpleBeanInfo {
    protected Class getBeanClass() {
        return DeleteEntry.class;
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(getBeanClass());
        bd.setDisplayName("<html><font color='red'><center>Delete Entry");
        return bd;
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  

            PropertyDescriptor queueEntryProp =
                new PropertyDescriptor("queueEntryPropName", getBeanClass());
            queueEntryProp.setBound(true);
            queueEntryProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
            queueEntryProp.setDisplayName("Entry Data:");
            queueEntryProp.setShortDescription("A QueueEntryData object that fully specifies a queue entry.");


            PropertyDescriptor rv[] =
                {queueEntryProp};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
}