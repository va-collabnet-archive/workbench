package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class ClassifyCurrentBeanInfo extends SimpleBeanInfo {
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ClassifyCurrent.class);
        // !!! bd.setDisplayName("<html><font color='green'><center>Classify in Current Tab");
        bd.setDisplayName("<html><font color='purple'><center>ClassifyCurrent Current Tab");
        return bd;
    }

}
