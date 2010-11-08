package org.ihtsdo.mojo.maven.rf1;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.dwfa.util.id.Type5UuidFactory;

public class Rf1SubsetId {
    private long subsetSctIdOriginal; // SCTID
    private UUID subsetRefsetUuid;
    private String subsetRefsetUuidStr;

    private String subsetPathUuidFromName; // Subset path name
    private UUID subsetPathUuid;
    private String subsetPathUuidStr;

    private String refsetFsName;
    private String refsetPrefName;

    private String refsetParentUuid;
    private String refsetDate; // 

    public Rf1SubsetId() throws NoSuchAlgorithmException, UnsupportedEncodingException {
    }

    public long getSubsetSctIdOriginal() {
        return subsetSctIdOriginal;
    }

    public void setSctIdOriginal(long subsetSctIdOriginal) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        this.subsetSctIdOriginal = subsetSctIdOriginal;
        this.subsetRefsetUuid = Type5UuidFactory.get(Rf1Dir.SUBSETREFSET_ID_NAMESPACE_UUID_TYPE1
                + Long.toString(subsetSctIdOriginal));
        this.subsetRefsetUuidStr = subsetRefsetUuid.toString();
    }

    public String getSubsetRefsetUuidStr() {
        return subsetRefsetUuidStr;
    }

    public String getSubsetUuidFromName() {
        return subsetPathUuidFromName;
    }

    public String getRefsetPathUuidStr() {
        return subsetPathUuidStr;
    }
    
    public void setRefsetPathUuid(String uuid) {
        this.subsetPathUuidFromName = uuid;
        this.subsetPathUuid = UUID.fromString(uuid);
        this.subsetPathUuidStr = uuid;        
    }

    public void setRefsetPathName(String name) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        this.subsetPathUuidFromName = name;
        this.subsetPathUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, name);
        this.subsetPathUuidStr = subsetPathUuid.toString();
        System.out.println("Rf1SubsetId.setRefsetPathName  subsetPathUuidStr  ="
                + this.subsetPathUuidStr); // :DEBUG:!!!:
    }

    public String getRefsetParentUuid() {
        return refsetParentUuid;
    }

    public void setRefsetParentUuid(String refestParentUuid) {
        this.refsetParentUuid = refestParentUuid;
    }

    public String getRefsetDate() {
        return refsetDate;
    }

    public void setRefsetDate(String refsetDate) {
        this.refsetDate = refsetDate;
    }

    public String getRefsetFsName() {
        return refsetFsName;
    }

    public void setRefsetFsName(String refsetFsName) {
        this.refsetFsName = refsetFsName;
    }

    public String getRefsetPrefName() {
        return refsetPrefName;
    }

    public void setRefsetPrefName(String refsetPrefName) {
        this.refsetPrefName = refsetPrefName;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(subsetSctIdOriginal + " (SCTID), ");
        sb.append(subsetPathUuidFromName + " (NAME), ");
        sb.append(subsetPathUuid + " (PATH), ");
        sb.append(subsetRefsetUuid + " (REFSET)");

        return sb.toString();
    }

}
