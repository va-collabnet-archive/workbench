package org.dwfa.ace.task.refset.spec.rel;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class RelationshipLogicalQuantifierIsBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(RelationshipLogicalQuantifierIs.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Relationship Logical<br>Quantifier Is<br>Clause to RefSet Spec");
        return bd;
    }
}
