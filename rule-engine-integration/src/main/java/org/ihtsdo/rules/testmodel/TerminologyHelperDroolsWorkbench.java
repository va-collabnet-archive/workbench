package org.ihtsdo.rules.testmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.helper.TerminologyHelperDrools;

public class TerminologyHelperDroolsWorkbench extends TerminologyHelperDrools {

	private I_GetConceptData semtagsRoot;
	private Map<String,I_GetConceptData> validSemtags;
	private Map<String,Set<String>> semtagParents;
	private List<String> domains;

	public TerminologyHelperDroolsWorkbench(){
		super();
		try {
			semtagsRoot = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMTAGS_ROOT.getUids());
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<String,I_GetConceptData> getValidSemtags() {
		if (validSemtags == null) {
			I_TermFactory tf = Terms.get();
			validSemtags = new HashMap<String, I_GetConceptData>();
			try {
				I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
				Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
				descendants = getDescendants(descendants, semtagsRoot);
				for (I_GetConceptData semtagConcept : descendants) {
					for (I_DescriptionTuple tuple : semtagConcept.getDescriptionTuples(config.getAllowedStatus(),
							config.getDescTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(),
							config.getConflictResolutionStrategy())) {
						validSemtags.put(tuple.getText(), semtagConcept);
					}
				}
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return validSemtags;
		} else {
			return validSemtags;
		}

	}

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
				for (I_GetConceptData semtagConcept : descendants) {
					for (I_DescriptionTuple tuple : semtagConcept.getDescriptionTuples(config.getAllowedStatus(),
							config.getDescTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(),
							config.getConflictResolutionStrategy())) {
						if (tuple.getTypeNid() == preferred && !semtagParents.keySet().contains(tuple.getText())) {
							Set<String> parents = new HashSet<String>();
							parents.add(tuple.getText());
							for (I_RelTuple relTuple : semtagConcept.getSourceRelTuples(
									config.getAllowedStatus(), config.getDestRelTypes(), 
									config.getViewPositionSetReadOnly(), config.getPrecedence(), 
									config.getConflictResolutionStrategy())) {
								if (relTuple.getTypeNid() == isa) {
									I_GetConceptData parent = tf.getConcept(relTuple.getDestinationNid());
									parents.add(parent.toString());
								}
							}
							semtagParents.put(tuple.getText(), parents);
						}
					}
				}
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return semtagParents;
		} else {
			return semtagParents;
		}

	}

	@Override
	public boolean isMemberOf(String conceptUUID, String refsetUUID) {
		boolean result = false;
		try {
			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			I_GetConceptData refsetConcept = tf.getConcept(UUID.fromString(refsetUUID));
			I_GetConceptData concept = tf.getConcept(UUID.fromString(conceptUUID));
			if (refsetConcept != null && concept != null) {
				result = RulesLibrary.isIncludedInRefsetSpec(refsetConcept, 
						concept, config);
			}
		} catch (TerminologyException e) {
			// error, reported as not member
		} catch (IOException e) {
			// error, reported as not member
		}
		return result;
	}

	public boolean isParentOf(String parent, String subtype) throws Exception {
		boolean result = false;
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		int parentConceptNid = Terms.get().uuidToNative(UUID.fromString(parent));
		int subtypeConceptNid = Terms.get().uuidToNative(UUID.fromString(subtype));
		if (RulesLibrary.myStaticIsACache == null) { 
			ConceptVersionBI parentConcept = Ts.get().getConceptVersion(config.getViewCoordinate(), parentConceptNid);
			ConceptVersionBI subtypeConcept = Ts.get().getConceptVersion(config.getViewCoordinate(), subtypeConceptNid);
			result = subtypeConcept.isKindOf(parentConcept);
		} else {
			//System.out.println("Using rules library isa cache!");
			result = RulesLibrary.myStaticIsACache.isKindOf(subtypeConceptNid, parentConceptNid);
		}
		return result;
	}

	public boolean isParentOfOrEqualTo(String parent, String subtype)
	throws Exception {
		boolean result = (subtype.equals(parent) || isParentOf(parent, subtype));
		return result;
	}

