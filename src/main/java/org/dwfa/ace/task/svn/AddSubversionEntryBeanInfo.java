package org.dwfa.ace.task.svn;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;

public class AddSubversionEntryBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor prompt =
                new PropertyDescriptor("prompt", getBeanDescriptor().getBeanClass());
            prompt.setBound(true);
            prompt.setPropertyEditorClass(JTextFieldEditor.class);
            prompt.setDisplayName("<html><font color='green'>prompt:");
            prompt.setShortDescription("The prompt to tell the user what type of subversion entry they are making.");

            PropertyDescriptor keyName =
                new PropertyDescriptor("keyName", getBeanDescriptor().getBeanClass());
            keyName.setBound(true);
            keyName.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            keyName.setDisplayName("<html><font color='green'>profile key:");
            keyName.setShortDescription("The key for the subversion entry.");

            PropertyDescriptor rv[] =
                { prompt, keyName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddSubversionEntry.class);
        bd.setDisplayName("<html><font color='green'><center>add subversion entry");
        return bd;
    }
}
