package org.dwfa.mojo.export.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.task.commit.validator.ValidationException;
import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.Concept;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.DescriptionDto;
import org.dwfa.dto.ExtensionDto;
import org.dwfa.dto.IdentifierDto;
import org.dwfa.dto.RelationshipDto;
import org.dwfa.maven.sctid.SctIdValidator;
import org.dwfa.maven.sctid.UuidSnomedDbMapHandler;
import org.dwfa.maven.sctid.UuidSnomedHandler;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.export.ExportOutputHandler;
import org.dwfa.tapi.NoMappingException;

public abstract class SnomedFileFormatOutputHandler implements ExportOutputHandler {
    /** Class logger. */
    private Logger logger = Logger.getLogger(SnomedFileFormatOutputHandler.class.getName());

    /** Uuid to Sct id map handler. */
    static UuidSnomedHandler snomedIdHandler;

    /***/
    private Map<UUID, Map<UUID, Date>> releasePathDateMap;

    /**
     * Until the baseline database is used with have to handle concepts with
     * multiple/wrong sct id in the berkeley database.
     */
    private Map<UUID, Long> uuidIgnorList = new HashMap<UUID, Long>();

    /** Set to true to throw exceptions on validation errors. */
    boolean failOnError = true;

    public SnomedFileFormatOutputHandler(Map<UUID, Map<UUID, Date>> releasePathDateMap) throws Exception {
        // TODO add factory class to do this
        if (snomedIdHandler == null) {
            snomedIdHandler = UuidSnomedDbMapHandler.getInstance();
        }

        this.releasePathDateMap = releasePathDateMap;

        uuidIgnorList.put(UUID.fromString("cc624429-b17d-4ac5-a69e-0b32448aaf3c"), 900000000000335009l);
        uuidIgnorList.put(UUID.fromString("125f3d04-de17-490e-afec-1431c2a39e29"), 900000000000336005l);
        uuidIgnorList.put(UUID.fromString("c2c710d1-5757-33ea-9be3-4cce55f6be35"), 108642051000036118l);
        uuidIgnorList.put(UUID.fromString("58e33018-e99e-3859-a5a5-62b0ee29a05c"), 108642061000036115l);
        uuidIgnorList.put(UUID.fromString("05f27978-6783-351c-91a5-c5dc39a37299"), 900000000000416022l);
        uuidIgnorList.put(UUID.fromString("fa28e447-d635-49b1-9b55-86254a7a2f97"), 32570041000036109l);
        uuidIgnorList.put(UUID.fromString("8578034a-0b2f-3ef1-900c-250d1b1ef877"), 32570701000036108l);
        uuidIgnorList.put(UUID.fromString("4859053b-8137-364f-9c4b-3f794572aa75"), 32570371000036100l);
    }

    /**
     * Write the details of the ComponentDto to file.
     *
     * @param componentDto ComponentDto
     */
    abstract void exportComponent(ComponentDto componentDto) throws Exception;

    /**
     *
     * @see org.dwfa.mojo.export.ExportOutputHandler#export(org.dwfa.dto.ComponentDto)
     */
    @Override
    public void export(ComponentDto componentDto) throws Exception {
        List<String> validationErrorList = validate(componentDto);
        StringBuffer validationErrors = new StringBuffer();

        if (validationErrorList.isEmpty()) {
            exportComponent(componentDto);
        } else {
            for (String error : validationErrorList) {
                validationErrors.append(error);
                validationErrors.append(System.getProperty("line.separator"));
            }

            logger.warning(componentDto.getConceptDtos().get(0).getConceptId().keySet().iterator().next()
                + " Validation Errors:" + validationErrors.toString());
            if (failOnError) {
                throw new ValidationException("Validation Errors:" + validationErrors);
            }
        }
    }

    /**
     * Validate the ComponentDto is valid for exporting.
     *
     * @param componentDto ComponentDto
     * @return List of validation errors.
     */
    private List<String> validate(ComponentDto componentDto) {
        List<String> validationErrorList = new ArrayList<String>();
        List<ConceptDto> conceptDtos = componentDto.getConceptDtos();
        List<DescriptionDto> descriptionDtos = componentDto.getDescriptionDtos();
        List<RelationshipDto> relationshipDtos = componentDto.getRelationshipDtos();
        List<ExtensionDto> componentExtensionDtos = componentDto.getConceptExtensionDtos();
        List<ExtensionDto> descriptionExtensionDtos = componentDto.getDescriptionExtensionDtos();
        List<ExtensionDto> relationshipExtensionDtos = componentDto.getRelationshipExtensionDtos();

        validateConcepts(validationErrorList, conceptDtos);
        validateDescription(validationErrorList, descriptionDtos);
        validationRelationships(validationErrorList, relationshipDtos);
        validationExtensions(validationErrorList, componentExtensionDtos);
        validationExtensions(validationErrorList, descriptionExtensionDtos);
        validationExtensions(validationErrorList, relationshipExtensionDtos);

        return validationErrorList;
    }

