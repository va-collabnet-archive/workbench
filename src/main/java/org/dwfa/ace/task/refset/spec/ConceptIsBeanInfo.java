package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class ConceptIsBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(ConceptIs.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Concept Is<br>Clause to RefSet Spec");
        return bd;
    }
}
