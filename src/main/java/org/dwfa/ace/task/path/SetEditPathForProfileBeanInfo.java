package org.dwfa.ace.task.path;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;
import org.dwfa.queue.bpa.tasks.move.QueueTypeEditor;

public class SetEditPathForProfileBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile property:");
            profilePropName.setShortDescription("The property containing the profile to change.");


            PropertyDescriptor editPathEntry =
                new PropertyDescriptor("editPathEntry", getBeanDescriptor().getBeanClass());
            editPathEntry.setBound(true);
            editPathEntry.setPropertyEditorClass(QueueTypeEditor.class);
            editPathEntry.setDisplayName("<html><font color='green'>editing path:");
            editPathEntry.setShortDescription("The property that contains the editing path.");

            PropertyDescriptor rv[] =
                { profilePropName, editPathEntry };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetEditPathForProfile.class);
        bd.setDisplayName("<html><font color='green'><center>set edit path<br>for profile");
        return bd;
    }
}
