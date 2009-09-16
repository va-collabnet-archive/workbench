package org.dwfa.ace.task.wfdetailsSheet;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ShowWorkflowDetailsSheetBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that will contain the current profile.");

            PropertyDescriptor show =
                new PropertyDescriptor("show", getBeanDescriptor().getBeanClass());
            show.setBound(true);
            show.setPropertyEditorClass(CheckboxEditor.class);
            show.setDisplayName("<html><font color='green'>Show sheet");
            show.setShortDescription("Choose whether to show the workflow details sheet.");

            PropertyDescriptor rv[] = { profilePropName, show };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowWorkflowDetailsSheet.class);
        bd.setDisplayName("<html><font color='green'><center>Show or Hide <br>"
                + "Workflow Detail<br>Sheet");
        return bd;
    }

}