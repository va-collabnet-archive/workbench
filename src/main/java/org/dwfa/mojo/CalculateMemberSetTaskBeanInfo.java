package org.dwfa.mojo;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.queue.bpa.tasks.move.QueueTypeEditor;

/**
 * Bean info to CalculateMemberSetTaskBeanInfo class.
 * @author Christine Hill
 *
 */
public class CalculateMemberSetTaskBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public CalculateMemberSetTaskBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor root =
                new PropertyDescriptor("root", CalculateMemberSetTaskBeanInfo.class);
            PropertyDescriptor refset =
                new PropertyDescriptor("refset", CalculateMemberSetTaskBeanInfo.class);
            PropertyDescriptor memberset =
                new PropertyDescriptor("memberset", CalculateMemberSetTaskBeanInfo.class);
            PropertyDescriptor membersetPath =
                new PropertyDescriptor("membersetPath", CalculateMemberSetTaskBeanInfo.class);

            root.setBound(true);
            root.setPropertyEditorClass(QueueTypeEditor.class);
            root.setDisplayName("<html><font color='green'>Drag root here:");
            root.setShortDescription("Calculate a specified member set");

            PropertyDescriptor rv[] = { root, refset, memberset, membersetPath };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CalculateMemberSetTaskBeanInfo.class);
        bd.setDisplayName("<html><font color='green'><center>Calculate Member Set");
        return bd;
    }

}
