package org.dwfa.ace.task.commit;

import java.beans.BeanDescriptor;

public class TestForSelfReferencingRelationshipsBeanInfo extends TestForUneditedDefaultsBeanInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TestForSelfReferencingRelationships.class);
        bd.setDisplayName("<html><font color='green'><center>Test For<br>Self-referencing<br>Relationship");
        return bd;
    }
	
}
