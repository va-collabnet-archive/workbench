package org.dwfa.ace.task.standalone.sync;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class UserToCentralSyncPkgBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
             PropertyDescriptor rv[] = {  };
            return rv;
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(UserToCentralSyncPkg.class);
        bd.setDisplayName("<html><font color='green'><center>create<br>user->central<br>sync package");
        return bd;
    }

}
