package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class ClassifySelectedBeanInfo extends SimpleBeanInfo {
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(Classify.class);
        // !!! bd.setDisplayName("<html><font color='green'><center>Classify Selected");
        bd.setDisplayName("<html><font color='purple'><center>ClassifySelected");
        return bd;
    }

}
