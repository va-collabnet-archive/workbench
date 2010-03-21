package org.dwfa.dto;

import java.util.Date;
import java.util.UUID;

import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

public interface Concept {
    /**
     * @return the conceptId
     */
    public UUID getConceptId();

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
     * @return the isActive
     */
    public boolean isActive();

    /**
     * @return the namespace
     */
    public NAMESPACE getNamespace();

    /**
     * @return the type
     */
    public TYPE getType();
}
