/*
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
package org.ihtsdo.rules.testmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSet;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.helper.TerminologyHelperDrools;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * The Class TerminologyHelperDroolsWorkbench.
 */
public class TerminologyHelperDroolsWorkbench extends TerminologyHelperDrools {

	/** The semtags root. */
	private I_GetConceptData semtagsRoot;

	/** The valid semtags. */
	private Map<String,I_GetConceptData> validSemtags;

	/** The semtag parents. */
	private Map<String,Set<String>> semtagParents;

	/** The domains. */
	private List<String> domains;

	/** The uuids map. */
	public static Map<String,UUID> uuidsMap = new HashMap<String,UUID>();

	/** The parents cache. */
	private static Map<String, ConceptVersionBI> parentsCache = new HashMap<String, ConceptVersionBI>();

	/** The refsets cache. */
	private static Map<String, I_GetConceptData> refsetsCache = new HashMap<String, I_GetConceptData>();

	private int fsnRf2Nid;
	private final MetadataConversor metadataConversor;

	/**
	 * Instantiates a new terminology helper drools workbench.
	 */
	public TerminologyHelperDroolsWorkbench(){
		super();
		metadataConversor = new MetadataConversor();
		try {
			semtagsRoot = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMTAGS_ROOT.getUids());
			fsnRf2Nid = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Gets the valid semtags.
	 *
	 * @return the valid semtags
	 */
	public Map<String,I_GetConceptData> getValidSemtags() {
		if (validSemtags == null) {
			I_TermFactory tf = Terms.get();
			validSemtags = new HashMap<String, I_GetConceptData>();
			try {
				I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
				Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
				descendants = getDescendants(descendants, semtagsRoot);
				int preferred = tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
				I_IntSet types = tf.newIntSet();
				types.add(preferred);
				for (I_GetConceptData semtagConcept : descendants) {
					for (I_DescriptionTuple tuple : semtagConcept.getDescriptionTuples(config.getAllowedStatus(),
							types, getMockViewSet(config), config.getPrecedence(),
							config.getConflictResolutionStrategy())) {
						validSemtags.put(tuple.getText(), semtagConcept);
					}
				}
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			return validSemtags;
		} else {
			return validSemtags;
		}

	}

	/**
	 * Gets the semtag parents.
	 *
	 * @return the semtag parents
	 */
	public Map<String,Set<String>> getSemtagParents() {
		if (semtagParents == null) {
			I_TermFactory tf = Terms.get();
			semtagParents = new HashMap<String, Set<String>>();
			try {
				int preferred = tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
				int isa = tf.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
				I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
				Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
				descendants = getDescendants(descendants, semtagsRoot);
				I_IntSet types = tf.newIntSet();
				types.add(preferred);
				for (I_GetConceptData semtagConcept : descendants) {
					for (I_DescriptionTuple tuple : semtagConcept.getDescriptionTuples(config.getAllowedStatus(),
							types, getMockViewSet(config), config.getPrecedence(),
							config.getConflictResolutionStrategy())) {
						if (tuple.getTypeNid() == preferred && !semtagParents.keySet().contains(tuple.getText())) {
							Set<String> parents = new HashSet<String>();
							parents.add(tuple.getText());
							for (I_RelTuple relTuple : semtagConcept.getSourceRelTuples(
									config.getAllowedStatus(), config.getDestRelTypes(), 
									getMockViewSet(config), config.getPrecedence(), 
									config.getConflictResolutionStrategy())) {
								if (relTuple.getTypeNid() == isa) {
									I_GetConceptData parent = tf.getConcept(relTuple.getTargetNid());
									parents.add(parent.toString());
								}
							}
							semtagParents.put(tuple.getText(), parents);
						}
					}
				}
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			return semtagParents;
		} else {
			return semtagParents;
		}

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#isMemberOf(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isMemberOf(String conceptUUID, String refsetUUID) throws Exception {
		boolean result = false;
		I_TermFactory tf = Terms.get();
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		I_GetConceptData refsetConcept;
		if (refsetsCache.containsKey(refsetUUID)) {
			refsetConcept = refsetsCache.get(refsetUUID);
		} else {
			refsetConcept = tf.getConcept(uuidFromString(refsetUUID));
			refsetsCache.put(refsetUUID, refsetConcept);
		}
		I_GetConceptData concept = tf.getConcept(uuidFromString(conceptUUID));
		if (refsetConcept != null && concept != null) {
			result = RulesLibrary.isIncludedInRefsetSpec(refsetConcept, 
					concept, config);
		} else {
			if (refsetConcept == null) {
				throw new Exception("Refset not found! [" + refsetUUID + "]");
			} else if (concept == null) {
				throw new Exception("Concept not found! [" + conceptUUID + "]");
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#isParentOf(java.lang.String, java.lang.String)
	 */
	public boolean isParentOf(String parent, String subtype) throws Exception {		
		boolean result = false;
		if (!Ts.get().hasUuid(uuidFromString(parent))) {
			// missing concept
			return result;
		}
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		ConceptVersionBI parentConcept;
		ConceptVersionBI subtypeConcept;
		int parentConceptNid = Integer.MIN_VALUE;
		int subtypeConceptNid = Integer.MIN_VALUE;
		if (parentsCache.containsKey(parent)) {
			parentConcept = parentsCache.get(parent);
			parentConceptNid = parentConcept.getConceptNid();
		} else {
			parentConceptNid = Terms.get().uuidToNative(uuidFromString(parent));
			parentConcept = Ts.get().getConceptVersion(config.getViewCoordinate(), parentConceptNid);
			parentsCache.put(parent, parentConcept);
		}

		subtypeConceptNid = Terms.get().uuidToNative(uuidFromString(subtype));
		subtypeConcept = Ts.get().getConceptVersion(config.getViewCoordinate(), subtypeConceptNid);

		if (parentConcept ==  null || subtypeConcept == null) {
			result = false;
		} else {
			// OLD Implementation 
			//result = subtypeConcept.isKindOf(parentConcept);
			
			// NEW Implementation
			ViewCoordinate testViewCoordinate = new ViewCoordinate(config.getViewCoordinate());

			testViewCoordinate.setRelationshipAssertionType(RelAssertionType.STATED);
			result =  Ts.get().isKindOf(subtypeConceptNid, parentConceptNid, testViewCoordinate);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#isParentOfOrEqualTo(java.lang.String, java.lang.String)
	 */
	public boolean isParentOfOrEqualTo(String parent, String subtype)
			throws Exception {
		boolean result = (subtype.equals(parent) || isParentOf(parent, subtype));
		return result;
	}

	@Override
	public boolean isDescriptionTextNotUniqueInProvidedHierarchy(String descText, String conceptUuid, 
			String hierarchyConceptUuid) throws Exception {
		I_TermFactory tf = Terms.get();
		SearchResult result = Terms.get().doLuceneSearch(QueryParser.escape(descText));
		int conceptNid = Terms.get().uuidToNative(UUID.fromString(conceptUuid));
		boolean unique = true;
		if (result.topDocs.totalHits == 0) {
			unique = true;
		} else {
			NidSetBI allowedStatusNids = Terms.get().getActiveAceFrameConfig().getViewCoordinate().getAllowedStatusNids();
			search: 
				for (int i = 0; i < result.topDocs.totalHits; i++) {
					Document doc = result.searcher.doc(result.topDocs.scoreDocs[i].doc);
					int cnid = Integer.parseInt(doc.get("cnid"));
					int dnid = Integer.parseInt(doc.get("dnid"));
					if (cnid != conceptNid) {
						I_DescriptionVersioned<?> potential_match = Terms.get().getDescription(dnid, cnid);

						int pfdn = Integer.MAX_VALUE;

						try {
							pfdn = Terms.get().uuidToNative(UUID.fromString("084283a0-b7ca-5626-b604-6dd69fb5ff2d"));
						} catch (Exception e) {
							// ignore, no pfdn in this environment
						}
						// if its not null and not "patient friendly preferred term" type and not preferred term type
						if (potential_match != null && potential_match.getTypeNid() != pfdn && 
								potential_match.getTypeNid() != Terms.get().uuidToNative(UUID.fromString("d8e3b37d-7c11-33ef-b1d0-8769e2264d44"))) {
							boolean preferredInUs = false;

							for (RefexChronicleBI annot : potential_match.getAnnotations()) {
								// has an US refset extension (annotated)
								if (annot.getRefexNid() == Terms.get().uuidToNative(UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6"))) {
									RefexNidVersionBI langAnnot = (RefexNidVersionBI) annot.getVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate());
									// has preferred acceptability value
									if (langAnnot != null && langAnnot.getNid1() == Terms.get().uuidToNative(UUID.fromString("15877c09-60d7-3464-bed8-635a98a7e5b2"))) {
										preferredInUs = true;
									}

								}
							}

							if (preferredInUs) {
								for (DescriptionVersionBI part_search : 
									potential_match.getVersions(Terms.get().getActiveAceFrameConfig().getViewCoordinate())) {
									String part_searchText1 = "";
									String descText2 = "";

									//                            if (part_search.getText().contains("(") && part_search.getText().indexOf("(") > 2) {
									//                                text1 = part_search.getText().substring(0, part_search.getText().lastIndexOf("(")-1).toLowerCase().trim();
									//                            } else {
									//                                text1 = part_search.getText().toLowerCase().trim();
									//                            }
									//                            
									//                            if (descText.contains("(")  && descText.indexOf("(") > 2) {
									//                                text2 = descText.substring(0, descText.lastIndexOf("(")-1).toLowerCase().trim();
									//                            } else {
									//                                text2 = descText.toLowerCase().trim();
									//                            }

									if (descText.contains("(")  
											&& descText.indexOf("(") > 2
											&& descText.endsWith(")")) {
										descText2 = descText.substring(0, descText.lastIndexOf("(")-1).toLowerCase().trim();
									} else {
										descText2 = descText.toLowerCase().trim();
									}

									if (allowedStatusNids.contains(part_search.getStatusNid())
											&& (part_search.getText().toLowerCase().equals(descText.toLowerCase()) 
													|| part_searchText1.equals(descText2)
													|| part_search.getText().toLowerCase().equals(descText2)
													|| part_searchText1.equals(descText.toLowerCase()))
													&& isParentOf(hierarchyConceptUuid, tf.nidToUuid(cnid).toString())) { 
										unique = false;
										break search;
									}
								}
							}
						}
					}
				}
		}
		return !unique;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#isDescriptionTextNotUniqueInHierarchy(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isDescriptionTextNotUniqueInHierarchy(String descText, String conceptUuid) throws Exception{
		boolean result = false;
		I_TermFactory tf = Terms.get();
		try {
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			int fsnTypeNid = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getConceptNid();
			String originalSemTag = "";
			String potentialMatchSemtag = "";

			I_GetConceptData originalConcept = Terms.get().getConcept(uuidFromString(conceptUuid));
			I_DescriptionTuple originalFsn = null;
			int preferred = tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
			I_IntSet types = tf.newIntSet();
			types.add(preferred);
			for (I_DescriptionTuple loopDescription : originalConcept.getDescriptionTuples(config.getAllowedStatus(), 
					types, getMockViewSet(config), 
					config.getPrecedence(), config.getConflictResolutionStrategy())) {
				if (loopDescription.getTypeNid() == fsnTypeNid && loopDescription.getLang().toLowerCase().startsWith("en")) {
					originalFsn = loopDescription;
					originalSemTag = originalFsn.getText().substring(originalFsn.getText().lastIndexOf("(")).trim();
				}
			}

			if (!originalSemTag.isEmpty()) {

				String query = "+\"" + QueryParser.escape(descText) + "\"";
				SearchResult results = tf.doLuceneSearch(query);
				TopDocs topDocs = results.topDocs;
				ScoreDoc[] docs = topDocs.scoreDocs;

				if (docs.length > 0) {
					for (int i = 0 ; i < docs.length  ; i++) {
						try{
							Document doc = results.searcher.doc(docs[i].doc);
							int cnid = Integer.parseInt(doc.get("cnid"));
							int dnid = Integer.parseInt(doc.get("dnid"));

							if (originalConcept.getConceptNid() != cnid) {

								DescriptionVersionBI description = (DescriptionVersionBI) 
										Ts.get().getComponentVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), dnid);
								//						AceLog.getAppLog().info("Evaluating match - Description: " + description.getText() + "Concept: " + potentialMatchConcept);

								if (description != null && description.getText().toLowerCase().equals(descText.toLowerCase())) { 

									I_GetConceptData potentialMatchConcept = Terms.get().getConcept(Integer.parseInt(doc.get("cnid")));

									if (isActive(potentialMatchConcept.getUids().iterator().next().toString())) {
										I_DescriptionTuple potentialMatchFsn = null;
										for (I_DescriptionTuple loopDescription : potentialMatchConcept.getDescriptionTuples(config.getAllowedStatus(), 
												types, getMockViewSet(config), 
												config.getPrecedence(), config.getConflictResolutionStrategy())) {
											if (loopDescription.getTypeNid() == fsnTypeNid && loopDescription.getLang().toLowerCase().startsWith("en")) {
												potentialMatchFsn = loopDescription;
												potentialMatchSemtag = potentialMatchFsn.getText().substring(potentialMatchFsn.getText().lastIndexOf("(")).trim();
											}
										}

										if (potentialMatchSemtag != null &&
												originalSemTag.equals(potentialMatchSemtag)) {
											result = true;
											//											AceLog.getAppLog().info("Hierarchy match found: " + originalFsn + " (" + 
											//													originalConcept.getUids().iterator().next() + ") & " + potentialMatchFsn.getText() 
											//													+ " (" + potentialMatchConcept.getUUIDs().iterator().next() + ")");
											break;
										}
									}
								}
							}
						}catch(Exception e){
							AceLog.getAppLog().alertAndLogException(e);
						}
					}
				}
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#isFsnTextNotUnique(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isFsnTextNotUnique(String fsn, String conceptUuid, String langCode) throws Exception{
		SearchResult result = Terms.get().doLuceneSearch(QueryParser.escape(fsn));
		int conceptNid = Terms.get().uuidToNative(UUID.fromString(conceptUuid));
		boolean unique = true;
		if (result.topDocs.totalHits == 0) {
			unique = true;
		} else {
			NidSetBI allowedStatusNids = Terms.get().getActiveAceFrameConfig().getViewCoordinate().getAllowedStatusNids();
			search:
				for (int i = 0; i < result.topDocs.totalHits; i++) {
					Document doc = result.searcher.doc(result.topDocs.scoreDocs[i].doc);
					int cnid = Integer.parseInt(doc.get("cnid"));
					int dnid = Integer.parseInt(doc.get("dnid"));
					if (cnid != conceptNid) {
						I_DescriptionVersioned<?> potential_fsn = Terms.get().getDescription(dnid, cnid);
						if (potential_fsn != null) {
							for (I_DescriptionPart part_search : potential_fsn.getMutableParts()) {
								if (allowedStatusNids.contains(part_search.getStatusNid())
										&& part_search.getText().toLowerCase().equals(fsn.toLowerCase())) {
									unique = false;
									break search;
								} 
							}
						}
					}
				}
		}
		return !unique;
	}

	/**
	 * Checks if is fsn text not unique old.
	 *
	 * @param fsn the fsn
	 * @param conceptUuid the concept uuid
	 * @param langCode the lang code
	 * @return true, if is fsn text not unique old
	 * @throws Exception the exception
	 */
	public boolean isFsnTextNotUniqueOld(String fsn, String conceptUuid, String langCode) throws Exception{
		boolean result = false;
		I_TermFactory tf = Terms.get();
		try {
			int fsnTypeNid = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();
			int activeNid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
			int sourceConceptNid = tf.uuidToNative(uuidFromString(conceptUuid));
			String workingSearchString = new String();
			workingSearchString = fsn.trim();
			Pattern p = Pattern.compile("[\\s\\(]");
			Matcher m = p.matcher(workingSearchString);
			workingSearchString = m.replaceAll(" +");
			String filteredDescription = "+" + QueryParser.escape(fsn);
			//AceLog.getAppLog().info(fsn + "  ---->  " + filteredDescription);
			SearchResult results = tf.doLuceneSearch(filteredDescription);
			TopDocs topDocs = results.topDocs;
			ScoreDoc[] docs = topDocs.scoreDocs;
			for (int i = 0 ; i < docs.length  ; i++) {
				Document doc = results.searcher.doc(docs[i].doc);
				int cnid = Integer.parseInt(doc.get("cnid"));
				int dnid = Integer.parseInt(doc.get("dnid"));
				if (cnid != sourceConceptNid) {
					try {
						I_DescriptionVersioned<?> potential_fsn = Terms.get().getDescription(dnid, cnid);
						if (potential_fsn != null) {
							for (I_DescriptionPart part_search : potential_fsn.getMutableParts()) {
								if (part_search.getStatusNid() == activeNid
										&& part_search.getTypeNid() == fsnTypeNid
										&& part_search.getText().equals(fsn)
										&& part_search.getLang().equals(langCode)) {
									result = true;
								}
							}
						}
					} catch (Exception e) {
						AceLog.getAppLog().alertAndLogException(e);
					}
				}
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} 

		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#isActive(java.lang.String)
	 */
	@Override
	public boolean isActive(String conceptUUID) {
		boolean result = false;

		try {
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			I_GetConceptData concept = Terms.get().getConcept(uuidFromString(conceptUUID));
			int status = concept.getConceptAttributeTuples(null, getMockViewSet(config), 
					config.getPrecedence(), config.getConflictResolutionStrategy()).iterator().next().getStatusNid();
			if (status == ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid() ||
					status == ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid() ||
					status == SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid()) {
				result = true;
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#isValidSemtag(java.lang.String)
	 */
	@Override
	public boolean isValidSemtag(String semtag){
		return getValidSemtags().keySet().contains(semtag);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#isValidSemtagInHierarchy(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isValidSemtagInHierarchy(String semtag, String langCode, String conceptUuid){
		boolean result = true;

		try {
			Map<String, I_GetConceptData> localValidSemtags = getValidSemtags();
			Map<String, Set<String>> localSemtagsParents = getSemtagParents();

			if (!localSemtagsParents.containsKey(semtag)) {
				return false;
			}

			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

			int fsnTypeNid = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();
			int activeNid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();

			I_GetConceptData testedConcept = termFactory.getConcept(uuidFromString(conceptUuid));
			List<I_GetConceptData> parents = new ArrayList<I_GetConceptData>();

			I_IntSet allowedTypes = termFactory.newIntSet();
			ConceptSpec spec = new ConceptSpec("Is a (attribute)", uuidFromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));
			allowedTypes.add(termFactory.uuidToNative(spec.getLenient().getPrimUuid()));

			for (I_RelTuple loopTuple : testedConcept.getSourceRelTuples(config.getAllowedStatus(), 
					allowedTypes, getMockViewSet(config), 
					config.getPrecedence(), config.getConflictResolutionStrategy(), 
					config.getClassifierConcept().getConceptNid(), 
					RelAssertionType.STATED)) {
				parents.add(Terms.get().getConcept(loopTuple.getC2Id()));
			}

			Set<String> parentSemtags = new HashSet<String>();
			for (I_GetConceptData loopParent : parents) {
				for (I_DescriptionTuple loopDescription : loopParent.getDescriptionTuples(config.getAllowedStatus(), 
						null, getMockViewSet(config), 
						Precedence.PATH, config.getConflictResolutionStrategy())) {
					if (loopDescription.getStatusNid() == activeNid &&
							loopDescription.getTypeNid() == fsnTypeNid && 
							loopDescription.getLang().equals(langCode) &&
							loopDescription.getText().contains("(") &&
							loopDescription.getText().contains(")") &&
							loopDescription.getText().lastIndexOf("(") < loopDescription.getText().lastIndexOf(")")) {
						parentSemtags.add(loopDescription.getText().substring(loopDescription.getText().lastIndexOf('(')+1,loopDescription.getText().lastIndexOf(')')));
					}
				}
			}

			if (parentSemtags.size() == 1 &&
					parentSemtags.iterator().next().equals(semtag)) {
				return true;
			}

			Set<String> validParentSemtags = localSemtagsParents.get(semtag);
			if (!validParentSemtags.containsAll(parentSemtags)) {
				result = false;
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return result;
	}

	/**
	 * Gets the descendants.
	 *
	 * @param descendants the descendants
	 * @param concept the concept
	 * @return the descendants
	 */
	public static Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			childrenSet.addAll(concept.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, 
					getMockViewSet(config), config.getPrecedence(), config.getConflictResolutionStrategy()));
			descendants.addAll(childrenSet);
			for (I_GetConceptData loopConcept : childrenSet) {
				descendants = getDescendants(descendants, loopConcept);
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return descendants;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#isParentOfStatedChildren(java.lang.String)
	 */
	@Override
	public boolean isParentOfStatedChildren(String conceptUuid){
		boolean result = false;
		if (conceptUuid != null) {
			try {
				I_GetConceptData concept = Terms.get().getConcept(uuidFromString(conceptUuid));
				I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
				ConceptSpec spec = new ConceptSpec("Is a (attribute)", uuidFromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));
				int isaType = spec.getLenient().getNid();
				I_IntSet allowedrels = Terms.get().newIntSet();
				allowedrels.add(isaType);

				if (concept.getDestRelTuples(config.getAllowedStatus(), 
						allowedrels, getMockViewSet(config), 
						config.getPrecedence(), config.getConflictResolutionStrategy(),
						config.getClassifierConcept().getConceptNid(), RelAssertionType.STATED).size() > 0) {
					result = true;
				}
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#getListOfDomainsUuids(java.lang.String)
	 */
	@Override
	public List<String> getListOfDomainsUuids(String conceptUuid) {
		if (domains == null) {
			domains = new ArrayList<String>();
			I_TermFactory tf = Terms.get();
			try {
				I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
				Set<I_GetConceptData> allDomains = getDescendants(new HashSet<I_GetConceptData>(), 
						Terms.get().getConcept(RefsetAuxiliary.Concept.MRCM_DOMAINS.getUids()));
				for (I_GetConceptData domain : allDomains) {
					if (isMemberOf(conceptUuid, domain.getPrimUuid().toString())) {
						domains.add(domain.getPrimUuid().toString());
					}
				}
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			return domains;
		} else {
			return domains;
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#isTargetOfReferToLink(java.lang.String)
	 */
	@Override
	public boolean isTargetOfReferToLink(String conceptUuid) {
		boolean result = false;
		try {
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			I_GetConceptData concept = Terms.get().getConcept(uuidFromString(conceptUuid));
			List<? extends DescriptionVersionBI> descriptionsList = concept.getDescriptionTuples(config.getAllowedStatus(), 
					null, getMockViewSet(config), 
					config.getPrecedence(), config.getConflictResolutionStrategy());
			ConceptSpec referToRefset = new ConceptSpec("REFERS TO concept association reference set (foundation metadata concept)", uuidFromString("d15fde65-ed52-3a73-926b-8981e9743ee9"));
			for (DescriptionVersionBI loopDescription : descriptionsList) {
				Collection<? extends RefexVersionBI<?>> currentAnnotations = loopDescription.getChronicle().getActiveAnnotations(config.getViewCoordinate());
				for (RefexVersionBI<?> annotation : currentAnnotations) {
					RefexNidVersionBI annotationCnid = (RefexNidVersionBI) annotation;
					int languageNid = annotationCnid.getRefexNid();
					if (annotationCnid.getRefexNid() != referToRefset.getLenient().getNid()) {
						result = true;
						break;
					}
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#isTargetOfHistoricalRelationships(java.lang.String)
	 */
	@Override
	public boolean isTargetOfHistoricalRelationships(String conceptUuid) {
		boolean result = false;
		try {
			I_GetConceptData oldStyleConcept = Terms.get().getConcept(uuidFromString(conceptUuid));
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			int historical = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC.getUids());
			for (RelationshipVersionBI relTuple :  oldStyleConcept.getDestRelTuples(config.getAllowedStatus(), 
					null, 
					getMockViewSet(config), config.getPrecedence(), 
					config.getConflictResolutionStrategy())) {
				if (relTuple.getCharacteristicNid() == historical) {
					result = true;
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#isExtensionConcept(java.lang.String)
	 */
	@Override
	public boolean isExtensionConcept(String conceptUuid) {
		//TODO implement when extensions representation is defined
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.helper.TerminologyHelperDrools#isSemanticTagEqualsInAllTerms(java.lang.String)
	 */
	@Override
	public boolean isSemanticTagEqualsInAllTerms(String conceptUuid) {
		boolean result = false;
		try {
			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

			I_GetConceptData focusConcept = tf.getConcept(uuidFromString(conceptUuid));
			List<String> currentSemtags = new ArrayList<String>();
			int preferred = tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
			I_IntSet types = tf.newIntSet();
			types.add(preferred);
			for (I_DescriptionTuple tuple : focusConcept.getDescriptionTuples(config.getAllowedStatus(),
					types, getMockViewSet(config), config.getPrecedence(),
					config.getConflictResolutionStrategy())) {
				if (tuple.getTypeNid() == SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid() 
						&& tuple.getLang().equals("en")) {
					if (tuple.getText().lastIndexOf("(") > -1 && tuple.getText().lastIndexOf(")") > -1) {
						currentSemtags.add(tuple.getText().substring(tuple.getText().lastIndexOf("(")+1,tuple.getText().lastIndexOf(")")));
					}
				}
			}

			if (getValidSemtags().keySet().containsAll(currentSemtags)) {
				result = true;
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/**
	 * Uuid from string.
	 *
	 * @param string the string
	 * @return the uUID
	 */
	private UUID uuidFromString(String string) {
		UUID uuid = TerminologyHelperDroolsWorkbench.uuidsMap.get(string);

		if (uuid == null) {
			uuid = UUID.fromString(string);
			TerminologyHelperDroolsWorkbench.uuidsMap.put(string, uuid);
		}

		return uuid;
	}

	/**
	 * Gets the mock view set.
	 *
	 * @param config the config
	 * @return the mock view set
	 */
	private static PositionSet getMockViewSet(I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		Set<PositionBI> viewPositions =  new HashSet<PositionBI>();
		try {
			for (PathBI loopPath : config.getEditingPathSet()) {
				PositionBI pos = termFactory.newPosition(loopPath, Long.MAX_VALUE);
				viewPositions.add(pos);
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		PositionSet mockViewSet = new PositionSet(viewPositions);
		return mockViewSet;
	}

}
