package org.dwfa.ace.task.svn;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class ModifyAllSvnEntriesBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor repoUrl =
                new PropertyDescriptor("repoUrl", getBeanDescriptor().getBeanClass());
            repoUrl.setBound(true);
            repoUrl.setPropertyEditorClass(JTextFieldEditor.class);
            repoUrl.setDisplayName("<html><font color='green'>repoUrl:");
            repoUrl.setShortDescription("The URL of the repository to be set.");

            PropertyDescriptor rv[] = { repoUrl };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ModifyAllSvnEntries.class);
        bd.setDisplayName("<html><font color='green'><center>Modify All Svn Entries");
        return bd;
    }
	
}
