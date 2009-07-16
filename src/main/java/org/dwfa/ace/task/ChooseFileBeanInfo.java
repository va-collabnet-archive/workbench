package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info to ChooseFile class.
 * 
 * @author Christine Hill
 * 
 */
public class ChooseFileBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ChooseFileBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor fileKey = new PropertyDescriptor("fileKey",
                    getBeanDescriptor().getBeanClass());
            fileKey.setBound(true);
            fileKey.setPropertyEditorClass(PropertyNameLabelEditor.class);
            fileKey
                    .setDisplayName("<html><font color='green'>Name of file key:");
            fileKey.setShortDescription("Name of file key.");

            PropertyDescriptor loadMode = new PropertyDescriptor("loadMode",
                    getBeanDescriptor().getBeanClass());
            loadMode.setBound(true);
            loadMode.setPropertyEditorClass(CheckboxEditor.class);
            loadMode.setDisplayName("<html><font color='green'>Load:");
            loadMode
                    .setShortDescription("Select if loading a file, deselect if saving a file.");

            PropertyDescriptor message = new PropertyDescriptor("message",
                    getBeanDescriptor().getBeanClass());
            message.setBound(true);
            message.setPropertyEditorClass(JTextFieldEditor.class);
            message.setDisplayName("<html><font color='green'>Prompt to user:");
            message.setShortDescription("Message when prompting user for file");

            PropertyDescriptor rv[] = { fileKey, loadMode, message };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChooseFile.class);
        bd.setDisplayName("<html><font color='green'><center>Choose File");
        return bd;
    }

}
