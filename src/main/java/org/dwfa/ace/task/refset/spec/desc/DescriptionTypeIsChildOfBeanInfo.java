package org.dwfa.ace.task.refset.spec.desc;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.refset.spec.AbstractAddRefsetSpecTaskBeanInfo;

public class DescriptionTypeIsChildOfBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(DescriptionTypeIsChildOf.class);
        bd
            .setDisplayName("<html><font color='green'><center>Add<br>Description Type Is<br>Child of<br>Clause to RefSet Spec");
        return bd;
    }
}
