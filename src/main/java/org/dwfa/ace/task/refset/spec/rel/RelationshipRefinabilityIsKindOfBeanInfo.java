package org.dwfa.ace.task.refset.spec.rel;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class RelationshipRefinabilityIsKindOfBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(RelationshipRefinabilityIsKindOf.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Description Refinability<br>Is Kind Of<br>Clause to RefSet Spec");
        return bd;
    }
}
