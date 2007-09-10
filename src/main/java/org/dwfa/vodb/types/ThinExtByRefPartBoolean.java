package org.dwfa.vodb.types;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartBoolean;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartBoolean extends ThinExtByRefPart {
   private boolean value;

   public boolean getValue() {
      return value;
   }

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

}
