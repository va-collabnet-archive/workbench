package org.dwfa.ace.task.cmrscs;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class GenerateTestCmrscsBeanInfo extends SimpleBeanInfo {
	   public PropertyDescriptor[] getPropertyDescriptors() {
	            
	            PropertyDescriptor rv[] = {  };
	            return rv;
	    }        
	    /**
	     * @see java.beans.BeanInfo#getBeanDescriptor()
	     */
	    public BeanDescriptor getBeanDescriptor() {
	        BeanDescriptor bd = new BeanDescriptor(GenerateTestCmrscs.class);
	        bd.setDisplayName("<html><font color='green'><center>Generate Test<br>CMRSCS Change Set");
	        return bd;
	    }
}