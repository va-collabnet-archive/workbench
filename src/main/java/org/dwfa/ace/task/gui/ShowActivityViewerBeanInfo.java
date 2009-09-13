package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;

/**
 * 
 * @author Christine Hill
 * 
 */
public class ShowActivityViewerBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ShowActivityViewerBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor showActivityViewer =
                    new PropertyDescriptor("showActivityViewer", ShowActivityViewer.class);
            showActivityViewer.setBound(true);
            showActivityViewer.setPropertyEditorClass(CheckboxEditor.class);
            showActivityViewer.setDisplayName("Show activity viewer:");
            showActivityViewer.setShortDescription("Select to show activity viewer.");

            PropertyDescriptor rv[] = { showActivityViewer };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowActivityViewer.class);
        bd.setDisplayName("<html><font color='green'><center>Show Activity Viewer");
        return bd;
    }

}
