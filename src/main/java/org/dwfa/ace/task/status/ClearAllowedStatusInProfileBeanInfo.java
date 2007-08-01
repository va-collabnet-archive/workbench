package org.dwfa.ace.task.status;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ClearAllowedStatusInProfileBeanInfo extends SimpleBeanInfo {
   

   public PropertyDescriptor[] getPropertyDescriptors() {
       try {  

           PropertyDescriptor profilePropName =
               new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
           profilePropName.setBound(true);
           profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           profilePropName.setDisplayName("<html><font color='green'>profile prop:");
           profilePropName.setShortDescription("The property that contains the profile to write to disk.");

           PropertyDescriptor rv[] =
               { profilePropName };
           return rv;
       } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }        
   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
       BeanDescriptor bd = new BeanDescriptor(ClearAllowedStatusInProfile.class);
       bd.setDisplayName("<html><font color='green'><center>clear allowed status<br>in profile");
       return bd;
   }
}
