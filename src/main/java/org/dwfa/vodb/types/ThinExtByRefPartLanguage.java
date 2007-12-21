package org.dwfa.vodb.types;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguage;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartLanguage;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartLanguage extends ThinExtByRefPart implements I_ThinExtByRefPartLanguage {
   private int acceptabilityId;
   private int correctnessId;
   private int degreeOfSynonymyId;
   
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#getAcceptabilityId()
    */
   public int getAcceptabilityId() {
      return acceptabilityId;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#setAcceptabilityId(int)
    */
   public void setAcceptabilityId(int acceptabilityId) {
      this.acceptabilityId = acceptabilityId;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#getCorrectnessId()
    */
   public int getCorrectnessId() {
      return correctnessId;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#setCorrectnessId(int)
    */
   public void setCorrectnessId(int correctnessId) {
      this.correctnessId = correctnessId;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#getDegreeOfSynonymyId()
    */
   public int getDegreeOfSynonymyId() {
      return degreeOfSynonymyId;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#setDegreeOfSynonymyId(int)
    */
   public void setDegreeOfSynonymyId(int degreeOfSynonymyId) {
      this.degreeOfSynonymyId = degreeOfSynonymyId;
   }
   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) {
         if (ThinExtByRefPartLanguage.class.isAssignableFrom(obj.getClass())) {
            ThinExtByRefPartLanguage another = (ThinExtByRefPartLanguage) obj;
            return acceptabilityId == another.acceptabilityId &&
            correctnessId == another.correctnessId &&
            degreeOfSynonymyId == another.degreeOfSynonymyId;
         }
      }
      return false;
   }
   
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#getUniversalPart()
    */
   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
      I_TermFactory tf = LocalVersionedTerminology.get();
      UniversalAceExtByRefPartLanguage universalPart = new UniversalAceExtByRefPartLanguage();
      universalPart.setAcceptabilityUids(tf.getUids(getAcceptabilityId()));
      universalPart.setCorrectnessUids(tf.getUids(getCorrectnessId()));
      universalPart.setDegreeOfSynonymyUids(tf.getUids(getDegreeOfSynonymyId()));
      universalPart.setPathUid(tf.getUids(getPathId()));
      universalPart.setStatusUid(tf.getUids(getStatus()));
      universalPart.setTime(ThinVersionHelper.convert(getVersion()));
      return universalPart;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#duplicatePart()
    */
   @Override
   public I_ThinExtByRefPart duplicatePart() {
      return new ThinExtByRefPartLanguage(this);
   }
   public ThinExtByRefPartLanguage(ThinExtByRefPartLanguage another) {
      super(another);
      this.acceptabilityId = another.acceptabilityId;
      this.correctnessId = another.correctnessId;
      this.degreeOfSynonymyId = another.degreeOfSynonymyId;
   }
   public ThinExtByRefPartLanguage() {
      super();
   }
   public int compareTo(ThinExtByRefPart o) {
       if (ThinExtByRefPartLanguage.class.isAssignableFrom(o.getClass())) {
           ThinExtByRefPartLanguage otherPart = (ThinExtByRefPartLanguage) o;
           return this.acceptabilityId - otherPart.acceptabilityId;
       }
       return 1;
   }

}
