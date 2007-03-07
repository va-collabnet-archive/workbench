/*
 * Created on Jul 26, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.util;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ProbabilityEditor;


public class RandomBranchBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor relativeTimeProp =
                new PropertyDescriptor("branchFrequency", RandomBranch.class);
            relativeTimeProp.setBound(true);
            relativeTimeProp.setPropertyEditorClass(ProbabilityEditor.class);
            relativeTimeProp.setDisplayName("branch probability");
            relativeTimeProp.setShortDescription("The probability this task evaluates to true.");



            PropertyDescriptor rv[] =
                {relativeTimeProp};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RandomBranch.class);
        bd.setDisplayName("<html><font color='green'><center>Random Branch");
        return bd;
    }
	
}
