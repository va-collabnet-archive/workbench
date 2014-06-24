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

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.ttk.lookup.InstanceWrapper;
import org.ihtsdo.ttk.lookup.LookupService;
import org.ihtsdo.ttk.queue.QueueAddress;

import org.openide.util.Lookup;

//~--- JDK imports ------------------------------------------------------------

import java.awt.HeadlessException;

import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

/**
 * @author kec
 *
 */
@BeanList(specs = { @Spec(
   directory = "tasks/queue tasks/move-to",
   type      = BeanType.TASK_BEAN
) })
public class ToUserSelectedQueue extends AbstractTask {
   private static final long          serialVersionUID = 1;
   private static final int           dataVersion      = 2;
   private TermEntry                  queueType        = TermEntry.getQueueType();
   private String                     message          = "";
   private transient I_QueueProcesses q;

   /**
    *
    */
   public ToUserSelectedQueue() {
      super();
   }

   /**
    * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
    *      org.dwfa.bpa.process.I_Work)
    */
   @Override
   public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
      try {
         getLogger().info("Starting complete, getting transaction.");

         Transaction t = worker.getActiveTransaction();

         getLogger().log(Level.INFO, "Got transaction: {0}", t);
         q.write(process, t);
         getLogger().info("Written to queue.");
      } catch (SystemException | IllegalStateException | NotSupportedException | IOException e) {
         throw new TaskFailedException(e);
      }
   }

   /**
    * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
    *      org.dwfa.bpa.process.I_Work)
    */
   @Override
   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
      try {
         Lookup.Template    lt           = new Lookup.Template<>(I_QueueProcesses.class, null, null);
         Lookup.Result      lookupResult = LookupService.get().lookup(lt);
         List<QueueAddress> addressList  = new ArrayList<>();

         for (Object item : lookupResult.allItems()) {
            InstanceWrapper<I_QueueProcesses> instance = (InstanceWrapper<I_QueueProcesses>) item;

            for (Object property : instance.getInstanceProperties()) {
               if (property instanceof QueueAddress) {
                  QueueAddress address = (QueueAddress) property;

                  addressList.add(address);
               }
            }
         }

         QueueAddress address = (QueueAddress) worker.selectFromList(addressList.toArray(),
                                   "Select destination", "Select the destination for this process");

         if (address == null) {
            throw new TaskFailedException("No address selected.");
         }

         worker.getLogger().log(Level.INFO, "Moving process {0} to destination: {1}",
                                new Object[] { process.getProcessID(),
             process.getDestination() });

         for (Object item : lookupResult.allItems()) {
            InstanceWrapper<I_QueueProcesses> instance = (InstanceWrapper<I_QueueProcesses>) item;

            for (Object property : instance.getInstanceProperties()) {
               if (property instanceof QueueAddress) {
                  QueueAddress queueAddress = (QueueAddress) property;

                  if (queueAddress.getAddress().equalsIgnoreCase(address.getAddress())) {
                     q = instance.getInstance();

                     break;
                  }
               }
            }
         }

         try {
            process.setDbDependencies(Ts.get().getLatestChangeSetDependencies());
         } catch (IOException e) {
            throw new TaskFailedException(e);
         }

         if ((message != null) &&!message.equals("")) {
            JFrame parentFrame = null;

            for (JFrame frame : OpenFrames.getFrames()) {
               if (frame.isActive()) {
                  parentFrame = frame;

                  break;
               }
            }

            JOptionPane.showMessageDialog(parentFrame, message);
         }

         return Condition.STOP;
      } catch (TaskFailedException | HeadlessException e) {
         throw new TaskFailedException(e);
      }
   }

   private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion >= 1) {
         this.queueType = (TermEntry) in.readObject();
      }

      if (objDataVersion >= 2) {
         message = (String) in.readObject();
      } else {
         message = "";
      }
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(this.queueType);
      out.writeObject(message);
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

   public String getMessage() {
      return message;
   }

   /**
    * @return Returns the queueType.
    */
   public TermEntry getQueueType() {
      return queueType;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public void setQueueType(TermEntry elementId) {
      TermEntry oldValue = this.queueType;

      this.queueType = elementId;
      this.firePropertyChange("queueType", oldValue, this.queueType);

      if (this.getLogger().isLoggable(Level.FINE)) {
         this.getLogger().log(Level.FINE, "setQueueType to: {0}", elementId);
      }
   }
}
