package org.dwfa.ace.task.profile;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class EditSetDefaultBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor defaultConcept =
                new PropertyDescriptor("defaultConcept", getBeanDescriptor().getBeanClass());
            defaultConcept.setBound(true);
            defaultConcept.setPropertyEditorClass(ConceptLabelPropEditor.class);
            defaultConcept.setDisplayName("<html><font color='green'>default:");
            defaultConcept.setShortDescription("The default value for the selected type");

            PropertyDescriptor type =
                new PropertyDescriptor("type", getBeanDescriptor().getBeanClass());
            type.setBound(true);
            type.setPropertyEditorClass(EditDefaultTypeEditor.class);
            type.setDisplayName("<html><font color='green'>type:");
            type.setShortDescription("The type for which the default applies.");

            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile to write to disk.");

            PropertyDescriptor rv[] =
                { defaultConcept, type, profilePropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(EditSetDefault.class);
        bd.setDisplayName("<html><font color='green'><center>Set Edit Default");
        return bd;
    }
}
