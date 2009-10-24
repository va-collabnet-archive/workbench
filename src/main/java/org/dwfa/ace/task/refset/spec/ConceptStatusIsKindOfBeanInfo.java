package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class ConceptStatusIsKindOfBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ConceptStatusIsKindOf.class);
        bd
            .setDisplayName("<html><font color='green'><center>Add<br>Concept Status Is<br>Kind of<br>Clause to RefSet Spec");
        return bd;
    }
}
