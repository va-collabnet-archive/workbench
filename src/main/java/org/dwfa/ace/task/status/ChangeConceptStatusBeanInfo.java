package org.dwfa.ace.task.status;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ChangeConceptStatusBeanInfo extends SimpleBeanInfo {

 
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor newStatus =
                new PropertyDescriptor("newStatus", getBeanDescriptor().getBeanClass());
            newStatus.setBound(true);
            newStatus.setPropertyEditorClass(ConceptLabelEditor.class);
            newStatus.setDisplayName("New status:");
            newStatus.setShortDescription("The new status value for the concept.");

            PropertyDescriptor activeConceptPropName =
                new PropertyDescriptor("activeConceptPropName", getBeanDescriptor().getBeanClass());
            activeConceptPropName.setBound(true);
            activeConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            activeConceptPropName.setDisplayName("<html><font color='green'>Concept property:");
            activeConceptPropName.setShortDescription("Name of the property containing the concept to change the status of. ");

            PropertyDescriptor rv[] =
                { newStatus, activeConceptPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChangeConceptStatus.class);
        bd.setDisplayName("<html><font color='green'><center>Change Concept Status");
        return bd;
    }
}
