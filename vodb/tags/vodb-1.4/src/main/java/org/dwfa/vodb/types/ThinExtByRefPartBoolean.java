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
package org.dwfa.vodb.types;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartBoolean;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartBoolean extends ThinExtByRefPart implements I_ThinExtByRefPartBoolean {
   private boolean value;

   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartBoolean#getValue()
    */
   public boolean getValue() {
      return value;
   }

   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartBoolean#setValue(boolean)
    */
   public void setValue(boolean value) {
      this.value = value;
   }
   
   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) {
         if (ThinExtByRefPartBoolean.class.isAssignableFrom(obj.getClass())) {
            ThinExtByRefPartBoolean another = (ThinExtByRefPartBoolean) obj;
            return value == another.value;
         }
      }
      return false;
   }

   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartBoolean#getUniversalPart()
    */
   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
      I_TermFactory tf = LocalVersionedTerminology.get();
      UniversalAceExtByRefPartBoolean booleanPart = new UniversalAceExtByRefPartBoolean();
      booleanPart.setBooleanValue(value);
      booleanPart.setPathUid(tf.getUids(getPathId()));
      booleanPart.setStatusUid(tf.getUids(getStatus()));
      booleanPart.setTime(ThinVersionHelper.convert(getVersion()));
      return booleanPart;
   }

   @Override
   public I_ThinExtByRefPart duplicatePart() {
      return new ThinExtByRefPartBoolean(this);
   }

   public ThinExtByRefPartBoolean() {
      super();
   }

   public ThinExtByRefPartBoolean(ThinExtByRefPartBoolean another) {
      super(another);
      this.value = another.value;
   }

}
