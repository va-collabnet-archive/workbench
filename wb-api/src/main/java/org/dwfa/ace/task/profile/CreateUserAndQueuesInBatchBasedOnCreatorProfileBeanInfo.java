/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.dwfa.ace.task.profile;

//~--- non-JDK imports --------------------------------------------------------


//~--- JDK imports ------------------------------------------------------------

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 *
 * @author kec
 */
public class CreateUserAndQueuesInBatchBasedOnCreatorProfileBeanInfo extends SimpleBeanInfo {

   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
    @Override
   public BeanDescriptor getBeanDescriptor() {
      BeanDescriptor bd = new BeanDescriptor(CreateUserAndQueuesInBatchBasedOnCreatorProfile.class);

      bd.setDisplayName(
          "<html><font color='green'><center>Batch Create<br>User Path,<br>User Concept, <br>and User Queues<br>Based on Creator");

      return bd;
   }

    @Override
   public PropertyDescriptor[] getPropertyDescriptors() {
      PropertyDescriptor rv[] = {};

      return rv;
   }
}
