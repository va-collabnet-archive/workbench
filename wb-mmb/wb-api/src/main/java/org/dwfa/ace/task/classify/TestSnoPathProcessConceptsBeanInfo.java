package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

public class TestSnoPathProcessConceptsBeanInfo extends SimpleBeanInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        String s = new String("<html><font color='#FF8000'>");
        s = s.concat("<center>Test SnoPathProcessConcepts");
        BeanDescriptor bd = new BeanDescriptor(TestSnoPathProcessConcepts.class);
        bd.setDisplayName(s);
        return bd;
    }
    
}
