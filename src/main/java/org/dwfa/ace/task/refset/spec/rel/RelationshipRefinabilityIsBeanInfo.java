package org.dwfa.ace.task.refset.spec.rel;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class RelationshipRefinabilityIsBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(RelationshipRefinabilityIs.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Description Refinability Is<br>Clause to RefSet Spec");
        return bd;
    }
}
