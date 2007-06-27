package org.dwfa.ace.task.svn;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;

public class CommitSvnEntryBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor svnEntryString = new PropertyDescriptor("svnEntryString", getBeanDescriptor()
                    .getBeanClass());
            svnEntryString.setBound(true);
            svnEntryString.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            svnEntryString.setDisplayName("<html><font color='green'>Svn entry:");
            svnEntryString.setShortDescription("The svn entry to process.");
            return new PropertyDescriptor[] { svnEntryString };
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CommitSvnEntry.class);
        bd.setDisplayName("<html><font color='green'><center>Commit Svn Entry");
        return bd;
    }
}