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



package org.dwfa.ace.task.isaCache;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Collection;

/**
 * Opens a Swing file dialog so that user can choose a file location.
 *
 * @author Christine Hill
 *
 */
@BeanList(specs = { @Spec(
   directory = "tasks/isaCache",
   type      = BeanType.TASK_BEAN
) })
public class SetupIsaCache extends AbstractTask {
   private static final int dataVersion = 1;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- methods -------------------------------------------------------------

   public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

      // Nothing to do
   }

   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
      try {
         if (Terms.get().getActiveAceFrameConfig().getViewCoordinate().getIsaCoordinates().size() != 1) {
            throw new TaskFailedException(
                "Only one is-a coordinate allowed. Found: "
                + Terms.get().getActiveAceFrameConfig().getViewCoordinate().getIsaCoordinates());
         }

         Terms.get().setupIsaCache(
             Terms.get().getActiveAceFrameConfig().getViewCoordinate().getIsaCoordinates().iterator().next());
      } catch (Exception e) {
         throw new TaskFailedException(e.getMessage());
      }

      return Condition.CONTINUE;
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion == 1) {}
      else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
   }

   //~--- get methods ---------------------------------------------------------

   public Collection<Condition> getConditions() {
      return AbstractTask.CONTINUE_CONDITION;
   }

   public int[] getDataContainerIds() {
      return new int[] {};
   }
}


//~ Formatted by Jindent --- http://www.jindent.com
