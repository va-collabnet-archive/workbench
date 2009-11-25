package org.dwfa.ace.task.refset.spec.rel;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class RelationshipStatusIsChildOfBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RelationshipStatusIsChildOf.class);
        bd
            .setDisplayName("<html><font color='green'><center>Add<br>Relationship Status Is<br>Child Of<br>Clause to RefSet Spec");
        return bd;
    }
}
