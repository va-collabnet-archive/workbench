package org.dwfa.ace.task.copy;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class CopyConceptFromViewsInProfileToPathWithStatusBeanInfo extends SimpleBeanInfo {

   
   public PropertyDescriptor[] getPropertyDescriptors() {
       try {  

          PropertyDescriptor profilePropName =
             new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
          profilePropName.setBound(true);
          profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
          profilePropName.setDisplayName("<html><font color='green'>profile prop:");
          profilePropName.setShortDescription("Name of the property to hold the profile from which the view positions are derived. ");

         PropertyDescriptor toPathPropName =
            new PropertyDescriptor("toPathPropName", getBeanDescriptor().getBeanClass());
         toPathPropName.setBound(true);
         toPathPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
         toPathPropName.setDisplayName("<html><font color='green'>to path:");
         toPathPropName.setShortDescription("Name of the property to copy the concept to. ");

         PropertyDescriptor conceptPropName =
            new PropertyDescriptor("conceptPropName", getBeanDescriptor().getBeanClass());
         conceptPropName.setBound(true);
         conceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
         conceptPropName.setDisplayName("<html><font color='green'>concept:");
         conceptPropName.setShortDescription("Name of the property holding the concept to copy. ");

         PropertyDescriptor statusPropName =
            new PropertyDescriptor("statusPropName", getBeanDescriptor().getBeanClass());
         statusPropName.setBound(true);
         statusPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
         statusPropName.setDisplayName("<html><font color='green'>status:");
         statusPropName.setShortDescription("Name of the property holding the new concept status. ");

           PropertyDescriptor rv[] =
               { profilePropName, toPathPropName, conceptPropName, statusPropName };
           return rv;
       } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }        
   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
       BeanDescriptor bd = new BeanDescriptor(CopyConceptFromViewsInProfileToPathWithStatus.class);
       bd.setDisplayName("<html><font color='green'><center>copy concept to path<br>from views in profile<br>with status");
       return bd;
   }
}
