/*
 * 
 */
package org.ihtsdo.mojo.schema.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.helper.dialect.DialectHelper;
import org.ihtsdo.helper.dialect.UnsupportedDialectOrLanguage;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.mojo.db.ConceptDescriptor;
import org.ihtsdo.mojo.schema.AbstractTransformer;
import org.ihtsdo.mojo.schema.config.TransformersConfigApi;
import org.ihtsdo.tk.binding.snomed.Language;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * The Transformer RF1ToRf2Transformer.<br> Transforms a concept in RF1 format
 * to a concept in RF2 format. Needs a transformConfig to match the following.
 * Path is required. US/GB are required if transforming English language
 * content. Lang is generic for other languages. Can use both lang and US/GB if
 * content includes English and another language. Currently supports only one
 * generic language, although could be extended to allow for more.
 * <code>
 * <config>
 * <transformer>
 * <id>rf1-to-rf2</id>
 * <class>org.ihtsdo.mojo.schema.transformer.RF1ToRF2Transformer
 * </class>
 * <parameters>
 * <langRefset>
 * <uuid>e57ec728-742f-56b3-9b53-9613670fb24d</uuid>
 * <description>Swedish [International Organization for Standardization 639-1
 * code sv] language reference set (foundation metadata concept)</description>
 * </langRefset>
 * <usDialectRefset>
 * <uuid>bca0a686-3516-3daf-8fcf-fe396d13cfad</uuid>
 * <description>United States of America English language reference set
 * (foundation metadata concept)</description>
 * </usDialectRefset>
 * <gbDialectRefset>
 * <uuid>eb9a5e42-3cba-356d-b623-3ed472e20b30</uuid>
 * <description>Great Britain English language reference set (foundation
 * metadata concept)</description>
 * </gbDialectRefset>
 * <path>
 * <uuid>454a9a98-0328-583d-8861-f3419ef6a809</uuid>
 * <description>SE SCT EXT UAT Latest development path</description>
 * </path>
 * </parameters>
 * </transformer>
 * </config>
 * </code>
 *
 */
public class RF1ToRF2Transformer extends AbstractTransformer {

