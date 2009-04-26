package org.dwfa.ace.task.standalone.sync;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class ProcessCentralToUserSyncPkgBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
             PropertyDescriptor rv[] = {  };
            return rv;
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ProcessCentralToUserSyncPkg.class);
        bd.setDisplayName("<html><font color='green'><center>process<br>central->user<br>sync package");
        return bd;
    }

}
