package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.refset.I_RefsetDefaults;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntList;

public class RefsetDefaults implements I_RefsetDefaults, Serializable {

   private I_GetConceptData defaultRefset;
   private I_GetConceptData defaultStatusForRefset;
   private IntList refsetPopupIds = new IntList();
   private IntList statusPopupIds = new IntList();
   
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(defaultRefset.getUids());
      out.writeObject(defaultStatusForRefset.getUids());
      IntList.writeIntList(out, refsetPopupIds);
      IntList.writeIntList(out, statusPopupIds);
   }

   @SuppressWarnings("unchecked")
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         defaultRefset = readConcept(in);
         defaultStatusForRefset = readConcept(in);
         refsetPopupIds = IntList.readIntListIgnoreMapErrors(in);
         statusPopupIds = IntList.readIntListIgnoreMapErrors(in);
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }

   }

   @SuppressWarnings("unchecked")
   protected I_GetConceptData readConcept(ObjectInputStream in) throws IOException, ClassNotFoundException, ToIoException {
      Collection<UUID> uids = (Collection<UUID>) in.readObject();
      try {
         return ConceptBean.get(uids);
      } catch (TerminologyException e) {
         throw new ToIoException(e);
      }
   }

   
   
   public RefsetDefaults() throws TerminologyException, IOException {
         defaultRefset = ConceptBean.get(RefsetAuxiliary.Concept.REFSET_AUXILIARY.getUids());
         refsetPopupIds.add(defaultRefset.getConceptId());
         
         defaultStatusForRefset = ConceptBean.get(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
         statusPopupIds.add(defaultStatusForRefset.getConceptId());
   }


   public I_GetConceptData getDefaultRefset() {
      return defaultRefset;
   }


   public void setDefaultRefset(I_GetConceptData defaultRefset) {
      this.defaultRefset = defaultRefset;
   }


   public I_GetConceptData getDefaultStatusForRefset() {
      return defaultStatusForRefset;
   }


   public void setDefaultStatusForRefset(I_GetConceptData defaultStatusForRefset) {
      this.defaultStatusForRefset = defaultStatusForRefset;
   }


   public I_IntList getRefsetPopupIds() {
      return refsetPopupIds;
   }


   public I_IntList getStatusPopupIds() {
      return statusPopupIds;
   }

}
