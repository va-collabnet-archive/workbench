package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class AddTextQueryToRefsetSpecBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(AddTextQueryToRefsetSpec.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Text Query<br>to RefSet Spec");
        return bd;
    }
}
