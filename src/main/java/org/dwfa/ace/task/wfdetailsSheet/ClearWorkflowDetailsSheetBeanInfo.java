package org.dwfa.ace.task.wfdetailsSheet;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class ClearWorkflowDetailsSheetBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
             PropertyDescriptor rv[] = {  };
            return rv;
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ClearWorkflowDetailsSheet.class);
        bd.setDisplayName("<html><font color='green'><center>Clear Workflow<br>Details Sheet");
        return bd;
    }

}
