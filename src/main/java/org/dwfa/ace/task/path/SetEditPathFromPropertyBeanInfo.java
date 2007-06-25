package org.dwfa.ace.task.path;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetEditPathFromPropertyBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile property:");
            profilePropName.setShortDescription("The property containing the profile to change.");


            PropertyDescriptor editPathConceptPropName =
                new PropertyDescriptor("editPathConceptPropName", getBeanDescriptor().getBeanClass());
            editPathConceptPropName.setBound(true);
            editPathConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            editPathConceptPropName.setDisplayName("<html><font color='green'>editing path prop:");
            editPathConceptPropName.setShortDescription("The property name that contains the editing path.");

            PropertyDescriptor rv[] =
                { profilePropName, editPathConceptPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetEditPathFromProperty.class);
        bd.setDisplayName("<html><font color='green'><center>set edit path<br>from property");
        return bd;
    }
}
