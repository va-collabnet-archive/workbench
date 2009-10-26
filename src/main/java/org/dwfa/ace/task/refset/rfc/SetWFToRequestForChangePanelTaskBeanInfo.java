package org.dwfa.ace.task.refset.rfc;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class SetWFToRequestForChangePanelTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public SetWFToRequestForChangePanelTaskBeanInfo() {
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
        BeanDescriptor bd = new BeanDescriptor(SetWFToRequestForChangePanelTask.class);
        bd.setDisplayName("<html><font color='green'><center>Set WF Panel to<br>request for change<br>panel");
        return bd;
    }

}
