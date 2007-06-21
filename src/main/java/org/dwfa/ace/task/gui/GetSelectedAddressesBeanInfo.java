package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;

/**
 * Bean info to SelectAddressesInAddressBook class.
 * @author Susan Castillo	
 *
 */
public class GetSelectedAddressesBeanInfo {

    /**
     *
     */
    public GetSelectedAddressesBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor addressesSelected =
                new PropertyDescriptor("addressesSelected", GetSelectedAddresses.class);
            addressesSelected.setBound(true);
            addressesSelected.setPropertyEditorClass(CheckboxEditor.class);
            addressesSelected.setDisplayName("<html><font color='green'>Select Addresses<br>in Address Book");
            addressesSelected.setShortDescription("Select Addresses");

            PropertyDescriptor rv[] = { addressesSelected };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetSelectedAddresses.class);
        bd.setDisplayName("<html><font color='green'><center>Show or Hide <br>Select Addresses<br>Address Book");
        return bd;
    }

}