    /**
     * Validate the concept details.
     *
     * @param validationErrorList List of String
     * @param conceptDtos List ConceptDto
     */
    private void validateConcepts(List<String> validationErrorList, List<ConceptDto> conceptDtos) {
        for (ConceptDto conceptDto : conceptDtos) {
            validateConceptDto(validationErrorList, conceptDto);
        }
    }

    /**
     * Validate the extension details (concept, description or relationship)
     *
     * @param validationErrorList List of String
     * @param extensionDtos List<ExtensionDto>
     */
    private void validationExtensions(List<String> validationErrorList, List<ExtensionDto> extensionDtos) {
        if (extensionDtos != null) {
            for (ExtensionDto extensionDto : extensionDtos) {
                validateConceptDto(validationErrorList, extensionDto);

                if (extensionDto.getMemberId() == null) {
                    validationErrorList.add(extensionDto.getConceptId().keySet().iterator().next() + " No member id");
                }
                if (extensionDto.getReferencedConceptId() == null) {
                    validationErrorList.add(extensionDto.getConceptId().keySet().iterator().next()
                        + " No referenced component id");
                }
                if (extensionDto.getConcept1Id() == null && extensionDto.getValue() == null) {
                    validationErrorList.add(extensionDto.getConceptId().keySet().iterator().next() + " value");
                }
            }

        }
    }

    /**
     * Validate the relationship details.
     *
     * @param validationErrorList List of String
     * @param relationshipDtos List<RelationshipDto>
     */
    private void validationRelationships(List<String> validationErrorList, List<RelationshipDto> relationshipDtos) {
        if (relationshipDtos != null) {
            for (RelationshipDto relationshipDto : relationshipDtos) {
                validateConceptDto(validationErrorList, relationshipDto);

                if (relationshipDto.getCharacteristicTypeCode() == null) {
                    validationErrorList.add(relationshipDto.getConceptId().keySet().iterator().next()
                        + " No characteristic type code");
                }
                if (relationshipDto.getCharacteristicTypeId() == null) {
                    validationErrorList.add(relationshipDto.getConceptId().keySet().iterator().next()
                        + " No characteristic type id");
                }
                if (relationshipDto.getModifierId() == null) {
                    validationErrorList.add(relationshipDto.getConceptId().keySet().iterator().next()
                        + " No modifier id");
                }
                if (relationshipDto.getRelationshipGroup() == null) {
                    validationErrorList.add(relationshipDto.getConceptId().keySet().iterator().next()
                        + " No group code");
                }
                if (relationshipDto.getSourceId() == null) {
                    validationErrorList.add(relationshipDto.getConceptId().keySet().iterator().next() + " No source id");
                }
                if (relationshipDto.getDestinationId() == null) {
                    validationErrorList.add(relationshipDto.getConceptId().keySet().iterator().next()
                        + " No destination id");
                }
                if (relationshipDto.getTypeId() == null) {
                    validationErrorList.add(relationshipDto.getConceptId().keySet().iterator().next() + " No type id");
                }
            }
        }
    }

    /**
     * Validate the description details
     *
     * @param validationErrorList List of String
     * @param conceptDto ConceptDto
     */
    private void validateDescription(List<String> validationErrorList, List<DescriptionDto> descriptionDtos) {
        if (descriptionDtos != null && !descriptionDtos.isEmpty() && descriptionDtos.size() > 1) {
            for (DescriptionDto descriptionDto : descriptionDtos) {
                validateConceptDto(validationErrorList, descriptionDto);
                if (descriptionDto.getDescriptionId() == null) {
                    validationErrorList.add(descriptionDto.getConceptId().keySet().iterator().next()
                        + " No description id");
                }
                if (descriptionDto.getDescription() == null) {
                    validationErrorList.add(descriptionDto.getConceptId().keySet().iterator().next()
                        + " No description text");
                }
                if (descriptionDto.getCaseSignificanceId() == null) {
                    validationErrorList.add(descriptionDto.getConceptId().keySet().iterator().next()
                        + " No case significance id");
                }
                if (descriptionDto.getDescriptionTypeCode() == null) {
                    validationErrorList.add(descriptionDto.getConceptId().keySet().iterator().next()
                        + " No case description type code");
                }
                if (descriptionDto.getInitialCapitalStatusCode() == null) {
                    validationErrorList.add(descriptionDto.getConceptId().keySet().iterator().next()
                        + " No initial capital status code");
                }
                if (descriptionDto.getLanguageCode() == null) {
                    validationErrorList.add(descriptionDto.getConceptId().keySet().iterator().next()
                        + " No language code");
                }
                if (descriptionDto.getLanguageId() == null) {
                    validationErrorList.add(descriptionDto.getConceptId().keySet().iterator().next()
                        + " No language id");
                }
                if (descriptionDto.getTypeId() == null || descriptionDto.getRf2TypeId() == null ) {
                    validationErrorList.add(descriptionDto.getConceptId().keySet().iterator().next() + " No type id");
                }
            }
        } else {
            validationErrorList.add("Concept is missing or has no descriptions");
        }
    }

