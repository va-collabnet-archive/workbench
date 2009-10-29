package org.dwfa.ace.task.status;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddAllowedStatusToProfileBeanInfo extends SimpleBeanInfo {
   

   public PropertyDescriptor[] getPropertyDescriptors() {
       try {  

           PropertyDescriptor profilePropName =
               new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
           profilePropName.setBound(true);
           profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           profilePropName.setDisplayName("<html><font color='green'>profile prop:");
           profilePropName.setShortDescription("The property that contains the profile to modify.");

           PropertyDescriptor allowedStatus =
               new PropertyDescriptor("allowedStatus", getBeanDescriptor().getBeanClass());
           allowedStatus.setBound(true);
           allowedStatus.setPropertyEditorClass(ConceptLabelPropEditor.class);
           allowedStatus.setDisplayName("<html><font color='green'>status:");
           allowedStatus.setShortDescription("The concept to add to the profile as an allowed status.");

           PropertyDescriptor rv[] =
               { profilePropName, allowedStatus };
           return rv;
       } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }        
   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
       BeanDescriptor bd = new BeanDescriptor(AddAllowedStatusToProfile.class);
       bd.setDisplayName("<html><font color='green'><center>add allowed status<br>to profile");
       return bd;
   }
}
