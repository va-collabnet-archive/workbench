package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;

public class DifferenceConceptStatusSearchInfo extends AbstractSeachTestSearchInfo {

	@Override
	protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
            PropertyDescriptor rv[] = { };
            return rv;
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(DifferenceConceptStatus.class);
        bd.setDisplayName("Concept status diff");
        return bd;
    }

}
