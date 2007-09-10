package org.dwfa.vodb.types;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConcept;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartConcept extends ThinExtByRefPart {
   private int conceptId;

   public int getConceptId() {
      return conceptId;
   }

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

}
