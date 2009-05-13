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
      booleanPart.setStatusUid(tf.getUids(getStatusId()));
      booleanPart.setTime(ThinVersionHelper.convert(getVersion()));
      return booleanPart;
   }

   public I_ThinExtByRefPart duplicate() {
      return new ThinExtByRefPartBoolean(this);
   }

   public ThinExtByRefPartBoolean() {
      super();
   }

   public ThinExtByRefPartBoolean(ThinExtByRefPartBoolean another) {
      super(another);
      this.value = another.value;
   }

    public int compareTo(I_ThinExtByRefPart o) {
        if (ThinExtByRefPartBoolean.class.isAssignableFrom(o.getClass())) {
            ThinExtByRefPartBoolean otherPart = (ThinExtByRefPartBoolean) o;
            if (this.value == otherPart.value) {
                return 0;
            } else if (this.value == true) {
                return 1;
            }
        }
        return 1;
    }

}
