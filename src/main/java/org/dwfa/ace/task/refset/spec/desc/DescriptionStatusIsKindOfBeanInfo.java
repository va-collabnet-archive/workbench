package org.dwfa.ace.task.refset.spec.desc;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class DescriptionStatusIsKindOfBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(DescriptionStatusIsKindOf.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Description Status Is<br>Kind Of<br>Clause to RefSet Spec");
        return bd;
    }
}
