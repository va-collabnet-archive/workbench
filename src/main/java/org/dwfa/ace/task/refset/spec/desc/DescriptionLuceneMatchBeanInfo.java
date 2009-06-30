package org.dwfa.ace.task.refset.spec.desc;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class DescriptionLuceneMatchBeanInfo extends
		AbstractAddRefsetSpecTaskBeanInfo {

	@Override
	public BeanDescriptor getBeanDescriptor()  {
        BeanDescriptor bd = new BeanDescriptor(DescriptionLuceneMatch.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>Description Lucene Match<br>Clause to RefSet Spec");
        return bd;
    }
}