    /**
     * Validate the concept details (concept, description, relationship or
     * extension)
     *
     * @param validationErrorList List of String
     * @param concept ConceptDto
     */
    private void validateConceptDto(List<String> validationErrorList, Concept concept) {
        if (concept != null) {
            if (concept.getConceptId() == null) {
                validationErrorList.add(concept.getConceptId() + " No concept id");
            }
            if (concept.getDateTime() == null) {
                validationErrorList.add(concept.getConceptId() + " No concept date");
            }
            if (concept.getPathId() == null) {
                validationErrorList.add(concept.getConceptId() + " No concept path");
            }
            if (concept.getStatusId() == null) {
                validationErrorList.add(concept.getConceptId() + " No concept status");
            }
            if (concept.getType() == null) {
                validationErrorList.add(concept.getConceptId() + " No concept type");
            }
            if (concept.getNamespace() == null) {
                validationErrorList.add(concept.getConceptId() + " No name space");
            }
            if (concept.getProject() == null) {
                validationErrorList.add(concept.getConceptId() + " No project");
            }
        }
    }

    /**
     * Get a new or retrieves the current mapping SCT id for the concepts id.
     *
     * @return Long SCTID
     * @throws Exception if cannot get an SCT id
     */
    protected Long getSctId(ConceptDto concept) throws Exception {
        return getSctId(concept, concept.getIdentifierDtos());
    }

    /**
     * Gets a new or retrieves the current mapping SCT id for a UUID based on
     * the ConceptDto name space and type.
     *
     * Validates the sct id in the id database against the sct id in the
     * IdentifierDto.
     *
     * @param concept Concept
     * @param uuid UUID
     * @return Long SCTID
     * @throws Exception if cannot get an SCTID
     */
    protected Long getSctId(Concept concept, List<IdentifierDto> identifierDtos) throws Exception {
        UUID uuid = concept.getConceptId().keySet().iterator().next();

        return getSctId(concept, uuid, identifierDtos, concept.getType());
    }

    /**
     * Gets a new or retrieves the current mapping SCT id for a UUID based on
     * the ConceptDto name space and type.
     *
     * Validates the sct id in the id database against the sct id in the
     * IdentifierDto.
     *
     * @param concept Concept
     * @param identifierDtos list of IdentifierDtos
     * @param type TYPE sct id type
     * @return Long SCTID
     * @throws Exception if cannot get an SCTID
     */
    protected Long getSctId(Concept concept, UUID uuid, List<IdentifierDto> identifierDtos, TYPE type) throws Exception {
        Long sctId;

        if (!identifierDtos.isEmpty()) {
            sctId = identifierDtos.get(0).getReferencedSctId();
            try {
                getCheckSctIdAndAddToDb(sctId, uuid, concept.getProject());
            } catch (NoMappingException nme) {
                //if (uuidIgnorList.containsKey(uuid)) {
                    sctId = snomedIdHandler.getWithoutGeneration(uuid, SctIdValidator.getInstance().getSctIdNamespace(
                        sctId.toString()), type);
                //} else {
                //    throw nme;
                //}
            }
        } else {
            sctId = snomedIdHandler.getWithGeneration(uuid, concept.getNamespace(), type, concept.getProject());
        }

        return sctId;
    }

