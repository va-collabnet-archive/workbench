package org.ihtsdo.mojo.maven.rf1;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.dwfa.util.id.Type5UuidFactory;

public class Rf1SubsetId {
    private long subsetSctIdOriginal; // SCTID
    private String subsetUuidFromName;
    private UUID subsetPathUuid;
    private String subsetPathUuidStr;
    private UUID subsetRefsetUuid;
    private String subsetRefsetUuidStr;

    public Rf1SubsetId(long originalId, String uuidName) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        this.subsetSctIdOriginal = originalId;
        setSubsetUuidFromName(uuidName);
    }

    public long getSubsetSctIdOriginal() {
        return subsetSctIdOriginal;
    }

    public void setSubsetSctIdOriginal(long subsetSctIdOriginal) {
        this.subsetSctIdOriginal = subsetSctIdOriginal;
    }

    public String getSubsetPathUuidStr() {
        return subsetPathUuidStr;
    }

    public String getSubsetRefsetUuidStr() {
        return subsetRefsetUuidStr;
    }

    public String getSubsetUuidFromName() {
        return subsetUuidFromName;
    }

    public void setSubsetUuidFromName(String name) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        this.subsetUuidFromName = name;
        this.subsetPathUuid = Type5UuidFactory
                .get(Rf1Dir.SUBSETPATH_ID_NAMESPACE_UUID_TYPE1 + name);
        this.subsetRefsetUuid = Type5UuidFactory.get(Rf1Dir.SUBSETREFSET_ID_NAMESPACE_UUID_TYPE1
                + name);
        
        subsetPathUuidStr = subsetPathUuid.toString();
        subsetRefsetUuidStr = subsetRefsetUuid.toString();
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        sb.append(subsetSctIdOriginal + " (SCTID), ");
        sb.append(subsetUuidFromName + " (NAME), ");
        sb.append(subsetPathUuid + " (PATH), ");
        sb.append(subsetRefsetUuid + " (REFSET)");
       
        return sb.toString();
    }

}
