package org.dwfa.ace.task.refset.spec.rel;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class RelationshipIsMemberOfBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(RelationshipIsMemberOf.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Relationship Is<br>Member Of<br>Clause to RefSet Spec");
        return bd;
    }
}
