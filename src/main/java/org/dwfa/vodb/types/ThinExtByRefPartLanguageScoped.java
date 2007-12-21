package org.dwfa.vodb.types;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguageScoped;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartScopedLanguage;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartLanguageScoped extends ThinExtByRefPartLanguage implements I_ThinExtByRefPartLanguageScoped {
   private int scopeId;
   private int priority;
   private int tagId;
   
   public ThinExtByRefPartLanguageScoped() {
      super();
   }
   
   public ThinExtByRefPartLanguageScoped(ThinExtByRefPartLanguageScoped another) {
      super(another);
      this.scopeId = another.scopeId;
      this.priority = another.priority;
      this.tagId = another.tagId;
   }
   
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguageScoped#getPriority()
    */
   public int getPriority() {
      return priority;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguageScoped#setPriority(int)
    */
   public void setPriority(int priority) {
      this.priority = priority;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguageScoped#getScopeId()
    */
   public int getScopeId() {
      return scopeId;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguageScoped#setScopeId(int)
    */
   public void setScopeId(int scopeId) {
      this.scopeId = scopeId;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguageScoped#getTagId()
    */
   public int getTagId() {
      return tagId;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguageScoped#setTagId(int)
    */
   public void setTagId(int tagId) {
      this.tagId = tagId;
   }
   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) {
         if (ThinExtByRefPartLanguageScoped.class.isAssignableFrom(obj.getClass())) {
            ThinExtByRefPartLanguageScoped another = (ThinExtByRefPartLanguageScoped) obj;
            return scopeId == another.scopeId &&
            priority == another.priority &&
            tagId == another.tagId;
         }
      }
      return false;
   }
   
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguageScoped#getUniversalPart()
    */
   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
      I_TermFactory tf = LocalVersionedTerminology.get();
      UniversalAceExtByRefPartScopedLanguage universalPart = new UniversalAceExtByRefPartScopedLanguage();
      universalPart.setAcceptabilityUids(tf.getUids(getAcceptabilityId()));
      universalPart.setCorrectnessUids(tf.getUids(getCorrectnessId()));
      universalPart.setDegreeOfSynonymyUids(tf.getUids(getDegreeOfSynonymyId()));
      universalPart.setScopeUids(tf.getUids(getScopeId()));
      universalPart.setTagUids(tf.getUids(getTagId()));
      universalPart.setPriority(getPriority());
      universalPart.setPathUid(tf.getUids(getPathId()));
      universalPart.setStatusUid(tf.getUids(getStatus()));
      universalPart.setTime(ThinVersionHelper.convert(getVersion()));
      return universalPart;
   }

   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguageScoped#duplicatePart()
    */
   @Override
   public I_ThinExtByRefPart duplicatePart() {
      return new ThinExtByRefPartLanguageScoped(this);
   }
   public int compareTo(ThinExtByRefPart o) {
       if (ThinExtByRefPartLanguageScoped.class.isAssignableFrom(o.getClass())) {
           ThinExtByRefPartLanguageScoped otherPart = (ThinExtByRefPartLanguageScoped) o;
           return this.priority- otherPart.priority;
       }
       return 1;
   }

}
