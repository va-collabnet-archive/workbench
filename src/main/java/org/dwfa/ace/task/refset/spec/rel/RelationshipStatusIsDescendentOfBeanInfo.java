package org.dwfa.ace.task.refset.spec.rel;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class RelationshipStatusIsDescendentOfBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RelationshipStatusIsDescendentOf.class);
        bd
            .setDisplayName("<html><font color='green'><center>Add<br>Relationship Status Is<br>Descendent Of<br>Clause to RefSet Spec");
        return bd;
    }
}
