/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.helper.econcept.transfrom;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.country.COUNTRY_CODE;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.*;

import java.util.Date;
import org.ihtsdo.helper.rf2.Rf2File.ConceptsFileFields;
import org.ihtsdo.helper.rf2.Rf2File.DescriptionsFileFields;
import org.ihtsdo.helper.rf2.Rf2File.IdentifiersFileFields;
import org.ihtsdo.helper.rf2.Rf2File.RelationshipsFileFields;
import org.ihtsdo.helper.rf2.Rf2File.ReleaseType;

// TODO: Auto-generated Javadoc
/**
 * The Class EConceptToRF2UuidTransformer.
 *
 * @author kec
 */
public class EConceptToRF2UuidTransformer implements EConceptTransformerBI {

    /**
     * The concepts writer.
     */
    Writer conceptsWriter;
    /**
     * The country.
     */
    COUNTRY_CODE country;
    /**
     * The descriptions writer.
     */
    Writer descriptionsWriter;
    /**
     * The effective date.
     */
    Date effectiveDate;
    /**
     * The identifiers writer.
     */
    Writer identifiersWriter;
    /**
     * The language.
     */
    LANG_CODE language;
    /**
     * The namespace.
     */
    String namespace;
    /**
     * The relationships writer.
     */
    Writer relationshipsWriter;
    /**
     * The release type.
     */
    ReleaseType releaseType;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new e concept to r f2 uuid transformer.
     *
     * @param directory the directory
     * @param releaseType the release type
     * @param language the language
     * @param country the country
     * @param namespace the namespace
     * @param effectiveDate the effective date
     * @throws IOException signals that an I/O exception has occurred
     */
    public EConceptToRF2UuidTransformer(File directory, ReleaseType releaseType, LANG_CODE language,
            COUNTRY_CODE country, String namespace, Date effectiveDate)
            throws IOException {
        directory.mkdirs();
        this.releaseType = releaseType;
        this.language = language;
        this.country = country;
        this.namespace = namespace;

        File conceptsFile = new File(directory,
                "sct2_Concept_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File descriptionsFile = new File(directory,
                "sct2_Description_" + releaseType.suffix + "-"
                + language.getFormatedLanguageCode() + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File relationshipsFile = new File(directory,
                "sct2_Relationship_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File identifiersFile = new File(directory,
                "sct2_Identifier_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");

        conceptsWriter = new BufferedWriter(new FileWriter(conceptsFile));
        descriptionsWriter = new BufferedWriter(new FileWriter(descriptionsFile));
        relationshipsWriter = new BufferedWriter(new FileWriter(relationshipsFile));
        identifiersWriter = new BufferedWriter(new FileWriter(identifiersFile));

        for (ConceptsFileFields field : ConceptsFileFields.values()) {
            conceptsWriter.write(field.headerText);

            if (field != ConceptsFileFields.DEFINITION_STATUS_ID) {
                conceptsWriter.write("\t");
            } else {
                conceptsWriter.write("\n");
            }
        }

        for (DescriptionsFileFields field : DescriptionsFileFields.values()) {
            descriptionsWriter.write(field.headerText);

            if (field != DescriptionsFileFields.CASE_SIGNIFICANCE_ID) {
                descriptionsWriter.write("\t");
            } else {
                descriptionsWriter.write("\n");
            }
        }

        for (RelationshipsFileFields field : RelationshipsFileFields.values()) {
            relationshipsWriter.write(field.headerText);

            if (field != RelationshipsFileFields.MODIFIER_ID) {
                relationshipsWriter.write("\t");
            } else {
                relationshipsWriter.write("\n");
            }
        }

        for (IdentifiersFileFields field : IdentifiersFileFields.values()) {
            identifiersWriter.write(field.headerText);

            if (field != IdentifiersFileFields.REFERENCED_COMPONENT_ID) {
                identifiersWriter.write("\t");
            } else {
                identifiersWriter.write("\n");
            }
        }
    }

    //~--- methods -------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.ihtsdo.helper.econcept.transfrom.EConceptTransformerBI#close()
     */
    @Override
    public void close() throws IOException {
        if (conceptsWriter != null) {
            conceptsWriter.close();
        }

        if (descriptionsWriter != null) {
            descriptionsWriter.close();
        }

        if (relationshipsWriter != null) {
            relationshipsWriter.close();
        }

        if (identifiersWriter != null) {
            identifiersWriter.close();
        }
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.helper.econcept.transfrom.EConceptTransformerBI#process(org.ihtsdo.tk.dto.concept.TkConcept)
     */
    @Override
    public void process(TkConcept c) throws Exception {
        TkConceptAttributes ca = c.getConceptAttributes();

        processConceptAttribute(ca);

        if (c.getDescriptions() != null) {
            for (TkDescription d : c.getDescriptions()) {
                processDescription(d);
            }
        }

        if (c.getRelationships() != null) {
            for (TkRelationship r : c.getRelationships()) {
                processRelationship(r);
            }
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Process concept attribute.
     *
     * @param ca the ca
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processConceptAttribute(TkConceptAttributes ca) throws IOException {
        if (ca != null) {
            for (ConceptsFileFields field : ConceptsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        conceptsWriter.write(ca.getStatusUuid().toString() + "\t");

                        break;

                    case DEFINITION_STATUS_ID:
                        conceptsWriter.write(ca.isDefined() + "\n");

                        break;

                    case EFFECTIVE_TIME:
                        conceptsWriter.write(TimeHelper.formatDateForFile(ca.getTime()) + "\t");

                        break;

                    case ID:
                        conceptsWriter.write(ca.getPrimordialComponentUuid().toString() + "\t");

                        break;

                    case MODULE_ID:
                        conceptsWriter.write(namespace + "\t");

                        break;
                }
            }

            if (ca.revisions != null) {
                for (TkConceptAttributesRevision car : ca.revisions) {
                    for (ConceptsFileFields field : ConceptsFileFields.values()) {
                        switch (field) {
                            case ACTIVE:
                                conceptsWriter.write(car.getStatusUuid().toString() + "\t");

                                break;

                            case DEFINITION_STATUS_ID:
                                conceptsWriter.write(car.isDefined() + "\n");

                                break;

                            case EFFECTIVE_TIME:
                                conceptsWriter.write(TimeHelper.formatDateForFile(car.getTime()) + "\t");

                                break;

                            case ID:
                                conceptsWriter.write(ca.getPrimordialComponentUuid().toString() + "\t");

                                break;

                            case MODULE_ID:
                                conceptsWriter.write(namespace + "\n");

                                break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Process description.
     *
     * @param desc the desc
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processDescription(TkDescription desc) throws IOException {
        if (desc != null) {
            for (DescriptionsFileFields field : DescriptionsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        descriptionsWriter.write(desc.getStatusUuid().toString() + "\t");

                        break;

                    case EFFECTIVE_TIME:
                        descriptionsWriter.write(TimeHelper.formatDateForFile(desc.getTime()) + "\t");

                        break;

                    case ID:
                        descriptionsWriter.write(desc.getPrimordialComponentUuid().toString() + "\t");

                        break;

                    case MODULE_ID:
                        descriptionsWriter.write(namespace + "\t");

                        break;

                    case CONCEPT_ID:
                        descriptionsWriter.write(desc.getConceptUuid() + "\t");

                        break;

                    case LANGUAGE_CODE:
                        descriptionsWriter.write(desc.getLang() + "\t");

                        break;

                    case TYPE_ID:
                        descriptionsWriter.write(desc.getTypeUuid() + "\t");

                        break;

                    case TERM:
                        descriptionsWriter.write(desc.getText() + "\t");

                        break;

                    case CASE_SIGNIFICANCE_ID:
                        descriptionsWriter.write(desc.isInitialCaseSignificant() + "\n");

                        break;
                }
            }

            if (desc.revisions != null) {
                for (TkDescriptionRevision descr : desc.revisions) {
                    for (DescriptionsFileFields field : DescriptionsFileFields.values()) {
                        switch (field) {
                            case ACTIVE:
                                descriptionsWriter.write(descr.getStatusUuid().toString() + "\t");

                                break;

                            case EFFECTIVE_TIME:
                                descriptionsWriter.write(TimeHelper.formatDateForFile(descr.getTime()) + "\t");

                                break;

                            case ID:
                                descriptionsWriter.write(desc.getPrimordialComponentUuid().toString() + "\t");

                                break;

                            case MODULE_ID:
                                descriptionsWriter.write(namespace + "\t");

                                break;

                            case CONCEPT_ID:
                                descriptionsWriter.write(desc.getConceptUuid() + "\t");

                                break;

                            case LANGUAGE_CODE:
                                descriptionsWriter.write(descr.getLang() + "\t");

                                break;

                            case TYPE_ID:
                                descriptionsWriter.write(descr.getTypeUuid() + "\t");

                                break;

                            case TERM:
                                descriptionsWriter.write(descr.getText() + "\t");

                                break;

                            case CASE_SIGNIFICANCE_ID:
                                descriptionsWriter.write(descr.isInitialCaseSignificant() + "\n");

                                break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Process relationship.
     *
     * @param r the r
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processRelationship(TkRelationship r) throws IOException {
        if (r != null) {
            for (RelationshipsFileFields field : RelationshipsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        relationshipsWriter.write(r.getStatusUuid().toString() + "\t");

                        break;

                    case EFFECTIVE_TIME:
                        relationshipsWriter.write(TimeHelper.formatDateForFile(r.getTime()) + "\t");

                        break;

                    case ID:
                        relationshipsWriter.write(r.getPrimordialComponentUuid().toString() + "\t");

                        break;

                    case MODULE_ID:
                        relationshipsWriter.write(namespace + "\t");

                        break;

                    case SOURCE_ID:
                        relationshipsWriter.write(r.getRelationshipSourceUuid() + "\t");

                        break;

                    case DESTINATION_ID:
                        relationshipsWriter.write(r.getRelationshipTargetUuid() + "\t");

                        break;

                    case RELATIONSHIP_GROUP:
                        relationshipsWriter.write(r.getRelationshipGroup() + "\t");

                        break;

                    case TYPE_ID:
                        relationshipsWriter.write(r.getTypeUuid() + "\t");

                        break;

                    case CHARCTERISTIC_ID:
                        relationshipsWriter.write(r.getCharacteristicUuid() + "\t");

                        break;

                    case MODIFIER_ID:
                        relationshipsWriter.write(r.getRefinabilityUuid() + "\n");

                        break;
                }
            }

            if (r.revisions != null) {
                for (TkRelationshipRevision rv : r.revisions) {
                    for (RelationshipsFileFields field : RelationshipsFileFields.values()) {
                        switch (field) {
                            case ACTIVE:
                                relationshipsWriter.write(rv.getStatusUuid().toString() + "\t");

                                break;

                            case EFFECTIVE_TIME:
                                relationshipsWriter.write(TimeHelper.formatDateForFile(rv.getTime()) + "\t");

                                break;

                            case ID:
                                relationshipsWriter.write(r.getPrimordialComponentUuid().toString() + "\t");

                                break;

                            case MODULE_ID:
                                relationshipsWriter.write(namespace + "\t");

                                break;

                            case SOURCE_ID:
                                relationshipsWriter.write(r.getRelationshipSourceUuid() + "\t");

                                break;

                            case DESTINATION_ID:
                                relationshipsWriter.write(r.getRelationshipTargetUuid() + "\t");

                                break;

                            case RELATIONSHIP_GROUP:
                                relationshipsWriter.write(rv.getGroup() + "\t");

                                break;

                            case TYPE_ID:
                                relationshipsWriter.write(rv.getTypeUuid() + "\t");

                                break;

                            case CHARCTERISTIC_ID:
                                relationshipsWriter.write(rv.getCharacteristicUuid() + "\t");

                                break;

                            case MODIFIER_ID:
                                relationshipsWriter.write(rv.getRefinabilityUuid() + "\n");

                                break;
                        }
                    }
                }
            }
        }
    }
}
