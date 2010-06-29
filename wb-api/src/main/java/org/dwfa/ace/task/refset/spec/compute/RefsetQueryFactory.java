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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.lucene.queryParser.ParseException;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class RefsetQueryFactory {

    public static RefsetSpecQuery createQuery(I_ConfigAceFrame configFrame, I_TermFactory termFactory,
            I_GetConceptData refsetSpec, I_GetConceptData refset, RefsetComputeType refsetType) throws IOException,
            TerminologyException, ParseException {

        // create tree object that corresponds to the database's refset spec
        Collection<? extends I_ExtendByRef> extensions =
                Terms.get()
                    .getAllExtensionsForComponent(refsetSpec.getConceptId(), true);
        HashMap<Integer, DefaultMutableTreeNode> extensionMap = new HashMap<Integer, DefaultMutableTreeNode>();
        HashSet<Integer> fetchedComponents = new HashSet<Integer>();
        fetchedComponents.add(refsetSpec.getConceptId());

        addExtensionsToMap(extensions, extensionMap, fetchedComponents, refsetSpec.getConceptId());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(refsetSpec);
        for (DefaultMutableTreeNode extNode : extensionMap.values()) {
            I_ExtendByRef ext = (I_ExtendByRef) extNode.getUserObject();
            if (ext.getComponentId() == refsetSpec.getConceptId()) {
                root.add(extNode);
            } else {
                extensionMap.get(ext.getComponentId()).add(extNode);
            }
        }

        // create refset spec query
        I_GetConceptData orConcept = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_OR_GROUPING.getUids());

        RefsetSpecQuery query = new RefsetSpecQuery(orConcept, refsetSpec.getNid());
        query = processNode(root, query, refsetType, configFrame, termFactory, refsetSpec.getNid());
              
        return query;

    }

    public static List<String> removeDangles(RefsetSpecQuery query) {
        List<String> warningList = new ArrayList<String>();
        Iterator<RefsetSpecQuery> subQueryItr = query.getSubqueries().iterator();
        while (subQueryItr.hasNext()) {
            RefsetSpecQuery subQuery = subQueryItr.next();
            warningList.addAll(removeDangles(subQuery));
            if (subQuery.getStatements().size() == 0 && subQuery.getSubqueries().size() == 0) {
                warningList.add("Dangling subquery: " + subQuery);
                query.getAllComponents().remove(subQuery);
                subQueryItr.remove();
            }
        }
        return warningList;
    }

    /**
     * Create a "possible" query that contains all historical clauses to iterate
     * over, even retired clauses.
     * 
     * @param configFrame
     * @param termFactory
     * @param refsetSpec
     * @param refset
     * @return
     * @throws IOException
     * @throws TerminologyException
     * @throws ParseException
     */
    /*
     * public static RefsetSpecQuery createPossibleQuery(I_ConfigAceFrame configFrame, I_TermFactory termFactory,
     * I_GetConceptData refsetSpec, I_GetConceptData refset) throws IOException, TerminologyException,
     * ParseException {
     * // create tree object that corresponds to the database's refset spec
     * List<I_ExtendByRef> extensions =
     * Terms.get().getAllExtensionsForComponent(refsetSpec.getConceptId(), true);
     * HashMap<Integer, DefaultMutableTreeNode> extensionMap = new HashMap<Integer, DefaultMutableTreeNode>();
     * HashSet<Integer> fetchedComponents = new HashSet<Integer>();
     * fetchedComponents.add(refsetSpec.getConceptId());
     * addExtensionsToMap(extensions, extensionMap, fetchedComponents);
     * DefaultMutableTreeNode root = new DefaultMutableTreeNode(refsetSpec);
     * for (DefaultMutableTreeNode extNode : extensionMap.values()) {
     * I_ExtendByRef ext = (I_ExtendByRef) extNode.getUserObject();
     * if (ext.getComponentId() == refsetSpec.getConceptId()) {
     * root.add(extNode);
     * } else {
     * extensionMap.get(ext.getComponentId()).add(extNode);
     * }
     * }
     * // create refset spec query
     * I_GetConceptData orConcept = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_OR_GROUPING.getUids());
     * RefsetSpecQuery query = new RefsetSpecQuery(orConcept);
     * query = processNode(root, query, CONCEPT, configFrame, termFactory);
     * return query;
     * }
     */

    /**
     * Processes a node in our refset spec tree structure. For each child of the
     * node, we recursively determine the refset type (corresponds to a
     * sub-query or statement). This includes checking any grandchildren,
     * great-grandchildren etc.
     * 
     * @param node
     *            The node to which the processing begins.
     * @param query
     *            The query to add the processed node's information to.
     * @param configFrame
     * @param termFactory
     * @return A query containing the updated node's information.
     * @throws IOException
     * @throws TerminologyException
     * @throws ParseException
     */
    private static RefsetSpecQuery processNode(DefaultMutableTreeNode node, RefsetSpecQuery query,
            RefsetComputeType computeType, I_ConfigAceFrame configFrame, I_TermFactory termFactory, int refsetSpecNid) throws IOException,
            TerminologyException, ParseException {
    	
        if (query == null) {
            throw new TerminologyException("Invalid refset spec : null query item used.");
        }

        int childCount = node.getChildCount();

        for (int i = 0; i < childCount; i++) {

            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);

            // determine type of current child
            I_ExtendByRef currExt = (I_ExtendByRef) childNode.getUserObject();

            List<I_ExtendByRefVersion> extensions =
                    (List<I_ExtendByRefVersion>) currExt.getTuples(configFrame.getAllowedStatus(), 
                        configFrame.getViewPositionSetReadOnly(), configFrame.getPrecedence(),
                        configFrame.getConflictResolutionStrategy());
            if (extensions.size() > 0) {
                I_ExtendByRefPart thinPart = extensions.get(0).getMutablePart();

                if (thinPart instanceof I_ExtendByRefPartCidCidCid) {

                    // structural query e.g. true : is-concept : height
                    I_ExtendByRefPartCidCidCid part = (I_ExtendByRefPartCidCidCid) thinPart;

                    I_GetConceptData truthToken = termFactory.getConcept(part.getC1id());
                    I_GetConceptData groupingToken = termFactory.getConcept(part.getC2id());
                    I_AmTermComponent constraint;
                    if (termFactory.hasConcept(part.getC3id())) {
                        constraint = termFactory.getConcept(part.getC3id());
                    } else {
                        constraint =
                                termFactory.getDescription(part.getC3id());
                    } // TODO add rel

                    RefsetComputeType statementType = RefsetComputeType.getTypeFromQueryToken(groupingToken);
                    switch (statementType) {
                    case CONCEPT:
                        if (computeType.equals(RefsetComputeType.CONCEPT)) {
                            query.addConceptStatement(getNegation(truthToken, termFactory), groupingToken, constraint, refsetSpecNid);
                            break;
                        } else {
                            throw new TerminologyException("Badly formed spec: '" + groupingToken.getInitialText()
                                + "' within a " + computeType.toString() + " refset compute.");
                        }
                    case DESCRIPTION:
                        if (computeType.equals(RefsetComputeType.DESCRIPTION)) {
                            query.addDescStatement(getNegation(truthToken, termFactory), groupingToken, constraint, refsetSpecNid);
                            break;
                        } else {
                            throw new TerminologyException("Badly formed spec: '" + groupingToken.getInitialText()
                                + "' within a " + computeType.toString() + " refset compute.");
                        }
                    case RELATIONSHIP:
                        if (computeType.equals(RefsetComputeType.RELATIONSHIP)) {
                            query.addRelStatement(getNegation(truthToken, termFactory), groupingToken, constraint, refsetSpecNid);
                            break;
                        } else {
                            throw new TerminologyException("Badly formed spec: '" + groupingToken.getInitialText()
                                + "' within a " + computeType.toString() + " refset compute.");
                        }
                    default:
                        throw new TerminologyException("Unknown type: " + groupingToken.getInitialText());
                    }
                } else if (thinPart instanceof I_ExtendByRefPartCidCidString) {
                    // structural query with string value
                    I_ExtendByRefPartCidCidString part = (I_ExtendByRefPartCidCidString) thinPart;

                    I_GetConceptData truthToken = termFactory.getConcept(part.getC1id());
                    I_GetConceptData groupingToken = termFactory.getConcept(part.getC2id());
                    String constraint = part.getStringValue();

                    RefsetComputeType statementType = RefsetComputeType.getTypeFromQueryToken(groupingToken);
                    switch (statementType) {
					case CONCEPT: {
						if (part.getC2id() == RefsetAuxiliary.Concept.DIFFERENCE_V1_IS
								.localize().getNid()
								|| part.getC2id() == RefsetAuxiliary.Concept.DIFFERENCE_V2_IS
										.localize().getNid()) {
							String pos_str = part.getStringValue();
							// System.out.println("1:" + pos_str);
							pos_str = pos_str.substring(pos_str
									.lastIndexOf("(") + 1, pos_str
									.lastIndexOf(")"));
							// System.out.println("2:" + pos_str);
							String p_str = pos_str.substring(0, pos_str
									.indexOf(" "));
							String v_str = pos_str.substring(pos_str
									.indexOf(" ") + 1);
							// System.out.println("p:" + p_str);
							// System.out.println("v:" + v_str);
							I_Path path = Terms.get().getPath(
									Integer.parseInt(p_str));
							if (part.getC2id() == RefsetAuxiliary.Concept.DIFFERENCE_V1_IS
									.localize().getNid()) {
								query.setV1Is(Terms.get().newPosition(path,
										Integer.parseInt(v_str)));
							} else {
								query.setV2Is(Terms.get().newPosition(path,
										Integer.parseInt(v_str)));
							}
						} else {
							throw new TerminologyException(
									"Error: Concept statement type returned within a concept-concept-string ext. This should only be description.");
						}
						break;
					}
                    case RELATIONSHIP:
                        throw new TerminologyException(
                            "Error: Relationship statement type returned within a concept-concept-string ext. This should only be description.");
                    case DESCRIPTION:
                        query.addDescStatement(getNegation(truthToken, termFactory), groupingToken, constraint, refsetSpecNid);
                        break;
                    default:
                        throw new TerminologyException("Unknown type: " + groupingToken.getInitialText());
                    }
                } else if (thinPart instanceof I_ExtendByRefPartCidCid) {

                    // logical OR, AND, CONCEPT-CONTAINS-REL, or
                    // CONCEPT-CONTAINS-DESC.
                    I_ExtendByRefPartCidCid part = (I_ExtendByRefPartCidCid) thinPart;

                    I_GetConceptData truthToken = termFactory.getConcept(part.getC1id());
                    I_GetConceptData groupingToken = termFactory.getConcept(part.getC2id());

                    boolean negate = getNegation(truthToken, termFactory);

                    RefsetComputeType statementType = RefsetComputeType.getTypeFromGrouping(groupingToken);
                    RefsetComputeType newComputeType;
                    switch (statementType) {
                    case CONCEPT:
                        newComputeType = computeType;
                        break; // don't change compute type since it might be a concept-contains-desc followed by OR
                    case DESCRIPTION:
                        newComputeType = RefsetComputeType.DESCRIPTION;
                        break;
                    case RELATIONSHIP:
                        newComputeType = RefsetComputeType.RELATIONSHIP;
                        break;
                    default:
                        newComputeType = computeType;
                        break;
                    }

                    // add subquery
                    RefsetSpecQuery subquery = query.addSubquery(groupingToken);

                    // process each grandchild
                    if (!childNode.isLeaf()) {
                        processNode(childNode, subquery, newComputeType, configFrame, termFactory, refsetSpecNid);
                    }
                    if (negate) {
                        subquery.negateQuery();
                    }
                } else if (thinPart instanceof I_ExtendByRefPartStr) {
                    // ignore - comments refset
                } else if (thinPart instanceof I_ExtendByRefPartCid) {
                    // ignore - Not part of spec...
                } else {
                    throw new TerminologyException("Unknown extension type : " + thinPart.getClass());
                }
            }
        }

        return query;
    }

    /**
     * Determines whether or not the associated statement or query needs to be
     * negated. If negation is required, the concept passed in will be
     * "boolean circle icon false".
     * 
     * @param c1
     *            The concept
     * @param termFactory
     * @return
     * @throws TerminologyException
     * @throws IOException
     */
    private static boolean getNegation(I_GetConceptData c1, I_TermFactory termFactory) throws TerminologyException,
            IOException {
        if (c1.equals(termFactory.getConcept(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.getUids()))) {
            return false;
        } else if (c1.equals(termFactory.getConcept(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_FALSE.getUids()))) {
            return true;
        } else {
            throw new TerminologyException("Unable to recognise truth type: " + c1.getInitialText());
        }
    }

    /**
     * Recursively adds extensions to a map - this is used to create a tree
     * structure representing the refset spec, prior to being converted to a
     * query object.
     * 
     * @param list
     * @param extensionMap
     * @param fetchedComponents
     * @throws IOException
     */
    public static void addExtensionsToMap(Collection<? extends I_ExtendByRef> list,
            HashMap<Integer, DefaultMutableTreeNode> extensionMap, HashSet<Integer> fetchedComponents, int refsetNid)
            throws IOException {
        for (I_ExtendByRef ext : list) {
            if (ext.getRefsetId() == refsetNid) {
            extensionMap.put(ext.getMemberId(), new DefaultMutableTreeNode(ext));
            if (fetchedComponents.contains(ext.getMemberId()) == false) {
                fetchedComponents.add(ext.getMemberId());
                    addExtensionsToMap(Terms.get()
                        .getAllExtensionsForComponent(ext.getMemberId(), true), extensionMap, fetchedComponents, refsetNid);
            }
        }
    }
}
}
