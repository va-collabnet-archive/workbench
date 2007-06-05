package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.dwfa.bpa.tasks.editor.CheckboxEditor;

/**
 * Bean info to ShowComponentView class.
 * @author Christine Hill
 *
 */
public class ShowComponentViewBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ShowComponentViewBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor visible =
                new PropertyDescriptor("visible", ShowComponentView.class);
            visible.setBound(true);
            visible.setPropertyEditorClass(CheckboxEditor.class);
            visible.setDisplayName("<html><font color='green'>Show component view");
            visible.setShortDescription("Choose whether to show component view.");

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
        BeanDescriptor bd = new BeanDescriptor(ShowComponentView.class);
        bd.setDisplayName("<html><font color='green'><center>Show or Hide <br>"
                + "Component View");
        return bd;
    }

}
