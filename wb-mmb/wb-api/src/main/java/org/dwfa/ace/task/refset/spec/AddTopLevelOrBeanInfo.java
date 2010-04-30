package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class AddTopLevelOrBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddTopLevelOr.class);
        bd.setDisplayName("<html><font color='green'><center>Add Top Level<br>OR Clause<br>to RefSet Spec");
        return bd;
    }
}