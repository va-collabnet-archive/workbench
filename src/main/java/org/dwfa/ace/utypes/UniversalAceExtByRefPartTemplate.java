package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

public class UniversalAceExtByRefPartTemplate extends UniversalAceExtByRefPartTemplateForRel {

    Collection<UUID> attributeUid;
    Collection<UUID> targetCodeUid;
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
       out.writeObject(attributeUid);
       out.writeObject(targetCodeUid);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == dataVersion) {
           attributeUid = (Collection<UUID>) in.readObject();
           targetCodeUid = (Collection<UUID>) in.readObject();
       } else {
          throw new IOException("Can't handle dataversion: " + objDataVersion);
       }
    }

    public Collection<UUID> getAttributeUid() {
        return attributeUid;
    }

    public void setAttributeUid(Collection<UUID> attributeUid) {
        this.attributeUid = attributeUid;
    }

    public Collection<UUID> getTargetCodeUid() {
        return targetCodeUid;
    }

    public void setTargetCodeUid(Collection<UUID> targetCodeUid) {
        this.targetCodeUid = targetCodeUid;
    }



}
