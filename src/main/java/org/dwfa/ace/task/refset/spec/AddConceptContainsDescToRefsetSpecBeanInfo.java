package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class AddConceptContainsDescToRefsetSpecBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddConceptContainsDescToRefsetSpec.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>concept-contains-desc<br>Clause<br>to RefSet Spec");
        return bd;
    }
}
