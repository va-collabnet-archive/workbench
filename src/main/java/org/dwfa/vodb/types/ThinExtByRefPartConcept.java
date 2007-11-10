package org.dwfa.vodb.types;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConcept;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartConcept extends ThinExtByRefPart implements I_ThinExtByRefPartConcept {
   private int conceptId;

   public ThinExtByRefPartConcept(ThinExtByRefPartConcept another) {
      super(another);
      this.conceptId = another.conceptId;
   }
   

   public ThinExtByRefPartConcept() {
      super();
   }


   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartConcept#getConceptId()
    */
   public int getConceptId() {
      return conceptId;
   }

   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartConcept#setConceptId(int)
    */
   public void setConceptId(int conceptId) {
      this.conceptId = conceptId;
   }
   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) {
         if (ThinExtByRefPartConcept.class.isAssignableFrom(obj.getClass())) {
            ThinExtByRefPartConcept another = (ThinExtByRefPartConcept) obj;
            return conceptId == another.conceptId;
         }
      }
      return false;
   }
   
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartConcept#getUniversalPart()
    */
   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
      I_TermFactory tf = LocalVersionedTerminology.get();
      UniversalAceExtByRefPartConcept universalPart = new UniversalAceExtByRefPartConcept();
      universalPart.setConceptUid(tf.getUids(getConceptId()));
      universalPart.setPathUid(tf.getUids(getPathId()));
      universalPart.setStatusUid(tf.getUids(getStatus()));
      universalPart.setTime(ThinVersionHelper.convert(getVersion()));
      return universalPart;
   }

   @Override
   public I_ThinExtByRefPart duplicatePart() {
      return new ThinExtByRefPartConcept(this);
   }

   
}
