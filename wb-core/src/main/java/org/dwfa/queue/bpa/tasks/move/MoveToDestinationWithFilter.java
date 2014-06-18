/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */



package org.dwfa.queue.bpa.tasks.move;

//~--- non-JDK imports --------------------------------------------------------


import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.queue.SelectAllWithSatisfiedDbConstraints;
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
import java.util.UUID;
import java.util.logging.Level;

/**
 * Moves a process for batch member submission to the electronic address.
 * Filters concepts that already exist and processes that are already in the
 * queue.
 */
@BeanList(specs = { @Spec(
   directory = "tasks/queue tasks/move-to",
   type      = BeanType.TASK_BEAN
) })
public class MoveToDestinationWithFilter extends AbstractTask {
   private static final long serialVersionUID = 1;
   private static final int  dataVersion      = 1;

   /**
    * @see
    * org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
    * org.dwfa.bpa.process.I_Work)
    */
   public void complete(I_EncodeBusinessProcess process, I_Work worker)
           throws TaskFailedException {
      try {
         Lookup.Template lt = new Lookup.Template<>(I_QueueProcesses.class,
                                 null, null);
         Lookup.Result    lookupResult = LookupService.get().lookup(lt);
         I_QueueProcesses q            = null;

         worker.getLogger().log(Level.INFO,
                                "Moving process {0} to destination: {1}",
                                new Object[] { process.getProcessID(),
                 process.getDestination() });

         for (Object item : lookupResult.allItems()) {
            InstanceWrapper<I_QueueProcesses> instance =
               (InstanceWrapper<I_QueueProcesses>) item;

            for (Object property : instance.getInstanceProperties()) {
               if (property instanceof QueueAddress) {
                  QueueAddress address = (QueueAddress) property;

                  if (address.getAddress().equalsIgnoreCase(
                          process.getDestination())) {
                     q = instance.getInstance();

                     break;
                  }
               }
            }

            if (q != null) {
               break;
            }
         }

         if (q == null) {
            throw new TaskFailedException(
                "No queue with the specified address could be found: "
                + process.getDestination());
         }

         String subject = process.getSubject();

         // check if concept exists
         String[] parts      = subject.split("\\t");
         String   uuidString = parts[0];

         if (uuidString.length() == 32) {
            uuidString = uuidString.toLowerCase();

            // split UUID into parts 8-4-4-4-12 and insert dashes
            String one   = uuidString.substring(0, 8);
            String two   = uuidString.substring(8, 12);
            String three = uuidString.substring(12, 16);
            String four  = uuidString.substring(16, 20);
            String five  = uuidString.substring(20, 32);

            uuidString = one + "-" + two + "-" + three + "-" + four + "-"
                         + five;
         }

         if (!Ts.get().hasUuid(UUID.fromString(uuidString)) || Ts.get().getConcept(UUID.fromString(uuidString)).isCanceled()) {

            // check if inbox already contains entry
            Collection<I_DescribeBusinessProcess> entries =
               q.getProcessMetaData(new SelectAllWithSatisfiedDbConstraints());

            if (entries.isEmpty()) {

               // move to destination
               q.write(process, worker.getActiveTransaction());
               worker.getLogger().info("Moved process "
                                       + process.getProcessID() + " to queue: "
                                       + q.getNodeInboxAddress());
            } else {
               boolean add = true;

               for (I_DescribeBusinessProcess entry : entries) {
                  if (entry.getSubject().equals(subject)) {
                     add = false;
                  }
               }

               if (add) {

                  // move to destination
                  q.write(process, worker.getActiveTransaction());
                  worker.getLogger().info("Moved process "
                                          + process.getProcessID()
                                          + " to queue: "
                                          + q.getNodeInboxAddress());
               } else {
                  worker.getLogger().info(
                      "NOT ADDING. Entry already exists in queue. "
                      + process.getSubject());
               }
            }
         } else {
            worker.getLogger().info("NOT ADDING. Concept already exists. "
                                    + process.getSubject());
         }
      } catch (Exception e) {
         throw new TaskFailedException(e);
      }
   }

   /**
    * @see
    * org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
    * org.dwfa.bpa.process.I_Work)
    */
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
   public Collection<Condition> getConditions() {
      return AbstractTask.STOP_CONDITION;
   }

   /**
    * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
    */
   public int[] getDataContainerIds() {
      return new int[] {};
   }
}
