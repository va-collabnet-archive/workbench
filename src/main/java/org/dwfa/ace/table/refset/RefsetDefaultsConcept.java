package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.refset.I_RefsetDefaultsConcept;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntList;

public class RefsetDefaultsConcept extends RefsetDefaults implements I_RefsetDefaultsConcept {

   private I_IntList conceptPopupIds = new IntList();

   private I_GetConceptData defaultForConceptRefset;

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(defaultForConceptRefset.getUids());
      IntList.writeIntList(out, conceptPopupIds);
   }

   @SuppressWarnings("unchecked")
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         defaultForConceptRefset = readConcept(in);
         conceptPopupIds = IntList.readIntListIgnoreMapErrors(in);
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }

   }

   public RefsetDefaultsConcept() throws TerminologyException, IOException {
      super();
      defaultForConceptRefset = ConceptBean.get(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids());
      conceptPopupIds.add(defaultForConceptRefset.getConceptId());
   }

   public I_GetConceptData getDefaultForConceptRefset() {
      return defaultForConceptRefset;
   }

   public void setDefaultForConceptRefset(I_GetConceptData defaultForConceptRefset) {
      this.defaultForConceptRefset = defaultForConceptRefset;
   }

   public I_IntList getConceptPopupIds() {
      return conceptPopupIds;
   }

}
