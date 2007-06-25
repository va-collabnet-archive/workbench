package org.dwfa.ace.task.view;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;
import org.dwfa.queue.bpa.tasks.move.QueueTypeEditor;

public class SetViewPositionBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile property:");
            profilePropName.setShortDescription("The property containing the profile to change.");


            PropertyDescriptor positionStr =
                new PropertyDescriptor("positionStr", getBeanDescriptor().getBeanClass());
            positionStr.setBound(true);
            positionStr.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            positionStr.setDisplayName("<html><font color='green'>position:");
            positionStr.setShortDescription("The version as a string. Expressed as \"latest\" or yyyy-MM-dd HH:mm:ss.");

            PropertyDescriptor viewPathEntry =
                new PropertyDescriptor("viewPathEntry", getBeanDescriptor().getBeanClass());
            viewPathEntry.setBound(true);
            viewPathEntry.setPropertyEditorClass(QueueTypeEditor.class);
            viewPathEntry.setDisplayName("<html><font color='green'>view path:");
            viewPathEntry.setShortDescription("The property that contains the concept that identifies the view path.");

            PropertyDescriptor rv[] =
                { profilePropName, positionStr, viewPathEntry };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetViewPosition.class);
        bd.setDisplayName("<html><font color='green'><center>set view position");
        return bd;
    }
}
