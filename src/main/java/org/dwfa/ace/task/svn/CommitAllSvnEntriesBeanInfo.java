package org.dwfa.ace.task.svn;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class CommitAllSvnEntriesBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[] {};
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CommitAllSvnEntries.class);
        bd.setDisplayName("<html><font color='green'><center>Commit All Svn Entries");
        return bd;
    }
}
