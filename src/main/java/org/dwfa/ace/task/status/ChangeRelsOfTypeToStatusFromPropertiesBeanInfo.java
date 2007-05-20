package org.dwfa.ace.task.status;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ChangeRelsOfTypeToStatusFromPropertiesBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ChangeRelsOfTypeToStatusFromPropertiesBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor relTypePropName =
                new PropertyDescriptor("relTypePropName", ChangeRelsOfTypeToStatusFromProperties.class);
            relTypePropName.setBound(true);
            relTypePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            relTypePropName.setDisplayName("<html><font color='green'>Rel type property:");
            relTypePropName.setShortDescription("The property containing the rel type to change the status of.");

            PropertyDescriptor newStatusPropName =
                new PropertyDescriptor("newStatusPropName", ChangeRelsOfTypeToStatusFromProperties.class);
            newStatusPropName.setBound(true);
            newStatusPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            newStatusPropName.setDisplayName("<html><font color='green'>New status property:");
            newStatusPropName.setShortDescription("The property containing the new status value for the relationship.");

            PropertyDescriptor activeConceptPropName =
                new PropertyDescriptor("activeConceptPropName", ChangeRelsOfTypeToStatusFromProperties.class);
            activeConceptPropName.setBound(true);
            activeConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            activeConceptPropName.setDisplayName("<html><font color='green'>Concept property:");
            activeConceptPropName.setShortDescription("Name of the property containing the concept to change the status of the specified relationships. ");

            PropertyDescriptor rv[] =
                { relTypePropName, newStatusPropName, activeConceptPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChangeRelsOfTypeToStatusFromProperties.class);
        bd.setDisplayName("<html><font color='green'><center>Change Rels of Type<br>to Status from Properties");
        return bd;
    }
}
