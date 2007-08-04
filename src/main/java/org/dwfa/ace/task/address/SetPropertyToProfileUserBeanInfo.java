package org.dwfa.ace.task.address;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetPropertyToProfileUserBeanInfo extends SimpleBeanInfo {
   

   public PropertyDescriptor[] getPropertyDescriptors() {
       try {  

           PropertyDescriptor profilePropName =
               new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
           profilePropName.setBound(true);
           profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           profilePropName.setDisplayName("<html><font color='green'>profile prop:");
           profilePropName.setShortDescription("The property that contains the profile to get the user from.");

           PropertyDescriptor userPropName =
               new PropertyDescriptor("userPropName", getBeanDescriptor().getBeanClass());
           userPropName.setBound(true);
           userPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           userPropName.setDisplayName("<html><font color='green'>user prop:");
           userPropName.setShortDescription("The property to put the username into.");

           PropertyDescriptor rv[] =
               { profilePropName, userPropName };
           return rv;
       } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }        
   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
       BeanDescriptor bd = new BeanDescriptor(SetPropertyToProfileUser.class);
       bd.setDisplayName("<html><font color='green'><center>set property<br>to profile user");
       return bd;
   }
}
