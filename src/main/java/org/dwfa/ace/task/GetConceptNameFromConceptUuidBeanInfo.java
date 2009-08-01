package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info for GetConceptNameFromConceptUuid class.
 * @author Susan Castillo
 *
 */
public class GetConceptNameFromConceptUuidBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public GetConceptNameFromConceptUuidBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor uuidPropName =
                new PropertyDescriptor("uuidPropName", getBeanDescriptor().getBeanClass());
            uuidPropName.setBound(true);
            uuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidPropName.setDisplayName("<html><font color='green'>Concept Uuid:");
            uuidPropName.setShortDescription("Uuuid list.");

            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile");
            
            
            PropertyDescriptor conceptPropName =
                new PropertyDescriptor("conceptPropName", getBeanDescriptor().getBeanClass());
            conceptPropName.setBound(true);
            conceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptPropName.setDisplayName("<html><font color='green'>Concept Name:");
            conceptPropName.setShortDescription("Concept");

            PropertyDescriptor rv[] = { uuidPropName, conceptPropName, profilePropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetConceptNameFromConceptUuid.class);
        bd.setDisplayName("<html><font color='green'><center>Get Concept Name <br> From Uuid");
        return bd;
    }

}
