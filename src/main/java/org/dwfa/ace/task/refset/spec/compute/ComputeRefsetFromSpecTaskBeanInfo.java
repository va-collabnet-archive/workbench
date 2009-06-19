package org.dwfa.ace.task.refset.spec.compute;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class ComputeRefsetFromSpecTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public ComputeRefsetFromSpecTaskBeanInfo() {
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
        BeanDescriptor bd = new BeanDescriptor(ComputeRefsetFromSpecTask.class);
        bd.setDisplayName("<html><font color='green'><center>Compute refset from<br>Refset Spec");
        return bd;
    }  
 
}