    /**
     * Gets a new or retrieves the current mapping SCT id for a UUID based on
     * the ConceptDto name space and type.
     *
     * Validates the sct id in the id database against the sct id in the
     * IdentifierDto.
     *
     * @param concept Concept
     * @param conceptIdMap Map UUID, Long
     * @param type TYPE
     * @return Object SCTID
     * @throws Exception if cannot get an SCTID
     */
    protected Long getSctId(Concept concept, Map<UUID, Long> conceptIdMap, TYPE type) throws Exception {
        Long sctId = conceptIdMap.values().iterator().next();
        UUID uuid = conceptIdMap.keySet().iterator().next();

        if (sctId == null) {
            sctId = snomedIdHandler.getWithGeneration(uuid, concept.getNamespace(), type, concept.getProject());
        } else {
            try {
                getCheckSctIdAndAddToDb(sctId, uuid, concept.getProject());
            } catch (NoMappingException nme) {
                //if (uuidIgnorList.containsKey(uuid)) {
                    sctId = snomedIdHandler.getWithoutGeneration(uuid, SctIdValidator.getInstance().getSctIdNamespace(
                        sctId.toString()), type);
                //} else {
                //    throw nme;
                //}
            }
        }

        return sctId;
    }

    /**
     * Checks the SCT-ID UUID mapping against the id database. if the id's are
     * not
     * mapped then id mapping is added to the id database.
     *
     * @param sctId Long
     * @param uuid UUID
     * @throws NoMappingException id mapping is not valid
     * @throws Exception Cannot add them to the database
     */
    private void getCheckSctIdAndAddToDb(Long sctId, UUID uuid, PROJECT project) throws NoMappingException, Exception {
        TYPE type = SctIdValidator.getInstance().getSctIdType(sctId.toString());

        NAMESPACE namespace = SctIdValidator.getInstance().getSctIdNamespace(sctId.toString());
        Long dbSctId = snomedIdHandler.getWithoutGeneration(uuid, namespace, type);

        if (dbSctId != null && !dbSctId.equals(sctId)) {
            String errorMessage = null;
            if (!uuidIgnorList.containsKey(uuid)) {
                errorMessage = "Id Missmatch for concept " + uuid + " Concept sct id " + sctId + " database id "
                    + dbSctId;
                logger.severe(errorMessage);
            }
            throw new NoMappingException(errorMessage);
        } else if (dbSctId == null) {
            snomedIdHandler.addSctId(uuid, sctId, namespace, type, project);
        }
    }

    /**
     * Gets a new or retrieves the current mapping SCT id for a UUID based on
     * the ConceptDto name space and type.
     *
     * @param concept Concept
     * @param uuid UUID
     * @return Long sct id
     * @throws Exception
     */
    protected Long getSctId(Concept concept, UUID uuid) throws Exception {
        return getSctId(concept, uuid, concept.getType());
    }

    /**
     * Gets a new or retrieves the current mapping SCT id for a UUID based on
     * the ConceptDto name space and type.
     *
     * @param concept Concept
     * @param uuid UUID
     * @param type TYPE sct id type
     * @return Long SCTID
     * @throws Exception if cannot get an SCTID
     */
    protected Long getSctId(Concept concept, UUID uuid, TYPE type) throws Exception {
        return snomedIdHandler.getWithGeneration(uuid, concept.getNamespace(), type, concept.getProject());
    }

    /**
     * The component release path/module
     *
     * @param concept Concept
     * @return Long SCTID
     * @throws Exception if cannot get an SCTID
     */
    protected Long getModuleId(Concept concept) throws Exception {
        Long moduleSctId;
        UUID moduleUuid = getModuleUuid(concept);

        moduleSctId = snomedIdHandler.getWithGeneration(moduleUuid, concept.getNamespace(), TYPE.CONCEPT, concept.getProject());

        return moduleSctId;
    }

    /**
     * The component release path/module UUID
     *
     * @param concept Concept
     * @return UUID module UUID
     * @throws Exception if cannot get an UUID
     */
    protected UUID getModuleUuid(Concept concept) {
        UUID moduleUuid;

        Map<UUID, Date> moduleDateMap = releasePathDateMap.get(concept.getPathId());
        if (moduleDateMap != null) {
            moduleUuid = moduleDateMap.keySet().iterator().next();
        } else {
            moduleUuid = concept.getPathId();
        }

        return moduleUuid;
    }

    /**
     * The component release path/module Date.
     *
     * @param concept Concept
     * @return String
     */
    protected String getReleaseDate(Concept concept) {
        Date releaseDate;

        Map<UUID, Date> moduleDateMap = releasePathDateMap.get(concept.getPathId());
        if (moduleDateMap != null) {
            releaseDate = moduleDateMap.values().iterator().next();
        } else {
            releaseDate = concept.getDateTime();
        }

        return getReleaseDateString(releaseDate);
    }

    /**
     * The release date (version) for the component.
     *
     * @param Concept concept
     * @return String
     */
    abstract String getReleaseDateString(Date conceptVersion);

    /**
     * Close all files.
     *
     * @throws IOException on file errors
     */
    abstract void closeFiles() throws IOException;
}
