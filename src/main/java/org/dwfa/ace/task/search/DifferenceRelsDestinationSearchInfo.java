package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;

public class DifferenceRelsDestinationSearchInfo extends AbstractSeachTestSearchInfo {

	@Override
	protected PropertyDescriptor[] getAdditionalPropertyDescriptors() {
            PropertyDescriptor rv[] = { };
            return rv;
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(DifferenceRelsDestination.class);
        bd.setDisplayName("Dest rel diff");
        return bd;
    }

}