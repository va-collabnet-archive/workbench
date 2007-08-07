package org.dwfa.ace.task.conflict;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;
import org.dwfa.queue.bpa.tasks.move.QueueTypeEditor;

public class PutCompletedConceptsWithNoConflictsInListViewBeanInfo extends SimpleBeanInfo {
   

   public PropertyDescriptor[] getPropertyDescriptors() {
       try {  

           PropertyDescriptor profilePropName =
               new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
           profilePropName.setBound(true);
           profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           profilePropName.setDisplayName("<html><font color='green'>profile prop:");
           profilePropName.setShortDescription("The property that contains the profile used to determine if conflicts are present.");

           PropertyDescriptor statusTermEntry =
              new PropertyDescriptor("statusTermEntry", getBeanDescriptor().getBeanClass());
           statusTermEntry.setBound(true);
           statusTermEntry.setPropertyEditorClass(QueueTypeEditor.class);
           statusTermEntry.setDisplayName("completion status:");
           statusTermEntry.setShortDescription("<html><font color='green'>The status of the concept when an assignment is complete, and the concept is still active.");

          PropertyDescriptor retiredTermEntry =
             new PropertyDescriptor("retiredTermEntry", getBeanDescriptor().getBeanClass());
         retiredTermEntry.setBound(true);
         retiredTermEntry.setPropertyEditorClass(QueueTypeEditor.class);
         retiredTermEntry.setDisplayName("retired status:");
         retiredTermEntry.setShortDescription("<html><font color='green'>The status of the concept when an assignment is complete, and the concept is flagged for retirement.");

           PropertyDescriptor rv[] =
               { profilePropName, statusTermEntry, retiredTermEntry };
           return rv;
       } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }        
   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
       BeanDescriptor bd = new BeanDescriptor(PutCompletedConceptsWithNoConflictsInListView.class);
       bd.setDisplayName("<html><font color='green'><center>put completed concepts<br>with no conflicts<br>in list view");
       return bd;
   }
}
