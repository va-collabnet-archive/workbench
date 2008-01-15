
package org.dwfa.ace.task.path;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;
import org.dwfa.queue.bpa.tasks.move.QueueTypeEditor;



public class NewPathBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile property:");
            profilePropName.setShortDescription("The property containing the profile to set the editing path to the created path.");


            PropertyDescriptor parentPathTermEntry =
                new PropertyDescriptor("parentPathTermEntry", getBeanDescriptor().getBeanClass());
            parentPathTermEntry.setBound(true);
            parentPathTermEntry.setPropertyEditorClass(QueueTypeEditor.class);
            parentPathTermEntry.setDisplayName("<html><font color='green'>path parent:");
            parentPathTermEntry.setShortDescription("The parent for the new editing path.");
            
            
            PropertyDescriptor originPathTermEntry =
                new PropertyDescriptor("originPathTermEntry", getBeanDescriptor().getBeanClass());
            originPathTermEntry.setBound(true);
            originPathTermEntry.setPropertyEditorClass(QueueTypeEditor.class);
            originPathTermEntry.setDisplayName("<html><font color='green'>origin Path:");
            originPathTermEntry.setShortDescription("The parent for the new editing path.");

            PropertyDescriptor originTime =
                new PropertyDescriptor("originTime", getBeanDescriptor().getBeanClass());
            originTime.setBound(true);
            originTime.setPropertyEditorClass(PropertyNameLabelEditor.class);
            originTime.setDisplayName("<html><font color='green'>origin time:");
            originTime.setShortDescription("The origin time in yyyy.MM.dd HH:mm:ss format or 'latest' for the latest change on the path.");
 
            PropertyDescriptor pathDescription =
                new PropertyDescriptor("DescriptionForNewPath", getBeanDescriptor().getBeanClass());
            pathDescription.setBound(true);
            pathDescription.setPropertyEditorClass(JTextFieldEditor.class);
            pathDescription.setDisplayName("<html><font color='green'>Path Description");
            pathDescription.setShortDescription("Description For NewPath");
 
            PropertyDescriptor rv[] =
                {  parentPathTermEntry,originPathTermEntry, originTime, profilePropName,pathDescription};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(NewPath.class);
        bd.setDisplayName("<html><font color='green'><center>new path");
        return bd;
    }
}
