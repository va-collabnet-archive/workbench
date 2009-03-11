package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * Bean info to MoveToDestination class.
 * @author Susan Castillo
 *
 */
public class MoveToDestinationBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public MoveToDestinationBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor rv[] =
		    { };
		return rv;
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(MoveToDestination.class);
        bd.setDisplayName("<html><font color='green'><center>Move To Destination");
        return bd;
    }
}
