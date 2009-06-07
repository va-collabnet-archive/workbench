package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class ConceptIsMemberOfBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(ConceptIsMemberOf.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Concept is Member of<br>Clause to RefSet Spec");
        return bd;
    }
}
