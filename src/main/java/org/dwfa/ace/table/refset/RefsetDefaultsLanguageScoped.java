package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.refset.I_RefsetDefaultsLanguageScoped;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntList;

public class RefsetDefaultsLanguageScoped extends RefsetDefaultsLanguage implements I_RefsetDefaultsLanguageScoped {


   private I_GetConceptData defaultScopeForScopedLanguageRefset;
   private I_IntList scopePopupIds = new IntList();

   private I_GetConceptData defaultTagForScopedLanguageRefset;
   private I_IntList tagPopupIds = new IntList();

   private int defaultPriorityForScopedLanguageRefset;
   private Integer[] priorityPopupItems;

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(defaultScopeForScopedLanguageRefset.getUids());
      IntList.writeIntList(out, scopePopupIds);
      out.writeObject(defaultTagForScopedLanguageRefset.getUids());
      IntList.writeIntList(out, tagPopupIds);
      out.writeInt(defaultPriorityForScopedLanguageRefset);
      out.writeObject(priorityPopupItems);
   }

   @SuppressWarnings("unchecked")
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         defaultScopeForScopedLanguageRefset = readConcept(in);
         scopePopupIds = IntList.readIntListIgnoreMapErrors(in);
         defaultTagForScopedLanguageRefset = readConcept(in);
         tagPopupIds = IntList.readIntListIgnoreMapErrors(in);
         defaultPriorityForScopedLanguageRefset = in.readInt();
         priorityPopupItems = (Integer[]) in.readObject();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }

   }

   public RefsetDefaultsLanguageScoped() throws TerminologyException, IOException {
      super();
      defaultScopeForScopedLanguageRefset = ConceptBean.get(RefsetAuxiliary.Concept.REFSET_AUXILIARY.getUids());
      scopePopupIds.add(defaultScopeForScopedLanguageRefset.getConceptId());
       
      defaultTagForScopedLanguageRefset = ConceptBean.get(RefsetAuxiliary.Concept.REFSET_AUXILIARY.getUids());
      tagPopupIds.add(defaultTagForScopedLanguageRefset.getConceptId());
       
      defaultPriorityForScopedLanguageRefset = 1;
      priorityPopupItems = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
   }

   public int getDefaultPriorityForScopedLanguageRefset() {
      return defaultPriorityForScopedLanguageRefset;
   }

   public void setDefaultPriorityForScopedLanguageRefset(int defaultPriorityForScopedLanguageRefset) {
      this.defaultPriorityForScopedLanguageRefset = defaultPriorityForScopedLanguageRefset;
   }

   public I_GetConceptData getDefaultScopeForScopedLanguageRefset() {
      return defaultScopeForScopedLanguageRefset;
   }

   public void setDefaultScopeForScopedLanguageRefset(I_GetConceptData defaultScopeForScopedLanguageRefset) {
      this.defaultScopeForScopedLanguageRefset = defaultScopeForScopedLanguageRefset;
   }

   public I_GetConceptData getDefaultTagForScopedLanguageRefset() {
      return defaultTagForScopedLanguageRefset;
   }

   public void setDefaultTagForScopedLanguageRefset(I_GetConceptData defaultTagForScopedLanguageRefset) {
      this.defaultTagForScopedLanguageRefset = defaultTagForScopedLanguageRefset;
   }

   public Integer[] getPriorityPopupItems() {
      return priorityPopupItems;
   }

   public void setPriorityPopupItems(Integer[] priorityPopupItems) {
      this.priorityPopupItems = priorityPopupItems;
   }

   public I_IntList getScopePopupIds() {
      return scopePopupIds;
   }

   public void setScopePopupIds(I_IntList scopePopupIds) {
      this.scopePopupIds = scopePopupIds;
   }

   public I_IntList getTagPopupIds() {
      return tagPopupIds;
   }

   public void setTagPopupIds(I_IntList tagPopupIds) {
      this.tagPopupIds = tagPopupIds;
   }

}
