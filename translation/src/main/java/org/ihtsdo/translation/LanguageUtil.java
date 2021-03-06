/**
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.translation;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.apache.lucene.document.Document;
import org.apache.lucene.store.AlreadyClosedException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.LanguageSpecRefset;
import org.ihtsdo.project.view.PanelHelperFactory;
import org.ihtsdo.project.view.TranslationHelperPanel;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.translation.tasks.PutsDescriptionsInLanguageRefset;
import org.ihtsdo.translation.tasks.commit.TranslationTermChangeListener;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.SimpleTranslationConceptEditor;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.ihtsdo.tk.query.helper.RefsetHelper;

/**
 * The Class LanguageUtil.
 */
public class LanguageUtil {

	/** The ADVANCE d_ ui. */
	public static int ADVANCED_UI = 0;

	/** The SIMPL e_ ui. */
	public static int SIMPLE_UI = 1;

	private static TranslationTermChangeListener translationTermChangeListener;

	/**
	 * The Enum Language.
	 */
	public static enum Language {

		/** The danish. */
		danish,
		/** The spanish. */
		spanish,
		/** The swedish. */
		swedish,
		/** The french_canadian. */
		french_canadian,
		/** The english_us. */
		english_us,
		/** The english_uk. */
		english_uk
	}

	/**
	 * Contextualize selected refset descriptions.
	 * 
	 * @param englishLanguageRefsetConcept
	 *            the english language refset concept
	 * @param languagePath
	 *            the language path
	 * @param langCode
	 *            the lang code
	 * @param config
	 *            the config
	 */
	public static void contextualizeSelectedRefsetDescriptions(I_GetConceptData englishLanguageRefsetConcept, I_GetConceptData languagePath, String langCode, I_ConfigAceFrame config) {
		I_TermFactory tf = Terms.get();

		try {
			I_GetConceptData preferred = tf.getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
			I_GetConceptData synonym = tf.getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
			I_GetConceptData fsn = tf.getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			I_GetConceptData acceptable = tf.getConcept(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid());

			config.getDescTypes().clear();
			config.getDescTypes().add(preferred.getConceptNid());
			config.getDescTypes().add(synonym.getConceptNid());
			config.getDescTypes().add(fsn.getConceptNid());

			I_GetConceptData snomedRoot = tf.getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));

			I_IntSet isaType = tf.newIntSet();
			isaType.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			long start = System.currentTimeMillis();
			tf.iterateConcepts(new PutsDescriptionsInLanguageRefset(snomedRoot, config, isaType, englishLanguageRefsetConcept, preferred, acceptable, fsn, synonym));
			long end = System.currentTimeMillis();
			System.out.println("Finished!! - Time: " + ((start - end) / 1000) + " seconds.");
			tf.commit();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Import descriptions file.
	 * 
	 * @param descriptionsFile
	 *            the descriptions file
	 * @param release
	 *            the release
	 * @param language
	 *            the language
	 */
	public static void importDescriptionsFile(File descriptionsFile, String release, Language language) {
		I_TermFactory tf = Terms.get();

		try {

			I_GetConceptData languageRefset = null;
			I_GetConceptData languagePath = null;
			I_GetConceptData languageCode = null;
			// TODO some language refset still belong to architectonic
			switch (language) {
			case danish:
				languageRefset = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_REFSET_DA.getUids());
				languagePath = tf.getConcept(ArchitectonicAuxiliary.Concept.SNOMED_LANGUAGE_DA_PATH.getUids());
				languageCode = tf.getConcept(ArchitectonicAuxiliary.Concept.DA_DK.getUids());
				break;
			case spanish:
				languageRefset = tf.getConcept(UUID.fromString("03615ef2-aa56-336d-89c5-a1b5c4cee8f6"));
				languagePath = tf.getConcept(ArchitectonicAuxiliary.Concept.SNOMED_LANGUAGE_ES_PATH.getUids());
				languageCode = tf.getConcept(ArchitectonicAuxiliary.Concept.ES.getUids());
				break;
			case swedish:
				languageRefset = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_REFSET_SV_SE.getUids());
				languagePath = tf.getConcept(ArchitectonicAuxiliary.Concept.SNOMED_LANGUAGE_SE_PATH.getUids());
				languageCode = tf.getConcept(ArchitectonicAuxiliary.Concept.SV_SE.getUids());
				break;
			case french_canadian:
				// languageRefset =
				// tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_REFSET_FR_CA.getUids());
				languageRefset = tf.getConcept(UUID.fromString("15d652f0-d246-54f1-914d-956cd3bd265a"));
				languagePath = tf.getConcept(ArchitectonicAuxiliary.Concept.SNOMED_LANGUAGE_FR_CA_PATH.getUids());
				languageCode = tf.getConcept(ArchitectonicAuxiliary.Concept.FR_CA.getUids());
				break;
			case english_us:
				languageRefset = tf.getConcept(SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getNid());
				languagePath = tf.getConcept(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids());
				languageCode = tf.getConcept(ArchitectonicAuxiliary.Concept.EN.getUids());
				break;
			case english_uk:
				languageRefset = tf.getConcept(SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getNid());
				languagePath = tf.getConcept(ArchitectonicAuxiliary.Concept.SNOMED_LANGUAGE_EN_GB_PATH.getUids());
				languageCode = tf.getConcept(ArchitectonicAuxiliary.Concept.EN_GB.getUids());
				break;
			default:
				System.out.println("Unknown language.");
				break;
			}

			if (languageRefset == null || languagePath == null || languageCode == null) {
				System.out.println("Unknown language.");
				return;
			}

			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

			Set<PathBI> savedEditPaths = config.getEditingPathSet();

			for (PathBI editPath : savedEditPaths) {
				config.removeEditingPath(editPath);
			}

			config.addEditingPath(tf.getPath(languagePath.getUids()));

			String refsetLangCode = ArchitectonicAuxiliary.getLanguageCode(languageCode.getUids());

			if (!LanguageMembershipRefset.validateAsLanguageRefset(languageRefset.getConceptNid(), config)) {
				LanguageMembershipRefset.createLanguageMembershipRefsetFromConcept(languageRefset, refsetLangCode, config);
			}

