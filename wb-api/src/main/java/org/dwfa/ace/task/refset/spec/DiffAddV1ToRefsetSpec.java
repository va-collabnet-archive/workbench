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



package org.dwfa.ace.task.refset.spec;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@BeanList(specs = { @Spec(
   directory = "tasks/refset/spec/diff",
   type      = BeanType.TASK_BEAN
) })
public class DiffAddV1ToRefsetSpec extends AbstractAddRefsetSpecTask {
   private static final int  dataVersion      = 1;
   private static final long serialVersionUID = 1L;

   //~--- methods -------------------------------------------------------------

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion == dataVersion) {

         //
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   protected int getRefsetPartTypeId() throws IOException, TerminologyException {
      int typeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid();

      return typeId;
   }

   @Override
   protected RefsetPropertyMap getRefsetPropertyMap(I_TermFactory tf, I_ConfigAceFrame configFrame)
           throws IOException, TerminologyException {
      RefsetPropertyMap refsetMap = new RefsetPropertyMap(REFSET_TYPES.CID_CID);

      if (getClauseIsTrue()) {
         refsetMap.put(REFSET_PROPERTY.CID_ONE, trueNid);
      } else {
         refsetMap.put(REFSET_PROPERTY.CID_ONE, falseNid);
      }

      int nid = RefsetAuxiliary.Concept.DIFFERENCE_V1_GROUPING.localize().getNid();

      refsetMap.put(REFSET_PROPERTY.CID_TWO, nid);
      refsetMap.put(REFSET_PROPERTY.STATUS, configFrame.getDefaultStatus().getNid());

      return refsetMap;
   }
}
