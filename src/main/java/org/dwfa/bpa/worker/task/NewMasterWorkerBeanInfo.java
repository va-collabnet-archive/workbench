package org.dwfa.bpa.worker.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class NewMasterWorkerBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    try {

      PropertyDescriptor workerPropName = new PropertyDescriptor(
          "workerPropName", getBeanDescriptor().getBeanClass());
      workerPropName.setBound(true);
      workerPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
      workerPropName.setDisplayName("<html><font color='green'>worker prop:");
      workerPropName.setShortDescription("The property to hold the new worker once it is created.");

      PropertyDescriptor startupDirectory = new PropertyDescriptor(
          "startupDirectory", getBeanDescriptor().getBeanClass());
      startupDirectory.setBound(true);
      startupDirectory.setPropertyEditorClass(JTextFieldEditorOneLine.class);

      PropertyDescriptor workerName = new PropertyDescriptor(
          "workerName", getBeanDescriptor().getBeanClass());
      workerName.setBound(true);
      workerName.setPropertyEditorClass(JTextFieldEditorOneLine.class);

      PropertyDescriptor rv[] = { workerPropName, startupDirectory, workerName };
      return rv;
    } catch (IntrospectionException e) {
      throw new Error(e.toString());
    }
  }

  /**
   * @see java.beans.BeanInfo#getBeanDescriptor()
   */
  public BeanDescriptor getBeanDescriptor() {
    BeanDescriptor bd = new BeanDescriptor(NewMasterWorker.class);
    bd.setDisplayName("<html><font color='green'><center>New Master Worker");
    return bd;
  }
}
