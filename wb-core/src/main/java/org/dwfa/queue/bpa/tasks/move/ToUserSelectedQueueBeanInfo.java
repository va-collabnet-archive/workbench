/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



/*
* Created on May 16, 2005
 */
package org.dwfa.queue.bpa.tasks.move;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

//~--- JDK imports ------------------------------------------------------------

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * @author kec
 *
 */
public class ToUserSelectedQueueBeanInfo extends SimpleBeanInfo {

   /**
    *
    */
   public ToUserSelectedQueueBeanInfo() {
      super();
   }

   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   @Override
   public BeanDescriptor getBeanDescriptor() {
      BeanDescriptor bd = new BeanDescriptor(ToUserSelectedQueue.class);

      bd.setDisplayName("<html><font color='green'><center>To Selected Queue");

      return bd;
   }

   @Override
   public PropertyDescriptor[] getPropertyDescriptors() {
      try {
         PropertyDescriptor message = new PropertyDescriptor("message", ToUserSelectedQueue.class);

         message.setBound(true);
         message.setPropertyEditorClass(JTextFieldEditor.class);
         message.setDisplayName("message");
         message.setShortDescription("A message to present to the user in a dialog after moving to queue.");

         PropertyDescriptor rv[] = { message };

         return rv;
      } catch (IntrospectionException e) {
         throw new Error(e.toString());
      }
   }
}
