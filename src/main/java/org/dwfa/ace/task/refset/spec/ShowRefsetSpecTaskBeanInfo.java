package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class ShowRefsetSpecTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public ShowRefsetSpecTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        PropertyDescriptor rv[] = {};
        return rv;
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowRefsetSpecTask.class);
        bd.setDisplayName("<html><font color='blue'><center>Show refset<br>spec");
        return bd;
    }

}
