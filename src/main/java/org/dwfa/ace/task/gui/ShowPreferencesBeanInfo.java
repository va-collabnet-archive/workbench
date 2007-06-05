package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.dwfa.bpa.tasks.editor.CheckboxEditor;

/**
 * Bean info to ShowPreferences class.
 * @author Christine Hill
 *
 */
public class ShowPreferencesBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ShowPreferencesBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor visible =
                new PropertyDescriptor("visible", ShowPreferences.class);
            visible.setBound(true);
            visible.setPropertyEditorClass(CheckboxEditor.class);
            visible.setDisplayName("<html><font color='green'>Show preferences view");
            visible.setShortDescription("Choose whether to show preferences view.");

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
        BeanDescriptor bd = new BeanDescriptor(ShowPreferences.class);
        bd.setDisplayName("<html><font color='green'><center>Show or Hide<br>"
                + "Preferences View");
        return bd;
    }

}
