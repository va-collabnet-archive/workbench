package org.dwfa.ace.task.refset.spec.rel;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class RelationshipCharacteristicIsChildOfBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RelationshipCharacteristicIsChildOf.class);
        bd
            .setDisplayName("<html><font color='green'><center>Add<br>Relationship characteristic<br>is child of<br>Clause to RefSet Spec");
        return bd;
    }
}
