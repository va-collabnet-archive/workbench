package org.dwfa.ace.task.address;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class AddAllProfilesToAddressListBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
             PropertyDescriptor rv[] = { };
            return rv;
     }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddAllProfilesToAddressList.class);
        bd.setDisplayName("<html><font color='green'><center>Add All Profiles<br>To Address List");
        return bd;
    }

}