			if (language.equals(Language.english_us)) {
				contextualizeSelectedRefsetDescriptions(languageRefset, languagePath, "en", config);
			} else {
				// read input file
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(descriptionsFile), "UTF8"));
				in.readLine();
				String str;
				int count = 0;
				long start = System.currentTimeMillis();
				while ((str = in.readLine()) != null) {
					count++;
					String[] column = str.split("\t");
					String descriptionId = column[0];
					String descriptionStatusId = column[1];
					I_GetConceptData descriptionStatus = null;
					if (descriptionStatusId.trim().equals("0")) {
						descriptionStatus = tf.getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
					} else {
						descriptionStatus = tf.getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
					}
					String conceptId = column[2];
					String term = column[3];
					String initialCapitalStatusId = column[4];

					String descriptionTypeId = column[5];
					I_GetConceptData descriptionType = null;
					if (descriptionTypeId.equals("3")) {
						descriptionType = tf.getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
					} else {
						descriptionType = tf.getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
					}
					String langCodeStr = column[6];

					Date date = null;
					try {
						DateFormat formatter;
						formatter = new SimpleDateFormat("yyyy-MM-dd");
						date = (Date) formatter.parse(release);
					} catch (Exception e) {
						// null date
					}

					SearchResult results = tf.doLuceneSearch(conceptId);
					if (results.topDocs.scoreDocs.length > 0 /*
															 * &&
															 * refsetLangCode.
															 * toLowerCase
															 * ().trim
															 * ().equals(
															 * langCodeStr .
															 * toLowerCase
															 * ().trim ())
															 */) {
						Document doc = results.searcher.doc(results.topDocs.scoreDocs[0].doc);
						int cnid = Integer.parseInt(doc.get("cnid"));
						int dnid = Integer.parseInt(doc.get("dnid"));
						I_GetConceptData concept = tf.getConcept(cnid);

						I_DescriptionVersioned<?> newDescription = tf.newDescription(UUID.randomUUID(), concept, langCodeStr, term, descriptionType, config);

						I_Identify descriptionWithIdentifiers = (I_Identify) newDescription;
						descriptionWithIdentifiers.addLongId(Long.parseLong(descriptionId), tf.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids()), SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config
								.getEditCoordinate().getModuleNid(), languagePath.getConceptNid());

						if (initialCapitalStatusId.trim().equals("0")) {
							newDescription.getMutableParts().iterator().next().setInitialCaseSignificant(false);
						} else {
							newDescription.getMutableParts().iterator().next().setInitialCaseSignificant(true);
						}
						newDescription.getMutableParts().iterator().next().setStatusNid(descriptionStatus.getConceptNid());
						// TODO: Implement set descriptionId
						// TODO: Implement set proper release date
						// if ( date!= null) {
						// newDescription.getMutableParts().iterator().next().setTime(date.getTime());
						// }

						I_GetConceptData acceptabilityConcept = tf.getConcept(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid());
						if (descriptionTypeId.trim().equals("1") || descriptionTypeId.trim().equals("3")) {
							acceptabilityConcept = tf.getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
						}
						I_GetConceptData languagerefsetConcept = tf.getConcept(languageRefset.getConceptNid());
                                                RefsetHelper helper = new RefsetHelper(config.getViewCoordinate(), config.getEditCoordinate());
                                                helper.newConceptRefsetExtension(languageRefset.getConceptNid(), newDescription.getDescId(), acceptabilityConcept.getConceptNid());
                                                helper.newConceptRefsetExtension(dnid, cnid, cnid);

						tf.addUncommittedNoChecks(concept);
						tf.addUncommittedNoChecks(languagerefsetConcept);
					} else {
						// System.out.println("Skipped line...");
						// System.out.println("Data: " + str);
					}
					if (count % 1000 == 0) {
						System.out.println("Imported: " + count);
						tf.commit();
					}
				}
				in.close();

				tf.commit();
				long end = System.currentTimeMillis();
				System.out.println("Finished!! - Total: " + count + " - Time: " + ((start - end) / 1000) + " seconds.");
			}
			config.removeEditingPath(tf.getPath(languagePath.getUids()));

			for (PathBI editPath : savedEditPaths) {
				config.addEditingPath(editPath);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	/**
	 * Gets the similarity results.
	 * 
	 * @param query
	 *            the query
	 * @param sourceLangCode
	 *            the source lang code
	 * @param targetLangCode
	 *            the target lang code
	 * @param config
	 *            the config
	 * 
	 * @return the similarity results
	 */
	@Deprecated
	public static List<SimilarityMatchedItem> getSimilarityResults(String query, String sourceLangCode, String targetLangCode, I_ConfigAceFrame config)  {
		if (query.contains("(")) {
			query = query.substring(0, query.indexOf("(") - 1).trim();
		}
		I_TermFactory tf = Terms.get();
		List<SimilarityMatchedItem> matches = new ArrayList<SimilarityMatchedItem>();
		try {
			SearchResult results = tf.doLuceneSearch(query);
			// System.out.println("Hits: " + results.length());
			config.getDescTypes().add(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid());

			I_IntSet allowedStatuses = tf.newIntSet();
			allowedStatuses.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());

			for (int i = 0; i < results.topDocs.scoreDocs.length; i++) {
				Document doc = results.searcher.doc(results.topDocs.scoreDocs[i].doc);
				String sourceText = "";
				String targetText = "";
				Integer targetDescriptionId = null;
				int cnid = Integer.parseInt(doc.get("cnid"));
				int dnid = Integer.parseInt(doc.get("dnid"));
				float score = results.topDocs.scoreDocs[i].score;
				I_DescriptionVersioned<?> matchedDescription = tf.getDescription(dnid, cnid);
				sourceText = matchedDescription.getTuples(config.getConflictResolutionStrategy()).iterator().next().getText();

				if (matchedDescription.getTuples(config.getConflictResolutionStrategy()).iterator().next().getTypeNid() == SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid()
						&& sourceLangCode.toLowerCase().startsWith(matchedDescription.getTuples(config.getConflictResolutionStrategy()).iterator().next().getLang().trim().toLowerCase().substring(0, 2))
						&& matchedDescription.getTuples(config.getConflictResolutionStrategy()).iterator().next().getStatusNid() == SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid()) {

					I_GetConceptData refsetConcept = (I_GetConceptData) Terms.get().getConcept(ArchitectonicAuxiliary.getLanguageConcept(targetLangCode).getUids());
					List<ContextualizedDescription> descriptions = null;
					try {
						descriptions = LanguageUtil.getContextualizedDescriptions(cnid, refsetConcept.getConceptNid(), true);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					for (I_ContextualizeDescription targetDesc : descriptions) {
						targetText = targetDesc.getText();
						targetDescriptionId = targetDesc.getDescId();
						if (targetDesc.getAcceptabilityId() == SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid()) {
							break;
						}
					}

				}

				if (targetDescriptionId != null) {
					matches.add(new SimilarityMatchedItem(cnid, dnid, sourceText, targetDescriptionId, targetText, score, query));
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
            }

		return matches;
	}

	/**
	 * Gets the similarity results.
	 * 
	 * @param query
	 *            the query
	 * @param sourceLangRefsetIds
	 *            the source lang refset ids
	 * @param targetLangRefsetId
	 *            the target lang refset id
	 * @param descTypes
	 *            the desc types
	 * @param worker
	 *            the worker
	 * @return the similarity results
	 */
	public static List<SimilarityMatchedItem> getSimilarityResults(String query, List<Integer> sourceLangRefsetIds, int targetLangRefsetId, List<Integer> descTypes, SwingWorker worker) {
		if (query.contains("(")) {
			query = query.substring(0, query.indexOf("(") - 1).trim();
		}
		I_TermFactory tf = Terms.get();
		I_ConfigAceFrame config = null;
		try {
			config = tf.getActiveAceFrameConfig();
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		List<SimilarityMatchedItem> matches = new ArrayList<SimilarityMatchedItem>();
		try {
			String escapedQuery = QueryParser.escape(query);
			SearchResult results = tf.doLuceneSearch(escapedQuery);
			// System.out.println("Hits: " + results.length());
			// config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());

			I_IntSet allowedStatuses = tf.newIntSet();
			allowedStatuses.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());

			int preferred = SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid();
			int synonym = SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid();

			int fsn = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();

			// 200 similarity matches cut off - previously
			// results.topDocs.scoreDocs.length
			for (int i = 0; i < 500 && matches.size() < 100 && i < results.topDocs.scoreDocs.length; i++) {
				if (worker != null && worker.isCancelled()) {
					break;
				}
				Document doc = null;
				try {
					doc = results.searcher.doc(results.topDocs.scoreDocs[i].doc);
				} catch (AlreadyClosedException e) {
					break;
				}
				String sourceText = "";
				String targetText = "";
				Integer targetDescriptionId = null;
				int cnid = Integer.parseInt(doc.get("cnid"));
				int dnid = Integer.parseInt(doc.get("dnid"));
				float score = results.topDocs.scoreDocs[i].score;
				boolean bFSN = false;

				List<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(dnid);
				I_ExtendByRef extensionUsed = null;
				I_ExtendByRefPartCid languageExtensionPart = null;
				ContextualizedDescription sourceDesc = null;
				boolean isMemberOfSource = false;
				for (I_ExtendByRef extension : extensions) {
					if (worker != null && worker.isCancelled()) {
						break;
					}
					if (sourceLangRefsetIds.contains(extension.getRefsetId())) {
						// Is member of language refset
						extensionUsed = extension;
						long lastVersion = Long.MIN_VALUE;
						for (I_ExtendByRefVersion loopTuple : extension.getTuples(config.getConflictResolutionStrategy())) {
							if (worker != null && worker.isCancelled()) {
								break;
							}
							if (loopTuple.getTime() >= lastVersion) {
								lastVersion = loopTuple.getTime();
								languageExtensionPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
							}
						}
						if (languageExtensionPart != null) {
							if (isActive(languageExtensionPart.getStatusNid())) {
								// is active member
								sourceDesc = new ContextualizedDescription(dnid, cnid, extensionUsed.getRefsetId());

								if (sourceDesc.getLanguageExtension() != null) {
									if (descTypes.contains(fsn) && sourceDesc.getTypeId() == fsn && (sourceDesc.getAcceptabilityId() == preferred || sourceDesc.getAcceptabilityId() == fsn)) {
										sourceText = sourceDesc.getText();
									} else if (descTypes.contains(preferred) && (sourceDesc.getTypeId() == synonym && sourceDesc.getAcceptabilityId() == preferred)) {
										sourceText = sourceDesc.getText();
									}
								}
								break;
							}
						}
					}
				}

				if (isMemberOfSource) {
					sourceText = sourceDesc.getText();
				}

				if (!sourceText.equals("")) {

					List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(cnid, targetLangRefsetId, true);
					for (I_ContextualizeDescription targetDesc : descriptions) {
						if (worker != null && worker.isCancelled()) {
							break;
						}
						targetDescriptionId = null;
						if (targetDesc.getLanguageExtension() != null) {
							if (descTypes.contains(fsn) && targetDesc.getTypeId() == fsn && (targetDesc.getAcceptabilityId() == preferred || targetDesc.getAcceptabilityId() == fsn)) {
								targetText = targetDesc.getText();
								targetDescriptionId = targetDesc.getDescId();
							} else if (descTypes.contains(preferred) && (targetDesc.getAcceptabilityId() == preferred && targetDesc.getTypeId() == synonym)) {
								targetText = targetDesc.getText();
								targetDescriptionId = targetDesc.getDescId();
							}
						}
						if (targetDescriptionId != null) {
							matches.add(new SimilarityMatchedItem(cnid, dnid, sourceText, targetDescriptionId, targetText, score, query));
						}
					}

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return matches;
	}

	/**
	 * Persist edited description.
	 * 
	 * @param concept
	 *            the concept
	 * @param descriptionId
	 *            the description id
	 * @param text
	 *            the text
	 * @param descType
	 *            the desc type
	 * @param langCode
	 *            the lang code
	 * @param config
	 *            the config
	 * @param isCaseSignificant
	 *            the is case significant
	 * @param statusId
	 *            the status id
	 */
	@Deprecated
	public static void persistEditedDescription(I_GetConceptData concept, int descriptionId, String text, int descType, String langCode, I_ConfigAceFrame config, boolean isCaseSignificant, int statusId) {
		I_TermFactory tf = Terms.get();
		try {
			if (descriptionId == Integer.MAX_VALUE) {
				I_GetConceptData typeConcept = tf.getConcept(descType);
				I_DescriptionVersioned<?> newDescription = tf.newDescription(UUID.randomUUID(), concept, langCode, text, typeConcept, config);
				newDescription.getMutableParts().iterator().next().setInitialCaseSignificant(isCaseSignificant);
				tf.addUncommitted(concept);
				tf.commit();
				return;
			} else {
				Collection<? extends I_DescriptionVersioned> descriptions = concept.getDescs();

				for (I_DescriptionVersioned<?> description : descriptions) {
					I_DescriptionTuple tuple = description.getTuples(config.getConflictResolutionStrategy()).iterator().next();
					if (tuple.getDescId() == descriptionId && (!tuple.getText().trim().equals(text.trim()) || tuple.isInitialCaseSignificant() != isCaseSignificant || tuple.getStatusId() != statusId)) {
						I_DescriptionPart newPart = tuple.duplicate();
						newPart.setText(text.trim());
						newPart.setInitialCaseSignificant(isCaseSignificant);
						newPart.setStatusId(statusId);
						// newPart.setVersion(Integer.MAX_VALUE);
						description.addVersion(newPart);
						tf.addUncommitted(concept);
						tf.commit();
						return;
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Persist edited description.
	 * 
	 * @param concept
	 *            the concept
	 * @param descriptionTuple
	 *            the description tuple
	 * @param text
	 *            the text
	 * @param descType
	 *            the desc type
	 * @param langCode
	 *            the lang code
	 * @param config
	 *            the config
	 * @param isCaseSignificant
	 *            the is case significant
	 * @param statusId
	 *            the status id
	 */
	public static void persistEditedDescription(I_GetConceptData concept, I_DescriptionTuple descriptionTuple, String text, int descType, String langCode, I_ConfigAceFrame config, boolean isCaseSignificant, int statusId) {
		I_TermFactory tf = Terms.get();
		try {
			// TODO: what to do with other uncommited changes
			/*
			 * Set<I_Transact> list = tf.getUncommitted(); for (I_Transact i :
			 * list) { i.abort(); }
			 */

			/*
			 * for (I_DescriptionVersioned uncommitedDescription :
			 * concept.getUncommittedDescriptions()) {
			 * tf.forget(uncommitedDescription); }
			 */
			if (descriptionTuple == null) {
				I_GetConceptData typeConcept = tf.getConcept(descType);
				I_DescriptionVersioned<?> newDescription = tf.newDescription(UUID.randomUUID(), concept, langCode, text, typeConcept, config);
				newDescription.getMutableParts().iterator().next().setInitialCaseSignificant(isCaseSignificant);
				tf.addUncommitted(concept);
				tf.commit();
				return;
			} else {
				I_DescriptionVersioned<?> description = tf.getDescription(descriptionTuple.getDescId(), descriptionTuple.getConceptNid());
				if (descriptionTuple.getDescId() == descriptionTuple.getDescId() && (!descriptionTuple.getText().trim().equals(text.trim()) || descriptionTuple.isInitialCaseSignificant() != isCaseSignificant || descriptionTuple.getStatusId() != statusId)) {
					for (PathBI editPath : config.getEditingPathSet()) {
						I_DescriptionPart newPart = (I_DescriptionPart) descriptionTuple.getMutablePart().makeAnalog(statusId, Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						newPart.setText(text.trim());
						newPart.setInitialCaseSignificant(isCaseSignificant);
						description.addVersion(newPart);
					}
					tf.addUncommitted(concept);
					tf.commit();
					return;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Gets the descendants.
	 * 
	 * @param descendants
	 *            the descendants
	 * @param concept
	 *            the concept
	 * 
	 * @return the descendants
	 */
	public static Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = Terms.get();
			// TODO add config as parameter
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			childrenSet.addAll(concept.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()));
			descendants.addAll(childrenSet);
			for (I_GetConceptData loopConcept : childrenSet) {
				descendants = getDescendants(descendants, loopConcept);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return descendants;
	}

	/**
	 * Gets the target equivalent sem tag.
	 * 
	 * @param sourceSemTag
	 *            the source sem tag
	 * @param sourceLangCode
	 *            the source lang code
	 * @param targetLangCode
	 *            the target lang code
	 * 
	 * @return the target equivalent sem tag
	 */
	public static String getTargetEquivalentSemTag(String sourceSemTag, String sourceLangCode, String targetLangCode) {
		I_TermFactory tf = Terms.get();
		String targetSemTag = sourceSemTag;
		if (sourceSemTag != null) {
			try {
				// TODO add config as parameter
				I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
				I_IntSet allowedTypes = config.getDescTypes();
				int synonymRF2 = SnomedMetadataRf2.SYNONYM_RF2.getLenient().getConceptNid();
				int fsnRF2 = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getConceptNid();
				int fsnRF1 = tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
				int prefRF1 = tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
				int synRF1 = tf.uuidToNative(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());

				if (!allowedTypes.contains(synonymRF2)) {
					allowedTypes.add(synonymRF2);
				}
				if (!allowedTypes.contains(fsnRF2)) {
					allowedTypes.add(fsnRF2);
				}
				if (!allowedTypes.contains(fsnRF1)) {
					allowedTypes.add(fsnRF1);
				}
				if (!allowedTypes.contains(prefRF1)) {
					allowedTypes.add(prefRF1);
				}
				if (!allowedTypes.contains(synRF1)) {
					allowedTypes.add(synRF1);
				}
				I_GetConceptData semtagsRoot = tf.getConcept(ArchitectonicAuxiliary.Concept.SEMTAGS_ROOT.getUids());
				Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
				descendants = getDescendants(descendants, semtagsRoot);
				// System.out.println("*******" + descendants.size());
				// System.out.println("*******" + descendants);
				HashMap<String, String> semtagsMap = new HashMap<String, String>();
				for (I_GetConceptData semtagConcept : descendants) {
					String source = "";
					String target = "";
					for (I_DescriptionTuple tuple : semtagConcept.getDescriptionTuples(config.getAllowedStatus(), config.getDescTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy())) {
						if (((tuple.getTypeNid() == prefRF1) || (tuple.getTypeNid() == synRF1)) && tuple.getLang().equals(sourceLangCode)) {
							source = tuple.getText();
						}
						if (((tuple.getTypeNid() == prefRF1) || (tuple.getTypeNid() == synRF1)) && tuple.getLang().equals(targetLangCode)) {
							target = tuple.getText();
						}
					}
					semtagsMap.put(source.trim(), target.trim());
				}
				/*
				 * System.out.println("*******" + semtagsMap.size());
				 * System.out.println("*******" + sourceSemTag); for (String key
				 * : semtagsMap.keySet()) { System.out.println("*******" + key +
				 * " - " + semtagsMap.get(key)); }
				 */
				targetSemTag = semtagsMap.get(sourceSemTag.trim());
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return targetSemTag;
	}

	/**
	 * Open tranlation ui.
	 * 
	 * @param concept
	 *            the concept
	 * @param config
	 *            the config
	 * @param sourceLangCode
	 *            the source lang code
	 * @param targetLangCode
	 *            the target lang code
	 * @param uiType
	 *            the ui type
	 */
	public static void openTranlationUI(I_GetConceptData concept, I_ConfigAceFrame config, String sourceLangCode, String targetLangCode, int uiType) {
		try {
			JPanel uiPanel = null;

			if (uiType == LanguageUtil.ADVANCED_UI) {
				// uiPanel = new TranslationConceptEditor(concept, config,
				// sourceLangCode, targetLangCode);
			} else if (uiType == LanguageUtil.SIMPLE_UI) {
				uiPanel = new SimpleTranslationConceptEditor(concept, config, sourceLangCode, targetLangCode);
			}

			TranslationHelperPanel thp = PanelHelperFactory.getTranslationHelperPanel();

			JTabbedPane tp = thp.getTabbedPanel();
			if (tp != null) {
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_TAB_NAME)) {
						tp.removeTabAt(i);
					}
				}
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());
				panel.add(uiPanel, BorderLayout.CENTER);

				tp.addTab(TranslationHelperPanel.TRANSLATION_TAB_NAME, panel);
				tp.setSelectedIndex(tp.getTabCount() - 1);
				thp.showTabbedPanel();
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Persist edited description.
	 * 
	 * @param concept
	 *            the concept
	 * @param descriptionTuple
	 *            the description tuple
	 * @param text
	 *            the text
	 * @param acceptabilityId
	 *            the acceptability id
	 * @param descType
	 *            the desc type
	 * @param langCode
	 *            the lang code
	 * @param languageRefsetId
	 *            the language refset id
	 * @param config
	 *            the config
	 * @param isCaseSignificant
	 *            the is case significant
	 * @param statusId
	 *            the status id
	 */
	public static void persistEditedDescription(I_GetConceptData concept, I_DescriptionTuple descriptionTuple, String text, int acceptabilityId, int descType, String langCode, int languageRefsetId, I_ConfigAceFrame config, boolean isCaseSignificant, int statusId) {
		I_TermFactory tf = Terms.get();
		try {

			// Cleaning uncommitted changes, remove in production
			// Set<I_Transact> list = tf.getUncommitted();
			// for (I_Transact i : list) {
			// i.abort();
			// }

			if (descriptionTuple == null) {
				I_GetConceptData typeConcept = tf.getConcept(descType);
				I_DescriptionVersioned<?> newDescription = tf.newDescription(UUID.randomUUID(), concept, langCode, text, typeConcept, config);
				newDescription.getMutableParts().iterator().next().setInitialCaseSignificant(isCaseSignificant);
				tf.addUncommitted(concept);

				I_GetConceptData acceptabilityConcept = tf.getConcept(acceptabilityId);
                                RefsetHelper helper = new RefsetHelper(config.getViewCoordinate(), config.getEditCoordinate());
                                helper.newConceptRefsetExtension(languageRefsetId, newDescription.getDescId(), acceptabilityConcept.getConceptNid());
				tf.commit();
				return;
			} else {
				I_DescriptionVersioned description = tf.getDescription(descriptionTuple.getDescId(), descriptionTuple.getConceptNid());
				if (descriptionTuple.getDescId() == descriptionTuple.getDescId() && (!descriptionTuple.getText().trim().equals(text.trim()) || descriptionTuple.isInitialCaseSignificant() != isCaseSignificant || descriptionTuple.getStatusNid() != statusId)) {
					for (PathBI editPath : config.getEditingPathSet()) {
						I_DescriptionPart newPart = (I_DescriptionPart) descriptionTuple.getMutablePart().makeAnalog(statusId, Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						newPart.setText(text.trim());
						newPart.setInitialCaseSignificant(isCaseSignificant);
						description.addVersion(newPart);
					}
					tf.addUncommitted(concept);
					tf.commit();
					return;
				}

				List<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(description.getDescId());
				boolean hasLanguageExtension = false;
				I_ExtendByRef languageExtension = null;
				for (I_ExtendByRef extension : extensions) {
					if (extension.getRefsetId() == languageRefsetId) {
						hasLanguageExtension = true;
						languageExtension = extension;
					}
				}

				if (hasLanguageExtension) {
					long lastVersion = Long.MIN_VALUE;
					I_ExtendByRefPart lastPart = null;
					for (I_ExtendByRefPart extensionPart : languageExtension.getMutableParts()) {
						if (extensionPart.getTime() >= lastVersion) {
							lastVersion = extensionPart.getTime();
							lastPart = extensionPart;
						}
					}

					I_GetConceptData acceptabilityConcept = tf.getConcept(acceptabilityId);

					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartCid newExtConceptPart = (I_ExtendByRefPartCid) lastPart.makeAnalog(statusId, Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						newExtConceptPart.setC1id(acceptabilityConcept.getConceptNid());
						languageExtension.addVersion(newExtConceptPart);
					}
					tf.addUncommitted(languageExtension);
					tf.commit();
				} else {
					I_GetConceptData acceptabilityConcept = tf.getConcept(acceptabilityId);
                                        RefsetHelper helper = new RefsetHelper(config.getViewCoordinate(), config.getEditCoordinate());
                                        helper.newConceptRefsetExtension(languageRefsetId, description.getDescId(), acceptabilityConcept.getConceptNid());
					tf.commit();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the contextualized descriptions.
	 * 
	 * @param conceptId
	 *            the concept id
	 * @param languageRefsetId
	 *            the language refset id
	 * @param returnConflictResolvedLatestState
	 *            the return conflict resolved latest state
	 * @return the contextualized descriptions
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws Exception
	 *             the exception
	 */
	public static List<ContextualizedDescription> getContextualizedDescriptions(int conceptId, int languageRefsetId, boolean returnConflictResolvedLatestState) throws TerminologyException, IOException, Exception {

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		return ContextualizedDescription.getContextualizedDescriptions(conceptId, languageRefsetId, config.getAllowedStatus(), config.getDescTypes(), config.getViewPositionSetReadOnly(), returnConflictResolvedLatestState);
	}

	/**
	 * Gets the contextualized descriptions.
	 * 
	 * @param conceptId
	 *            the concept id
	 * @param languageRefsetId
	 *            the language refset id
	 * @param allowedStatus
	 *            the allowed status
	 * @param allowedTypes
	 *            the allowed types
	 * @param positions
	 *            the positions
	 * @param returnConflictResolvedLatestState
	 *            the return conflict resolved latest state
	 * @return the contextualized descriptions
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws Exception
	 *             the exception
	 */
	public static List<ContextualizedDescription> getContextualizedDescriptions(int conceptId, int languageRefsetId, I_IntSet allowedStatus, I_IntSet allowedTypes, PositionSetReadOnly positions, boolean returnConflictResolvedLatestState) throws TerminologyException, IOException, Exception {
		return ContextualizedDescription.getContextualizedDescriptions(conceptId, languageRefsetId, allowedStatus, allowedTypes, positions, returnConflictResolvedLatestState);
	}

	/**
	 * Compute language refset spec multi origin.
	 * 
	 * @param languageSpecRefsetId
	 *            the language spec refset id
	 */
	@Deprecated
	public static void computeLanguageRefsetSpecMultiOrigin(int languageSpecRefsetId) {
		I_TermFactory tf = Terms.get();
		try {
			// TODO add config as parameter
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			I_GetConceptData languageSpecConcept = tf.getConcept(languageSpecRefsetId);
			LanguageSpecRefset languageSpec = new LanguageSpecRefset(languageSpecConcept);
			I_GetConceptData enumeratedOriginConcept = languageSpec.getEnumeratedOriginRefsetConcept(tf.getActiveAceFrameConfig());
			I_GetConceptData specsPreferenceOrderConcept = null;// languageSpec.getLanguagePreferenceOrderRefsetConcept();

			HashMap<Integer, Integer> descIdAcceptabilityMap = new HashMap<Integer, Integer>();

			for (I_ExtendByRef enumeratedOriginMember : tf.getRefsetExtensionMembers(enumeratedOriginConcept.getConceptNid())) {
				int lastVersion = Integer.MIN_VALUE;
				I_ExtendByRefPartCid languageExtensionPart = null;
				for (I_ExtendByRefVersion loopTuple : enumeratedOriginMember.getTuples(config.getConflictResolutionStrategy())) {
					if (loopTuple.getVersion() >= lastVersion) {
						lastVersion = loopTuple.getVersion();
						languageExtensionPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
					}
				}
				descIdAcceptabilityMap.put(enumeratedOriginMember.getComponentId(), languageExtensionPart.getC1id());
			}

			Collection<? extends I_ExtendByRef> originSpecs = tf.getRefsetExtensionMembers(specsPreferenceOrderConcept.getConceptNid());

			List<? extends I_ExtendByRef> orderedOriginSpecs = new ArrayList(originSpecs);

			Collections.sort(orderedOriginSpecs, new Comparator<I_ExtendByRef>() {
				public int compare(I_ExtendByRef e1, I_ExtendByRef e2) {
					Integer e1Value = Integer.MIN_VALUE;
					Integer e2Value = Integer.MIN_VALUE;
					try {
						int lastVersion = Integer.MIN_VALUE;
						I_ExtendByRefPartCidInt lastE1Part = null;
						for (I_ExtendByRefVersion loopTuple : e1.getTuples(Terms.get().getActiveAceFrameConfig().getConflictResolutionStrategy())) {
							if (loopTuple.getVersion() >= lastVersion) {
								lastVersion = loopTuple.getVersion();
								lastE1Part = (I_ExtendByRefPartCidInt) loopTuple.getMutablePart();
							}
						}
						e1Value = lastE1Part.getIntValue();
						I_ExtendByRefPartCidInt lastE2Part = null;
						for (I_ExtendByRefVersion loopTuple : e2.getTuples(Terms.get().getActiveAceFrameConfig().getConflictResolutionStrategy())) {
							if (loopTuple.getVersion() >= lastVersion) {
								lastVersion = loopTuple.getVersion();
								lastE2Part = (I_ExtendByRefPartCidInt) loopTuple.getMutablePart();
							}
						}
						e2Value = lastE2Part.getIntValue();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (TerminologyException e) {
						e.printStackTrace();
					}
					return (e1Value.compareTo(e2Value));
				}
			});

			for (I_ExtendByRef originSpec : orderedOriginSpecs) {
				int lastVersion = Integer.MIN_VALUE;
				I_ExtendByRefPartCidInt originSpecPart = null;
				for (I_ExtendByRefVersion loopTuple : originSpec.getTuples(Terms.get().getActiveAceFrameConfig().getConflictResolutionStrategy())) {
					if (loopTuple.getVersion() >= lastVersion) {
						lastVersion = loopTuple.getVersion();
						originSpecPart = (I_ExtendByRefPartCidInt) loopTuple.getMutablePart();
					}
				}
				I_GetConceptData loopRefset = tf.getConcept(originSpecPart.getC1id());
				for (I_ExtendByRef loopMember : tf.getRefsetExtensionMembers(loopRefset.getConceptNid())) {
					lastVersion = Integer.MIN_VALUE;
					I_ExtendByRefPartCidInt loopSpecPart = null;
					for (I_ExtendByRefVersion loopTuple : loopMember.getTuples(Terms.get().getActiveAceFrameConfig().getConflictResolutionStrategy())) {
						if (loopTuple.getVersion() >= lastVersion) {
							lastVersion = loopTuple.getVersion();
							loopSpecPart = (I_ExtendByRefPartCidInt) loopTuple.getMutablePart();
						}
					}
					descIdAcceptabilityMap.put(loopMember.getComponentId(), loopSpecPart.getC1id());
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	/**
	 * Compute language refset spec.
	 * 
	 * @param languageSpecRefsetId
	 *            the language spec refset id
	 */
	@Deprecated
	public static void computeLanguageRefsetSpec(int languageSpecRefsetId) {
		I_TermFactory tf = Terms.get();
		try {
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			I_GetConceptData current = tf.getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
			I_GetConceptData retired = tf.getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
			// I_GetConceptData notAcceptable =
			// tf.getConcept(ArchitectonicAuxiliary.Concept.NOT_ACCEPTABLE.getUids());
			I_GetConceptData languageSpecConcept = tf.getConcept(languageSpecRefsetId);
			LanguageSpecRefset languageSpec = new LanguageSpecRefset(languageSpecConcept);
			I_GetConceptData enumeratedOriginConcept = languageSpec.getEnumeratedOriginRefsetConcept(tf.getActiveAceFrameConfig());
			I_GetConceptData languageMembershipConcept = null;// languageSpec.getLanguageMembershipRefsetConcept();
			HashMap<Integer, Integer> descIdAcceptabilityMap = new HashMap<Integer, Integer>();

			// adding enumerated members to map
			for (I_ExtendByRef enumeratedOriginMember : tf.getRefsetExtensionMembers(enumeratedOriginConcept.getConceptNid())) {
				long lastVersion = Long.MIN_VALUE;
				I_ExtendByRefPartCid languageExtensionPart = null;
				for (I_ExtendByRefVersion loopTuple : enumeratedOriginMember.getTuples(config.getConflictResolutionStrategy())) {
					if (loopTuple.getTime() >= lastVersion) {
						lastVersion = loopTuple.getTime();
						languageExtensionPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
					}
				}
				descIdAcceptabilityMap.put(enumeratedOriginMember.getComponentId(), languageExtensionPart.getC1id());
			}

			// adding exceptions to map
			I_GetConceptData refsetSpec = tf.getConcept(languageSpecRefsetId);
			for (I_ExtendByRef loopMember : tf.getRefsetExtensionMembers(refsetSpec.getConceptNid())) {
				long lastVersion = Long.MIN_VALUE;
				I_ExtendByRefPartCid loopSpecPart = null;
				for (I_ExtendByRefVersion loopTuple : loopMember.getTuples(config.getConflictResolutionStrategy())) {
					if (loopTuple.getTime() >= lastVersion) {
						lastVersion = loopTuple.getTime();
						loopSpecPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
					}
				}
				descIdAcceptabilityMap.put(loopMember.getComponentId(), loopSpecPart.getC1id());
			}

			// retiring not acceptable and missing descriptions from previous
			// computation
			for (I_ExtendByRef previousComputationMember : tf.getRefsetExtensionMembers(languageMembershipConcept.getConceptNid())) {
				if (!descIdAcceptabilityMap.containsKey(previousComputationMember.getComponentId())) {
					long lastVersion = Long.MIN_VALUE;
					I_ExtendByRefPartCid previousComputationPart = null;
					for (I_ExtendByRefVersion loopTuple : previousComputationMember.getTuples(config.getConflictResolutionStrategy())) {
						if (loopTuple.getTime() >= lastVersion) {
							lastVersion = loopTuple.getTime();
							previousComputationPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
						}
					}

					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartCid newExtConceptPart = (I_ExtendByRefPartCid) previousComputationPart.makeAnalog(retired.getConceptNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						previousComputationMember.addVersion(newExtConceptPart);
					}
					tf.addUncommitted(previousComputationMember);
				}
			}
			// Adding or updating positive acceptance members
			for (Integer loopDescId : descIdAcceptabilityMap.keySet()) {
				// if (descIdAcceptabilityMap.get(loopDescId) !=
				// notAcceptable.getConceptNid()) {
				I_ExtendByRef currentMember = null;
				for (I_ExtendByRef specMember : tf.getAllExtensionsForComponent(loopDescId)) {
					if (specMember.getRefsetId() == languageMembershipConcept.getConceptNid()) {
						currentMember = specMember;
					}
				}
				if (currentMember != null) {
					Integer lastVersion = Integer.MIN_VALUE;
					I_ExtendByRefPartCidInt specPart = null;
					for (I_ExtendByRefVersion loopTuple : currentMember.getTuples(config.getConflictResolutionStrategy())) {
						if (loopTuple.getVersion() >= lastVersion) {
							lastVersion = loopTuple.getVersion();
							specPart = (I_ExtendByRefPartCidInt) loopTuple.getMutablePart();
						}
					}
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartCid newExtConceptPart = (I_ExtendByRefPartCid) specPart.makeAnalog(current.getConceptNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						newExtConceptPart.setC1id(descIdAcceptabilityMap.get(loopDescId));
						currentMember.addVersion(newExtConceptPart);
					}
					tf.addUncommitted(currentMember);
				} else {
                                        RefsetHelper helper = new RefsetHelper(config.getViewCoordinate(), config.getEditCoordinate());
                                        helper.newConceptRefsetExtension(languageMembershipConcept.getConceptNid(), loopDescId, descIdAcceptabilityMap.get(loopDescId));
				}
				// }
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	/**
	 * Gets the translation config.
	 * 
	 * @param config
	 *            the config
	 * @return the translation config
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static ConfigTranslationModule getTranslationConfig(I_ConfigAceFrame config) throws IOException {
		ConfigTranslationModule translationConfig = null;
		if (config != null) {
			translationConfig = (ConfigTranslationModule) config.getDbConfig().getProperty("TRANSLATION_CONFIG");
			if (translationConfig == null) {
				translationConfig = new ConfigTranslationModule();
			}
		} else {
			translationConfig = new ConfigTranslationModule();
		}
		return translationConfig;
	}

	/**
	 * Sets the translation config.
	 * 
	 * @param config
	 *            the config
	 * @param translationConfig
	 *            the translation config
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void setTranslationConfig(I_ConfigAceFrame config, ConfigTranslationModule translationConfig) throws IOException {
		config.getDbConfig().setProperty("TRANSLATION_CONFIG", translationConfig);
	}

	/**
	 * Generate fsn.
	 * 
	 * @param concept
	 *            the concept
	 * @param sourceLangRefset
	 *            the source lang refset
	 * @param targetLangRefset
	 *            the target lang refset
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the i_ contextualize description
	 * @throws FSNGenerationException
	 *             the fSN generation exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws Exception
	 *             the exception
	 */
	public static I_ContextualizeDescription generateFSN(I_GetConceptData concept, LanguageMembershipRefset sourceLangRefset, LanguageMembershipRefset targetLangRefset, TranslationProject project, I_ConfigAceFrame config) throws FSNGenerationException, IOException, Exception {

		ContextualizedDescription sourceFSN = null;
		I_ContextualizeDescription generatedTargetFSN = null;
		ContextualizedDescription targetPreferred = null;

		ConfigTranslationModule translationConfig = getDefaultTranslationConfig(project);

		if (!translationConfig.getSelectedFsnGenStrategy().equals(ConfigTranslationModule.FsnGenerationStrategy.NONE)) {

			I_GetConceptData fsn = Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());

			I_GetConceptData preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());

			I_GetConceptData synonym = Terms.get().getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());

			List<ContextualizedDescription> sourceDescriptions = LanguageUtil.getContextualizedDescriptions(concept.getConceptNid(), sourceLangRefset.getRefsetId(), true);

			for (ContextualizedDescription loopDescription : sourceDescriptions) {
				if (loopDescription.getTypeId() == fsn.getConceptNid() && isActive(loopDescription.getDescriptionStatusId()) && loopDescription.getLanguageExtension() != null) {
					sourceFSN = loopDescription;
				}
			}

			List<ContextualizedDescription> targetDescriptions = LanguageUtil.getContextualizedDescriptions(concept.getConceptNid(), targetLangRefset.getRefsetId(), true);

			boolean alreadyHasFSN = false;
			for (ContextualizedDescription loopDescription : targetDescriptions) {
				if (loopDescription.getTypeId() == synonym.getConceptNid() && loopDescription.getAcceptabilityId() == preferred.getConceptNid() && isActive(loopDescription.getDescriptionStatusId()) && isActive(loopDescription.getExtensionStatusId()) && loopDescription.getLanguageExtension() != null) {
					targetPreferred = loopDescription;
				} else if (loopDescription.getTypeId() == fsn.getConceptNid() && isActive(loopDescription.getDescriptionStatusId()) && loopDescription.getLanguageExtension() != null) {
					alreadyHasFSN = true;
				}
			}
			if (sourceFSN != null) {
				if (!alreadyHasFSN && translationConfig.getSelectedFsnGenStrategy().toString().equals(ConfigTranslationModule.FsnGenerationStrategy.SAME_AS_PREFERRED.toString()) && targetPreferred != null) {
					String targetFSNText = "";
					if (sourceFSN.getText().lastIndexOf("(") > 0 && sourceFSN.getText().lastIndexOf(")") > sourceFSN.getText().lastIndexOf("(")) {
						String sourceSemtag = sourceFSN.getText().substring(sourceFSN.getText().lastIndexOf("(") + 1, sourceFSN.getText().lastIndexOf(")"));
						String targetSemtag = getTargetEquivalentSemTag(sourceSemtag, sourceLangRefset.getLangCode(config), ArchitectonicAuxiliary.LANG_CODE.valueOf(targetLangRefset.getLangCode(config).toUpperCase()).getFormatedLanguageCode());

						targetFSNText = targetPreferred.getText().trim() + " (" + targetSemtag + ")";

					} else {
						targetFSNText = targetPreferred.getText().trim();
					}
					generatedTargetFSN = ContextualizedDescription.createNewContextualizedDescription(concept.getConceptNid(), targetLangRefset.getRefsetId(), targetLangRefset.getLangCode(config));

					generatedTargetFSN.setTypeId(fsn.getConceptNid());
					generatedTargetFSN.setText(targetFSNText);
					generatedTargetFSN.setInitialCaseSignificant(targetPreferred.isInitialCaseSignificant());
					generatedTargetFSN.persistChanges();
					return generatedTargetFSN;
				}
				if (!alreadyHasFSN && translationConfig.getSelectedFsnGenStrategy().toString().equals(ConfigTranslationModule.FsnGenerationStrategy.COPY_SOURCE_LANGUAGE.toString())) {

					generatedTargetFSN = ContextualizedDescription.createNewContextualizedDescription(concept.getConceptNid(), targetLangRefset.getRefsetId(), targetLangRefset.getLangCode(config));

					generatedTargetFSN.setTypeId(fsn.getConceptNid());
					generatedTargetFSN.setText(sourceFSN.getText());
					generatedTargetFSN.setInitialCaseSignificant(sourceFSN.isInitialCaseSignificant());
					generatedTargetFSN.persistChanges();
					return generatedTargetFSN;

				}
				if (!alreadyHasFSN && translationConfig.getSelectedFsnGenStrategy().toString().equals(ConfigTranslationModule.FsnGenerationStrategy.LINK_SOURCE_LANGUAGE.toString())) {
					generatedTargetFSN = sourceFSN.contextualizeThisDescription(targetLangRefset.getRefsetId(), preferred.getConceptNid());
					return generatedTargetFSN;
				}
			}
			if (!alreadyHasFSN) {
				throw new FSNGenerationException("Cannot generate FSN with " + translationConfig.getSelectedFsnGenStrategy().toString() + " strategy");
			}
		}

		return generatedTargetFSN;
	}

	/**
	 * Gets the default preferred term text.
	 * 
	 * @param concept
	 *            the concept
	 * @param sourceLangRefset
	 *            the source lang refset
	 * @param targetLangRefset
	 *            the target lang refset
	 * @param config
	 *            the config
	 * @return the default preferred term text
	 * @throws Exception
	 *             the exception
	 */
	public static String getDefaultPreferredTermText(I_GetConceptData concept, LanguageMembershipRefset sourceLangRefset, LanguageMembershipRefset targetLangRefset, I_ConfigAceFrame config) throws Exception {
		String defaultPreferredTerm = "";

		ConfigTranslationModule translationConfig = getTranslationConfig(config);
		if (translationConfig != null && translationConfig.getSelectedPrefTermDefault() != null) {
			if (translationConfig.getSelectedPrefTermDefault().equals(ConfigTranslationModule.PreferredTermDefault.BLANK)) {
				// Do nothing, empty default
			} else {
				ContextualizedDescription sourceFSN = null;
				I_GetConceptData fsn = Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
				I_GetConceptData preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());

				List<ContextualizedDescription> sourceDescriptions = LanguageUtil.getContextualizedDescriptions(concept.getConceptNid(), sourceLangRefset.getRefsetId(), true);

				for (ContextualizedDescription loopDescription : sourceDescriptions) {
					if (loopDescription.getTypeId() == fsn.getConceptNid() && isActive(loopDescription.getDescriptionStatusId()) && loopDescription.getLanguageRefsetId() == sourceLangRefset.getRefsetId()) {
						sourceFSN = loopDescription;
					}
				}
				if (translationConfig.getSelectedPrefTermDefault().equals(ConfigTranslationModule.PreferredTermDefault.SOURCE)) {

					if (sourceFSN.getText().contains("(")) {
						defaultPreferredTerm = sourceFSN.getText().substring(0, sourceFSN.getText().indexOf("(") - 1).trim();
					} else {
						defaultPreferredTerm = sourceFSN.getText();
					}

				} else if (translationConfig.getSelectedPrefTermDefault().equals(ConfigTranslationModule.PreferredTermDefault.BEST_SIMILARITY_MATCH)) {
					List<Integer> types = new ArrayList<Integer>();
					types.add(fsn.getConceptNid());
					types.add(preferred.getConceptNid());
					List<Integer> sourceIds = new ArrayList<Integer>();
					sourceIds.add(sourceLangRefset.getRefsetId());
					List<SimilarityMatchedItem> matches = LanguageUtil.getSimilarityResults(sourceFSN.getText().substring(0, sourceFSN.getText().indexOf("(") - 1).trim(), sourceIds, targetLangRefset.getRefsetId(), types, null);

					if (!matches.isEmpty()) {
						defaultPreferredTerm = matches.iterator().next().getTargetText().toString();
					}

				}
			}
		}
		return defaultPreferredTerm;
	}

	/**
	 * Gets the default ics.
	 * 
	 * @param concept
	 *            the concept
	 * @param sourceLangRefset
	 *            the source lang refset
	 * @param targetLangRefset
	 *            the target lang refset
	 * @param config
	 *            the config
	 * @param translationConfig
	 * @return the default ics
	 * @throws Exception
	 *             the exception
	 */
	public static Boolean getDefaultICS(I_GetConceptData concept, LanguageMembershipRefset sourceLangRefset, LanguageMembershipRefset targetLangRefset, I_ConfigAceFrame config, ConfigTranslationModule translationConfig) throws Exception {
		Boolean defaultICS = false;

		if (translationConfig != null && translationConfig.getSelectedIcsGenerationStrategy() != null) {
			if (translationConfig.getSelectedIcsGenerationStrategy().equals(ConfigTranslationModule.IcsGenerationStrategy.NONE)) {
				// Do nothing, empty default
			} else {
				ContextualizedDescription sourceFSN = null;
				I_GetConceptData fsn = Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());

				List<ContextualizedDescription> sourceDescriptions = LanguageUtil.getContextualizedDescriptions(concept.getConceptNid(), sourceLangRefset.getRefsetId(), true);

				for (ContextualizedDescription loopDescription : sourceDescriptions) {
					if (loopDescription.getTypeId() == fsn.getConceptNid()) {
						sourceFSN = loopDescription;
					}
				}

				if (translationConfig.getSelectedIcsGenerationStrategy().equals(ConfigTranslationModule.IcsGenerationStrategy.COPY_FROM_SOURCE)) {
					defaultICS = sourceFSN.isInitialCaseSignificant();
				}
			}
		}
		return defaultICS;
	}

	/**
	 * Gets the linguistic guidelines.
	 * 
	 * @param concept
	 *            the concept
	 * @return the linguistic guidelines
	 * @throws Exception
	 *             the exception
	 */
	public static String getLinguisticGuidelines(I_ContextualizeDescription sourcePreferredDescription, I_ContextualizeDescription sourceFsnDescription) throws Exception {
		String htmlResponse = "";

		LinguisticGuidelinesInterpreter interpreter = LinguisticGuidelinesInterpreter.createLinguisticGuidelinesInterpreter();
		List<String> guidelines = interpreter.getLinguisticGuidelines(sourcePreferredDescription, sourceFsnDescription);

		if (guidelines != null && !guidelines.isEmpty()) {
			htmlResponse = "<html><body>";
			for (String resultsItem : guidelines) {
				htmlResponse = htmlResponse + resultsItem + "<br><br>";
			}
			htmlResponse = htmlResponse + "</body></html>";
		}
		// ResultsCollectorWorkBench resultsCollector =
		// RulesLibrary.checkConcept(concept,
		// Terms.get().getConcept(RefsetAuxiliary.Concept.LANG_GUIDELINES_CONTEXT.getUids()),
		// false, Terms.get()
		// .getActiveAceFrameConfig());
		//
		// if (!resultsCollector.getResultsItems().isEmpty()) {
		// htmlResponse = "<html><body>";
		// for (ResultsItem resultsItem : resultsCollector.getResultsItems()) {
		// if (resultsItem.getErrorCode() == 1099) {
		// htmlResponse = htmlResponse + resultsItem.getMessage() + "<br><br>";
		// }
		// }
		// htmlResponse = htmlResponse + "</body></html>";
		// }

		return htmlResponse;
	}

	/**
	 * Checks if is active.
	 * 
	 * @param statusId
	 *            the status id
	 * @return true, if is active
	 */
	public static boolean isActive(int statusId) {
		List<Integer> activeStatuses = new ArrayList<Integer>();
		I_TermFactory tf = Terms.get();
		try {
			activeStatuses.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			activeStatuses.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
			activeStatuses.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
			activeStatuses.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return (activeStatuses.contains(statusId));
	}

	/**
	 * Checks if is inactive.
	 * 
	 * @param statusId
	 *            the status id
	 * @return true, if is inactive
	 */
	private static boolean isInactive(int statusId) {
		List<Integer> inactiveStatuses = new ArrayList<Integer>();
		I_TermFactory tf = Terms.get();
		try {
			inactiveStatuses.add(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
			inactiveStatuses.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.INACTIVE.getUids()));
			inactiveStatuses.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids()));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return (inactiveStatuses.contains(statusId));
	}

	/**
	 * Sets the default translation config.
	 * 
	 * @param defaultConfig
	 *            the default config
	 * @param project
	 *            the project
	 */
	public static void setDefaultTranslationConfig(ConfigTranslationModule defaultConfig, TranslationProject project) {
		try {
			File configFile = TerminologyProjectDAO.getTranslationProjectDefaultConfigFile(project);
			FileOutputStream fos = new FileOutputStream(configFile);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(defaultConfig);
			os.flush();
			os.close();
			fos.flush();
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Gets the default translation config.
	 * 
	 * @param project
	 *            the project
	 * @return the default translation config
	 */
	public static ConfigTranslationModule getDefaultTranslationConfig(TranslationProject project) {
		ConfigTranslationModule result = new ConfigTranslationModule();
		try {
			File configFile = TerminologyProjectDAO.getTranslationProjectDefaultConfigFile(project);

			if (configFile.exists()) {
				FileInputStream fis = null;
				ObjectInputStream ois = null;
				try {
					fis = new FileInputStream(configFile);
					ois = new ObjectInputStream(fis);
					result = (ConfigTranslationModule) ois.readObject();
				} catch (Exception e) {
					setDefaultTranslationConfig(result, project);
				} finally {
					if (ois != null) {
						ois.close();
					}
					if (fis != null) {
						fis.close();
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;

	}

	public static TranslationTermChangeListener getTranslationTermChangeListener() {
		if (translationTermChangeListener != null) {
			return translationTermChangeListener;
		}
		translationTermChangeListener = new TranslationTermChangeListener();
		return translationTermChangeListener;
	}

	/**
	 * Checks if is item being sent.
	 * 
	 * @param e
	 *            the e
	 * @return true, if is item being sent
	 */
	public static boolean isItemBeingSent(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton button = (JButton) e.getSource();
			return !(button.getText().contains("Keep in inbox") || button.getText().contains("Cancel"));
		}
		return true;
	}

	/**
	 * Gets the children.
	 * 
	 * @param concept
	 *            the concept
	 * @param config
	 *            the config
	 * @return the children
	 */
	public static Set<? extends I_GetConceptData> getChildren(I_GetConceptData concept, I_ConfigAceFrame config) {
		Set<? extends I_GetConceptData> children = new HashSet<I_GetConceptData>();
		I_TermFactory tf = Terms.get();
		try {
			I_IntSet allowedDestRelTypes = tf.newIntSet();

			allowedDestRelTypes.add(Terms.get().getConcept(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")).getNid());
			allowedDestRelTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			allowedDestRelTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_DUP_REL.getUids()));
			children = concept.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return children;
	}

	/** The default project for language cache. */
	private static DefaultProjectForLanguage defPrjForLanguage = null;

	/**
	 * Read default project.
	 * 
	 * @return the default project for language
	 */
	synchronized public static DefaultProjectForLanguage readDefaultProjects() {
		if (defPrjForLanguage != null) {
			return defPrjForLanguage;
		} else {
			File file = new File("sampleProcesses/DefaultProjects.pdl");
			if (file.exists()) {
				XStream xStream = new XStream(new DomDriver());
				defPrjForLanguage = (DefaultProjectForLanguage) xStream.fromXML(file);
				return defPrjForLanguage;
			}
			return null;
		}

	}

	/**
	 * Write default project for language.
	 * 
	 * @param defaultProjectForLanguage
	 *            the default project for language map
	 */
	synchronized public static void writeDefaultProjects(DefaultProjectForLanguage defaultProjectForLanguage) {
		File outputFile = new File("sampleProcesses/DefaultProjects.pdl");
		XStream xStream = new XStream(new DomDriver());

		FileOutputStream rfos;
		try {
			rfos = new FileOutputStream(outputFile);
			OutputStreamWriter rosw = new OutputStreamWriter(rfos, "UTF-8");
			xStream.toXML(defaultProjectForLanguage, rosw);
			defPrjForLanguage = defaultProjectForLanguage;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}
}
