package org.dwfa.ace.task.prop;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetPropertyToHierarchySelectionUuidBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public SetPropertyToHierarchySelectionUuidBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor propName = new PropertyDescriptor("propName", SetPropertyToHierarchySelectionUuid.class);
            propName.setBound(true);
            propName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            propName.setDisplayName("<html><font color='green'>Uuid property:");
            propName.setShortDescription("Name of the property containing the concept uuid. ");

            PropertyDescriptor rv[] = { propName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetPropertyToHierarchySelectionUuid.class);
        bd.setDisplayName("<html><font color='green'><center>Set Property<br>to Hierarchy Selection UUID");
        return bd;
    }

}