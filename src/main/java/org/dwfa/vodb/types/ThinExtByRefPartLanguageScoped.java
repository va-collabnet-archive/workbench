package org.dwfa.vodb.types;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartScopedLanguage;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartLanguageScoped extends ThinExtByRefPartLanguage {
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
   
   public int getPriority() {
      return priority;
   }
   public void setPriority(int priority) {
      this.priority = priority;
   }
   public int getScopeId() {
      return scopeId;
   }
   public void setScopeId(int scopeId) {
      this.scopeId = scopeId;
   }
   public int getTagId() {
      return tagId;
   }
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

   @Override
   public ThinExtByRefPart duplicatePart() {
      return new ThinExtByRefPartLanguageScoped(this);
   }

}
