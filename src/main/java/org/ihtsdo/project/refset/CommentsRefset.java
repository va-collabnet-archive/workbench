package org.ihtsdo.project.refset;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.Precedence;

public class CommentsRefset extends Refset {

	private final Pattern HTTP_PATTERN = Pattern.compile("(http[s]?://)?(([\\w]+\\.)+[\\w]+)");
	private I_GetConceptData defaultCommentType;
	private I_GetConceptData comment;

	public CommentsRefset(I_GetConceptData refsetConcept) throws Exception {
		super();
		// TODO: validate if refsetConcept is comments refset?
		this.refsetConcept = refsetConcept;
		this.refsetName = refsetConcept.toString();
		this.refsetId = refsetConcept.getConceptNid();
		this.comment = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_COMMENT.getPrimoridalUid());
		this.defaultCommentType = termFactory.getConcept(Concept.WORKFLOW_COMMENT.getPrimoridalUid());
		termFactory = Terms.get();
	}

	public HashMap<I_ExtendByRef, String> getComments(int componentId) throws IOException, TerminologyException {
		// TODO: move config to parameter
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		HashMap<I_ExtendByRef, String> comments = new HashMap<I_ExtendByRef, String>();
		for (I_ExtendByRef commentsMember : termFactory.getAllExtensionsForComponent(componentId, true)) {
			if (commentsMember.getRefsetId() == this.refsetId) {
				long lastVersion = Long.MIN_VALUE;
				I_ExtendByRefPartCidCidString commentsExtensionPart = null;
				for (I_ExtendByRefVersion loopTuple : commentsMember.getTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), Precedence.TIME, config
						.getConflictResolutionStrategy())) {
					if (loopTuple.getTime() >= lastVersion) {
						lastVersion = loopTuple.getTime();
						commentsExtensionPart = (I_ExtendByRefPartCidCidString) loopTuple.getMutablePart();
					}
				}
				// TODO: convert time from int to readable time
				if (commentsExtensionPart != null) {
					comments.put(commentsMember, commentsExtensionPart.getStringValue() + " - Time: " + commentsExtensionPart.getTime());
				}
			}
		}
		return comments;
	}

	public static void retireCommentsMember(I_ExtendByRef commentstMember) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_ExtendByRefPart lastPart = TerminologyProjectDAO.getLastExtensionPart(commentstMember);
			for (PathBI editPath : config.getEditingPathSet()) {
				I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
				lastPart.makeAnalog(
						ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
						editPath.getConceptNid(),
						Long.MAX_VALUE);
				commentstMember.addVersion(part);
			}
			termFactory.addUncommittedNoChecks(commentstMember);
			termFactory.commit();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return;
	}

	public static List<I_GetConceptData> getCommentTypes() {
		List<I_GetConceptData> descendants = new ArrayList<I_GetConceptData>();
		return getCommentTypes(descendants);
	}

	public static List<I_GetConceptData> getCommentTypes(List<I_GetConceptData> descendants) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_GetConceptData comment =  Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_COMMENT_TYPE.getPrimoridalUid());
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			childrenSet.addAll(comment.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config
					.getConflictResolutionStrategy()));
			descendants.addAll(childrenSet);
			//			for (I_GetConceptData loopConcept : childrenSet) {
			//				descendants = getCommentSubTypes(descendants, loopConcept);
			//			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return descendants;
	}

	public List<Comment> getFullComments(int componentId) throws IOException, TerminologyException {
		// TODO: move config to parameter
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		List<Comment> comments = new ArrayList<Comment>();
		for (I_ExtendByRef commentsMember : termFactory.getAllExtensionsForComponent(componentId, true)) {
			if (commentsMember.getRefsetId() == this.refsetId) {
				long lastVersion = Long.MIN_VALUE;
				I_ExtendByRefPartCidCidString commentsExtensionPart = null;
				for (I_ExtendByRefVersion loopTuple : commentsMember.getTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), Precedence.TIME, config
						.getConflictResolutionStrategy())) {
					if (loopTuple.getTime() >= lastVersion) {
						lastVersion = loopTuple.getTime();
						commentsExtensionPart = (I_ExtendByRefPartCidCidString) loopTuple.getMutablePart();
					}
				}
				// TODO: convert time from int to readable time
				if (commentsExtensionPart != null) {
					Comment comment = new Comment(commentsExtensionPart.getC1id(), commentsExtensionPart.getC2id(), 
							commentsExtensionPart.getStringValue() + " - Time: " + commentsExtensionPart.getTime());
					comments.add(comment);
				}
			}
		}
		Collections.sort(comments, new CommentComparator());
		return comments;
	}

	public static List<I_GetConceptData> getCommentSubTypes(I_GetConceptData type) {
		List<I_GetConceptData> descendants = new ArrayList<I_GetConceptData>();
		return getCommentSubTypes(descendants, type);
	}

	public static List<I_GetConceptData> getCommentSubTypes(List<I_GetConceptData> descendants, I_GetConceptData type) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			childrenSet.addAll(type.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config
					.getConflictResolutionStrategy()));
			descendants.addAll(childrenSet);
			//			for (I_GetConceptData loopConcept : childrenSet) {
			//				descendants = getCommentSubTypes(descendants, loopConcept);
			//			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return descendants;
	}

	public void addComment(int componentId, String comment) throws Exception {
		addComment(componentId, defaultCommentType.getNid(), defaultCommentType.getNid(), comment);
	}

	public void addComment(int componentId, int commentType, String comment) throws Exception {
		addComment(componentId, commentType, defaultCommentType.getNid(), comment);
	}

	public void addComment(int componentId, int commentType, int commentSubType, String comment) throws Exception {
		// TODO: move config to parameter
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

		I_GetConceptData componentConcept = termFactory.getConcept(componentId);
		I_HelpRefsets refsetHelper = termFactory.getRefsetHelper(config);

		RefsetPropertyMap rfPropMap = new RefsetPropertyMap();
		rfPropMap.put(REFSET_PROPERTY.CID_ONE, commentType);
		rfPropMap.put(REFSET_PROPERTY.CID_TWO, commentSubType);
		rfPropMap.put(REFSET_PROPERTY.STRING_VALUE, comment);

		refsetHelper.newRefsetExtension(this.refsetId, componentId, EConcept.REFSET_TYPES.CID_CID_STR, rfPropMap, config);

		termFactory.addUncommittedNoChecks(componentConcept);
		termFactory.addUncommittedNoChecks(refsetConcept);
		termFactory.commit();
	}

	/**
	 * Parses each comment for a component, and returns a list with all URLs,
	 * using the comment as the description
	 * 
	 * @param componentId
	 *            the component id
	 * 
	 * @return the urls
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TerminologyException
	 *             the terminology exception
	 */
	public HashMap<URL, String> getUrls(int componentId) throws IOException, TerminologyException {
		HashMap<URL, String> urls = new HashMap<URL, String>();
		HashMap<I_ExtendByRef, String> comments = getComments(componentId);
		for (String comment : comments.values()) {
			Matcher matcher = HTTP_PATTERN.matcher(comment);
			while (matcher.find()) {
				try {
					urls.put(new URL(matcher.group()), comment);
				} catch (MalformedURLException e) {
					// Problem with URL, regex match was a false positive, do
					// nothing
				}
			}
		}
		return urls;
	}
}
