package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class ConceptStatusIsDescendantOfBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ConceptStatusIsDescendantOf.class);
        bd
            .setDisplayName("<html><font color='green'><center>Add<br>Concept Status Is<br>Descendant of<br>Clause to RefSet Spec");
        return bd;
    }
}
