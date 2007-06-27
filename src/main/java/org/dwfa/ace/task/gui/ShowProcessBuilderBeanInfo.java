package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;

public class ShowProcessBuilderBeanInfo extends SimpleBeanInfo {


    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor show =
                new PropertyDescriptor("show", ShowProcessBuilder.class);
            show.setBound(true);
            show.setPropertyEditorClass(CheckboxEditor.class);
            show.setDisplayName("Show Process Builder:");
            show.setShortDescription("Select to show the process builder to the user.");

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
        BeanDescriptor bd = new BeanDescriptor(ShowProcessBuilder.class);
        bd.setDisplayName("<html><font color='green'><center>Show Process Builder");
        return bd;
    }

}
