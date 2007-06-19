package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class AddConceptAndPotDupToListBeanInfo extends SimpleBeanInfo {

	/**
	 * Bean info for AddConceptAndPotDupToList class.
	 * @author Susan Castillo
	 *
	 */
    public AddConceptAndPotDupToListBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
             PropertyDescriptor rv[] = { };
            return rv;
      }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddConceptAndPotDupToList.class);
        bd.setDisplayName("<html><font color='green'><center>Add Concept and<br>Pot Dup<br>to List");
        return bd;
    }

}
