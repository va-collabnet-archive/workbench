package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class TestEPath_NewBeanInfo extends SimpleBeanInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        String s = new String("<html><font color='#FF8000'>");
        s = s.concat("<center>Test Edit Path -- New");
        BeanDescriptor bd = new BeanDescriptor(TestEPath_New.class);
        bd.setDisplayName(s);
        return bd;
    }
    
}
