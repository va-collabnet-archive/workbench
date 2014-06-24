package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class TestSnoPathProcessStatedBeanInfo extends SimpleBeanInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    @Override
    public BeanDescriptor getBeanDescriptor() {
        String s = "<html><font color='#FF8000'>";
        s = s.concat("<center>Test SnoPathProcessStated");
        BeanDescriptor bd = new BeanDescriptor(TestSnoPathProcessStated.class);
        bd.setDisplayName(s);
        return bd;
    }
    
}
