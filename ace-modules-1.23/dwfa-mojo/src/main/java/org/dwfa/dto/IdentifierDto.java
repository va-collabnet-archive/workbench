package org.dwfa.dto;

import java.util.Map;
import java.util.UUID;

public class IdentifierDto extends BaseConceptDto implements Concept {
    private Map<UUID, Long> conceptId;
    private Long referencedSctId;
    private UUID identifierSchemeUuid;

    public IdentifierDto() {

    }

    /**
     * @return the conceptId
     */
    public Map<UUID, Long> getConceptId() {
        return conceptId;
    }

    /**
     * @param conceptId the conceptId to set
     */
    public void setConceptId(Map<UUID, Long> conceptId) {
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
