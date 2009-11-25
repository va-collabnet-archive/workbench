package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;

public class AddConceptContainsRelToRefsetSpecBeanInfo extends AbstractAddRefsetSpecTaskBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddConceptContainsRelToRefsetSpec.class);
        bd.setDisplayName("<html><font color='green'><center>Add<br>concept-contains-rel<br>Clause<br>to RefSet Spec");
        return bd;
    }
}
