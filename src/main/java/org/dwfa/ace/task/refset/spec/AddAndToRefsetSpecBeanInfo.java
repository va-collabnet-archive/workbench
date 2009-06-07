package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class AddAndToRefsetSpecBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(AddAndToRefsetSpec.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>AND Clause<br>to RefSet Spec");
        return bd;
    }
}
