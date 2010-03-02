package org.dwfa.ace.task.promote;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class PromoteConceptBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor conceptPropName = new PropertyDescriptor("conceptPropName",
                    getBeanDescriptor().getBeanClass());
            conceptPropName.setBound(true);
            conceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptPropName.setDisplayName("<html><font color='green'>concept prop:");
            conceptPropName.setShortDescription("The property that will contain the concept to promote.");
                
                PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
                        getBeanDescriptor().getBeanClass());
                    profilePropName.setBound(true);
                    profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
                    profilePropName.setDisplayName("<html><font color='green'>profile prop:");
                    profilePropName.setShortDescription("The property that will contain the profile this task"
                        + " will use to determine the path for promotion, and the source (edit) path.");

            PropertyDescriptor rv[] = { conceptPropName, profilePropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(PromoteConcept.class);
        bd.setDisplayName("<html><font color='green'><center>Promote Concept");
        return bd;
    }
}
