package org.dwfa.ace.task.profile;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetChangeSetRootBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  

            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile to modify.");

            PropertyDescriptor rootDirName =
                new PropertyDescriptor("rootDirName", getBeanDescriptor().getBeanClass());
            rootDirName.setBound(true);
            rootDirName.setPropertyEditorClass(JTextFieldEditor.class);
            rootDirName.setDisplayName("<html><font color='green'>cs root:");
            rootDirName.setShortDescription("The root directory for the change sets.");

            PropertyDescriptor rv[] =
                { profilePropName, rootDirName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetChangeSetRoot.class);
        bd.setDisplayName("<html><font color='green'><center>set change set<br>root");
        return bd;
    }
}
