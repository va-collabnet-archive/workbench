package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.IncrementEditor;

/**
 * Bean info to ShowConceptTab class.
 * @author Christine Hill
 */
public class ShowConceptTabBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ShowConceptTabBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {
            PropertyDescriptor index =
                new PropertyDescriptor("index", ShowConceptTab.class);
            index.setBound(true);
            index.setPropertyEditorClass(IncrementEditor.class);
            index.setDisplayName("<html><font color='green'>Concept tab:");
            index.setShortDescription("Index of tab to switch to. ");
            PropertyDescriptor rv[] = {  };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowConceptTab.class);
        bd.setDisplayName("<html><font color='green'><center>Switch to <br>"
                + "Concept Tab");
        return bd;
    }

}
