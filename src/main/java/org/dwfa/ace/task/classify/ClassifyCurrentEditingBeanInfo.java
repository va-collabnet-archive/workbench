package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class ClassifyCurrentEditingBeanInfo extends SimpleBeanInfo {
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ClassifyCurrentEditing.class);
        // !!! bd.setDisplayName("<html><font color='green'><center>Classify Current editing");
        bd.setDisplayName("<html><font color='purple'><center>ClassifyCurrentEditing");
        return bd;
    }

}
