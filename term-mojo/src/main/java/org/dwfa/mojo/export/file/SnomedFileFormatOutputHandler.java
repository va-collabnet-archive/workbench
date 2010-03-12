package org.dwfa.mojo.export.file;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.task.commit.validator.ValidationException;
import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.Concept;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.DescriptionDto;
import org.dwfa.dto.ExtensionDto;
import org.dwfa.dto.RelationshipDto;
import org.dwfa.maven.sctid.UuidSnomedDbMapHandler;
import org.dwfa.maven.sctid.UuidSnomedHandler;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.export.ExportOutputHandler;

public abstract class SnomedFileFormatOutputHandler implements ExportOutputHandler {
    /** Class logger. */
    private Logger logger = Logger.getLogger(SnomedFileFormatOutputHandler.class.getName());

    /** Uuid to Sct id map handler. */
    UuidSnomedHandler snomedIdHandler;

    /** Set to true to throw exceptions on validation errors. */
    boolean failOnError = true;

    public SnomedFileFormatOutputHandler(File SctIdDbDirectory) throws IOException, SQLException, ClassNotFoundException {
        //TODO add factory class to do this
        snomedIdHandler = new UuidSnomedDbMapHandler(SctIdDbDirectory);
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

            logger.warning(componentDto.getConceptDto().getConceptId() + " Validation Errors:" + validationErrors.toString());
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
        ConceptDto conceptDto = componentDto.getConceptDto();
        List<DescriptionDto> descriptionDtos = componentDto.getDescriptionDtos();
        List<RelationshipDto> relationshipDtos = componentDto.getRelationshipDtos();
        List<ExtensionDto> componentExtensionDtos = componentDto.getConceptExtensionDtos();
        List<ExtensionDto> descriptionExtensionDtos = componentDto.getDescriptionExtensionDtos();
        List<ExtensionDto> relationshipExtensionDtos = componentDto.getRelationshipExtensionDtos();

        validateConceptDto(validationErrorList, conceptDto);
        validateDescription(validationErrorList, descriptionDtos);
        validationRelationships(validationErrorList, relationshipDtos);
        validationExtensions(validationErrorList, componentExtensionDtos);
        validationExtensions(validationErrorList, descriptionExtensionDtos);
        validationExtensions(validationErrorList, relationshipExtensionDtos);


        return validationErrorList;
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
                    validationErrorList.add(extensionDto.getConceptId() + " No member id");
                }
                if (extensionDto.getConcept1Id() == null) {
                    validationErrorList.add(extensionDto.getConceptId() + " No referenced component id");
                }
            }

        }
    }

    /**
     * Validate the relationship details.
     *
     * @param validationErrorList  List of String
     * @param relationshipDtos List<RelationshipDto>
     */
    private void validationRelationships(List<String> validationErrorList, List<RelationshipDto> relationshipDtos) {
        if (relationshipDtos != null) {
            for (RelationshipDto relationshipDto : relationshipDtos) {
                validateConceptDto(validationErrorList, relationshipDto);

                if (relationshipDto.getCharacteristicTypeCode() == null) {
                    validationErrorList.add(relationshipDto.getConceptId() + " No characteristic type code");
                }
                if (relationshipDto.getCharacteristicTypeId() == null) {
                    validationErrorList.add(relationshipDto.getConceptId() + " No characteristic type id");
                }
                if (relationshipDto.getModifierId() == null) {
                    validationErrorList.add(relationshipDto.getConceptId() + " No modifier id");
                }
                if (relationshipDto.getRelationshipGroupCode() == null) {
                    validationErrorList.add(relationshipDto.getConceptId() + " No group code");
                }
                if (relationshipDto.getSourceId() == null) {
                    validationErrorList.add(relationshipDto.getConceptId() + " No source id");
                }
                if (relationshipDto.getDestinationId() == null) {
                    validationErrorList.add(relationshipDto.getConceptId() + " No destination id");
                }
                if (relationshipDto.getTypeId() == null) {
                    validationErrorList.add(relationshipDto.getConceptId() + " No type id");
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
                    validationErrorList.add(descriptionDto.getConceptId() + " No description id");
                }
                if (descriptionDto.getDescription() == null) {
                    validationErrorList.add(descriptionDto.getConceptId() + " No description text");
                }
                if (descriptionDto.getCaseSignificanceId() == null) {
                    validationErrorList.add(descriptionDto.getConceptId() + " No case significance id");
                }
                if (descriptionDto.getDescriptionTypeCode() == null) {
                    validationErrorList.add(descriptionDto.getConceptId() + " No case description type code");
                }
                if (descriptionDto.getInitialCapitalStatusCode() == null) {
                    validationErrorList.add(descriptionDto.getConceptId() + " No initial capital status code");
                }
                if (descriptionDto.getLanguageCode() == null) {
                    validationErrorList.add(descriptionDto.getConceptId() + " No language code");
                }
                if (descriptionDto.getLanguageId() == null) {
                    validationErrorList.add(descriptionDto.getConceptId() + " No language id");
                }
                if (descriptionDto.getTypeId() == null) {
                    validationErrorList.add(descriptionDto.getConceptId() + " No type id");
                }
            }
        } else {
            validationErrorList.add("Concept is missing or has no descriptions");
        }
    }

    /**
     * Validate the concept details (concept, description, relationship or extension)
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
        }
    }

    /**
     * Get a new or retrieves the current mapping SCT id for the concepts id.
     *
     * @return Long SCTID
     * @throws Exception if cannot get an SCT id
     */
    protected Long getSctId(Concept concept) throws Exception {
        return getSctId(concept, concept.getConceptId());
    }

    /**
     * Gets a new or retrieves the current mapping SCT id for a UUID based on
     * the ConceptDto name space and type.
     *
     * @param concept Concept
     * @param uuid UUID
     * @return Long SCTID
     * @throws Exception if cannot get an SCTID
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
        return snomedIdHandler.getWithGeneration(uuid, concept.getNamespace(), type);
    }

    /**
     * The component release path/module
     *
     * @param concept Concept
     * @return Long SCTID
     * @throws Exception if cannot get an SCTID
     */
    protected Long getModuleId(Concept concept) throws Exception {
        return snomedIdHandler.getWithGeneration(concept.getPathId(), concept.getNamespace(), TYPE.CONCEPT);
    }

    /**
     * The release date for the component.
     *
     * @param Concept concept
     * @return Date
     */
    abstract String getReleaseDate(Concept concept);

    /**
     * Close all files.
     *
     * @throws IOException on file errors
     */
    abstract void closeFiles() throws IOException ;
}
