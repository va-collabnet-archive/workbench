package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;

public class ShowQueueViewerBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ShowQueueViewerBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor show =
                new PropertyDescriptor("show", ShowQueueViewer.class);
            show.setBound(true);
            show.setPropertyEditorClass(CheckboxEditor.class);
            show.setDisplayName("Show Queue Viewer:");
            show.setShortDescription("Select to show the queue viewer to the user.");

            PropertyDescriptor rv[] =
                { show };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowQueueViewer.class);
        bd.setDisplayName("<html><font color='green'><center>Show Queue Viewer");
        return bd;
    }

}
