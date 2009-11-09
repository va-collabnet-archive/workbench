package org.dwfa.ace.task.refset.refresh;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class SetWFDSheetToRefreshRefsetSpecParamsPanelTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public SetWFDSheetToRefreshRefsetSpecParamsPanelTaskBeanInfo() {
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
        BeanDescriptor bd = new BeanDescriptor(SetWFDSheetToRefreshRefsetSpecParamsPanelTask.class);
        bd.setDisplayName("<html><font color='green'><center>Set WFD Sheet to<br>Refresh Refset Spec<br>Params panel");
        return bd;
    }

}
