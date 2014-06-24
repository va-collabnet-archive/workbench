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
* Created on Jun 1, 2005
 */
package org.dwfa.queue.bpa.tasks.move;

//~--- non-JDK imports --------------------------------------------------------


import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import org.ihtsdo.ttk.lookup.InstanceWrapper;
import org.ihtsdo.ttk.lookup.LookupService;

import org.openide.util.Lookup;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import java.util.Collection;
import java.util.List;
import org.ihtsdo.ttk.queue.QueueAddress;

/**
 * Presents a dialog box to the user to select a destination queue. Sets the
 * destination address of the process that directly contains this task to the
 * selected destination.
 *
 * @author kec
 *
 */
@BeanList(specs = { @Spec(
   directory = "tasks/queue tasks/set-select",
   type      = BeanType.TASK_BEAN
) })
public class SelectDestination extends AbstractTask {
   private static final long serialVersionUID = 1;
   private static final int  dataVersion      = 1;

   /**
    * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
    *      org.dwfa.bpa.process.I_Work)
    */
   @Override
   public void complete(I_EncodeBusinessProcess process, I_Work worker)
           throws TaskFailedException {

      // Nothing to do.
   }

   /**
    * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
    *      org.dwfa.bpa.process.I_Work)
    */
   @Override
   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
           throws TaskFailedException {
      try {
         Lookup.Template lt = new Lookup.Template<>(I_QueueProcesses.class,
                                 null, null);
         Lookup.Result lookupResult = LookupService.get().lookup(lt);

         List<QueueAddress> addressList = new ArrayList<>();
         
         for (Object item : lookupResult.allItems()) {
            InstanceWrapper<I_QueueProcesses> instance =
               (InstanceWrapper<I_QueueProcesses>) item;
            for (Object property : instance.getInstanceProperties()) {
               if (property instanceof QueueAddress) {
                  QueueAddress address = (QueueAddress) property;
                  addressList.add(address);
               }
            }
         }

         QueueAddress address     =
            (QueueAddress) worker.selectFromList(addressList.toArray(),
               "Select destination", "Select the destination for this process");

         if (address == null) {
            throw new TaskFailedException("No address selected.");
         }

         process.setDestination(address.getAddress());
         process.validateAddresses();

         return Condition.CONTINUE;
      } catch (Exception ex) {
         throw new TaskFailedException(ex);
      }
   }

   @SuppressWarnings("empty-statement")
   private void readObject(java.io.ObjectInputStream in)
           throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion == 1) {
         ;
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
   }

   /**
    * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
    */
   @Override
   public Collection<Condition> getConditions() {
      return CONTINUE_CONDITION;
   }

   /**
    * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
    */
   @Override
   public int[] getDataContainerIds() {
      return new int[] {};
   }
}
