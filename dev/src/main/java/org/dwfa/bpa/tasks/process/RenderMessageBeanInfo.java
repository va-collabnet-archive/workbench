package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class RenderMessageBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public RenderMessageBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
            return new PropertyDescriptor[] {};
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RenderMessage.class);
        bd.setDisplayName("<html><font color='green'><center>Render Message");
        return bd;
    }

}
