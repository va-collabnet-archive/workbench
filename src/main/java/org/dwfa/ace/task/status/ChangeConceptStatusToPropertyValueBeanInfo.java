package org.dwfa.ace.task.status;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ChangeConceptStatusToPropertyValueBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ChangeConceptStatusToPropertyValueBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor newStatusPropName =
                new PropertyDescriptor("newStatusPropName", ChangeConceptStatusToPropertyValue.class);
            newStatusPropName.setBound(true);
            newStatusPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            newStatusPropName.setDisplayName("<html><font color='green'>New status property:");
            newStatusPropName.setShortDescription("The property containing the new status value for the concept.");

            PropertyDescriptor activeConceptPropName =
                new PropertyDescriptor("activeConceptPropName", ChangeConceptStatusToPropertyValue.class);
            activeConceptPropName.setBound(true);
            activeConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            activeConceptPropName.setDisplayName("<html><font color='green'>Concept property:");
            activeConceptPropName.setShortDescription("Name of the property containing the concept to change the status of. ");

            PropertyDescriptor rv[] =
                { newStatusPropName, activeConceptPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChangeConceptStatusToPropertyValue.class);
        bd.setDisplayName("<html><font color='green'><center>Change Concept Status<br>to Property Value");
        return bd;
    }
}
