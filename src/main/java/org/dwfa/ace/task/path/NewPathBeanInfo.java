
package org.dwfa.ace.task.path;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;


public class NewPathBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile property:");
            profilePropName.setShortDescription("The property containing the profile which will be set to view and edit the newly created path. Leave blank to ignore.");
            
            PropertyDescriptor parentPathTermEntry =
                new PropertyDescriptor("parentPathTermEntry", getBeanDescriptor().getBeanClass());
            parentPathTermEntry.setBound(true);
            parentPathTermEntry.setPropertyEditorClass(ConceptLabelEditor.class);
            parentPathTermEntry.setDisplayName("<html><font color='green'>path parent:");
            parentPathTermEntry.setShortDescription("The parent for the new editing path.");
            
 
            PropertyDescriptor pathDescription =
                new PropertyDescriptor("PathDescription", getBeanDescriptor().getBeanClass());
            pathDescription.setBound(true);
            pathDescription.setPropertyEditorClass(PropertyNameLabelEditor.class);
            pathDescription.setDisplayName("<html><font color='green'>Path Description");
            pathDescription.setShortDescription("Description For NewPath");
 
            PropertyDescriptor newConceptPropName =
                new PropertyDescriptor("newConceptPropName", getBeanDescriptor().getBeanClass());
            newConceptPropName.setBound(true);
            newConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            newConceptPropName.setDisplayName("<html><font color='green'>new path concept property:");
            newConceptPropName.setShortDescription("The property which will be set to contain the new path concept");
            
            PropertyDescriptor rv[] =
                { parentPathTermEntry, profilePropName, pathDescription, newConceptPropName};
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
