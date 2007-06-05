package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * Bean info to ShowListView class.
 * @author Christine Hill
 *
 */
public class ShowListViewBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ShowListViewBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor rv[] = {  };
        return rv;
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowListView.class);
        bd.setDisplayName("<html><font color='green'><center>Show List View");
        return bd;
    }

}
