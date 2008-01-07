package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

public class UniversalAceExtByRefPartCrossmapForRel extends UniversalAceExtByRefPart {

    Collection<UUID> refineFlagUid;
    Collection<UUID> additionalCodeUid;
    int elementNo;
    int blockNo;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
       out.writeObject(refineFlagUid);
       out.writeObject(additionalCodeUid);
       out.writeInt(elementNo);
       out.writeInt(blockNo);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == dataVersion) {
           refineFlagUid = (Collection<UUID>) in.readObject();
           additionalCodeUid = (Collection<UUID>) in.readObject();
           elementNo = in.readInt();
           blockNo = in.readInt();
       } else {
          throw new IOException("Can't handle dataversion: " + objDataVersion);
       }
    }

    public Collection<UUID> getRefineFlagUid() {
        return refineFlagUid;
    }

    public void setRefineFlagUid(Collection<UUID> refineFlagUid) {
        this.refineFlagUid = refineFlagUid;
    }

    public Collection<UUID> getAdditionalCodeUid() {
        return additionalCodeUid;
    }

    public void setAdditionalCodeUid(Collection<UUID> additionalCodeUid) {
        this.additionalCodeUid = additionalCodeUid;
    }

    public int getElementNo() {
        return elementNo;
    }

    public void setElementNo(int elementNo) {
        this.elementNo = elementNo;
    }

    public int getBlockNo() {
        return blockNo;
    }

    public void setBlockNo(int blockNo) {
        this.blockNo = blockNo;
    }


}
