package org.dwfa.ace.task.refset.spec.rel;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class RelationshipCharacteristicIsBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(RelationshipCharacteristicIs.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Relationship characteristic is<br>Clause to RefSet Spec");
        return bd;
    }
}
