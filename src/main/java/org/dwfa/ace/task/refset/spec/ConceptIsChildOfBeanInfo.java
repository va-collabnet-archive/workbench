package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class ConceptIsChildOfBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ConceptIsChildOf.class);
        bd
                .setDisplayName("<html><font color='green'><center>Add<br>Concept is Child of<br>Clause to RefSet Spec");
        return bd;
    }
}
