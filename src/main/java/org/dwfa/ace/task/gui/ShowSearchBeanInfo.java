package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;

/**
 * 
 * @author susan
 *
 */
public class ShowSearchBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public ShowSearchBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor showSearch =
                new PropertyDescriptor("showSearch", ShowSearch.class);
            showSearch.setBound(true);
            showSearch.setPropertyEditorClass(CheckboxEditor.class);
            showSearch.setDisplayName("Show Search:");
            showSearch.setShortDescription("Select to present search panel to the user.");

            PropertyDescriptor rv[] =
                { showSearch };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowSearch.class);
        bd.setDisplayName("<html><font color='green'><center>Show Search");
        return bd;
    }

}
