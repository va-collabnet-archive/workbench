package org.dwfa.dto;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

public interface Concept {
    /** Latest part flag. */
    public boolean isLatest();
    /**
     * @return the conceptId
     */
    public Map<UUID, Long> getConceptId();

    /**
     * @return the pathId
     */
    public UUID getPathId();

    /**
     * @return the status
     */
    public UUID getStatusId();

    /**
     * @return the dateTime
     */
    public Date getDateTime();

    /**
     * @return the RF2 dateTime
     */
    public Date getRf2DateTime();

    /**
     * @return the isActive
     */
    public boolean isActive();

    /**
     * @return the namespace
     */
    public NAMESPACE getNamespace();

    /**
     * @return the project
     */
    public PROJECT getProject();

    /**
     * @return the type
     */
    public TYPE getType();
}
