package org.dwfa.ace.task.refset.refresh;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * The RefreshRefsetSpecCompareTaskBeanInfo class describes the visible elements of the 
 * Workflow task RefreshRefsetSpecCompareTask so that it can be displayed in the 
 * Process Builder.  The RefreshRefsetSpecCompareTask uses the information 
 * collected in the RefreshRefsetSpecWizardTask task to create a list of differences 
 * between the selected Refset and the selected version of SNOMED.  
 * 
 * @author  Perry Reid
 * @version 1, November 2009 
 */
public class RefreshRefsetSpecCompareTaskBeanInfo extends SimpleBeanInfo {

    /**
    *  Constructor
    */
    public RefreshRefsetSpecCompareTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        PropertyDescriptor rv[] = {};
        return rv;
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RefreshRefsetSpecCompareTask.class);
        bd.setDisplayName("<html><font color='green'><center>"
        		+ "Compare<br>Refset to SNOMED<br>and List Differences");
        return bd;
    }

}