package org.ihtsdo.rules.testmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.helper.TerminologyHelperDrools;

public class TerminologyHelperDroolsWorkbench extends TerminologyHelperDrools {

	private I_GetConceptData semtagsRoot;
	private List<String> validSemtags;
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

	public List<String> getValidSemtags() {
		if (validSemtags == null) {
			I_TermFactory tf = Terms.get();
			validSemtags = new ArrayList<String>();
			try {
				I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
				Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
				descendants = getDescendants(descendants, semtagsRoot);
				for (I_GetConceptData semtagConcept : descendants) {
					for (I_DescriptionTuple tuple : semtagConcept.getDescriptionTuples(config.getAllowedStatus(),
							config.getDescTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(),
							config.getConflictResolutionStrategy())) {
						validSemtags.add(tuple.getText());
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
			int fsnTypeNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
			String originalDescText = descText;
			String originalSemTag = "";
			String potentialMatchSemtag = "";

			descText = "+\"" + QueryParser.escape(descText) + "\"";
			SearchResult results = tf.doLuceneSearch(descText);
			TopDocs topDocs = results.topDocs;
			ScoreDoc[] docs = topDocs.scoreDocs;
			if (docs.length > 0) {
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
				for (int i = 0 ; i < docs.length  ; i++) {
					try{
						Document doc = results.searcher.doc(i);
						int cnid = Integer.parseInt(doc.get("cnid"));
						int dnid = Integer.parseInt(doc.get("dnid"));
						
						I_GetConceptData potentialMatchConcept = Terms.get().getConcept(Integer.parseInt(doc.get("cnid")));
						
						I_DescriptionTuple potentialMatchFsn = null;
						
						for (I_DescriptionTuple loopDescription : potentialMatchConcept.getDescriptionTuples(config.getAllowedStatus(), 
								config.getDescTypes(), config.getViewPositionSetReadOnly(), 
								config.getPrecedence(), config.getConflictResolutionStrategy())) {
							if (loopDescription.getTypeNid() == fsnTypeNid && loopDescription.getLang().toLowerCase().startsWith("en")) {
								potentialMatchFsn = loopDescription;
								potentialMatchSemtag = potentialMatchFsn.getText().substring(potentialMatchFsn.getText().lastIndexOf("(")).trim();
							}
						}
						
						I_DescriptionVersioned potential_match = Terms.get().getDescription(dnid, cnid);
						if (potential_match != null) {

							DescriptionVersionBI description = (DescriptionVersionBI) 
							Ts.get().getComponentVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), dnid);

							if (description.getText() == originalDescText && 
									originalSemTag.equals(potentialMatchSemtag)) {
								result = true;
							}
						}
					}catch(Exception e){
						//Do Nothing
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
	public boolean isFsnTextNotUnique(String fsn, String conceptUuid) throws Exception{
		boolean result = false;
		I_TermFactory tf = Terms.get();
		try {
			int fsnTypeNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
			fsn = "+\"" + QueryParser.escape(fsn) + "\"";
			SearchResult results = tf.doLuceneSearch(fsn);
			TopDocs topDocs = results.topDocs;
			ScoreDoc[] docs = topDocs.scoreDocs;
			for (int i = 0 ; i < docs.length  ; i++) {
				try{
					Document doc = results.searcher.doc(i);
					int cnid = Integer.parseInt(doc.get("cnid"));
					int dnid = Integer.parseInt(doc.get("dnid"));
					I_DescriptionVersioned potential_fsn = Terms.get().getDescription(dnid, cnid);
					if (potential_fsn != null) {

						DescriptionVersionBI description = (DescriptionVersionBI) 
						Ts.get().getComponentVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), dnid);

						if (description.getTypeNid() == fsnTypeNid
								&& description.getText().equals(fsn)
								&& description.getLang().equals(potential_fsn.getLang())) {
							result = true;
						}
					}
				}catch(Exception e){
					//Do Nothing
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
	public boolean isActive(String conceptUUID) {
		boolean result = false;

		try {
			ConceptVersionBI concept = Ts.get().getConceptVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), UUID.fromString(conceptUUID));
			int status = concept.getConAttrs().getVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate()).getStatusNid();
			if (status == ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid() ||
					status == ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()) {
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
		return getValidSemtags().contains(semtag);
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
		//TODO implement for Stated when the definitive structure is defined
		if (conceptUuid != null) {
			try {
				result = getDescendants(new HashSet<I_GetConceptData>(), Terms.get().getConcept(UUID.fromString(conceptUuid))).size() > 0;
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
				if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid() 
						&& tuple.getLang().equals("en")) {
					if (tuple.getText().lastIndexOf("(") > -1 && tuple.getText().lastIndexOf(")") > -1) {
						currentSemtags.add(tuple.getText().substring(tuple.getText().lastIndexOf("(")+1,tuple.getText().lastIndexOf(")")));
					}
				}
			}

			if (getValidSemtags().containsAll(currentSemtags)) {
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
