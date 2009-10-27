package org.dwfa.vodb.types;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartInteger;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartInteger extends ThinExtByRefPart implements I_ThinExtByRefPartInteger {
   public int value;

	public ArrayIntList getPartComponentNids() {
		ArrayIntList partComponentNids = new ArrayIntList(2);
		partComponentNids.add(getPathId());
		partComponentNids.add(getStatusId());
		return partComponentNids;
	}

	/* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartInteger#getValue()
    */
   @Deprecated
   public int getValue() {
      return getIntValue();
   }

   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartInteger#setValue(int)
    */
   @Deprecated
   public void setValue(int value) {
      setIntValue(value);      
   }
   
   public int getIntValue() {
      return value;
   }

   public void setIntValue(int value) {
      this.value = value;
   }
   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) {
         if (ThinExtByRefPartInteger.class.isAssignableFrom(obj.getClass())) {
            ThinExtByRefPartInteger another = (ThinExtByRefPartInteger) obj;
            return value == another.value;
         }
      }
      return false;
   }
   
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartInteger#getUniversalPart()
    */
   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
      I_TermFactory tf = LocalVersionedTerminology.get();
      UniversalAceExtByRefPartInteger universalPart = new UniversalAceExtByRefPartInteger();
      universalPart.setIntValue(value);
      universalPart.setPathUid(tf.getUids(getPathId()));
      universalPart.setStatusUid(tf.getUids(getStatusId()));
      universalPart.setTime(ThinVersionHelper.convert(getVersion()));
      return universalPart;
   }

   public I_ThinExtByRefPart duplicate() {
      return new ThinExtByRefPartInteger(this);
   }

   public ThinExtByRefPartInteger(ThinExtByRefPartInteger another) {
      super(another);
      this.value = another.value;
   }

   public ThinExtByRefPartInteger() {
      super();
   }

   public int compareTo(I_ThinExtByRefPart o) {
       if (ThinExtByRefPartInteger.class.isAssignableFrom(o.getClass())) {
           ThinExtByRefPartInteger otherPart = (ThinExtByRefPartInteger) o;
           return this.value - otherPart.value;
       }
       return 1;
   }

}
