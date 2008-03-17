package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;

public class InverseRelSubsumptionMatchBeanInfo extends RelSubsumptionMatchBeanInfo {
    private static InverseRelSubsumptionMatchSearchInfo searchInfo = new InverseRelSubsumptionMatchSearchInfo();
    
    @Override
    public BeanDescriptor getBeanDescriptor() {
        return searchInfo.getBeanDescriptor();
    }

}
