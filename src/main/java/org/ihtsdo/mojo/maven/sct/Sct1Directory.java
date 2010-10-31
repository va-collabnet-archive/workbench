package org.ihtsdo.mojo.maven.sct;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.dwfa.util.id.Type5UuidFactory;

class Sct1Directory {
    private String directoryName;

    private Boolean keepQualifierFromInferred; // 1
    private Boolean keepHistoricalFromInferred; // 2
    private Boolean keepAdditionalFromInferred; // 3

    private Boolean mapSctIdInferredToStated;
    
    private String wbPathUuidCoreFromName; // Workbench Path Name
    private String wbPathUuidStatedFromName; // Workbench Path Name
    private String wbPathUuidInferredFromName; // Workbench Path Name
    private UUID wbPathUuidCore; // UUID derived from name
    private UUID wbPathUuidStated; // UUID derived from name
    private UUID wbPathUuidInferred; // UUID derived from name

    public Sct1Directory() {
        directoryName = "";

        mapSctIdInferredToStated = false;

        keepQualifierFromInferred = false; // 1
        keepHistoricalFromInferred = false; // 2
        keepAdditionalFromInferred = false; // 3
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String dirName) {
        this.directoryName = dirName;
    }

    public Boolean getKeepHistoricalFromInferred() {
        return keepHistoricalFromInferred;
    }

    /*
     * String setter is required because POM passes in String
     */
    public void setKeepHistoricalFromInferred(String s) {
        if (s.equalsIgnoreCase("true"))
            this.keepHistoricalFromInferred = true;
        else
            this.keepHistoricalFromInferred = false;
    }

    public void setKeepHistoricalFromInferred(Boolean keep) {
        this.keepHistoricalFromInferred = keep;
    }

    public Boolean getKeepQualifierFromInferred() {
        return keepQualifierFromInferred;
    }

    /*
     * String setter is required because POM passes in String
     */
    public void setKeepQualifierFromInferred(String s) {
        if (s.equalsIgnoreCase("true"))
            this.keepQualifierFromInferred = true;
        else
            this.keepQualifierFromInferred = false;
    }

    public void setKeepQualifierFromInferred(Boolean keep) {
        this.keepQualifierFromInferred = keep;
    }

    public Boolean getKeepAdditionalFromInferred() {
        return keepAdditionalFromInferred;
    }

    /*
     * String setter is required because POM passes in String
     */
    public void setKeepAdditionalFromInferred(String s) {
        if (s.equalsIgnoreCase("true"))
            this.keepAdditionalFromInferred = true;
        else
            this.keepAdditionalFromInferred = false;
    }

    public void setKeepAdditionalFromInferred(Boolean keep) {
        this.keepAdditionalFromInferred = keep;
    }

    public Boolean doMapSctIdInferredToStated() {
        return mapSctIdInferredToStated;
    }

    public void setMapSctIdInferredToStated(String doIdMapping) {
        if (doIdMapping.equalsIgnoreCase("true"))
            this.mapSctIdInferredToStated = true;
        else
            this.mapSctIdInferredToStated = false;
    }

    public void setMapSctIdInferredToStated(Boolean mapSctIdInferredToStated) {
        this.mapSctIdInferredToStated = mapSctIdInferredToStated;
    }

    /* PATHS */
    public UUID getWbPathUuidCore() {
        return wbPathUuidCore;
    }

    public void setWbPathUuidCore(String s) {
        this.wbPathUuidCore = UUID.fromString(s);
    }

    public String getWbPathUuidCoreFromName() {
        return wbPathUuidCoreFromName;
    }

    public void setWbPathUuidCoreFromName(String name) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        this.wbPathUuidCoreFromName = name;
        this.wbPathUuidCore = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, name);
    }

    public UUID getWbPathUuidStated() {
        return wbPathUuidStated;
    }

    public void setWbPathUuidStated(String s) {
        this.wbPathUuidStated = UUID.fromString(s);
    }

    public String getWbPathUuidStatedFromName() {
        return wbPathUuidStatedFromName;
    }

    public void setWbPathUuidStatedFromName(String name) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        this.wbPathUuidStatedFromName = name;
        this.wbPathUuidStated = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, name);
    }

    public UUID getWbPathUuidInferred() {
        return wbPathUuidInferred;
    }

    public void setWbPathUuidInferred(String s) {
        this.wbPathUuidInferred = UUID.fromString(s);
    }

    public String getWbPathUuidInferredFromName() {
        return wbPathUuidInferredFromName;
    }

    public void setWbPathUuidInferredFromName(String name) throws NoSuchAlgorithmException,
            UnsupportedEncodingException {
        this.wbPathUuidInferredFromName = name;
        this.wbPathUuidInferred = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, name);
    }

    
    
}