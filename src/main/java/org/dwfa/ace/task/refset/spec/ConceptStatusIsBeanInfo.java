package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class ConceptStatusIsBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(ConceptStatusIs.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Concept Status Is<br>Clause to RefSet Spec");
        return bd;
    }
}
