package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.dwfa.bpa.tasks.editor.CheckboxEditor;

/**
 * Bean info to ShowHistoryView class.
 * @author Christine Hill
 *
 */
public class ShowHistoryBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ShowHistoryBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor visible =
                new PropertyDescriptor("visible", ShowHistory.class);
            visible.setBound(true);
            visible.setPropertyEditorClass(CheckboxEditor.class);
            visible.setDisplayName("<html><font color='green'>Show history view");
            visible.setShortDescription("Choose whether to show history view.");

            PropertyDescriptor rv[] = { visible };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowHistory.class);
        bd.setDisplayName("<html><font color='green'><center>Show or Hide <br> "
                + " History View");
        return bd;
    }

}
