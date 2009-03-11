package org.dwfa.bpa.worker.task;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddGetWorkFromSelectedQueuePluginBeanInfo extends SimpleBeanInfo {
  public PropertyDescriptor[] getPropertyDescriptors() {
    try {  
      PropertyDescriptor workerPropName = new PropertyDescriptor(
          "workerPropName", getBeanDescriptor().getBeanClass());
      workerPropName.setBound(true);
      workerPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
      workerPropName.setDisplayName("<html><font color='green'>worker prop:");
      workerPropName.setShortDescription("The property that holds the worker to add the plugin to.");

        PropertyDescriptor rv[] =
            {workerPropName };
        return rv;
    } catch (Exception e) {
         throw new Error(e.toString());
    } 
 }        


  /**
   * @see java.beans.BeanInfo#getBeanDescriptor()
   */
  public BeanDescriptor getBeanDescriptor() {
    BeanDescriptor bd = new BeanDescriptor(AddGetWorkFromSelectedQueuePlugin.class);
    bd.setDisplayName("<html><font color='green'><center>Add Plugin:<br>I_GetWorkFromQueue<br>(user selected)");
    return bd;
  }

}
