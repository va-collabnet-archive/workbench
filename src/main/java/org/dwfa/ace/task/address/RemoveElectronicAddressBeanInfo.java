package org.dwfa.ace.task.address;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;



/**
 * Bean info to RemoveElectronicAddress class.
 * @author Christine Hill
 *
 */
public class RemoveElectronicAddressBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public RemoveElectronicAddressBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
       PropertyDescriptor rv[] = { };
       return rv;
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RemoveElectronicAddress.class);
        bd.setDisplayName("<html><font color='green'><center>Remove Selected<br>Electronic Address");
        return bd;
    }

}
