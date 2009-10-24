package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class ConceptStatusIsChildOfBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ConceptStatusIsChildOf.class);
        bd
            .setDisplayName("<html><font color='green'><center>Add<br>Concept Status Is<br>Child of<br>Clause to RefSet Spec");
        return bd;
    }
}