    /**
     * The id.
     */
    private final String id = "rf1-to-rf2";
    /**
     * The concept count.
     */
    private transient int conceptCount = 0;
    private UUID usRefsetUUID;
    private UUID gbRefsetUUID;
    private UUID langRefsetUUID;
    private UUID pathUUID;
    private final UUID auxPathUUID = UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66");
    private final UUID authorUser = UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c");
    private final UUID legacyUsDialectRefsetUuid = UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6");
    private final UUID legacyGbDialectRefsetUuid = UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd");
    private final UUID rf2UsDialectRefsetUuid = UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad");
    private final UUID rf2GbDialectRefsetUuid = UUID.fromString("eb9a5e42-3cba-356d-b623-3ed472e20b30");
    private final UUID rf1PreferredUuid = SnomedMetadataRf1.PREFERRED_ACCEPTABILITY_RF1.getUuids()[0];
    private final UUID rf1AcceptableUuid = SnomedMetadataRf1.ACCEPTABLE_DESCRIPTION_TYPE_RF1.getUuids()[0];
    private final UUID rf1SynomymUuid = SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getUuids()[0];
    private final UUID rf2PreferredUuid = SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0];
    private final UUID rf2AcceptableUuid = SnomedMetadataRf2.ACCEPTABLE_RF2.getUuids()[0];

    /**
     * Instantiates a new transformer.
     */
    public RF1ToRF2Transformer() {
    }


    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#setupFromXml(java.lang.String)
     */
    @Override
    public void setupFromXml(String xmlFile) throws Exception {
        TransformersConfigApi api = new TransformersConfigApi(xmlFile);
        ConceptDescriptor usRefset = api.getConceptDescriptor(api.getIntId(id), "parameters.usDialectRefset");
        ConceptDescriptor gbRefset = api.getConceptDescriptor(api.getIntId(id), "parameters.gbDialectRefset");
        ConceptDescriptor langRefset = api.getConceptDescriptor(api.getIntId(id), "parameters.langRefset");
        ConceptDescriptor path = api.getConceptDescriptor(api.getIntId(id), "parameters.path");
        if (path.getUuid() != null) {
            this.pathUUID = path.getVerifiedConcept().getPrimUuid();
        } else {
            // when path is not configured, the transformer will directly test for SNOMED concepts
            this.pathUUID = null;
        }

        if (usRefset.getUuid() != null) {
            this.usRefsetUUID = usRefset.getVerifiedConcept().getPrimUuid();
        }
        if (gbRefset.getUuid() != null) {
            this.gbRefsetUUID = gbRefset.getVerifiedConcept().getPrimUuid();
        }
        if (langRefset.getUuid() != null) {
            this.langRefsetUUID = langRefset.getVerifiedConcept().getPrimUuid();
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformAttributes(org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes)
     */
    @Override
    public void transformAttributes(TkConceptAttributes attributes, TkConcept concept) {
        try {
            if ((pathUUID == null && attributes.pathUuid.compareTo(auxPathUUID) != 0)
                    || (pathUUID != null && attributes.getPathUuid().equals(pathUUID))) {
                if (attributes.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                    attributes.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (attributes.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    attributes.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (attributes.getStatusUuid().equals(SnomedMetadataRf1.AMBIGUOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    attributes.setStatusUuid(SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (attributes.getStatusUuid().equals(SnomedMetadataRf1.DUPLICATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    attributes.setStatusUuid(SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (attributes.getStatusUuid().equals(SnomedMetadataRf1.ERRONEOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    attributes.setStatusUuid(SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (attributes.getStatusUuid().equals(SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    attributes.setStatusUuid(SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (attributes.getStatusUuid().equals(SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    attributes.setStatusUuid(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getPrimUuid());
                }
            }

            if ((pathUUID == null)
                    || (pathUUID != null && attributes.getPathUuid().equals(pathUUID))) {
                if (attributes.getAuthorUuid() == null) {
                    attributes.setAuthorUuid(authorUser);
                }
                if (attributes.getModuleUuid() == null) {
                    attributes.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                }
            }

            if (attributes.getAdditionalIdComponents() != null) {
                List<TkIdentifier> additionalIdList = attributes.getAdditionalIdComponents();
                for (TkIdentifier tkIdentifier : additionalIdList) {
                    if ((pathUUID == null && tkIdentifier.pathUuid.compareTo(auxPathUUID) != 0)
                            || (pathUUID != null && tkIdentifier.getPathUuid().equals(pathUUID))) {
                        if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.AMBIGUOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.DUPLICATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.ERRONEOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getPrimUuid());
                        }
                    }

                    if ((pathUUID == null)
                            || (pathUUID != null && tkIdentifier.getPathUuid().equals(pathUUID))) {
                        if (tkIdentifier.getAuthorUuid() == null) {
                            tkIdentifier.setAuthorUuid(authorUser);
                        }
                        if (tkIdentifier.getModuleUuid() == null) {
                            tkIdentifier.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                        }
                    }

                }
            }

        } catch (ValidationException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        }

        if (concept.getConceptAttributes().getRevisionList() != null) {
            List<TkConceptAttributesRevision> conceptAttributeList = concept.getConceptAttributes().getRevisionList();
            for (TkConceptAttributesRevision tkConceptAttributesRevision : conceptAttributeList) {
                transformAttributesRevision(tkConceptAttributesRevision, concept);
            }
        }

    }

    /* (non-Javadoc)
     * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformAttributesRevision(org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision)
     */
    private void transformAttributesRevision(TkConceptAttributesRevision attributeRevision, TkConcept concept) {
        try {
            if ((pathUUID == null && attributeRevision.pathUuid.compareTo(auxPathUUID) != 0)
                    || (pathUUID != null && attributeRevision.getPathUuid().equals(pathUUID))) {
                if (attributeRevision.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                    attributeRevision.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (attributeRevision.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    attributeRevision.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (attributeRevision.getStatusUuid().equals(SnomedMetadataRf1.AMBIGUOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    attributeRevision.setStatusUuid(SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (attributeRevision.getStatusUuid().equals(SnomedMetadataRf1.DUPLICATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    attributeRevision.setStatusUuid(SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (attributeRevision.getStatusUuid().equals(SnomedMetadataRf1.ERRONEOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    attributeRevision.setStatusUuid(SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (attributeRevision.getStatusUuid().equals(SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    attributeRevision.setStatusUuid(SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (attributeRevision.getStatusUuid().equals(SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    attributeRevision.setStatusUuid(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getPrimUuid());
                }
            }
            if ((pathUUID == null)
                    || (pathUUID != null && attributeRevision.getPathUuid().equals(pathUUID))) {
                if (attributeRevision.getAuthorUuid() == null) {
                    attributeRevision.setAuthorUuid(authorUser);
                }
                if (attributeRevision.getModuleUuid() == null) {
                    attributeRevision.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                }
            }
        } catch (ValidationException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformDescription(org.ihtsdo.tk.dto.concept.component.description.TkDescription)
     */
    @Override
    public void transformDescription(TkDescription description, TkConcept concept) {
        try {
            if ((pathUUID == null && description.pathUuid.compareTo(auxPathUUID) != 0)
                    || (pathUUID != null && description.getPathUuid().equals(pathUUID))) {
                if (description.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                    description.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (description.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    description.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (description.getStatusUuid().equals(SnomedMetadataRf1.AMBIGUOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    description.setStatusUuid(SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (description.getStatusUuid().equals(SnomedMetadataRf1.DUPLICATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    description.setStatusUuid(SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (description.getStatusUuid().equals(SnomedMetadataRf1.ERRONEOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    description.setStatusUuid(SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (description.getStatusUuid().equals(SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    description.setStatusUuid(SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (description.getStatusUuid().equals(SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    description.setStatusUuid(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getPrimUuid());
                }

                boolean hasGbAnnotation = false;
                boolean hasUsAnnotation = false;
                if (description.getAnnotations() != null) {
                    for (TkRefexAbstractMember<?> tkram : description.getAnnotations()) {
                        if (tkram.getRefexUuid().compareTo(rf2GbDialectRefsetUuid) == 0) {
                            hasGbAnnotation = true;
                        }
                        if (tkram.getRefexUuid().compareTo(rf2UsDialectRefsetUuid) == 0) {
                            hasUsAnnotation = true;
                        }
                    }
                }

                if (hasGbAnnotation || hasUsAnnotation) {
                    // do not add language refset set
                    if (description.getTypeUuid().equals(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getPrimUuid())) {
                        description.setTypeUuid(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid());
                    } else if (description.getTypeUuid().equals(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
                        description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
                    } else if (description.getTypeUuid().equals(SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
                        description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
                    }
                } else if (langRefsetUUID != null && !description.getLang().equals(LANG_CODE.EN.getFormatedLanguageCode())) {
                    processLangDescription(description, concept);
                } else if (usRefsetUUID != null && !DialectHelper.isTextForDialect(description.getText(), Language.EN_US.getLenient().getNid())
                        && gbRefsetUUID != null && !DialectHelper.isTextForDialect(description.getText(), Language.EN_UK.getLenient().getNid())
                        && description.getLang().equals(LANG_CODE.EN.getFormatedLanguageCode())) {
                    processEnDescription(description, concept);
                } else if (usRefsetUUID != null && DialectHelper.isTextForDialect(description.getText(), Language.EN_US.getLenient().getNid())) {
                    processUSDescription(description, concept);
                } else if (gbRefsetUUID != null && DialectHelper.isTextForDialect(description.getText(), Language.EN_UK.getLenient().getNid())) {
                    processGBDescription(description, concept);
                } else if (gbRefsetUUID == null && usRefsetUUID != null && description.lang.equalsIgnoreCase("en")) {
                    // review condition of this case ... when/if more cases get added
                    processUSDescription(description, concept);
                } else {
                    throw new UnsupportedDialectOrLanguage("Can't support language or dialect.");
                }
            }

            if ((pathUUID == null)
                    || (pathUUID != null && description.getPathUuid().equals(pathUUID))) {
                if (description.getAuthorUuid() == null) {
                    description.setAuthorUuid(authorUser);
                }
                if (description.getModuleUuid() == null) {
                    description.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                }
            }

            if (description.getAdditionalIdComponents() != null) {
                List<TkIdentifier> additionalIdList = description.getAdditionalIdComponents();
                for (TkIdentifier tkIdentifier : additionalIdList) {
                    if ((pathUUID == null && tkIdentifier.pathUuid.compareTo(auxPathUUID) != 0)
                            || (pathUUID != null && tkIdentifier.getPathUuid().equals(pathUUID))) {
                        if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.AMBIGUOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.DUPLICATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.ERRONEOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getPrimUuid());
                        }
                    }

                    if ((pathUUID == null)
                            || (pathUUID != null && tkIdentifier.getPathUuid().equals(pathUUID))) {
                        if (tkIdentifier.getAuthorUuid() == null) {
                            tkIdentifier.setAuthorUuid(authorUser);
                        }
                        if (tkIdentifier.getModuleUuid() == null) {
                            tkIdentifier.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                        }
                    }
                }
            }

        } catch (UnsupportedDialectOrLanguage | IOException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        }

        if (description.getRevisionList() != null) {
            List<TkDescriptionRevision> revisionList = description.getRevisionList();
            for (TkDescriptionRevision r : revisionList) {
                transformDescriptionRevision(r, concept);
            }
        }
    }

    private void transformDescriptionRevision(TkDescriptionRevision revision, TkConcept concept) {
        try {
            if ((pathUUID == null && revision.pathUuid.compareTo(auxPathUUID) != 0)
                    || (pathUUID != null && revision.getPathUuid().equals(pathUUID))) {
                if (revision.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                    revision.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (revision.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    revision.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (revision.getStatusUuid().equals(SnomedMetadataRf1.AMBIGUOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    revision.setStatusUuid(SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (revision.getStatusUuid().equals(SnomedMetadataRf1.DUPLICATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    revision.setStatusUuid(SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (revision.getStatusUuid().equals(SnomedMetadataRf1.ERRONEOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    revision.setStatusUuid(SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (revision.getStatusUuid().equals(SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    revision.setStatusUuid(SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getPrimUuid());
                } else if (revision.getStatusUuid().equals(SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    revision.setStatusUuid(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getPrimUuid());
                }

            }

            if (revision.getTypeUuid().equals(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getPrimUuid())) {
                revision.setTypeUuid(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid());
            } else if (revision.getTypeUuid().equals(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
                revision.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            } else if (revision.getTypeUuid().equals(SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
                revision.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            }

            if ((pathUUID == null)
                    || (pathUUID != null && revision.getPathUuid().equals(pathUUID))) {
                if (revision.getAuthorUuid() == null) {
                    revision.setAuthorUuid(authorUser);
                }
                if (revision.getModuleUuid() == null) {
                    revision.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                }
            }

        } catch (IOException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformRelationship(org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship)
     */
    @Override
    public void transformRelationship(TkRelationship relationship, TkConcept concept) {
        try {
            if ((pathUUID == null && relationship.pathUuid.compareTo(auxPathUUID) != 0)
                    || (pathUUID != null && relationship.getPathUuid().equals(pathUUID))) {
                if (relationship.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                    relationship.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (relationship.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    relationship.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                }

                if (relationship.getCharacteristicUuid().equals(SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getPrimUuid())) {
                    relationship.setCharacteristicUuid(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getPrimUuid());
                } else if (relationship.getCharacteristicUuid().equals(SnomedMetadataRf1.STATED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getPrimUuid())) {
                    relationship.setCharacteristicUuid(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getPrimUuid());
                }

                if (relationship.getRefinabilityUuid().equals(SnomedMetadataRf1.NOT_REFINABLE_REFINABILITY_TYPE_RF1.getLenient().getPrimUuid())) {
                    relationship.setRefinabilityUuid(SnomedMetadataRf2.NOT_REFINABLE_RF2.getLenient().getPrimUuid());
                } else if (relationship.getRefinabilityUuid().equals(SnomedMetadataRf1.OPTIONAL_REFINABILITY_TYPE_RF1.getLenient().getPrimUuid())) {
                    relationship.setRefinabilityUuid(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getLenient().getPrimUuid());
                } else if (relationship.getRefinabilityUuid().equals(SnomedMetadataRf1.MANDATORY_REFINABILITY_TYPE_RF1.getLenient().getPrimUuid())) {
                    relationship.setRefinabilityUuid(SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2.getLenient().getPrimUuid());
                }

                if ((pathUUID == null)
                        || (pathUUID != null && relationship.getPathUuid().equals(pathUUID))) {
                    if (relationship.getAuthorUuid() == null) {
                        relationship.setAuthorUuid(authorUser);
                    }
                    if (relationship.getModuleUuid() == null) {
                        relationship.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                    }
                }
            }

            if (relationship.getAdditionalIdComponents() != null) {
                List<TkIdentifier> additionalIdList = relationship.getAdditionalIdComponents();
                for (TkIdentifier tkIdentifier : additionalIdList) {
                    if ((pathUUID == null && tkIdentifier.pathUuid.compareTo(auxPathUUID) != 0)
                            || (pathUUID != null && tkIdentifier.getPathUuid().equals(pathUUID))) {
                        if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.AMBIGUOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.DUPLICATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.ERRONEOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getPrimUuid());
                        } else if (tkIdentifier.getStatusUuid().equals(SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                            tkIdentifier.setStatusUuid(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getPrimUuid());
                        }
                    }

                    if ((pathUUID == null)
                            || (pathUUID != null && tkIdentifier.getPathUuid().equals(pathUUID))) {
                        if (tkIdentifier.getAuthorUuid() == null) {
                            tkIdentifier.setAuthorUuid(authorUser);
                        }
                        if (tkIdentifier.getModuleUuid() == null) {
                            tkIdentifier.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                        }
                    }
                }
            }

        } catch (IOException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        }

        if (relationship.getRevisionList() != null) {
            List<TkRelationshipRevision> revisionList = relationship.getRevisionList();
            for (TkRelationshipRevision r : revisionList) {
                transformRelationshipRevision(r, concept);
            }
        }
    }

    private void transformRelationshipRevision(TkRelationshipRevision revision, TkConcept concept) {
        try {
            if ((pathUUID == null && revision.pathUuid.compareTo(auxPathUUID) != 0)
                    || (pathUUID != null && revision.getPathUuid().equals(pathUUID))) {
                if (revision.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                    revision.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (revision.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    revision.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                }

                if (revision.getCharacteristicUuid().equals(SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getPrimUuid())) {
                    revision.setCharacteristicUuid(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getPrimUuid());
                } else if (revision.getCharacteristicUuid().equals(SnomedMetadataRf1.STATED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getPrimUuid())) {
                    revision.setCharacteristicUuid(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getPrimUuid());
                }

                if (revision.getRefinabilityUuid().equals(SnomedMetadataRf1.NOT_REFINABLE_REFINABILITY_TYPE_RF1.getLenient().getPrimUuid())) {
                    revision.setRefinabilityUuid(SnomedMetadataRf2.NOT_REFINABLE_RF2.getLenient().getPrimUuid());
                } else if (revision.getRefinabilityUuid().equals(SnomedMetadataRf1.OPTIONAL_REFINABILITY_TYPE_RF1.getLenient().getPrimUuid())) {
                    revision.setRefinabilityUuid(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getLenient().getPrimUuid());
                } else if (revision.getRefinabilityUuid().equals(SnomedMetadataRf1.MANDATORY_REFINABILITY_TYPE_RF1.getLenient().getPrimUuid())) {
                    revision.setRefinabilityUuid(SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2.getLenient().getPrimUuid());
                }

                if ((pathUUID == null)
                        || (pathUUID != null && revision.getPathUuid().equals(pathUUID))) {
                    if (revision.getAuthorUuid() == null) {
                        revision.setAuthorUuid(authorUser);
                    }
                    if (revision.getModuleUuid() == null) {
                        revision.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                    }
                }
            }
        } catch (IOException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformAnnotation(org.ihtsdo.tk.dto.concept.component.refset.TkRefexAbstractMember,
     * org.ihtsdo.tk.dto.concept.component.TkComponent)
     */
    @Override
    public void transformAnnotation(TkRefexAbstractMember<?> annotation,
            TkComponent<?> component) {
        try {
            if (annotation.refsetUuid.compareTo(legacyGbDialectRefsetUuid) == 0) {
                UUID uuid1 = ((TkRefexUuidMember) annotation).uuid1;
                if (uuid1.compareTo(rf1AcceptableUuid) == 0) {
                    ((TkRefexUuidMember) annotation).uuid1 = rf2AcceptableUuid;
                } else if (uuid1.compareTo(rf1PreferredUuid) == 0) {
                    ((TkRefexUuidMember) annotation).uuid1 = rf2PreferredUuid;
                } else if (uuid1.compareTo(rf1SynomymUuid) == 0) {
                    ((TkRefexUuidMember) annotation).uuid1 = rf2AcceptableUuid;
                }
                ((TkRefexUuidMember) annotation).refsetUuid = rf2GbDialectRefsetUuid;
            }

            if (annotation.refsetUuid.compareTo(legacyUsDialectRefsetUuid) == 0) {
                UUID uuid1 = ((TkRefexUuidMember) annotation).uuid1;

                if (uuid1.compareTo(rf1AcceptableUuid) == 0) {
                    ((TkRefexUuidMember) annotation).uuid1 = rf2AcceptableUuid;
                } else if (uuid1.compareTo(rf1PreferredUuid) == 0) {
                    ((TkRefexUuidMember) annotation).uuid1 = rf2PreferredUuid;
                } else if (uuid1.compareTo(rf1SynomymUuid) == 0) {
                    ((TkRefexUuidMember) annotation).uuid1 = rf2AcceptableUuid;
                }
                ((TkRefexUuidMember) annotation).refsetUuid = rf2UsDialectRefsetUuid;
            }

            if ((pathUUID == null && annotation.pathUuid.compareTo(auxPathUUID) != 0)
                    || (pathUUID != null && annotation.getPathUuid().equals(pathUUID))) {
                if (annotation.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                    annotation.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (annotation.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    annotation.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                }
            }

            if ((pathUUID == null)
                    || (pathUUID != null && annotation.getPathUuid().equals(pathUUID))) {
                if (annotation.getAuthorUuid() == null) {
                    annotation.setAuthorUuid(authorUser);
                }
                if (annotation.getModuleUuid() == null) {
                    annotation.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                }
            }
        } catch (ValidationException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        }

        if (annotation.getRevisionList() != null) {
            List<? extends TkRevision> revisionList = annotation.getRevisionList();
            for (TkRevision r : revisionList) {
                transformAnnotationRevision(r, component);
            }
        }
    }

    private void transformAnnotationRevision(TkRevision revision, TkComponent<?> component) {
        try {

            if ((pathUUID == null && revision.pathUuid.compareTo(auxPathUUID) != 0)
                    || (pathUUID != null && revision.getPathUuid().equals(pathUUID))) {
                if (revision.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                    revision.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (revision.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    revision.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                }
            }

            if ((pathUUID == null)
                    || (pathUUID != null && revision.getPathUuid().equals(pathUUID))) {
                if (revision.getAuthorUuid() == null) {
                    revision.setAuthorUuid(authorUser);
                }
                if (revision.getModuleUuid() == null) {
                    revision.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                }
            }
        } catch (ValidationException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformMember(org.ihtsdo.tk.dto.concept.component.refset.TkRefexAbstractMember,
     * org.ihtsdo.tk.dto.concept.TkConcept)
     */
    @Override
    public void transformMember(TkRefexAbstractMember<?> member, TkConcept concept) {
        try {
            if ((pathUUID == null && member.pathUuid.compareTo(auxPathUUID) != 0)
                    || (pathUUID != null && member.getPathUuid().equals(pathUUID))) {
                if (member.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                    member.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (member.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    member.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                }
            }

            if ((pathUUID == null)
                    || (pathUUID != null && member.getPathUuid().equals(pathUUID))) {
                if (member.getAuthorUuid() == null) {
                    member.setAuthorUuid(authorUser);
                }
                if (member.getModuleUuid() == null) {
                    member.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                }
            }
        } catch (ValidationException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        }

        if (member.getRevisionList() != null) {
            List<? extends TkRevision> revisionList = member.getRevisionList();
            for (TkRevision r : revisionList) {
                transformMemberRevision(r, concept);
            }
        }

    }

    private void transformMemberRevision(TkRevision revision, TkConcept concept) {
        try {
            if ((pathUUID == null && revision.pathUuid.compareTo(auxPathUUID) != 0)
                    || (pathUUID != null && revision.getPathUuid().equals(pathUUID))) {
                if (revision.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                    revision.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (revision.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    revision.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                }
            }

            if ((pathUUID == null)
                    || (pathUUID != null && revision.getPathUuid().equals(pathUUID))) {
                if (revision.getAuthorUuid() == null) {
                    revision.setAuthorUuid(authorUser);
                }
                if (revision.getModuleUuid() == null) {
                    revision.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                }
            }
        } catch (ValidationException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#postProcessConcept()
     */
    @Override
    public boolean postProcessConcept(TkConcept eConcept) {
        checkForMultipleFsn(eConcept);
        return true;
    }

    private void checkForMultipleFsn(TkConcept eConcept) {
        // check for multiple active FSN descriptions
        if (eConcept.descriptions != null) {
            UUID[] fsnUuids = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids();
            List<UUID> fsnUuidsList = Arrays.asList(fsnUuids);
            UUID[] activeUuids = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids();
            List<UUID> activeUuidsList = Arrays.asList(activeUuids);
            List<TkDescription> dList = eConcept.getDescriptions();
            int fsnCount = 0;
            for (TkDescription tkd : dList) {
                boolean isActive = false;
                boolean isFsn = false;
                long time = tkd.time;
                if (fsnUuidsList.contains(tkd.typeUuid)
                        && activeUuidsList.contains(tkd.statusUuid)) {
                    isActive = true;
                    isFsn = true;
                }
                if (tkd.revisions != null) {
                    for (TkDescriptionRevision tkdr : tkd.revisions) {
                        if (tkdr.time > time) {
                            time = tkdr.time;
                            if (fsnUuidsList.contains(tkdr.typeUuid)
                                    && activeUuidsList.contains(tkdr.statusUuid)) {
                                isFsn = true;
                                isActive = true;
                            } else if (fsnUuidsList.contains(tkdr.typeUuid)
                                    && activeUuidsList.contains(tkdr.statusUuid) == false) {
                                isFsn = true;
                                isActive = false;
                            } else {
                                isFsn = false;
                            }
                        }
                    }
                }
                if (isFsn && isActive) {
                    fsnCount++;
                }
            }

            if (fsnCount > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("\r\n RF1ToRF2Transformer found concept with active FSN count=");
                sb.append(fsnCount);
                sb.append(" UUID: ");
                sb.append(eConcept.primordialUuid.toString());
                // sb.append(" Concept: ");
                // sb.append(eConcept.toString());
                AceLog.getAppLog().log(Level.INFO, sb.toString());
            }
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#postProcessIteration()
     */
    @Override
    public List<TkConcept> postProcessIteration() {
        System.out.println("**** Final, total converted " + conceptCount + " members");
        List<TkConcept> postProcessList = new ArrayList<>();
        return postProcessList;
    }


    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#preProcessIteration()
     */
    @Override
    public void preProcessIteration() {
    }

    /**
     * Count.
     */
    public void count() {
        conceptCount++;
        if (conceptCount % 1000 == 0 || conceptCount == 1) {
            System.out.println("**** Converted " + conceptCount + " members");
        }
    }

    @Override
    public String getId() {
        return id;
    }

    private void processEnDescription(TkDescription description, TkConcept concept) throws ValidationException, IOException {
        List<TkRefexAbstractMember<?>> annotations = description.getAnnotations();
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        if (description.getTypeUuid().equals(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid());
            TkRefexUuidMember usMember = new TkRefexUuidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);

            TkRefexUuidMember gbMember = new TkRefexUuidMember();
            gbMember.setPrimordialComponentUuid(UUID.randomUUID());
            gbMember.setAuthorUuid(description.getAuthorUuid());
            gbMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            gbMember.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            gbMember.setComponentUuid(description.getPrimordialComponentUuid());
            gbMember.setPathUuid(description.getPathUuid());
            gbMember.setRefsetUuid(gbRefsetUUID);
            gbMember.setStatusUuid(description.getStatusUuid());
            gbMember.setTime(description.getTime());
            annotations.add(gbMember);
            description.setAnnotations(annotations);
        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefexUuidMember usMember = new TkRefexUuidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);

            TkRefexUuidMember gbMember = new TkRefexUuidMember();
            gbMember.setPrimordialComponentUuid(UUID.randomUUID());
            gbMember.setAuthorUuid(description.getAuthorUuid());
            gbMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            gbMember.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            gbMember.setComponentUuid(description.getPrimordialComponentUuid());
            gbMember.setPathUuid(description.getPathUuid());
            gbMember.setRefsetUuid(gbRefsetUUID);
            gbMember.setStatusUuid(description.getStatusUuid());
            gbMember.setTime(description.getTime());
            annotations.add(gbMember);
            description.setAnnotations(annotations);

        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefexUuidMember usMember = new TkRefexUuidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setUuid1(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);

            TkRefexUuidMember gbMember = new TkRefexUuidMember();
            gbMember.setPrimordialComponentUuid(UUID.randomUUID());
            gbMember.setAuthorUuid(description.getAuthorUuid());
            gbMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            gbMember.setUuid1(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
            gbMember.setComponentUuid(description.getPrimordialComponentUuid());
            gbMember.setPathUuid(description.getPathUuid());
            gbMember.setRefsetUuid(gbRefsetUUID);
            gbMember.setStatusUuid(description.getStatusUuid());
            gbMember.setTime(description.getTime());
            annotations.add(gbMember);
            description.setAnnotations(annotations);

        }
    }

    private void processUSDescription(TkDescription description, TkConcept concept) throws ValidationException, IOException {
        List<TkRefexAbstractMember<?>> annotations = description.getAnnotations();
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        if (description.getTypeUuid().equals(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid());
            TkRefexUuidMember usMember = new TkRefexUuidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);

            if (gbRefsetUUID != null) {
                TkRefexUuidMember gbMember = new TkRefexUuidMember();
                gbMember.setPrimordialComponentUuid(UUID.randomUUID());
                gbMember.setAuthorUuid(description.getAuthorUuid());
                gbMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                gbMember.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
                gbMember.setComponentUuid(description.getPrimordialComponentUuid());
                gbMember.setPathUuid(description.getPathUuid());
                gbMember.setRefsetUuid(gbRefsetUUID);
                gbMember.setStatusUuid(description.getStatusUuid());
                gbMember.setTime(description.getTime());
                annotations.add(gbMember);
                description.setAnnotations(annotations);
            }
        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefexUuidMember usMember = new TkRefexUuidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);
        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefexUuidMember usMember = new TkRefexUuidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setUuid1(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);

            if (gbRefsetUUID != null) {
                TkRefexUuidMember gbMember = new TkRefexUuidMember();
                gbMember.setPrimordialComponentUuid(UUID.randomUUID());
                gbMember.setAuthorUuid(description.getAuthorUuid());
                gbMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                gbMember.setUuid1(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
                gbMember.setComponentUuid(description.getPrimordialComponentUuid());
                gbMember.setPathUuid(description.getPathUuid());
                gbMember.setRefsetUuid(gbRefsetUUID);
                gbMember.setStatusUuid(description.getStatusUuid());
                gbMember.setTime(description.getTime());
                annotations.add(gbMember);
                description.setAnnotations(annotations);
            }

        }
    }

    private void processGBDescription(TkDescription description, TkConcept concept) throws ValidationException, IOException {
        //no FSN transform since current use of RF2 only uses US dialect as FSN
        List<TkRefexAbstractMember<?>> annotations = description.getAnnotations();
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        if (description.getTypeUuid().equals(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefexUuidMember gbMember = new TkRefexUuidMember();
            gbMember.setPrimordialComponentUuid(UUID.randomUUID());
            gbMember.setAuthorUuid(description.getAuthorUuid());
            gbMember.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            gbMember.setComponentUuid(description.getPrimordialComponentUuid());
            gbMember.setPathUuid(description.getPathUuid());
            gbMember.setRefsetUuid(gbRefsetUUID);
            gbMember.setStatusUuid(description.getStatusUuid());
            gbMember.setTime(description.getTime());
            annotations.add(gbMember);
            description.setAnnotations(annotations);
        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefexUuidMember usMember = new TkRefexUuidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setUuid1(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);

            TkRefexUuidMember gbMember = new TkRefexUuidMember();
            gbMember.setPrimordialComponentUuid(UUID.randomUUID());
            gbMember.setAuthorUuid(description.getAuthorUuid());
            gbMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            gbMember.setUuid1(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
            gbMember.setComponentUuid(description.getPrimordialComponentUuid());
            gbMember.setPathUuid(description.getPathUuid());
            gbMember.setRefsetUuid(gbRefsetUUID);
            gbMember.setStatusUuid(description.getStatusUuid());
            gbMember.setTime(description.getTime());
            annotations.add(gbMember);
            description.setAnnotations(annotations);
        }
    }

    private void processLangDescription(TkDescription description, TkConcept concept) throws ValidationException, IOException {
        List<TkRefexAbstractMember<?>> annotations = description.getAnnotations();
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        if (description.getTypeUuid().equals(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid());
            TkRefexUuidMember member = new TkRefexUuidMember();
            member.setPrimordialComponentUuid(UUID.randomUUID());
            member.setAuthorUuid(description.getAuthorUuid());
            member.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            member.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            member.setComponentUuid(description.getPrimordialComponentUuid());
            member.setPathUuid(description.getPathUuid());
            member.setRefsetUuid(langRefsetUUID);
            member.setStatusUuid(description.getStatusUuid());
            member.setTime(description.getTime());
            annotations.add(member);
            description.setAnnotations(annotations);
        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefexUuidMember member = new TkRefexUuidMember();
            member.setPrimordialComponentUuid(UUID.randomUUID());
            member.setAuthorUuid(description.getAuthorUuid());
            member.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            member.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            member.setComponentUuid(description.getPrimordialComponentUuid());
            member.setPathUuid(description.getPathUuid());
            member.setRefsetUuid(langRefsetUUID);
            member.setStatusUuid(description.getStatusUuid());
            member.setTime(description.getTime());
            annotations.add(member);
            description.setAnnotations(annotations);
        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefexUuidMember member = new TkRefexUuidMember();
            member.setPrimordialComponentUuid(UUID.randomUUID());
            member.setAuthorUuid(description.getAuthorUuid());
            member.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            member.setUuid1(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
            member.setComponentUuid(description.getPrimordialComponentUuid());
            member.setPathUuid(description.getPathUuid());
            member.setRefsetUuid(langRefsetUUID);
            member.setStatusUuid(description.getStatusUuid());
            member.setTime(description.getTime());
            annotations.add(member);
            description.setAnnotations(annotations);
        }
    }
}