	@Override
	public boolean isDescriptionTextNotUniqueInHierarchy(String descText, String conceptUuid) throws Exception{
		boolean result = false;
		I_TermFactory tf = Terms.get();
		try {
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			int fsnTypeNid = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getConceptNid();
			String originalSemTag = "";
			String potentialMatchSemtag = "";

			I_GetConceptData originalConcept = Terms.get().getConcept(UUID.fromString(conceptUuid));
			I_DescriptionTuple originalFsn = null;

			for (I_DescriptionTuple loopDescription : originalConcept.getDescriptionTuples(config.getAllowedStatus(), 
					config.getDescTypes(), config.getViewPositionSetReadOnly(), 
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
								//						System.out.println("Evaluating match - Description: " + description.getText() + "Concept: " + potentialMatchConcept);

								if (description != null && description.getText().toLowerCase().equals(descText.toLowerCase())) { 

									I_GetConceptData potentialMatchConcept = Terms.get().getConcept(Integer.parseInt(doc.get("cnid")));

									if (isActive(potentialMatchConcept.getUids().iterator().next().toString())) {
										I_DescriptionTuple potentialMatchFsn = null;

										for (I_DescriptionTuple loopDescription : potentialMatchConcept.getDescriptionTuples(config.getAllowedStatus(), 
												config.getDescTypes(), config.getViewPositionSetReadOnly(), 
												config.getPrecedence(), config.getConflictResolutionStrategy())) {
											if (loopDescription.getTypeNid() == fsnTypeNid && loopDescription.getLang().toLowerCase().startsWith("en")) {
												potentialMatchFsn = loopDescription;
												potentialMatchSemtag = potentialMatchFsn.getText().substring(potentialMatchFsn.getText().lastIndexOf("(")).trim();
											}
										}

										if (potentialMatchSemtag != null &&
												originalSemTag.equals(potentialMatchSemtag)) {
											result = true;
//											System.out.println("Hierarchy match found: " + originalFsn + " (" + 
//													originalConcept.getUids().iterator().next() + ") & " + potentialMatchFsn.getText() 
//													+ " (" + potentialMatchConcept.getUUIDs().iterator().next() + ")");
											break;
										}
									}
								}
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean isFsnTextNotUnique(String fsn, String conceptUuid, String langCode) throws Exception{
		boolean result = false;
		I_TermFactory tf = Terms.get();
		try {
			int fsnTypeNid = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();
			int activeNid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
			int sourceConceptNid = tf.uuidToNative(UUID.fromString(conceptUuid));
			String workingSearchString = new String();
			workingSearchString = fsn.trim();
			Pattern p = Pattern.compile("[\\s\\(]");
			Matcher m = p.matcher(workingSearchString);
			workingSearchString = m.replaceAll(" +");
			String filteredDescription = "+" + QueryParser.escape(fsn);
			//System.out.println(fsn + "  ---->  " + filteredDescription);
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
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public boolean isActive(String conceptUUID) {
		boolean result = false;

		try {
			ConceptVersionBI concept = Ts.get().getConceptVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), UUID.fromString(conceptUUID));
			int status = concept.getConAttrs().getVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate()).getStatusNid();
			if (status == ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid() ||
					status == ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid() ||
					status == SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid()) {
				result = true;
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ContraditionException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public boolean isValidSemtag(String semtag){
		return getValidSemtags().keySet().contains(semtag);
	}

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

			I_GetConceptData testedConcept = termFactory.getConcept(UUID.fromString(conceptUuid));
			List<I_GetConceptData> parents = new ArrayList<I_GetConceptData>();

			I_IntSet allowedTypes = termFactory.newIntSet();
			allowedTypes.add(termFactory.uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));

			parents.addAll(testedConcept.getSourceRelTargets(config.getAllowedStatus(), allowedTypes, 
					config.getViewPositionSetReadOnly(), Precedence.PATH, config.getConflictResolutionStrategy()));

			Set<String> parentSemtags = new HashSet<String>();
			for (I_GetConceptData loopParent : parents) {
				for (I_DescriptionTuple loopDescription : loopParent.getDescriptionTuples(config.getAllowedStatus(), 
						null, config.getViewPositionSetReadOnly(), 
						Precedence.PATH, config.getConflictResolutionStrategy())) {
					if (loopDescription.getStatusNid() == activeNid &&
							loopDescription.getTypeNid() == fsnTypeNid && 
							loopDescription.getLang().equals(langCode)) {
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			childrenSet.addAll(concept.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, 
					config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()));
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

	@Override
	public boolean isParentOfStatedChildren(String conceptUuid){
		boolean result = false;
		if (conceptUuid != null) {
			try {
				I_GetConceptData concept = Terms.get().getConcept(UUID.fromString(conceptUuid));
				I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
				int isaType = Terms.get().uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));
				for (I_RelTuple loopTuple : concept.getDestRelTuples(config.getAllowedStatus(), 
						config.getDestRelTypes(), config.getViewPositionSetReadOnly(), 
						config.getPrecedence(), config.getConflictResolutionStrategy())) {
					if (loopTuple.getTypeNid() == isaType &&
							loopTuple.getStatusNid() == SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid()) {
						result = true;
					}
				}
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

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
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return domains;
		} else {
			return domains;
		}
	}

	@Override
	public boolean isTargetOfReferToLink(String conceptUuid) {
		//TODO implement when refer to import is defined
		return false;
	}

	@Override
	public boolean isExtensionConcept(String conceptUuid) {
		//TODO implement when extensions representation is defined
		return false;
	}

	@Override
	public boolean isSemanticTagEqualsInAllTerms(String conceptUuid) {
		boolean result = false;
		try {
			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

			I_GetConceptData focusConcept = tf.getConcept(UUID.fromString(conceptUuid));
			List<String> currentSemtags = new ArrayList<String>();
			for (I_DescriptionTuple tuple : focusConcept.getDescriptionTuples(config.getAllowedStatus(),
					config.getDescTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(),
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
