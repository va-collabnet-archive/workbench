package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.dwfa.bpa.tasks.editor.CheckboxEditor;

/**
 * Bean info to ShowHierarchyView class.
 * @author Christine Hill
 *
 */
public class ShowHierarchyViewBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ShowHierarchyViewBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor visible =
                new PropertyDescriptor("visible", ShowHierarchyView.class);
            visible.setBound(true);
            visible.setPropertyEditorClass(CheckboxEditor.class);
            visible.setDisplayName("<html><font color='green'>Show Hierarchy view");
            visible.setShortDescription("Choose whether to hierarchy list view.");

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
        BeanDescriptor bd = new BeanDescriptor(ShowHierarchyView.class);
        bd.setDisplayName("<html><font color='green'><center>Show or Hide <br> "
                + "Hierarchy View");
        return bd;
    }

}
