/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.task.refset.spec.compute;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery.GROUPING_TYPE;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.time.TimeUtil;

/**
 * Represents partial information contained in a refset spec. An example of a
 * statement is : "NOT: Concept is : Paracetamol"
 * 
 * @author Chrissy Hill
 * 
 */

public class DescStatement extends RefsetSpecStatement {

    private Collection<I_ShowActivity> activities;
    private StopActionListener stopListener = new StopActionListener();
    private I_RepresentIdSet possibleLuceneDescMatches;
    private I_RepresentIdSet possibleLuceneConcMatches;
    private Pattern regexPattern;

    private class StopActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            for (I_ShowActivity a : activities) {
                a.cancel();
            }
        }
    }

    /**
     * Constructor for refset spec statement.
     * 
     * @param useNotQualifier
     *            Whether to use the NOT qualifier.
     * @param queryToken
     *            The query type to use (e.g. "concept is")
     * @param queryConstraint
     *            The destination concept (e.g. "paracetamol")
     * @throws Exception
     */
    public DescStatement(boolean useNotQualifier, I_GetConceptData queryToken, I_AmTermComponent queryConstraint,
            int refsetSpecNid, I_ConfigAceFrame config) throws Exception {
        super(useNotQualifier, queryToken, queryConstraint, refsetSpecNid, config);
        for (QUERY_TOKENS token : QUERY_TOKENS.values()) {
            if (queryToken.getConceptNid() == token.nid) {
                tokenEnum = token;
                break;
            }
        }
        if (tokenEnum == null) {
            throw new RuntimeException("Unknown query type : " + queryToken);
        }
    }

    /**
     * Constructor for refset spec statement.
     * 
     * @param useNotQualifier
     *            Whether to use the NOT qualifier.
     * @param queryToken
     *            The query type to use (e.g. "concept is")
     * @param queryConstraint
     *            String value for regex or lucene search
     * @throws Exception
     */
    public DescStatement(boolean useNotQualifier, I_GetConceptData queryToken, String queryConstraint,
            int refsetSpecNid, I_ConfigAceFrame config) throws Exception {
        super(useNotQualifier, queryToken, queryConstraint, refsetSpecNid, config);

        for (QUERY_TOKENS token : QUERY_TOKENS.values()) {
            if (queryToken.getConceptNid() == token.nid) {
                tokenEnum = token;
                break;
            }
        }
        if (tokenEnum == null) {
            throw new RuntimeException("Unknown query type : " + queryToken);
        }
    }

    public boolean getStatementResult(I_AmTermComponent component, GROUPING_TYPE version, I_Position v1_is,
			I_Position v2_is) throws IOException, TerminologyException {
        if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
            I_DescriptionVersioned descriptionVersioned = (I_DescriptionVersioned) component;
			I_DescriptionTuple descriptionTuple = descriptionVersioned
					.getLastTuple();

			if (version != null || v1_is != null || v2_is != null) {
				if (version == null)
					throw new TerminologyException("Not in scope of V1 or V2: "
							+ tokenEnum + " " + descriptionTuple.getText());
				if (v1_is == null)
					throw new TerminologyException("Need to set V1 IS: "
							+ tokenEnum + " " + descriptionTuple.getText());
				if (v2_is == null)
					throw new TerminologyException("Need to set V2 IS: "
							+ tokenEnum + " " + descriptionTuple.getText());
			}

            switch (tokenEnum) {
            case DESC_IS:
				if (version == null) {
                return descriptionIs(descriptionTuple);
				} else {
					return descriptionIs(descriptionVersioned, getVersion(
							version, v1_is, v2_is));
				}
            case DESC_IS_MEMBER_OF:
				if (version == null) {
                return descriptionIsMemberOf(descriptionTuple);
				} else {
					throw new TerminologyException(tokenEnum
							+ ": Unsupported operation for version scope.");
				}
            case DESC_STATUS_IS:
				if (version == null) {
                return descriptionStatusIs(descriptionTuple);
				} else {
					return descriptionStatusIs(descriptionVersioned,
							getVersion(version, v1_is, v2_is));
				}
            case DESC_STATUS_IS_CHILD_OF:
				if (version == null) {
                return descriptionStatusIsChildOf(descriptionTuple);
				} else {
					return descriptionStatusIsChildOf(descriptionVersioned,
							getVersion(version, v1_is, v2_is));
				}
            case DESC_STATUS_IS_KIND_OF:
				if (version == null) {
                return descriptionStatusIsKindOf(descriptionTuple);
				} else {
					return descriptionStatusIsKindOf(descriptionVersioned,
							getVersion(version, v1_is, v2_is));
				}
            case DESC_STATUS_IS_DESCENDENT_OF:
				if (version == null) {
           		     return descriptionStatusIsDescendentOf(descriptionTuple);
				} else {
					return descriptionStatusIsDescendentOf(
							descriptionVersioned, getVersion(version, v1_is,
									v2_is));
				}
            case DESC_TYPE_IS:
				if (version == null) {
                return descriptionTypeIs(descriptionTuple);
				} else {
					return descriptionTypeIs(descriptionVersioned, getVersion(
							version, v1_is, v2_is));
				}
            case DESC_TYPE_IS_CHILD_OF:
				if (version == null) {
                return descriptionTypeIsChildOf(descriptionTuple);
				} else {
					return descriptionTypeIsChildOf(descriptionVersioned,
							getVersion(version, v1_is, v2_is));
				}
            case DESC_TYPE_IS_KIND_OF:
				if (version == null) {
                return descriptionTypeIsKindOf(descriptionTuple);
				} else {
					return descriptionTypeIsKindOf(descriptionVersioned,
							getVersion(version, v1_is, v2_is));
				}
            case DESC_TYPE_IS_DESCENDENT_OF:
				if (version == null) {
                return descriptionTypeIsDescendentOf(descriptionTuple);
				} else {
					return descriptionTypeIsDescendentOf(descriptionVersioned,
							getVersion(version, v1_is, v2_is));
				}
            case DESC_REGEX_MATCH:
				if (version == null) {
                return descriptionRegexMatch(descriptionTuple);
				} else {
					return descriptionRegexMatch(descriptionVersioned,
							getVersion(version, v1_is, v2_is));
				}
            case DESC_LUCENE_MATCH:
				if (version == null) {
                return descriptionLuceneMatch(descriptionTuple);
				} else {
					throw new TerminologyException(tokenEnum
							+ ": Unsupported operation for version scope.");
				}
			case ADDED_DESCRIPTION:
				return addedDescription(descriptionVersioned, version, v1_is,
						v2_is);
			case CHANGED_DESCRIPTION_CASE:
				return changedDescriptionCase(descriptionVersioned, version,
						v1_is, v2_is);
			case CHANGED_DESCRIPTION_LANGUAGE:
				return changedDescriptionLanguage(descriptionVersioned,
						version, v1_is, v2_is);
			case CHANGED_DESCRIPTION_STATUS:
				return changedDescriptionStatus(descriptionVersioned, version,
						v1_is, v2_is);
			case CHANGED_DESCRIPTION_TERM:
				return changedDescriptionTerm(descriptionVersioned, version,
						v1_is, v2_is);
			case CHANGED_DESCRIPTION_TYPE:
				return changedDescriptionType(descriptionVersioned, version,
						v1_is, v2_is);
            default:
                throw new RuntimeException("Can't handle queryToken: " + queryToken);
            }
        } else {
            return false;
        }
    }

    private I_ShowActivity setupActivityPanel(I_RepresentIdSet parentPossibleConcepts) {
        I_ShowActivity activity =
                Terms.get().newActivityPanel(true, config, "<html>Possible: <br>" + this.toHtmlFragment(), true);
        activity.setIndeterminate(true);
        activity.setProgressInfoLower("Incoming count: " + parentPossibleConcepts.cardinality());
        activity.addStopActionListener(stopListener);
        return activity;
    }

    public I_RepresentIdSet getPossibleConcepts(I_RepresentIdSet parentPossibleConcepts,
            Collection<I_ShowActivity> activities) throws TerminologyException, IOException {
        I_ShowActivity activity = setupActivityPanel(parentPossibleConcepts);
        activities.add(activity);
        long startTime = System.currentTimeMillis();

        I_RepresentIdSet possibleConcepts = termFactory.getEmptyIdSet();
        if (parentPossibleConcepts == null) {
            parentPossibleConcepts = termFactory.getConceptNidSet();
        }
        activity.setProgressInfoLower("Incoming count: " + parentPossibleConcepts.cardinality());

        switch (tokenEnum) {
        case DESC_IS:
            possibleConcepts.or(parentPossibleConcepts);
            break;
        case DESC_IS_MEMBER_OF:
            Collection<? extends I_ExtendByRef> refsetExtensions =
                    termFactory.getRefsetExtensionMembers(((I_GetConceptData) queryConstraint).getConceptNid());
            Set<I_GetConceptData> refsetMembers = new HashSet<I_GetConceptData>();
            for (I_ExtendByRef ext : refsetExtensions) {
                refsetMembers.add(termFactory.getConcept(ext.getComponentId()));
            }
            I_RepresentIdSet refsetMemberSet = termFactory.getIdSetfromTermCollection(refsetMembers);
            if (isNegated()) {
                possibleConcepts.or(parentPossibleConcepts);
            } else {
                possibleConcepts.or(refsetMemberSet);
            }

            break;
        case DESC_LUCENE_MATCH:
            getPossibleDescriptions(termFactory.getEmptyIdSet(), activities);
            if (possibleLuceneConcMatches != null) {
                possibleConcepts.or(possibleLuceneConcMatches);
            }
            break;
        case DESC_STATUS_IS:
        case DESC_STATUS_IS_CHILD_OF:
        case DESC_STATUS_IS_KIND_OF:
        case DESC_STATUS_IS_DESCENDENT_OF:
        case DESC_TYPE_IS:
        case DESC_TYPE_IS_CHILD_OF:
        case DESC_TYPE_IS_KIND_OF:
        case DESC_TYPE_IS_DESCENDENT_OF:
        case DESC_REGEX_MATCH:
            possibleConcepts.or(parentPossibleConcepts);
            break;
		case ADDED_DESCRIPTION:
		case CHANGED_DESCRIPTION_CASE:
		case CHANGED_DESCRIPTION_LANGUAGE:
		case CHANGED_DESCRIPTION_STATUS:
		case CHANGED_DESCRIPTION_TERM:
		case CHANGED_DESCRIPTION_TYPE:
			// TODO - EKM
			possibleConcepts.or(parentPossibleConcepts);
			break;
        default:
            throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
        setPossibleConceptsCount(possibleConcepts.cardinality());

        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;
        String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);
        activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";  Incoming count: "
            + parentPossibleConcepts.cardinality() + "; Outgoing count: " + possibleConcepts.cardinality());
        activity.complete();
        return possibleConcepts;
    }

    public I_RepresentIdSet getPossibleDescriptions(I_RepresentIdSet parentPossibleDescriptions,
            Collection<I_ShowActivity> activities) throws TerminologyException, IOException {

        I_RepresentIdSet possibleDescriptions = termFactory.getEmptyIdSet();
        possibleLuceneDescMatches = null;
        possibleLuceneConcMatches = null;

        if (parentPossibleDescriptions == null) {
            parentPossibleDescriptions = termFactory.getConceptNidSet();
        }

        switch (tokenEnum) {
        case DESC_IS:
            if (isNegated()) {
                possibleDescriptions.or(parentPossibleDescriptions);
            } else {
                possibleDescriptions.setMember(((I_DescriptionVersioned) queryConstraint).getConceptNid());
            }
            break;
        case DESC_IS_MEMBER_OF:
            Collection<? extends I_ExtendByRef> refsetExtensions =
                    termFactory.getRefsetExtensionMembers(((I_AmTermComponent) queryConstraint).getNid());
            Set<Integer> refsetMembers = new HashSet<Integer>();
            for (I_ExtendByRef ext : refsetExtensions) {
                int componentId = ext.getComponentId();
                if (Terms.get().hasConcept(componentId)) {
                    refsetMembers.add(componentId);
                } else {
                    I_DescriptionVersioned desc = Terms.get().getDescription(componentId);
                    if (desc != null) {
                        refsetMembers.add(desc.getConceptNid());
                    }
                }
            }
            I_RepresentIdSet refsetMemberSet = termFactory.getIdSetFromIntCollection(refsetMembers);
            if (isNegated()) {
                possibleDescriptions.or(parentPossibleDescriptions);
            } else {
                possibleDescriptions.or(refsetMemberSet);
            }

            break;
        case DESC_STATUS_IS:
        case DESC_STATUS_IS_CHILD_OF:
        case DESC_STATUS_IS_KIND_OF:
        case DESC_STATUS_IS_DESCENDENT_OF:
        case DESC_TYPE_IS:
        case DESC_TYPE_IS_CHILD_OF:
        case DESC_TYPE_IS_KIND_OF:
        case DESC_TYPE_IS_DESCENDENT_OF:
        case DESC_REGEX_MATCH:
            possibleDescriptions.or(parentPossibleDescriptions);
            break;
        case DESC_LUCENE_MATCH:
            if (isNegated()) {
                possibleDescriptions.or(parentPossibleDescriptions);
            } else {
                String queryConstraintString = (String) queryConstraint;
                try {
                    SearchResult results = termFactory.doLuceneSearch(queryConstraintString);

                    if (results == null || results.topDocs.totalHits == 0) {
                        possibleDescriptions.or(parentPossibleDescriptions);
                        break;
                    }
                    possibleLuceneConcMatches = Terms.get().getEmptyIdSet();
                    possibleLuceneDescMatches = Terms.get().getEmptyIdSet();
                    for (ScoreDoc scoreDoc: results.topDocs.scoreDocs) {
                        Document doc = results.searcher.doc(scoreDoc.doc);

                        int cnid = Integer.parseInt(doc.get("cnid"));
                        int dnid = Integer.parseInt(doc.get("dnid"));
                        possibleLuceneDescMatches.setMember(dnid);
                        possibleLuceneConcMatches.setMember(cnid);
                        possibleDescriptions.setMember(dnid);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new TerminologyException(e.getMessage());

                }
            }
            break;
        default:
            throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
        setPossibleConceptsCount(possibleDescriptions.cardinality());
        return possibleDescriptions;
    }

    @Override
    public I_RepresentIdSet getPossibleRelationships(I_RepresentIdSet parentPossibleConcepts,
            Collection<I_ShowActivity> activities) throws TerminologyException, IOException {
        throw new TerminologyException("Get possible relationships in desc statement unsupported operation.");
    }

    private boolean descriptionIsMemberOf(I_DescriptionTuple descriptionBeingTested) throws IOException,
            TerminologyException {
        return componentIsMemberOf(descriptionBeingTested.getDescId());
    }

    private boolean descriptionTypeIs(I_DescriptionTuple descriptionBeingTested) {
        return descriptionTypeIs((I_GetConceptData) queryConstraint, descriptionBeingTested);
    }

    private boolean descriptionTypeIs(I_GetConceptData requiredDescriptionType,
            I_DescriptionTuple descriptionBeingTested) {
        return descriptionBeingTested.getTypeNid() == requiredDescriptionType.getConceptNid();
    }

    /**
     * Checks if the description being tested has a description type matching
     * the query constraint. This also checks for the description type's
     * children (depth >= 1);
     */
    private boolean descriptionTypeIsKindOf(I_DescriptionTuple descriptionBeingChecked) throws IOException,
            TerminologyException {

        I_GetConceptData descTypeBeingChecked = termFactory.getConcept(descriptionBeingChecked.getTypeNid());

        return (((I_GetConceptData) queryConstraint).isParentOfOrEqualTo(descTypeBeingChecked, currentStatuses,
            allowedTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config
                .getConflictResolutionStrategy()));
    }

    /**
     * This checks the description type for depth >= 1.
     * 
     * @param requiredType
     * @param descriptionBeingChecked
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean descriptionTypeIsDescendentOf(I_GetConceptData requiredType,
            I_DescriptionTuple descriptionBeingChecked) throws IOException, TerminologyException {

        I_GetConceptData descTypeBeingChecked = termFactory.getConcept(descriptionBeingChecked.getTypeNid());

        return (((I_GetConceptData) queryConstraint).isParentOf(descTypeBeingChecked, currentStatuses, allowedTypes,
            config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()));
    }

    private boolean descriptionTypeIsDescendentOf(I_DescriptionTuple descriptionBeingChecked) throws IOException,
            TerminologyException {
        return descriptionTypeIsDescendentOf((I_GetConceptData) queryConstraint, descriptionBeingChecked);
    }

    private boolean descriptionTypeIsChildOf(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException,
            IOException {
        try {

            // get list of all children of input concept
            Set<? extends I_GetConceptData> childDescTypes =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, config
                        .getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

            // call descriptionTypeIs on each
            for (I_GetConceptData childDescType : childDescTypes) {
                if (descriptionTypeIs(childDescType, descriptionBeingChecked)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }

        return false;
    }

    private boolean descriptionStatusIs(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException {
        return descriptionBeingChecked.getStatusNid() == ((I_GetConceptData) queryConstraint).getConceptNid();
    }

    private boolean descriptionStatusIs(I_GetConceptData requiredStatus, I_DescriptionTuple descriptionBeingChecked)
            throws TerminologyException {
        return descriptionBeingChecked.getStatusNid() == requiredStatus.getConceptNid();
    }

    private boolean descriptionStatusIsChildOf(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException,
            IOException {
        try {

            Set<? extends I_GetConceptData> childStatuses =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, config
                        .getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

            for (I_GetConceptData childStatus : childStatuses) {
                if (descriptionStatusIs(childStatus, descriptionBeingChecked)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean descriptionStatusIsDescendentOf(I_DescriptionTuple descriptionBeingChecked)
            throws TerminologyException, IOException {
        return descriptionStatusIsDescendentOf((I_GetConceptData) queryConstraint, descriptionBeingChecked);
    }

    private boolean descriptionStatusIsDescendentOf(I_GetConceptData requiredStatus,
            I_DescriptionTuple descriptionBeingChecked) throws TerminologyException, IOException {

        I_GetConceptData statusBeingChecked = termFactory.getConcept(descriptionBeingChecked.getStatusNid());

        return (((I_GetConceptData) queryConstraint).isParentOf(statusBeingChecked, currentStatuses, allowedTypes,
            config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()));

    }

    private boolean descriptionStatusIsKindOf(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException,
            IOException {
        I_GetConceptData statusBeingChecked = termFactory.getConcept(descriptionBeingChecked.getStatusNid());

        return (((I_GetConceptData) queryConstraint).isParentOfOrEqualTo(statusBeingChecked, currentStatuses,
            allowedTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config
                .getConflictResolutionStrategy()));
    }

    private boolean descriptionIs(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException {
        I_DescriptionVersioned queryConstraintDesc = (I_DescriptionVersioned) queryConstraint;
        return descriptionBeingChecked.getDescId() == queryConstraintDesc.getDescId();
    }

    private boolean descriptionRegexMatch(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException {
        if (regexPattern == null) {
            String queryConstraintString = (String) queryConstraint;
            regexPattern = Pattern.compile(queryConstraintString);
            AceLog.getAppLog().info("Compiling regex: " + regexPattern + " into: " + regexPattern);
        }

        if (regexPattern.matcher(descriptionBeingChecked.getText()).find()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean descriptionLuceneMatch(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException {
        if (possibleLuceneDescMatches != null) {
            return possibleLuceneDescMatches.isMember(descriptionBeingChecked.getDescId());
        }
        String queryConstraintString = (String) queryConstraint;
        SearchResult results;
        try {
        	results = termFactory.doLuceneSearch(queryConstraintString);

            if (results == null || results.topDocs.totalHits == 0) {
                return false;
            } else {

                 for (ScoreDoc scoreDoc: results.topDocs.scoreDocs) {
                    Document doc = results.searcher.doc(scoreDoc.doc);
                    int dnid = Integer.parseInt(doc.get("dnid"));
                    int cnid = Integer.parseInt(doc.get("cnid"));

                    I_DescriptionVersioned description = termFactory.getDescription(dnid, cnid);
                    if (descriptionBeingChecked.getDescId() == description.getDescId()) {
                        return true;
                    }
                }

                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

	private boolean addedDescription(
			I_DescriptionVersioned descriptionBeingTested,
			GROUPING_TYPE version, I_Position v1_is, I_Position v2_is)
			throws TerminologyException, IOException {
		try {
			I_DescriptionPart a1 = getVersion(descriptionBeingTested, v1_is);
			I_DescriptionPart a2 = getVersion(descriptionBeingTested, v2_is);
			return (a1 == null && a2 != null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

	private boolean changedDescriptionCase(
			I_DescriptionVersioned descriptionBeingTested,
			GROUPING_TYPE version, I_Position v1_is, I_Position v2_is)
			throws TerminologyException, IOException {
		try {
			I_DescriptionPart a1 = getVersion(descriptionBeingTested, v1_is);
			I_DescriptionPart a2 = getVersion(descriptionBeingTested, v2_is);
			return (a1 != null && a2 != null
					&& a1.getVersion() != a2.getVersion() && a1
					.isInitialCaseSignificant() != a2
					.isInitialCaseSignificant());
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

	private boolean changedDescriptionLanguage(
			I_DescriptionVersioned descriptionBeingTested,
			GROUPING_TYPE version, I_Position v1_is, I_Position v2_is)
			throws TerminologyException, IOException {
		try {
			I_DescriptionPart a1 = getVersion(descriptionBeingTested, v1_is);
			I_DescriptionPart a2 = getVersion(descriptionBeingTested, v2_is);
			return (a1 != null && a2 != null
					&& a1.getVersion() != a2.getVersion() && !a1.getLang()
					.equals(a2.getLang()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

	private boolean changedDescriptionStatus(
			I_DescriptionVersioned descriptionBeingTested,
			GROUPING_TYPE version, I_Position v1_is, I_Position v2_is)
			throws TerminologyException, IOException {
		try {
			I_DescriptionPart a1 = getVersion(descriptionBeingTested, v1_is);
			I_DescriptionPart a2 = getVersion(descriptionBeingTested, v2_is);
			return (a1 != null && a2 != null
					&& a1.getVersion() != a2.getVersion() && a1.getStatusId() != a2
					.getStatusId());
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

	private boolean changedDescriptionTerm(
			I_DescriptionVersioned descriptionBeingTested,
			GROUPING_TYPE version, I_Position v1_is, I_Position v2_is)
			throws TerminologyException, IOException {
		try {
			I_DescriptionPart a1 = getVersion(descriptionBeingTested, v1_is);
			I_DescriptionPart a2 = getVersion(descriptionBeingTested, v2_is);
			return (a1 != null && a2 != null
					&& a1.getVersion() != a2.getVersion() && !a1.getText()
					.equals(a2.getText()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

	private boolean changedDescriptionType(
			I_DescriptionVersioned descriptionBeingTested,
			GROUPING_TYPE version, I_Position v1_is, I_Position v2_is)
			throws TerminologyException, IOException {
		try {
			I_DescriptionPart a1 = getVersion(descriptionBeingTested, v1_is);
			I_DescriptionPart a2 = getVersion(descriptionBeingTested, v2_is);
			return (a1 != null && a2 != null
					&& a1.getVersion() != a2.getVersion() && a1.getTypeId() != a2
					.getTypeId());
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

	private I_DescriptionPart getVersion(
			I_DescriptionVersioned descriptionBeingTested, I_Position vn_is)
			throws TerminologyException {
		try {
			ArrayList<I_AmPart> parts = new ArrayList<I_AmPart>(
					descriptionBeingTested.getMutableParts());
			I_AmPart part = getVersion(parts, vn_is, false);
			return (I_DescriptionPart) part;
			// I_DescriptionPart an = null;
			// for (I_DescriptionPart a :
			// descriptionBeingTested.getMutableParts()) {
			// // Must be on the path
			// // Find the greatest version <= the one of interest
			// if (a.getPathId() == vn_is.getPath().getConceptId()
			// && a.getVersion() <= vn_is.getVersion()
			// && (an == null || an.getVersion() < a.getVersion()))
			// an = a;
			// }
			// return an;
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

	private boolean descriptionIs(
			I_DescriptionVersioned descriptionBeingTested, I_Position pos)
			throws TerminologyException {
		I_DescriptionVersioned queryConstraintDesc = (I_DescriptionVersioned) queryConstraint;
		I_DescriptionPart a = getVersion(descriptionBeingTested, pos);
		return (a != null && descriptionBeingTested.getDescId() == queryConstraintDesc
				.getDescId());
	}

	private boolean descriptionStatusIs(
			I_DescriptionVersioned descriptionBeingTested, I_Position pos)
			throws TerminologyException {
		I_DescriptionPart a = getVersion(descriptionBeingTested, pos);
		return (a != null && a.getStatusId() == ((I_GetConceptData) queryConstraint)
				.getConceptNid());
	}

	private boolean descriptionStatusIsChildOf(
			I_DescriptionVersioned descriptionBeingTested, I_Position pos)
			throws TerminologyException, IOException {
		I_DescriptionPart a = getVersion(descriptionBeingTested, pos);
		if (a == null)
			return false;
		return conceptIsChildOf(Terms.get().getConcept(a.getStatusId()),
				(I_GetConceptData) queryConstraint, pos);
	}

	private boolean descriptionStatusIsDescendentOf(
			I_DescriptionVersioned descriptionBeingTested, I_Position pos)
			throws TerminologyException, IOException {
		I_DescriptionPart a = getVersion(descriptionBeingTested, pos);
		if (a == null)
			return false;
		return conceptIsDescendantOf(Terms.get().getConcept(a.getStatusId()),
				(I_GetConceptData) queryConstraint, pos);
	}

	private boolean descriptionStatusIsKindOf(
			I_DescriptionVersioned descriptionBeingTested, I_Position pos)
			throws TerminologyException, IOException {
		return descriptionStatusIs(descriptionBeingTested, pos)
				|| descriptionStatusIsDescendentOf(descriptionBeingTested, pos);
	}

	private boolean descriptionTypeIs(
			I_DescriptionVersioned descriptionBeingTested, I_Position pos)
			throws TerminologyException {
		I_DescriptionPart a = getVersion(descriptionBeingTested, pos);
		return (a != null && a.getTypeId() == ((I_GetConceptData) queryConstraint)
				.getConceptNid());
	}

	private boolean descriptionTypeIsChildOf(
			I_DescriptionVersioned descriptionBeingTested, I_Position pos)
			throws TerminologyException, IOException {
		I_DescriptionPart a = getVersion(descriptionBeingTested, pos);
		if (a == null)
			return false;
		return conceptIsChildOf(Terms.get().getConcept(a.getTypeId()),
				(I_GetConceptData) queryConstraint, pos);
	}

	private boolean descriptionTypeIsDescendentOf(
			I_DescriptionVersioned descriptionBeingTested, I_Position pos)
			throws TerminologyException, IOException {
		I_DescriptionPart a = getVersion(descriptionBeingTested, pos);
		if (a == null)
			return false;
		return conceptIsDescendantOf(Terms.get().getConcept(a.getTypeId()),
				(I_GetConceptData) queryConstraint, pos);
	}

	private boolean descriptionTypeIsKindOf(
			I_DescriptionVersioned descriptionBeingTested, I_Position pos)
			throws TerminologyException, IOException {
		return descriptionTypeIs(descriptionBeingTested, pos)
				|| descriptionTypeIsDescendentOf(descriptionBeingTested, pos);
	}

	private boolean descriptionRegexMatch(
			I_DescriptionVersioned descriptionBeingTested, I_Position pos)
			throws TerminologyException {
		if (regexPattern == null) {
			String queryConstraintString = (String) queryConstraint;
			regexPattern = Pattern.compile(queryConstraintString);
			AceLog.getAppLog().info(
					"Compiling regex: " + regexPattern + " into: "
							+ regexPattern);
		}
		I_DescriptionPart a = getVersion(descriptionBeingTested, pos);
		if (a == null)
			return false;
		return regexPattern.matcher(a.getText()).find();
	}

}
