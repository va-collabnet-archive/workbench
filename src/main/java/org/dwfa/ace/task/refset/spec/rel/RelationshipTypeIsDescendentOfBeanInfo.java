package org.dwfa.ace.task.refset.spec.rel;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class RelationshipTypeIsDescendentOfBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RelationshipTypeIsDescendentOf.class);
        bd
            .setDisplayName("<html><font color='green'><center>Add<br>Relationship Type<br>Is Descendent Of<br>Clause to RefSet Spec");
        return bd;
    }
}
