package org.dwfa.vodb.types;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartString;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartString extends ThinExtByRefPart implements I_ThinExtByRefPartString {
    private String stringValue;

    
    @Override
    public boolean equals(Object obj) {
       if (super.equals(obj)) {
          if (ThinExtByRefPartString.class.isAssignableFrom(obj.getClass())) {
              ThinExtByRefPartString another = (ThinExtByRefPartString) obj;
             return stringValue.equals(another.stringValue);
          }
       }
       return false;
    }

    /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartString#getUniversalPart()
    */
   @Override
    public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
       I_TermFactory tf = LocalVersionedTerminology.get();
       UniversalAceExtByRefPartString stringPart = new UniversalAceExtByRefPartString();
       stringPart.setStringValue(stringValue);
       stringPart.setPathUid(tf.getUids(getPathId()));
       stringPart.setStatusUid(tf.getUids(getStatus()));
       stringPart.setTime(ThinVersionHelper.convert(getVersion()));
       return stringPart;
    }

    /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartString#duplicatePart()
    */
   @Override
    public ThinExtByRefPartString duplicatePart() {
       return new ThinExtByRefPartString(this);
    }

    public ThinExtByRefPartString() {
       super();
    }

    public ThinExtByRefPartString(ThinExtByRefPartString another) {
       super(another);
       this.stringValue = another.stringValue;
    }

    /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartString#getStringValue()
    */
   public String getStringValue() {
        return stringValue;
    }

    /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartString#setStringValue(java.lang.String)
    */
   public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

   public int compareTo(I_ThinExtByRefPart o) {
       if (ThinExtByRefPartString.class.isAssignableFrom(o.getClass())) {
           ThinExtByRefPartString otherPart = (ThinExtByRefPartString) o;
           return this.stringValue.compareTo(otherPart.stringValue);
       }
       return 1;
   }

 }
