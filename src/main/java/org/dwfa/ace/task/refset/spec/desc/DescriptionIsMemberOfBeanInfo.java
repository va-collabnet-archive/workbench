package org.dwfa.ace.task.refset.spec.desc;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class DescriptionIsMemberOfBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(DescriptionIsMemberOf.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Description Is<br>Member Of<br>Clause to RefSet Spec");
        return bd;
    }
}
