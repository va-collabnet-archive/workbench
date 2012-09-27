package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class TestSnoPathProcessInferredBeanInfo extends SimpleBeanInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        String s = new String("<html><font color='#FF8000'>");
        s = s.concat("<center>Test SnoPathProcessInferred");
        BeanDescriptor bd = new BeanDescriptor(TestSnoPathProcessInferred.class);
        bd.setDisplayName(s);
        return bd;
    }
    
}
