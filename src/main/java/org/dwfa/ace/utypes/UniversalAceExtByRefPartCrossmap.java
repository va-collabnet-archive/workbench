package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

public class UniversalAceExtByRefPartCrossmap extends UniversalAceExtByRefPartCrossmapForRel {

    Collection<UUID> mapStatusUid;
    Collection<UUID> targetCodeUid;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
        out.writeObject(mapStatusUid);
       out.writeObject(targetCodeUid);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == dataVersion) {
           mapStatusUid = (Collection<UUID>) in.readObject();
           targetCodeUid = (Collection<UUID>) in.readObject();
       } else {
          throw new IOException("Can't handle dataversion: " + objDataVersion);
       }
    }

    public Collection<UUID> getMapStatusUid() {
        return mapStatusUid;
    }

    public void setMapStatusUid(Collection<UUID> mapStatusUid) {
        this.mapStatusUid = mapStatusUid;
    }

    public Collection<UUID> getTargetCodeUid() {
        return targetCodeUid;
    }

    public void setTargetCodeUid(Collection<UUID> targetCodeUid) {
        this.targetCodeUid = targetCodeUid;
    }

}
