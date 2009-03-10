package org.dwfa.ace.task.tree;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetTaxonomySelectionBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor conceptPropName =
                new PropertyDescriptor("conceptPropName", getBeanDescriptor().getBeanClass());
            conceptPropName.setBound(true);
            conceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptPropName.setDisplayName("<html><font color='green'>concept prop:");
            conceptPropName.setShortDescription("The property to hold the concept.");

            PropertyDescriptor rv[] = { conceptPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }
    
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetTaxonomySelection.class);
        bd.setDisplayName("<html><font color='green'><center>Set Taxonomy<br>Selection to Concept");
        return bd;
    }

}
