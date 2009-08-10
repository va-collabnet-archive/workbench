package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;

public class DifferenceFullySpecifiedNameSearchInfo extends AbstractSeachTestSearchInfo {

	@Override
	protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
            PropertyDescriptor rv[] = { };
            return rv;
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(DifferenceFullySpecifiedName.class);
        bd.setDisplayName("Fully specified diff");
        return bd;
    }

}
