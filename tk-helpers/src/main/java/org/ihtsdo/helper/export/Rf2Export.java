/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.helper.export;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.country.COUNTRY_CODE;
import org.ihtsdo.helper.rf2.Rf2File;
import org.ihtsdo.helper.rf2.Rf2File.ReleaseType;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.conattr.ConAttrChronicleBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.*;

import java.util.Date;
import java.util.Set;

/**
 *
 * @author kec
 */
public class Rf2Export implements ProcessUnfetchedConceptDataBI {
   NidBitSetBI        conceptsToProcess;
   Writer             conceptsWriter;
   COUNTRY_CODE       country;
   Writer             descriptionsWriter;
   Date               effectiveDate;
   String             effectiveDateString;
   Writer             identifiersWriter;
   LANG_CODE          language;
   String             namespace;
   Writer             relationshipsWriter;
   ReleaseType        releaseType;
   Set<Integer>       sapNids;
   TerminologyStoreDI store;
   ViewCoordinate     vc;

   //~--- constructors --------------------------------------------------------

   public Rf2Export(File directory, ReleaseType releaseType, LANG_CODE language, COUNTRY_CODE country,
                    String namespace, Date effectiveDate, Set<Integer> sapNids, ViewCoordinate vc,
                    NidBitSetBI conceptsToProcess)
           throws IOException {
      directory.mkdirs();
      this.releaseType         = releaseType;
      this.effectiveDate       = effectiveDate;
      this.language            = language;
      this.country             = country;
      this.namespace           = namespace;
      this.sapNids             = sapNids;
      this.store               = Ts.get();
      this.vc                  = vc;
      this.conceptsToProcess   = conceptsToProcess;
      this.effectiveDateString = TimeHelper.formatDateForFile(effectiveDate.getTime());

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

      conceptsWriter      = new BufferedWriter(new FileWriter(conceptsFile));
      descriptionsWriter  = new BufferedWriter(new FileWriter(descriptionsFile));
      relationshipsWriter = new BufferedWriter(new FileWriter(relationshipsFile));
      identifiersWriter   = new BufferedWriter(new FileWriter(identifiersFile));

      for (Rf2File.ConceptsFileFields field : Rf2File.ConceptsFileFields.values()) {
         conceptsWriter.write(field.headerText + field.seperator);
      }

      for (Rf2File.DescriptionsFileFields field : Rf2File.DescriptionsFileFields.values()) {
         descriptionsWriter.write(field.headerText + field.seperator);
      }

      for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields.values()) {
         relationshipsWriter.write(field.headerText + field.seperator);
      }

      for (Rf2File.IdentifiersFileFields field : Rf2File.IdentifiersFileFields.values()) {
         identifiersWriter.write(field.headerText + field.seperator);
      }
   }

   //~--- methods -------------------------------------------------------------

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

   @Override
   public boolean continueWork() {
      return true;
   }

   public void process(ConceptChronicleBI c) throws Exception {
      ConAttrChronicleBI ca = c.getConAttrs();

      processConceptAttribute(ca);

      if (c.getDescs() != null) {
         for (DescriptionChronicleBI d : c.getDescs()) {
            processDescription(d);
         }
      }

      if (c.getRelsOutgoing() != null) {
         for (RelationshipChronicleBI r : c.getRelsOutgoing()) {
            processRelationship(r);
         }
      }
   }

   private void processConceptAttribute(ConAttrChronicleBI ca) throws IOException {
      if (ca != null) {
         for (ConAttrVersionBI car : ca.getVersions(vc)) {
            if (sapNids.contains(car.getSapNid())) {
               for (Rf2File.ConceptsFileFields field : Rf2File.ConceptsFileFields.values()) {
                  switch (field) {
                  case ACTIVE :
                     conceptsWriter.write(store.getUuidPrimordialForNid(car.getStatusNid())
                                          + field.seperator);

                     break;

                  case DEFINITION_STATUS_ID :
                     conceptsWriter.write(car.isDefined() + field.seperator);

                     break;

                  case EFFECTIVE_TIME :
                     conceptsWriter.write(effectiveDateString + field.seperator);

                     break;

                  case ID :
                     conceptsWriter.write(store.getUuidPrimordialForNid(car.getNid()) + field.seperator);

                     break;

                  case MODULE_ID :
                     conceptsWriter.write(namespace + field.seperator);

                     break;
                  }
               }
            }
         }
      }
   }

   private void processDescription(DescriptionChronicleBI desc) throws IOException {
      if (desc != null) {
         for (DescriptionVersionBI descr : desc.getVersions(vc)) {
            if (sapNids.contains(descr.getSapNid())) {
               for (Rf2File.DescriptionsFileFields field : Rf2File.DescriptionsFileFields.values()) {
                  switch (field) {
                  case ACTIVE :
                     descriptionsWriter.write(store.getUuidPrimordialForNid(descr.getStatusNid())
                                              + field.seperator);

                     break;

                  case EFFECTIVE_TIME :
                     descriptionsWriter.write(effectiveDateString + field.seperator);

                     break;

                  case ID :
                     descriptionsWriter.write(store.getUuidPrimordialForNid(desc.getNid()) + field.seperator);

                     break;

                  case MODULE_ID :
                     descriptionsWriter.write(namespace + field.seperator);

                     break;

                  case CONCEPT_ID :
                     descriptionsWriter.write(store.getUuidPrimordialForNid(desc.getConceptNid())
                                              + field.seperator);

                     break;

                  case LANGUAGE_CODE :
                     descriptionsWriter.write(descr.getLang() + field.seperator);

                     break;

                  case TYPE_ID :
                     descriptionsWriter.write(store.getUuidPrimordialForNid(descr.getTypeNid())
                                              + field.seperator);

                     break;

                  case TERM :
                     descriptionsWriter.write(descr.getText() + field.seperator);

                     break;

                  case CASE_SIGNIFICANCE_ID :
                     descriptionsWriter.write(descr.isInitialCaseSignificant() + field.seperator);

                     break;
                  }
               }
            }
         }
      }
   }

   private void processRelationship(RelationshipChronicleBI r) throws IOException {
      if (r != null) {
         for (RelationshipVersionBI rv : r.getVersions(vc)) {
            if (sapNids.contains(rv.getSapNid())) {
               for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields.values()) {
                  switch (field) {
                  case ACTIVE :
                     relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getStatusNid())
                                               + field.seperator);

                     break;

                  case EFFECTIVE_TIME :
                     relationshipsWriter.write(effectiveDateString + field.seperator);

                     break;

                  case ID :
                     relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getNid()) + field.seperator);

                     break;

                  case MODULE_ID :
                     relationshipsWriter.write(namespace + field.seperator);

                     break;

                  case SOURCE_ID :
                     relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getOriginNid())
                                               + field.seperator);

                     break;

                  case DESTINATION_ID :
                     relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getDestinationNid())
                                               + field.seperator);

                     break;

                  case RELATIONSHIP_GROUP :
                     relationshipsWriter.write(rv.getGroup() + field.seperator);

                     break;

                  case TYPE_ID :
                     relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getTypeNid())
                                               + field.seperator);

                     break;

                  case CHARCTERISTIC_ID :
                     relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getCharacteristicNid())
                                               + field.seperator);

                     break;

                  case MODIFIER_ID :
                     relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getRefinabilityNid())
                                               + field.seperator);

                     break;
                  }
               }
            }
         }
      }
   }

   @Override
   public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
      process(fetcher.fetch());
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public NidBitSetBI getNidSet() throws IOException {
      return conceptsToProcess;
   }
}
