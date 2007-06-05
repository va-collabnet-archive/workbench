package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.dwfa.bpa.tasks.editor.CheckboxEditor;

/**
 * Bean info to ShowAddressBook class.
 * @author Christine Hill
 *
 */
public class ShowAddressBookBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ShowAddressBookBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor showAddressBook =
                new PropertyDescriptor("showAddressBook", ShowAddressBook.class);
            showAddressBook.setBound(true);
            showAddressBook.setPropertyEditorClass(CheckboxEditor.class);
            showAddressBook.setDisplayName("<html><font color='green'>Show address book");
            showAddressBook.setShortDescription("Choose whether to show address book.");

            PropertyDescriptor rv[] = { showAddressBook };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowAddressBook.class);
        bd.setDisplayName("<html><font color='green'><center>Show or Hide <br>Address Book");
        return bd;
    }

}
