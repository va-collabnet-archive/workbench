package org.dwfa.dto;

import java.util.UUID;

public class IdentifierDto extends BaseConceptDto implements Concept {
    private UUID conceptId;
    private Long referencedSctId;
    private UUID identifierSchemeUuid;

    public IdentifierDto() {

    }

    /**
     * @return the conceptId
     */
    public UUID getConceptId() {
        return conceptId;
    }

    /**
     * @param conceptId the conceptId to set
     */
    public void setConceptId(UUID conceptId) {
        this.conceptId = conceptId;
    }

    /**
     * @return the identifierSchemeUuid
     */
    public UUID getIdentifierSchemeUuid() {
        return identifierSchemeUuid;
    }

    /**
     * @param identifierSchemeUuid the identifierSchemeUuid to set
     */
    public void setIdentifierSchemeUuid(UUID identifierSchemeUuid) {
        this.identifierSchemeUuid = identifierSchemeUuid;
    }

    /**
     * @return the referencedSctId
     */
    public Long getReferencedSctId() {
        return referencedSctId;
    }

    /**
     * @param referencedSctId the referencedSctId to set
     */
    public void setReferencedSctId(Long referencedSctId) {
        this.referencedSctId = referencedSctId;
    }
}
