package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class ConceptIsDescendantOfBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ConceptIsDescendantOf.class);
        bd.setDisplayName("<html><font color='green'><center>Add Concept is<br>Descendant of<br>Clause to RefSet Spec");
        return bd;
    }
}
