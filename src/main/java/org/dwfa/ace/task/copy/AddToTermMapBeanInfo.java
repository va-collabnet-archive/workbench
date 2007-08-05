package org.dwfa.ace.task.copy;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;
import org.dwfa.queue.bpa.tasks.move.QueueTypeEditor;

public class AddToTermMapBeanInfo extends SimpleBeanInfo {

   
   public PropertyDescriptor[] getPropertyDescriptors() {
       try {  
          PropertyDescriptor valueTermEntry =
             new PropertyDescriptor("valueTermEntry", getBeanDescriptor().getBeanClass());
          valueTermEntry.setBound(true);
          valueTermEntry.setPropertyEditorClass(QueueTypeEditor.class);
          valueTermEntry.setDisplayName("concept value:");
          valueTermEntry.setShortDescription("The value for the map.");

         PropertyDescriptor keyTermEntry =
            new PropertyDescriptor("keyTermEntry", getBeanDescriptor().getBeanClass());
        keyTermEntry.setBound(true);
        keyTermEntry.setPropertyEditorClass(QueueTypeEditor.class);
        keyTermEntry.setDisplayName("concept key:");
        keyTermEntry.setShortDescription("The key for the map.");

           PropertyDescriptor mapPropName =
               new PropertyDescriptor("mapPropName", getBeanDescriptor().getBeanClass());
           mapPropName.setBound(true);
           mapPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           mapPropName.setDisplayName("<html><font color='green'>concept property:");
           mapPropName.setShortDescription("Name of the property to copy the concept to to. ");

           PropertyDescriptor rv[] =
               { keyTermEntry, valueTermEntry, mapPropName };
           return rv;
       } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }        
   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
       BeanDescriptor bd = new BeanDescriptor(AddToTermMap.class);
       bd.setDisplayName("<html><font color='green'><center>add to term map");
       return bd;
   }
}
