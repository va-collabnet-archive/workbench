package org.dwfa.ace.task.rel;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CreateRelationshipBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public CreateRelationshipBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor relParentPropName =
                new PropertyDescriptor("relParentPropName", CreateRelationship.class);
            relParentPropName.setBound(true);
            relParentPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            relParentPropName.setDisplayName("<html><font color='green'>Rel parent:");
            relParentPropName.setShortDescription("The property containing the new parent value for the relationship.");

            PropertyDescriptor activeConceptPropName =
                new PropertyDescriptor("activeConceptPropName", CreateRelationship.class);
            activeConceptPropName.setBound(true);
            activeConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            activeConceptPropName.setDisplayName("<html><font color='green'>Concept property:");
            activeConceptPropName.setShortDescription("Name of the property containing the concept to add the rel to. ");

            PropertyDescriptor relType =
                new PropertyDescriptor("relType", CreateRelationship.class);
            relType.setBound(true);
            relType.setPropertyEditorClass(ConceptLabelEditor.class);
            relType.setDisplayName("Rel type:");
            relType.setShortDescription("The relationship type for the new relationship.");

            PropertyDescriptor relCharacteristic =
                new PropertyDescriptor("relCharacteristic", CreateRelationship.class);
            relCharacteristic.setBound(true);
            relCharacteristic.setPropertyEditorClass(ConceptLabelEditor.class);
            relCharacteristic.setDisplayName("Rel characteristic:");
            relCharacteristic.setShortDescription("The characteristic for the new relationship.");

            PropertyDescriptor relRefinability =
                new PropertyDescriptor("relRefinability", CreateRelationship.class);
            relRefinability.setBound(true);
            relRefinability.setPropertyEditorClass(ConceptLabelEditor.class);
            relRefinability.setDisplayName("Rel refinability:");
            relRefinability.setShortDescription("The refinability of the new relationship.");

            PropertyDescriptor relStatus =
                new PropertyDescriptor("relStatus", CreateRelationship.class);
            relStatus.setBound(true);
            relStatus.setPropertyEditorClass(ConceptLabelEditor.class);
            relStatus.setDisplayName("Rel status:");
            relStatus.setShortDescription("The status of the new relationship.");

            PropertyDescriptor rv[] =
                { relParentPropName, activeConceptPropName, relType, relCharacteristic, relRefinability, relStatus };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateRelationship.class);
        bd.setDisplayName("<html><font color='green'><center>Create Relationship<br>from Selected Parent");
        return bd;
    }
}
