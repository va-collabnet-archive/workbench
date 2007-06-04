package org.dwfa.ace.task.address;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;


/**
 * Bean info to RemoveAllElectronicAddress class.
 * @author Christine Hill
 *
 */
public class RemoveAllElectronicAddressBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public RemoveAllElectronicAddressBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
       PropertyDescriptor rv[] = {  };
       return rv;

    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RemoveAllElectronicAddress.class);
        bd.setDisplayName("<html><font color='green'><center>Remove ALL <br>Addresses");
        return bd;
    }

}
