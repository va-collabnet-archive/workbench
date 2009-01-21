package org.dwfa.ace.task.svn;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;

public class AddSubversionEntryAndQueueEntryBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor repoUrl =
                new PropertyDescriptor("repoUrl", getBeanDescriptor().getBeanClass());
            repoUrl.setBound(true);
            repoUrl.setPropertyEditorClass(JTextFieldEditor.class);
            repoUrl.setDisplayName("<html><font color='green'>repoUrl:");
            repoUrl.setShortDescription("The URL of the repository to checkout.");

            PropertyDescriptor workingCopy =
                new PropertyDescriptor("workingCopy", getBeanDescriptor().getBeanClass());
            workingCopy.setBound(true);
            workingCopy.setPropertyEditorClass(JTextFieldEditor.class);
            workingCopy.setDisplayName("<html><font color='green'>working copy:");
            workingCopy.setShortDescription("The local directory to hold the working copy.");

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
                { prompt, keyName, repoUrl, workingCopy };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddSubversionEntryAndQueueEntry.class);
        bd.setDisplayName("<html><font color='green'><center>add subversion entry<br>and queue entry<br>from selected directory");
        return bd;
    }
}
