package org.dwfa.ace.task.address;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class SetOriginatorFromProfileBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public SetOriginatorFromProfileBeanInfo() {
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
        BeanDescriptor bd = new BeanDescriptor(SetOriginatorFromProfile.class);
        bd.setDisplayName("<html><font color='green'><center>Set Originator<br>From Profile");
        return bd;
    }

}
