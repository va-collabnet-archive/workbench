package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;

public class IsNotChildOfBeanInfo extends IsChildOfBeanInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(IsNotChildOf.class);
        bd.setDisplayName("is not child of");
        return bd;
    }

}
