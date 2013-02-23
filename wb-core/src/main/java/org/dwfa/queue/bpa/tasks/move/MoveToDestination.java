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

import org.ihtsdo.tk.Ts;
import org.ihtsdo.ttk.lookup.InstanceWrapper;
import org.ihtsdo.ttk.lookup.LookupService;
import org.ihtsdo.ttk.queue.QueueAddress;

import org.openide.util.Lookup;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.Collection;
import java.util.logging.Level;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

/**
 * @author Susan Castillo
 *         <p>
 *         Moves a process to the electronic address.
 */
@BeanList(specs = { @Spec(
   directory = "tasks/queue tasks/move-to",
   type      = BeanType.TASK_BEAN
) })
public class MoveToDestination extends AbstractTask {
   private static final long serialVersionUID = 1;
   private static final int  dataVersion      = 1;

   /**
    * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
    *      org.dwfa.bpa.process.I_Work)
    */
   @Override
   public void complete(I_EncodeBusinessProcess process, I_Work worker)
           throws TaskFailedException {
      try {
         worker.getLogger().log(Level.INFO,
                                "Moving process {0} to destination: {1}",
                                new Object[] { process.getProcessID(),
                 process.getDestination() });

         Lookup.Template lt = new Lookup.Template<>(I_QueueProcesses.class,
                                 null, null);
         Lookup.Result lookupResult = LookupService.get().lookup(lt);

         for (Object item : lookupResult.allItems()) {
            InstanceWrapper<I_QueueProcesses> instance =
               (InstanceWrapper<I_QueueProcesses>) item;

            for (Object property : instance.getInstanceProperties()) {
               if (property instanceof QueueAddress) {
                  QueueAddress address = (QueueAddress) property;

                  if (address.getAddress().equalsIgnoreCase(
                          process.getDestination())) {
                     instance.getInstance().write(
                         process, worker.getActiveTransaction());
                     worker.getLogger().log(Level.INFO,
                                            "Moved process {0} to queue: {1}",
                                            new Object[] {
                                               process.getProcessID(),
                                               instance.getDisplayName() });

                     return;
                  }
               }
            }
         }

         throw new TaskFailedException(
             "No queue with the specified address could be found: "
             + process.getDestination());
      } catch (SystemException | IllegalStateException | NotSupportedException
               | IOException e) {
         throw new TaskFailedException(e);
      }
   }

   /**
    * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
    *      org.dwfa.bpa.process.I_Work)
    */
   @Override
   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
           throws TaskFailedException {
      try {
         process.setDbDependencies(Ts.get().getLatestChangeSetDependencies());
      } catch (IOException e) {
         throw new TaskFailedException(e);
      }

      return Condition.STOP;
   }

   private void readObject(java.io.ObjectInputStream in)
           throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion == 1) {}
      else {
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
      return AbstractTask.STOP_CONDITION;
   }

   /**
    * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
    */
   @Override
   public int[] getDataContainerIds() {
      return new int[] {};
   }
}
