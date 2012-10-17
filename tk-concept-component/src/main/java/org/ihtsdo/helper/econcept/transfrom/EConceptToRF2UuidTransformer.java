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
import org.ihtsdo.helper.rf2.Rf2File;
import org.ihtsdo.helper.rf2.Rf2File.ConceptsFileFields;
import org.ihtsdo.helper.rf2.Rf2File.DescriptionsFileFields;
import org.ihtsdo.helper.rf2.Rf2File.IdentifiersFileFields;
import org.ihtsdo.helper.rf2.Rf2File.RelationshipsFileFields;
import org.ihtsdo.helper.rf2.Rf2File.ReleaseType;


/**
 * The Class EConceptToRF2UuidTransformer converts an eConcept into a uuid-based
 * RF2 release file.
 *
 */
public class EConceptToRF2UuidTransformer implements EConceptTransformerBI {

    Writer conceptsWriter;
    COUNTRY_CODE country;
    Writer descriptionsWriter;
    Date effectiveDate;
    Writer identifiersWriter;
    LANG_CODE language;
    String namespace;
    Writer relationshipsWriter;
    ReleaseType releaseType;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new eConcept to RF2 uuid transformer.
     *
     * @param directory the directory to write the files to
     * @param releaseType the type of release
     * @param language the language associated with the
     * @param country the country associated with the release
     * @param namespace the namespace associated with the release
     * @param effectiveDate the publication date
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
    /**
     *
     * @throws IOException signals an I/O exception has occurred
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

    /**
     * Writes the concept's components to the RF2 files.
     *
     * @param tkConcept the concept to process
     * @throws Exception indicates an exception has occurred
     */
    @Override
    public void process(TkConcept tkConcept) throws Exception {
        TkConceptAttributes ca = tkConcept.getConceptAttributes();

        processConceptAttribute(ca);

        if (tkConcept.getDescriptions() != null) {
            for (TkDescription d : tkConcept.getDescriptions()) {
                processDescription(d);
            }
        }

        if (tkConcept.getRelationships() != null) {
            for (TkRelationship r : tkConcept.getRelationships()) {
                processRelationship(r);
            }
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Converts the concept attribute to uuids and writes the concept file according
     * to the
     * <code>ConceptsFileFields</code>.
     *
     * @param tkConceptAttributes the concept attributes to process
     * @throws IOException signals that an I/O exception has occurred
     * @see Rf2File.ConceptsFileFields
     */
    private void processConceptAttribute(TkConceptAttributes tkConceptAttributes) throws IOException {
        if (tkConceptAttributes != null) {
            for (ConceptsFileFields field : ConceptsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        conceptsWriter.write(tkConceptAttributes.getStatusUuid().toString() + "\t");

                        break;

                    case DEFINITION_STATUS_ID:
                        conceptsWriter.write(tkConceptAttributes.isDefined() + "\n");

                        break;

                    case EFFECTIVE_TIME:
                        conceptsWriter.write(TimeHelper.formatDateForFile(tkConceptAttributes.getTime()) + "\t");

                        break;

                    case ID:
                        conceptsWriter.write(tkConceptAttributes.getPrimordialComponentUuid().toString() + "\t");

                        break;

                    case MODULE_ID:
                        conceptsWriter.write(namespace + "\t");

                        break;
                }
            }

            if (tkConceptAttributes.revisions != null) {
                for (TkConceptAttributesRevision car : tkConceptAttributes.revisions) {
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
                                conceptsWriter.write(tkConceptAttributes.getPrimordialComponentUuid().toString() + "\t");

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
     * Converts the description to uuids and writes the description file according
     * to the
     * <code>DescriptionsFileFields</code>.
     *
     * @param tkDescription the description to process
     * @throws IOException signals that an I/O exception has occurred
     * @see Rf2File.DescriptionsFileFields
     */
    private void processDescription(TkDescription tkDescription) throws IOException {
        if (tkDescription != null) {
            for (DescriptionsFileFields field : DescriptionsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        descriptionsWriter.write(tkDescription.getStatusUuid().toString() + "\t");

                        break;

                    case EFFECTIVE_TIME:
                        descriptionsWriter.write(TimeHelper.formatDateForFile(tkDescription.getTime()) + "\t");

                        break;

                    case ID:
                        descriptionsWriter.write(tkDescription.getPrimordialComponentUuid().toString() + "\t");

                        break;

                    case MODULE_ID:
                        descriptionsWriter.write(namespace + "\t");

                        break;

                    case CONCEPT_ID:
                        descriptionsWriter.write(tkDescription.getConceptUuid() + "\t");

                        break;

                    case LANGUAGE_CODE:
                        descriptionsWriter.write(tkDescription.getLang() + "\t");

                        break;

                    case TYPE_ID:
                        descriptionsWriter.write(tkDescription.getTypeUuid() + "\t");

                        break;

                    case TERM:
                        descriptionsWriter.write(tkDescription.getText() + "\t");

                        break;

                    case CASE_SIGNIFICANCE_ID:
                        descriptionsWriter.write(tkDescription.isInitialCaseSignificant() + "\n");

                        break;
                }
            }

            if (tkDescription.revisions != null) {
                for (TkDescriptionRevision descr : tkDescription.revisions) {
                    for (DescriptionsFileFields field : DescriptionsFileFields.values()) {
                        switch (field) {
                            case ACTIVE:
                                descriptionsWriter.write(descr.getStatusUuid().toString() + "\t");

                                break;

                            case EFFECTIVE_TIME:
                                descriptionsWriter.write(TimeHelper.formatDateForFile(descr.getTime()) + "\t");

                                break;

                            case ID:
                                descriptionsWriter.write(tkDescription.getPrimordialComponentUuid().toString() + "\t");

                                break;

                            case MODULE_ID:
                                descriptionsWriter.write(namespace + "\t");

                                break;

                            case CONCEPT_ID:
                                descriptionsWriter.write(tkDescription.getConceptUuid() + "\t");

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
     * Converts the relationship to uuids and writes the relationship file according
     * to the
     * <code>RelationshipsFileFields</code>.
     *
     * @param tkRelationship the relationship to process
     * @throws IOException signals that an I/O exception has occurred
     * @see Rf2File.RelationshipsFileFields
     */
    private void processRelationship(TkRelationship tkRelationship) throws IOException {
        if (tkRelationship != null) {
            for (RelationshipsFileFields field : RelationshipsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        relationshipsWriter.write(tkRelationship.getStatusUuid().toString() + "\t");

                        break;

                    case EFFECTIVE_TIME:
                        relationshipsWriter.write(TimeHelper.formatDateForFile(tkRelationship.getTime()) + "\t");

                        break;

                    case ID:
                        relationshipsWriter.write(tkRelationship.getPrimordialComponentUuid().toString() + "\t");

                        break;

                    case MODULE_ID:
                        relationshipsWriter.write(namespace + "\t");

                        break;

                    case SOURCE_ID:
                        relationshipsWriter.write(tkRelationship.getRelationshipSourceUuid() + "\t");

                        break;

                    case DESTINATION_ID:
                        relationshipsWriter.write(tkRelationship.getRelationshipTargetUuid() + "\t");

                        break;

                    case RELATIONSHIP_GROUP:
                        relationshipsWriter.write(tkRelationship.getRelationshipGroup() + "\t");

                        break;

                    case TYPE_ID:
                        relationshipsWriter.write(tkRelationship.getTypeUuid() + "\t");

                        break;

                    case CHARCTERISTIC_ID:
                        relationshipsWriter.write(tkRelationship.getCharacteristicUuid() + "\t");

                        break;

                    case MODIFIER_ID:
                        relationshipsWriter.write(tkRelationship.getRefinabilityUuid() + "\n");

                        break;
                }
            }

            if (tkRelationship.revisions != null) {
                for (TkRelationshipRevision rv : tkRelationship.revisions) {
                    for (RelationshipsFileFields field : RelationshipsFileFields.values()) {
                        switch (field) {
                            case ACTIVE:
                                relationshipsWriter.write(rv.getStatusUuid().toString() + "\t");

                                break;

                            case EFFECTIVE_TIME:
                                relationshipsWriter.write(TimeHelper.formatDateForFile(rv.getTime()) + "\t");

                                break;

                            case ID:
                                relationshipsWriter.write(tkRelationship.getPrimordialComponentUuid().toString() + "\t");

                                break;

                            case MODULE_ID:
                                relationshipsWriter.write(namespace + "\t");

                                break;

                            case SOURCE_ID:
                                relationshipsWriter.write(tkRelationship.getRelationshipSourceUuid() + "\t");

                                break;

                            case DESTINATION_ID:
                                relationshipsWriter.write(tkRelationship.getRelationshipTargetUuid() + "\t");

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
