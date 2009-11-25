package org.dwfa.ace.task.refset.spec.rel;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class RelationshipCharacteristicIsDescendentOfBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RelationshipCharacteristicIsDescendentOf.class);
        bd
            .setDisplayName("<html><font color='green'><center>Add<br>Relationship characteristic<br>is descendent of<br>Clause to RefSet Spec");
        return bd;
    }
}
