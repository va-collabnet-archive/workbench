package org.dwfa.ace.task.status;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ChangeRelsOfTypeToStatusBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ChangeRelsOfTypeToStatusBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor relType =
                new PropertyDescriptor("relType", ChangeRelsOfTypeToStatus.class);
            relType.setBound(true);
            relType.setPropertyEditorClass(ConceptLabelPropEditor.class);
            relType.setDisplayName("Rel type:");
            relType.setShortDescription("The type of rels to change the status of.");

            PropertyDescriptor newStatus =
                new PropertyDescriptor("newStatus", ChangeRelsOfTypeToStatus.class);
            newStatus.setBound(true);
            newStatus.setPropertyEditorClass(ConceptLabelPropEditor.class);
            newStatus.setDisplayName("New status:");
            newStatus.setShortDescription("The new status value for the concept.");

            PropertyDescriptor activeConceptPropName =
                new PropertyDescriptor("activeConceptPropName", ChangeRelsOfTypeToStatus.class);
            activeConceptPropName.setBound(true);
            activeConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            activeConceptPropName.setDisplayName("<html><font color='green'>Concept property:");
            activeConceptPropName.setShortDescription("Name of the property containing the concept to change the status of. ");

            PropertyDescriptor rv[] =
                { relType, newStatus, activeConceptPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChangeRelsOfTypeToStatus.class);
        bd.setDisplayName("<html><font color='green'><center>Change Rels of Type<br> to Status");
        return bd;
    }
}