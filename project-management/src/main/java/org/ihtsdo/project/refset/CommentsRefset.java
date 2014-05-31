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
package org.ihtsdo.project.refset;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class CommentsRefset.
 */
public class CommentsRefset extends Refset {

    /**
     * The HTT p_ pattern.
     */
    private final Pattern HTTP_PATTERN = Pattern.compile("(http[s]?://)?(([\\w\\-]+\\.)+[\\w]+)");
    /**
     * The default comment type.
     */
    private I_GetConceptData defaultCommentType;
    /**
     * The comment.
     */
    private I_GetConceptData comment;

    /**
     * Instantiates a new comments refset.
     *
     * @param refsetConcept the refset concept
     * @throws Exception the exception
     */
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

    /**
     * Gets the comments.
     *
     * @param componentId the component id
     * @return the comments
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
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
                    comments.put(commentsMember, commentsExtensionPart.getString1Value() + " - Time: " + commentsExtensionPart.getTime());
                }
            }
        }
        return comments;
    }

    /**
     * Retire comments member.
     *
     * @param commentstMember the commentst member
     */
    public static void retireCommentsMember(I_ExtendByRef commentstMember) {
        try {
            I_TermFactory termFactory = Terms.get();
            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            I_GetConceptData concept = termFactory.getConcept(commentstMember.getRefsetId());
            I_ExtendByRefPart lastPart = TerminologyProjectDAO.getLastExtensionPart(commentstMember);
            for (PathBI editPath : config.getEditingPathSet()) {
                I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart.makeAnalog(
                        SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(),
                        Long.MAX_VALUE,
                        config.getDbConfig().getUserConcept().getNid(),
                        config.getEditCoordinate().getModuleNid(),
                        editPath.getConceptNid());
                commentstMember.addVersion(part);
            }
            termFactory.addUncommittedNoChecks(concept);
            termFactory.addUncommittedNoChecks(commentstMember);
            concept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }

        return;
    }

    /**
     * Gets the comment types.
     *
     * @return the comment types
     */
    public static List<I_GetConceptData> getCommentTypes() {
        List<I_GetConceptData> descendants = new ArrayList<I_GetConceptData>();
        return getCommentTypes(descendants);
    }

    /**
     * Gets the comment types.
     *
     * @param descendants the descendants
     * @return the comment types
     */
    public static List<I_GetConceptData> getCommentTypes(List<I_GetConceptData> descendants) {
        try {
            I_TermFactory termFactory = Terms.get();
            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            I_GetConceptData comment = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_COMMENT_TYPE.getPrimoridalUid());
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
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return descendants;
    }

    /**
     * Gets the full comments.
     *
     * @param componentId the component id
     * @return the full comments
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
     */
    public LinkedList<Comment> getFullComments(int componentId) throws IOException, TerminologyException {
        // TODO: move config to parameter
        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
        LinkedList<Comment> comments = new LinkedList<Comment>();
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
                            commentsExtensionPart.getString1Value() + " - Time: " + commentsExtensionPart.getTime(),
                            commentsExtensionPart.getTime(), commentsMember);
                    comments.add(comment);
                }
            }
        }
        Collections.sort(comments);
        return comments;
    }

    /**
     * Gets the comment sub types.
     *
     * @param type the type
     * @return the comment sub types
     */
    public static List<I_GetConceptData> getCommentSubTypes(I_GetConceptData type) {
        List<I_GetConceptData> descendants = new ArrayList<I_GetConceptData>();
        return getCommentSubTypes(descendants, type);
    }

    /**
     * Gets the comment sub types.
     *
     * @param descendants the descendants
     * @param type the type
     * @return the comment sub types
     */
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
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return descendants;
    }

    /**
     * Adds the comment.
     *
     * @param componentId the component id
     * @param comment the comment
     * @throws Exception the exception
     */
    public void addComment(int componentId, String comment) throws Exception {
        addComment(componentId, defaultCommentType.getNid(), defaultCommentType.getNid(), comment);
    }

    /**
     * Adds the comment.
     *
     * @param componentId the component id
     * @param commentType the comment type
     * @param comment the comment
     * @throws Exception the exception
     */
    public void addComment(int componentId, int commentType, String comment) throws Exception {
        addComment(componentId, commentType, defaultCommentType.getNid(), comment);
    }

    /**
     * Adds the comment.
     *
     * @param componentId the component id
     * @param commentType the comment type
     * @param commentSubType the comment sub type
     * @param comment the comment
     * @throws Exception the exception
     */
    public void addComment(int componentId, int commentType, int commentSubType, String comment) throws Exception {
        // TODO: move config to parameter
        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

        I_HelpRefsets refsetHelper = termFactory.getRefsetHelper(config);

        RefsetPropertyMap rfPropMap = new RefsetPropertyMap();
        rfPropMap.put(REFSET_PROPERTY.CID_ONE, commentType);
        rfPropMap.put(REFSET_PROPERTY.CID_TWO, commentSubType);
        rfPropMap.put(REFSET_PROPERTY.STRING_VALUE, comment);

        // hack for easy solution of duplicated comment text preventing a new comment add
        int count = 0;
        while (refsetHelper.hasRefsetExtension(this.refsetId, componentId, rfPropMap)) {
            rfPropMap.put(REFSET_PROPERTY.STRING_VALUE, comment + ".");
            if (count > 20) {
                // Security check: there is some problem here, exiting loop
                break;
            }
            count++;
        }

        // Removed to allow duplicate strings creations, using BdbTermFactory.createMember instead
        //refsetHelper.newRefsetExtension(this.refsetId, componentId, EConcept.REFSET_TYPES.CID_CID_STR, rfPropMap, config);
		//        BdbTermFactory.createMember(UUID.randomUUID(), componentId, EConcept.REFSET_TYPES.CID_CID_STR, org.ihtsdo.concept.Concept.get(refsetId),
		//                config, rfPropMap);
        
		refsetHelper.newRefsetExtension(refsetId, componentId, EConcept.REFSET_TYPES.CID_CID_STR, rfPropMap, // metadata
				config);
		termFactory.addUncommittedNoChecks(Terms.get().getConcept(refsetId));
        refsetConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
    }

    /**
     * Parses each comment for a component, and returns a list with all URLs,
     * using the comment as the description.
     *
     * @param componentId the component id
     * @return the urls
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TerminologyException the terminology exception
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

    /**
     * Adds the comment.
     *
     * @param componentId the component id
     * @param conceptNid the concept nid
     * @param conceptNid2 the concept nid2
     * @param comment2 the comment2
     * @throws TerminologyException the terminology exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws Exception the exception
     */
    public void addComment(UUID componentId, int conceptNid, int conceptNid2,
            String comment2) throws TerminologyException, IOException, Exception {
        addComment(termFactory.uuidToNative(componentId), conceptNid, conceptNid2,
                comment2);

    }

    /**
     * Adds the comment.
     *
     * @param componentId the component id
     * @param conceptNid the concept nid
     * @param comment2 the comment2
     * @throws TerminologyException the terminology exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws Exception the exception
     */
    public void addComment(UUID componentId, int conceptNid, String comment2) throws TerminologyException, IOException, Exception {

        addComment(termFactory.uuidToNative(componentId), conceptNid,
                comment2);

    }
}
