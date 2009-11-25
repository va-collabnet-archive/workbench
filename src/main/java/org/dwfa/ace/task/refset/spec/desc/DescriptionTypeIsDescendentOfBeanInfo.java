package org.dwfa.ace.task.refset.spec.desc;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class DescriptionTypeIsDescendentOfBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(DescriptionTypeIsDescendentOf.class);
        bd
            .setDisplayName("<html><font color='green'><center>Add<br>Description Type Is<br>Descendent of<br>Clause to RefSet Spec");
        return bd;
    }
}
