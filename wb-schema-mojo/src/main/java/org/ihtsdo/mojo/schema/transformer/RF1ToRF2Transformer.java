/*
 * 
 */
package org.ihtsdo.mojo.schema.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.ace.api.I_GetConceptData;

import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
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
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * The Transformer RF1ToRf2Transformer.<br> Transforms a concept in RF1 format
 * to a concept in RF2 format. Needs a transformConfig to match the following.
 * Path is required. US/GB are required if transforming English language
 * content. Lang is generic for other languages. Can use both lang and US/GB if
 * content includes English and another language. Currently supports only one generic
 * language, although could be extended to allow for more.
 * <code>
 * <config>
 * <transformer>
 * <id>rf1-to-rf2</id>
 * <class>org.ihtsdo.mojo.schema.transformer.RF1ToRF2Transformer
 * </class>
 * <parameters>
 * <langRefset>
 * <uuid>e57ec728-742f-56b3-9b53-9613670fb24d</uuid>
 * <description>Swedish [International Organization for Standardization 639-1 code sv] language reference set (foundation metadata concept)</description>
 * </langRefset>
 * <usDialectRefset>
 * <uuid>bca0a686-3516-3daf-8fcf-fe396d13cfad</uuid>
 * <description>United States of America English language reference set (foundation metadata concept)</description>
 * </usDialectRefset>
 * <gbDialectRefset>
 * <uuid>eb9a5e42-3cba-356d-b623-3ed472e20b30</uuid>
 * <description>Great Britain English language reference set (foundation metadata concept)</description>
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
    private UUID authorUser = UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c");

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
        this.pathUUID = path.getVerifiedConcept().getPrimUuid();

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
        if (attributes.getPathUuid().equals(pathUUID)) {
            try {
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
                if (attributes.getAuthorUuid() == null) {
                    attributes.setAuthorUuid(authorUser);
                }
                if (attributes.getModuleUuid() == null) {
                    attributes.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                }
            } catch (ValidationException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
            } catch (IOException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformDescription(org.ihtsdo.tk.dto.concept.component.description.TkDescription)
     */
    @Override
    public void transformDescription(TkDescription description, TkConcept concept) {
        if (description.getPathUuid().equals(pathUUID)) {
            try {
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

                if (langRefsetUUID != null && !description.getLang().equals(LANG_CODE.EN.getFormatedLanguageCode())) {
                    processLangDescription(description, concept);
                } else if (usRefsetUUID != null && DialectHelper.isTextForDialect(description.getText(), Language.EN_US.getLenient().getNid())
                        && gbRefsetUUID != null && DialectHelper.isTextForDialect(description.getText(), Language.EN_UK.getLenient().getNid())) {
                    processEnDescription(description, concept);
                } else if (usRefsetUUID != null && DialectHelper.isTextForDialect(description.getText(), Language.EN_US.getLenient().getNid())) {
                    processUSDescription(description, concept);
                } else if (gbRefsetUUID != null && DialectHelper.isTextForDialect(description.getText(), Language.EN_UK.getLenient().getNid())) {
                    processGBDescription(description, concept);
                } else {
                    throw new UnsupportedDialectOrLanguage("Can't support language or dialect.");
                }

                if (description.getAuthorUuid() == null) {
                    description.setAuthorUuid(authorUser);
                }
                if (description.getModuleUuid() == null) {
                    description.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                }

            } catch (UnsupportedDialectOrLanguage e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
            } catch (ValidationException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
            } catch (IOException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformRelationship(org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship)
     */
    @Override
    public void transformRelationship(TkRelationship relationship, TkConcept concept) {
        if (relationship.getPathUuid().equals(pathUUID)) {
            try {
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

                if (relationship.getAuthorUuid() == null) {
                    relationship.setAuthorUuid(authorUser);
                }
                if (relationship.getModuleUuid() == null) {
                    relationship.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                }
            } catch (IOException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformAnnotation(org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember,
     * org.ihtsdo.tk.dto.concept.component.TkComponent)
     */
    @Override
    public void transformAnnotation(TkRefsetAbstractMember<?> annotation,
            TkComponent<?> component) {
        if (annotation.getPathUuid().equals(pathUUID)) {
            try {
                if (annotation.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                    annotation.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (annotation.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    annotation.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                }
                if (annotation.getAuthorUuid() == null) {
                    annotation.setAuthorUuid(authorUser);
                }
                if (annotation.getModuleUuid() == null) {
                    annotation.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                }
            } catch (ValidationException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
            } catch (IOException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformMember(org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember,
     * org.ihtsdo.tk.dto.concept.TkConcept)
     */
    @Override
    public void transformMember(TkRefsetAbstractMember<?> member,
            TkConcept concept) {
        if (member.getPathUuid().equals(pathUUID)) {
            try {
                if (member.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
                    member.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                } else if (member.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
                    member.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
                }
                if (member.getAuthorUuid() == null) {
                    member.setAuthorUuid(authorUser);
                }
                if (member.getModuleUuid() == null) {
                    member.setModuleUuid(TkRevision.unspecifiedModuleUuid);
                }
            } catch (ValidationException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
            } catch (IOException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#postProcessConcept()
     */
    @Override
    public boolean postProcessConcept() {
        return true;
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#postProcessIteration()
     */
    @Override
    public List<EConcept> postProcessIteration() {
        System.out.println("**** Final, total converted " + conceptCount + " members");
        List<EConcept> postProcessList = new ArrayList<EConcept>();
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
        List<TkRefsetAbstractMember<?>> annotations = description.getAnnotations();
        if (annotations == null) {
            annotations = new ArrayList<TkRefsetAbstractMember<?>>();
        }
        if (description.getTypeUuid().equals(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid());
            TkRefsetCidMember usMember = new TkRefsetCidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setC1Uuid(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);

            TkRefsetCidMember gbMember = new TkRefsetCidMember();
            gbMember.setPrimordialComponentUuid(UUID.randomUUID());
            gbMember.setAuthorUuid(description.getAuthorUuid());
            gbMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            gbMember.setC1Uuid(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            gbMember.setComponentUuid(description.getPrimordialComponentUuid());
            gbMember.setPathUuid(description.getPathUuid());
            gbMember.setRefsetUuid(gbRefsetUUID);
            gbMember.setStatusUuid(description.getStatusUuid());
            gbMember.setTime(description.getTime());
            annotations.add(gbMember);
            description.setAnnotations(annotations);
        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefsetCidMember usMember = new TkRefsetCidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setC1Uuid(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);

            TkRefsetCidMember gbMember = new TkRefsetCidMember();
            gbMember.setPrimordialComponentUuid(UUID.randomUUID());
            gbMember.setAuthorUuid(description.getAuthorUuid());
            gbMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            gbMember.setC1Uuid(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            gbMember.setComponentUuid(description.getPrimordialComponentUuid());
            gbMember.setPathUuid(description.getPathUuid());
            gbMember.setRefsetUuid(gbRefsetUUID);
            gbMember.setStatusUuid(description.getStatusUuid());
            gbMember.setTime(description.getTime());
            annotations.add(gbMember);
            description.setAnnotations(annotations);

        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefsetCidMember usMember = new TkRefsetCidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setC1Uuid(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);

            TkRefsetCidMember gbMember = new TkRefsetCidMember();
            gbMember.setPrimordialComponentUuid(UUID.randomUUID());
            gbMember.setAuthorUuid(description.getAuthorUuid());
            gbMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            gbMember.setC1Uuid(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
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
        List<TkRefsetAbstractMember<?>> annotations = description.getAnnotations();
        if (annotations == null) {
            annotations = new ArrayList<TkRefsetAbstractMember<?>>();
        }
        if (description.getTypeUuid().equals(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid());
            TkRefsetCidMember usMember = new TkRefsetCidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setC1Uuid(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);

            TkRefsetCidMember gbMember = new TkRefsetCidMember();
            gbMember.setPrimordialComponentUuid(UUID.randomUUID());
            gbMember.setAuthorUuid(description.getAuthorUuid());
            gbMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            gbMember.setC1Uuid(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            gbMember.setComponentUuid(description.getPrimordialComponentUuid());
            gbMember.setPathUuid(description.getPathUuid());
            gbMember.setRefsetUuid(gbRefsetUUID);
            gbMember.setStatusUuid(description.getStatusUuid());
            gbMember.setTime(description.getTime());
            annotations.add(gbMember);
            description.setAnnotations(annotations);
        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefsetCidMember usMember = new TkRefsetCidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setC1Uuid(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);
        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefsetCidMember usMember = new TkRefsetCidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setC1Uuid(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);

            TkRefsetCidMember gbMember = new TkRefsetCidMember();
            gbMember.setPrimordialComponentUuid(UUID.randomUUID());
            gbMember.setAuthorUuid(description.getAuthorUuid());
            gbMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            gbMember.setC1Uuid(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
            gbMember.setComponentUuid(description.getPrimordialComponentUuid());
            gbMember.setPathUuid(description.getPathUuid());
            gbMember.setRefsetUuid(gbRefsetUUID);
            gbMember.setStatusUuid(description.getStatusUuid());
            gbMember.setTime(description.getTime());
            annotations.add(gbMember);
            description.setAnnotations(annotations);

        }
    }

    private void processGBDescription(TkDescription description, TkConcept concept) throws ValidationException, IOException {
        //no FSN transform since current use of RF2 only uses US dialect as FSN
        List<TkRefsetAbstractMember<?>> annotations = description.getAnnotations();
        if (annotations == null) {
            annotations = new ArrayList<TkRefsetAbstractMember<?>>();
        }
        if (description.getTypeUuid().equals(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefsetCidMember gbMember = new TkRefsetCidMember();
            gbMember.setPrimordialComponentUuid(UUID.randomUUID());
            gbMember.setAuthorUuid(description.getAuthorUuid());
            gbMember.setC1Uuid(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            gbMember.setComponentUuid(description.getPrimordialComponentUuid());
            gbMember.setPathUuid(description.getPathUuid());
            gbMember.setRefsetUuid(gbRefsetUUID);
            gbMember.setStatusUuid(description.getStatusUuid());
            gbMember.setTime(description.getTime());
            annotations.add(gbMember);
            description.setAnnotations(annotations);
        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefsetCidMember usMember = new TkRefsetCidMember();
            usMember.setPrimordialComponentUuid(UUID.randomUUID());
            usMember.setAuthorUuid(description.getAuthorUuid());
            usMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            usMember.setC1Uuid(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
            usMember.setComponentUuid(description.getPrimordialComponentUuid());
            usMember.setPathUuid(description.getPathUuid());
            usMember.setRefsetUuid(usRefsetUUID);
            usMember.setStatusUuid(description.getStatusUuid());
            usMember.setTime(description.getTime());
            annotations.add(usMember);
            description.setAnnotations(annotations);

            TkRefsetCidMember gbMember = new TkRefsetCidMember();
            gbMember.setPrimordialComponentUuid(UUID.randomUUID());
            gbMember.setAuthorUuid(description.getAuthorUuid());
            gbMember.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            gbMember.setC1Uuid(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
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
        List<TkRefsetAbstractMember<?>> annotations = description.getAnnotations();
        if (annotations == null) {
            annotations = new ArrayList<TkRefsetAbstractMember<?>>();
        }
        if (description.getTypeUuid().equals(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid());
            TkRefsetCidMember member = new TkRefsetCidMember();
            member.setPrimordialComponentUuid(UUID.randomUUID());
            member.setAuthorUuid(description.getAuthorUuid());
            member.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            member.setC1Uuid(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            member.setComponentUuid(description.getPrimordialComponentUuid());
            member.setPathUuid(description.getPathUuid());
            member.setRefsetUuid(langRefsetUUID);
            member.setStatusUuid(description.getStatusUuid());
            member.setTime(description.getTime());
            annotations.add(member);
            description.setAnnotations(annotations);
        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefsetCidMember member = new TkRefsetCidMember();
            member.setPrimordialComponentUuid(UUID.randomUUID());
            member.setAuthorUuid(description.getAuthorUuid());
            member.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            member.setC1Uuid(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
            member.setComponentUuid(description.getPrimordialComponentUuid());
            member.setPathUuid(description.getPathUuid());
            member.setRefsetUuid(langRefsetUUID);
            member.setStatusUuid(description.getStatusUuid());
            member.setTime(description.getTime());
            annotations.add(member);
            description.setAnnotations(annotations);
        } else if (description.getTypeUuid().equals(SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
            description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
            TkRefsetCidMember member = new TkRefsetCidMember();
            member.setPrimordialComponentUuid(UUID.randomUUID());
            member.setAuthorUuid(description.getAuthorUuid());
            member.setModuleUuid(TkRevision.unspecifiedModuleUuid);
            member.setC1Uuid(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
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
