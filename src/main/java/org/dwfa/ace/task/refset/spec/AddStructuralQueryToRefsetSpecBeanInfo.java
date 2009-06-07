package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class AddStructuralQueryToRefsetSpecBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(AddStructuralQueryToRefsetSpec.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Structural Query<br>to RefSet Spec");
        return bd;
    }
}
