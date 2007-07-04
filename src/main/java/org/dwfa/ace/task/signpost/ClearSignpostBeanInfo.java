package org.dwfa.ace.task.signpost;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class ClearSignpostBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
             PropertyDescriptor rv[] = {  };
            return rv;
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ClearSignpost.class);
        bd.setDisplayName("<html><font color='green'><center>Clear Signpost Panel");
        return bd;
    }

}
