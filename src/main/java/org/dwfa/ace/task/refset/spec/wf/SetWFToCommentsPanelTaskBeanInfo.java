package org.dwfa.ace.task.refset.spec.wf;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class SetWFToCommentsPanelTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public SetWFToCommentsPanelTaskBeanInfo() {
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
        BeanDescriptor bd = new BeanDescriptor(SetWFToCommentsPanelTask.class);
        bd.setDisplayName("<html><font color='green'><center>Set WF Panel to<br>comments<br>panel");
        return bd;
    }

}
