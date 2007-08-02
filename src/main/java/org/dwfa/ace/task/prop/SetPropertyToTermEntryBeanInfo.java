package org.dwfa.ace.task.prop;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;
import org.dwfa.queue.bpa.tasks.move.QueueTypeEditor;

public class SetPropertyToTermEntryBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  

            PropertyDescriptor termPropName =
                new PropertyDescriptor("termPropName", getBeanDescriptor().getBeanClass());
            termPropName.setBound(true);
            termPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            termPropName.setDisplayName("<html><font color='green'>profile prop:");
            termPropName.setShortDescription("The property that contains the profile to modify.");

            PropertyDescriptor termEntry =
                new PropertyDescriptor("termEntry", getBeanDescriptor().getBeanClass());
            termEntry.setBound(true);
            termEntry.setPropertyEditorClass(QueueTypeEditor.class);
            termEntry.setDisplayName("<html><font color='green'>term:");
            termEntry.setShortDescription("The concept to add to the property.");

            PropertyDescriptor rv[] =
                { termPropName, termEntry };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetPropertyToTermEntry.class);
        bd.setDisplayName("<html><font color='green'><center>set property<br>to term entry");
        return bd;
    }
}
