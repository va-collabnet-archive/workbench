package org.dwfa.ace.task.refset.spec;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class RefsetSpecWizardTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public RefsetSpecWizardTaskBeanInfo() {
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
        BeanDescriptor bd = new BeanDescriptor(RefsetSpecWizardTask.class);
        bd.setDisplayName("<html><font color='blue'><center>Start refset<br>spec wizard");
        return bd;
    }

}
