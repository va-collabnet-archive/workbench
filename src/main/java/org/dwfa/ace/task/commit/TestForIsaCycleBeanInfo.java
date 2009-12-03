package org.dwfa.ace.task.commit;

import java.beans.BeanDescriptor;

public class TestForIsaCycleBeanInfo extends TestForUneditedDefaultsBeanInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(TestForIsaCycle.class);
        bd.setDisplayName("<html><font color='green'><center>Test For<br>IS_A Cycle");
        return bd;
    }
    
}
