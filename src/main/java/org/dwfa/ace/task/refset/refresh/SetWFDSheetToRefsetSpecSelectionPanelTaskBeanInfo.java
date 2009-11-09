package org.dwfa.ace.task.refset.refresh;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class SetWFDSheetToRefsetSpecSelectionPanelTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public SetWFDSheetToRefsetSpecSelectionPanelTaskBeanInfo() {
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
        BeanDescriptor bd = new BeanDescriptor(SetWFDSheetToRefsetSpecSelectionPanelTask.class);
        bd.setDisplayName("<html><font color='green'><center>Set WFD Sheet to<br>Refset Spec<br>Selection panel");
        return bd;
    }

}
