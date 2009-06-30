package org.dwfa.ace.task.refset.spec.rel;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class RelationshipTypeIsKindOfBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(RelationshipTypeIsKindOf.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Relationship Type<br>Is Kind Of<br>Clause to RefSet Spec");
        return bd;
    }
}
