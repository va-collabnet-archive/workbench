package org.dwfa.ace.task.signpost;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class SetSignpostToDbPropertiesBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
      return new PropertyDescriptor[] {};
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetSignpostToDbProperties.class);
        bd.setDisplayName("<html><font color='green'><center>Set Signpost<br>to DB info");
        return bd;
    }

}
