package org.dwfa.ace.task.path;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;
import org.dwfa.queue.bpa.tasks.move.QueueTypeEditor;

/**
 * @author Ming Zhang
 *
 * @created 18/01/2008
 */
public class SetEditPathFromDescriptionBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor pathDescription =
                new PropertyDescriptor("PathDescription", getBeanDescriptor().getBeanClass());
            pathDescription.setBound(true);
            pathDescription.setPropertyEditorClass(PropertyNameLabelEditor.class);
            pathDescription.setDisplayName("<html><font color='green'>path Description:");
            pathDescription.setShortDescription("The property that contains the editing path.");

            PropertyDescriptor rv[] =
                { pathDescription };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetEditPathFromDescription.class);
        bd.setDisplayName("<html><font color='green'><center>set edit path<br>From Description");
        return bd;
    }
}
