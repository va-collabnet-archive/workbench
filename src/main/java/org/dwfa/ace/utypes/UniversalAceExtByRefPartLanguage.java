package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

public class UniversalAceExtByRefPartLanguage extends UniversalAceExtByRefPart {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private Collection<UUID> acceptabilityUids;
   private Collection<UUID> correctnessUids;
   private Collection<UUID> degreeOfSynonymyUids;

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(acceptabilityUids);
      out.writeObject(correctnessUids);
      out.writeObject(degreeOfSynonymyUids);
   }

   @SuppressWarnings("unchecked")
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         acceptabilityUids = (Collection<UUID>) in.readObject();
         correctnessUids = (Collection<UUID>) in.readObject();
         degreeOfSynonymyUids = (Collection<UUID>) in.readObject();
         
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   public Collection<UUID> getAcceptabilityUids() {
      return acceptabilityUids;
   }

   public void setAcceptabilityUids(Collection<UUID> acceptabilityUids) {
      this.acceptabilityUids = acceptabilityUids;
   }

   public Collection<UUID> getCorrectnessUids() {
      return correctnessUids;
   }

   public void setCorrectnessUids(Collection<UUID> correctnessUids) {
      this.correctnessUids = correctnessUids;
   }

   public Collection<UUID> getDegreeOfSynonymyUids() {
      return degreeOfSynonymyUids;
   }

   public void setDegreeOfSynonymyUids(Collection<UUID> degreeOfSynonymyUids) {
      this.degreeOfSynonymyUids = degreeOfSynonymyUids;
   }

}
