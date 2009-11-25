package org.dwfa.ace.task.refset.spec.desc;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class DescriptionStatusIsDescendentOfBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(DescriptionStatusIsDescendentOf.class);
        bd
            .setDisplayName("<html><font color='green'><center>Add<br>Description Status Is<br>Descendent of<br>Clause to RefSet Spec");
        return bd;
    }
}
