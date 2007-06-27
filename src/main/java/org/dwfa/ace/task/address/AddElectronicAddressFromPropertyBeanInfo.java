package org.dwfa.ace.task.address;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddElectronicAddressFromPropertyBeanInfo  extends SimpleBeanInfo {

    /**
     *
     */
    public AddElectronicAddressFromPropertyBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor addressPropName =
                new PropertyDescriptor("addressPropName", AddElectronicAddressFromProperty.class);
            addressPropName.setBound(true);
            addressPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            addressPropName.setDisplayName("<html><font color='green'>Electronic address prop:");
            addressPropName.setShortDescription("Enter the property name of the electronic address to be added.");

            PropertyDescriptor rv[] = { addressPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddElectronicAddressFromProperty.class);
        bd.setDisplayName("<html><font color='green'><center>Add Electronic Address<br>From Property");
        return bd;
    }

}
