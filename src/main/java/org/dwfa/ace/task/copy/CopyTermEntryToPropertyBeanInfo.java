package org.dwfa.ace.task.copy;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;
import org.dwfa.queue.bpa.tasks.move.QueueTypeEditor;

public class CopyTermEntryToPropertyBeanInfo extends SimpleBeanInfo {

   
   public PropertyDescriptor[] getPropertyDescriptors() {
       try {  
           PropertyDescriptor termEntry =
               new PropertyDescriptor("termEntry", getBeanDescriptor().getBeanClass());
           termEntry.setBound(true);
           termEntry.setPropertyEditorClass(QueueTypeEditor.class);
           termEntry.setDisplayName("concept:");
           termEntry.setShortDescription("The concept to copy to the property.");

           PropertyDescriptor propertyName =
               new PropertyDescriptor("propertyName", getBeanDescriptor().getBeanClass());
           propertyName.setBound(true);
           propertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           propertyName.setDisplayName("<html><font color='green'>concept property:");
           propertyName.setShortDescription("Name of the property to copy the concept to to. ");

           PropertyDescriptor rv[] =
               { termEntry, propertyName };
           return rv;
       } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }        
   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
       BeanDescriptor bd = new BeanDescriptor(CopyTermEntryToProperty.class);
       bd.setDisplayName("<html><font color='green'><center>copy term entry<br>to property");
       return bd;
   }
}
