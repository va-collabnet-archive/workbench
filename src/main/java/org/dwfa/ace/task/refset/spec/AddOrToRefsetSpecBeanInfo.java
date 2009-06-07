package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class AddOrToRefsetSpecBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(AddOrToRefsetSpec.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>OR Clause<br>to RefSet Spec");
        return bd;
    }
}
