package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;

public class InverseRelSubsumptionMatchSearchInfo extends RelSubsumptionMatchSearchInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
	@Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RelSubsumptionMatch.class);
        bd.setDisplayName("not rel kind");
        return bd;
    }
}
