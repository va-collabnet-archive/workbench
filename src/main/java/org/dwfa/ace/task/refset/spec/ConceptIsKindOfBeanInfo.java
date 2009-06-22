package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class ConceptIsKindOfBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(ConceptIsKindOf.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Concept is Kind of<br>Clause to RefSet Spec");
        return bd;
    }
}
