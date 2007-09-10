package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

public class UniversalAceExtByRefPartConcept extends UniversalAceExtByRefPart {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private Collection<UUID> conceptUid;

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(conceptUid);
   }

   @SuppressWarnings("unchecked")
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         conceptUid = (Collection<UUID>) in.readObject();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   public Collection<UUID> getConceptUid() {
      return conceptUid;
   }

   public void setConceptUid(Collection<UUID> conceptUid) {
      this.conceptUid = conceptUid;
   }

}
