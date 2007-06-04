package org.dwfa.ace.task.address;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * Bean info to AddElectronicAddress class.
 * @author Christine Hill
 *
 */
public class AddElectronicAddressBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public AddElectronicAddressBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor address =
                new PropertyDescriptor("address", AddElectronicAddress.class);
            address.setBound(true);
            address.setPropertyEditorClass(JTextFieldEditor.class);
            address.setDisplayName("<html><font color='green'>Electronic address:");
            address.setShortDescription("Enter the electronic address to be added.");

            PropertyDescriptor rv[] = { address };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddElectronicAddress.class);
        bd.setDisplayName("<html><font color='green'><center>Add Electronic <br>Address");
        return bd;
    }

}
