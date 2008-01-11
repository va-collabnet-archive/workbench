package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UniversalAceExtByRefBean implements I_AmChangeSetObject, Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private Collection<UUID> refsetUid;
   private Collection<UUID> memberUid;
   private Collection<UUID> componentUid;
   private Collection<UUID> typeUid; 
   private List<UniversalAceExtByRefPart> versions = new ArrayList<UniversalAceExtByRefPart>();

   public UniversalAceExtByRefBean(Collection<UUID> refsetUid, Collection<UUID> memberUid, Collection<UUID> componentUid, Collection<UUID> typeUid) {
      super();
      this.refsetUid = refsetUid;
      this.memberUid = memberUid;
      this.componentUid = componentUid;
      this.typeUid = typeUid;
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(refsetUid);
      out.writeObject(memberUid);
      out.writeObject(componentUid);
      out.writeObject(typeUid);
      out.writeObject(versions);
   }

   @SuppressWarnings("unchecked")
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         refsetUid = (Collection<UUID>) in.readObject();
         memberUid = (Collection<UUID>) in.readObject();
         componentUid = (Collection<UUID>) in.readObject();
         typeUid = (Collection<UUID>) in.readObject();
         versions = (List<UniversalAceExtByRefPart>) in.readObject();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }

   }

   public Collection<UUID> getTypeUid() {
      return typeUid;
   }

   public void setTypeUid(Collection<UUID> typeUid) {
      this.typeUid = typeUid;
   }

   public Collection<UUID> getComponentUid() {
      return componentUid;
   }

   public Collection<UUID> getMemberUid() {
      return memberUid;
   }

   public Collection<UUID> getRefsetUid() {
      return refsetUid;
   }

   public List<UniversalAceExtByRefPart> getVersions() {
      return versions;
   }

}
