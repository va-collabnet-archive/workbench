package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;

public class IsNotChildOfSearchInfo extends IsChildOfSearchInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(IsNotChildOf.class);
        bd.setDisplayName("is not child of");
        return bd;
    }

}
