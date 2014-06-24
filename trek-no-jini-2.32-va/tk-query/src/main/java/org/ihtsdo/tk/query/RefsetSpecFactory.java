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
package org.ihtsdo.tk.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_string.RefexNidNidStringVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.query.RefsetComputer.ComputeType;
import org.ihtsdo.tk.query.RefsetSpecQuery.GROUPING_TYPE;
import org.ihtsdo.tk.query.RefsetSpecStatement.QUERY_TOKENS;

public class RefsetSpecFactory {
    private static TerminologyStoreDI ts = Ts.get();
    private static ViewCoordinate vc;
    public static RefsetSpecQuery createQuery(ViewCoordinate viewCoordinate,
            ConceptChronicleBI refsetSpec, ConceptChronicleBI refset, ComputeType refsetType) throws Exception {
        vc = viewCoordinate;
        // create tree object that corresponds to the database's refset spec
        
        Collection<? extends RefexChronicleBI> extensions =
                refsetSpec.getRefsetMembersActive(vc);
        HashMap<Integer, DefaultMutableTreeNode> extensionMap = new HashMap<Integer, DefaultMutableTreeNode>();
        HashSet<Integer> fetchedComponents = new HashSet<Integer>();
        fetchedComponents.add(refsetSpec.getConceptNid());

        addExtensionsToMap(extensions, extensionMap, fetchedComponents,
                refsetSpec.getConceptNid());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(refsetSpec);
        for (DefaultMutableTreeNode extNode : extensionMap.values()) {
            RefexVersionBI r = (RefexVersionBI) extNode.getUserObject();
            if (r.getReferencedComponentNid() == refsetSpec.getConceptNid()) {
                root.add(extNode);
            } else {
                extensionMap.get(r.getReferencedComponentNid()).add(extNode);
            }
        }

        // create refset spec query
        ConceptChronicleBI orConcept = ts.getConcept(RefsetAuxiliary.Concept.REFSET_OR_GROUPING.getUids());

        RefsetSpecQuery query = new RefsetSpecQuery(orConcept, true, viewCoordinate);
        query = processNode(root, query, refsetType, viewCoordinate, refsetSpec.getNid());

        return query;

    }

    public static List<String> removeDangles(RefsetSpecQuery query) {
        List<String> warningList = new ArrayList<String>();
        Iterator<RefsetSpecQuery> subQueryItr = query.getSubqueries().iterator();
        while (subQueryItr.hasNext()) {
            RefsetSpecQuery subQuery = subQueryItr.next();
            warningList.addAll(removeDangles(subQuery));
            if (subQuery.getStatements().isEmpty() && subQuery.getSubqueries().isEmpty()) {
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
     * public static RefsetSpecQuery createPossibleQuery(I_ConfigAceFrame
     * configFrame, I_TermFactory termFactory,
     * I_GetConceptData refsetSpec, I_GetConceptData refset) throws IOException,
     * TerminologyException,
     * ParseException {
     * // create tree object that corresponds to the database's refset spec
     * List<I_ExtendByRef> extensions =
     * Terms.get().getAllExtensionsForComponent(refsetSpec.getConceptNid(),
     * true);
     * HashMap<Integer, DefaultMutableTreeNode> extensionMap = new
     * HashMap<Integer, DefaultMutableTreeNode>();
     * HashSet<Integer> fetchedComponents = new HashSet<Integer>();
     * fetchedComponents.add(refsetSpec.getConceptNid());
     * addExtensionsToMap(extensions, extensionMap, fetchedComponents);
     * DefaultMutableTreeNode root = new DefaultMutableTreeNode(refsetSpec);
     * for (DefaultMutableTreeNode extNode : extensionMap.values()) {
     * I_ExtendByRef ext = (I_ExtendByRef) extNode.getUserObject();
     * if (ext.getComponentId() == refsetSpec.getConceptNid()) {
     * root.add(extNode);
     * } else {
     * extensionMap.get(ext.getComponentId()).add(extNode);
     * }
     * }
     * // create refset spec query
     * I_GetConceptData orConcept =
     * termFactory.getConcept(RefsetAuxiliary.Concept
     * .REFSET_OR_GROUPING.getUids());
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
     * @throws Exception
     */
    private static RefsetSpecQuery processNode(DefaultMutableTreeNode node, RefsetSpecQuery query,
            ComputeType computeType, ViewCoordinate vc, int refsetSpecNid)
            throws Exception {

        if (query == null) {
            throw new TerminologyException("Invalid refset spec : null query item used.");
        }

        int childCount = node.getChildCount();

        for (int i = 0; i < childCount; i++) {

            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);

            // determine type of current child
            RefexVersionBI member = (RefexVersionBI) childNode.getUserObject();

                if (member instanceof RefexNidNidNidVersionBI) {

                    // structural query e.g. true : is-concept : height
                    RefexNidNidNidVersionBI part = (RefexNidNidNidVersionBI) member;

                    ConceptChronicleBI truthToken = ts.getConcept(part.getNid1());
                    ConceptChronicleBI groupingToken = ts.getConcept(part.getNid2());
                    ConceptChronicleBI constraint = ts.getConcept(part.getNid3());

                    ComputeType statementType = getTypeFromQueryToken(groupingToken);
                    switch (statementType) {
                        case CONCEPT:
                            if (computeType.equals(ComputeType.CONCEPT)) {
                                query.addConceptStatement(getNegation(truthToken), groupingToken, constraint);
                                break;
                            } else {
                                throw new TerminologyException("Badly formed spec: '" + groupingToken.toUserString()
                                        + "' within a " + computeType.toString() + " refset compute.");
                            }
                        case DESCRIPTION:
                            if (computeType.equals(ComputeType.DESCRIPTION)) {
                                query.addDescStatement(getNegation(truthToken), groupingToken, constraint);
                                break;
                            } else {
                                throw new TerminologyException("Badly formed spec: '" + groupingToken.toUserString()
                                        + "' within a " + computeType.toString() + " refset compute.");
                            }
                        case RELATIONSHIP:
                            if (computeType.equals(ComputeType.RELATIONSHIP)) {
                                query.addRelStatement(getNegation(truthToken), groupingToken, constraint);
                                break;
                            } else {
                                throw new TerminologyException("Badly formed spec: '" + groupingToken.toUserString()
                                        + "' within a " + computeType.toString() + " refset compute.");
                            }
                        default:
                            throw new TerminologyException("Unknown type: " + groupingToken.toUserString());
                    }
                } else if (member instanceof RefexNidNidStringVersionBI) {
                    // structural query with string value
                    RefexNidNidStringVersionBI part = (RefexNidNidStringVersionBI) member;

                    ConceptChronicleBI truthToken = ts.getConcept(part.getNid1());
                    ConceptChronicleBI groupingToken = ts.getConcept(part.getNid2());
                    String constraint = part.getString1();

                    ComputeType statementType = getTypeFromQueryToken(groupingToken);
                    switch (statementType) {
                        case CONCEPT: {
                            if (part.getNid2() == RefsetAuxiliary.Concept.DIFFERENCE_V1_IS.localize().getNid()
                                    || part.getNid2() == RefsetAuxiliary.Concept.DIFFERENCE_V2_IS.localize().getNid()) {
                                String pos_str = part.getString1();
                                // System.out.println("1:" + pos_str);
                                pos_str = pos_str.substring(pos_str.lastIndexOf("(") + 1, pos_str.lastIndexOf(")"));
                                // System.out.println("2:" + pos_str);
                                String p_str = pos_str.substring(0, pos_str.indexOf(" "));
                                String v_str = pos_str.substring(pos_str.indexOf(" ") + 1);
                                // System.out.println("p:" + p_str);
                                // System.out.println("v:" + v_str);
                                PathBI path = ts.getPath(ts.getNidForUuids(UUID.fromString(p_str)));
                                if (part.getNid2() == RefsetAuxiliary.Concept.DIFFERENCE_V1_IS.localize().getNid()) {
                                    ViewCoordinate v1 = new ViewCoordinate(vc);
                                    PositionBI positionV1 = ts.newPosition(path, Long.parseLong(v_str));
                                    PositionSetBI positionSet = v1.getPositionSet();
                                    positionSet.clear();
                                    positionSet.add(positionV1);
                                    query.setV1Is(v1);
                                } else {
                                    ViewCoordinate v2 = new ViewCoordinate(vc);
                                    PositionBI positionV2 = ts.newPosition(path, Long.parseLong(v_str));
                                    PositionSetBI positionSet = v2.getPositionSet();
                                    positionSet.clear();
                                    positionSet.add(positionV2);
                                    query.setV1Is(v2);
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
                            query.addDescStatement(getNegation(truthToken), constraint, groupingToken);
                            break;
                        default:
                            throw new TerminologyException("Unknown type: " + groupingToken.toUserString());
                    }
                } else if (member instanceof RefexNidNidVersionBI) {

                    // logical OR, AND, CONCEPT-CONTAINS-REL, or
                    // CONCEPT-CONTAINS-DESC.
                    RefexNidNidVersionBI part = (RefexNidNidVersionBI) member;

                    ConceptChronicleBI truthToken = ts.getConcept(part.getNid1());
                    ConceptChronicleBI groupingToken = ts.getConcept(part.getNid2());

                    boolean negate = getNegation(truthToken);

                    ComputeType statementType = getTypeFromGrouping(groupingToken);
                    ComputeType newComputeType;
                    switch (statementType) {
                        case CONCEPT:
                            newComputeType = computeType;
                            break; // don't change compute type since it might be a concept-contains-desc followed by OR
                        case DESCRIPTION:
                            newComputeType =ComputeType.DESCRIPTION;
                            break;
                        case RELATIONSHIP:
                            newComputeType = ComputeType.RELATIONSHIP;
                            break;
                        default:
                            newComputeType = computeType;
                            break;
                    }

                    // add subquery
                    boolean truth;
                    if (truthToken.getNid() == RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.localize().getNid()) {
                        truth = true;
                    } else {
                        truth = false;
                    }
                    RefsetSpecQuery subquery = query.addSubquery(groupingToken, truth);

                    // process each grandchild
                    if (!childNode.isLeaf()) {
                        processNode(childNode, subquery, newComputeType, vc, refsetSpecNid);
                    }
                    //if (negate) {
                    //    subquery.negateQuery();
                    //}
                } else if (member instanceof RefexStringVersionBI) {
                    // ignore - comments refset
                } else if (member instanceof RefexNidVersionBI) {
                    // ignore - Not part of spec...
                } else {
                    throw new TerminologyException("Unknown extension type : " + member.getClass());
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
    private static boolean getNegation(ConceptChronicleBI c) throws TerminologyException,
            IOException {
        if (c.equals(ts.getConcept(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.getUids()))) {
            return true;
        } else if (c.equals(ts.getConcept(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_FALSE.getUids()))) {
            return false;
        } else {
            throw new TerminologyException("Unable to recognise truth type: " + c.toUserString());
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
    public static void addExtensionsToMap(Collection<? extends RefexChronicleBI> list,
            HashMap<Integer, DefaultMutableTreeNode> extensionMap,
            HashSet<Integer> fetchedComponents, int refsetNid)
            throws IOException, ContradictionException {
        for (RefexChronicleBI member : list) {
            if (member.getRefexNid() == refsetNid) {
                extensionMap.put(member.getNid(), new DefaultMutableTreeNode(member));
                if (fetchedComponents.contains(member.getNid()) == false) {
                    fetchedComponents.add(member.getNid());
                    ConceptChronicleBI refsetConcept = ts.getConcept(refsetNid);
                    addExtensionsToMap(refsetConcept.getRefsetMembersActive(vc), extensionMap,
                            fetchedComponents, refsetNid);
                }
            }
        }
    }
    
    public static ComputeType getTypeFromQueryToken(ConceptChronicleBI groupingToken) {
        QUERY_TOKENS tokenEnum = null;
        for (QUERY_TOKENS token : QUERY_TOKENS.values()) {
            if (groupingToken.getConceptNid() == token.nid) {
                tokenEnum = token;
                break;
            }
        }
        if (tokenEnum == null) {
            throw new RuntimeException("Unknown query type : " + groupingToken);
        }
        switch (tokenEnum) {
            case CONCEPT_IS:
            case CONCEPT_IS_CHILD_OF:
            case CONCEPT_IS_DESCENDENT_OF:
            case CONCEPT_IS_KIND_OF:
            case CONCEPT_IS_MEMBER_OF:
            case CONCEPT_STATUS_IS:
            case CONCEPT_STATUS_IS_CHILD_OF:
            case CONCEPT_STATUS_IS_DESCENDENT_OF:
            case CONCEPT_STATUS_IS_KIND_OF:
                return ComputeType.CONCEPT;
            case DESC_IS:
            case DESC_IS_MEMBER_OF:
            case DESC_STATUS_IS:
            case DESC_STATUS_IS_CHILD_OF:
            case DESC_STATUS_IS_KIND_OF:
            case DESC_STATUS_IS_DESCENDENT_OF:
            case DESC_TYPE_IS:
            case DESC_TYPE_IS_CHILD_OF:
            case DESC_TYPE_IS_KIND_OF:
            case DESC_TYPE_IS_DESCENDENT_OF:
            case DESC_REGEX_MATCH:
            case DESC_LUCENE_MATCH:
                return ComputeType.DESCRIPTION;
            case REL_IS_MEMBER_OF:
            case REL_IS:
            case REL_RESTRICTION_IS:
            case REL_STATUS_IS:
            case REL_STATUS_IS_KIND_OF:
            case REL_STATUS_IS_CHILD_OF:
            case REL_STATUS_IS_DESCENDENT_OF:
            case REL_TYPE_IS:
            case REL_TYPE_IS_KIND_OF:
            case REL_TYPE_IS_CHILD_OF:
            case REL_TYPE_IS_DESCENDENT_OF:
            case REL_LOGICAL_QUANTIFIER_IS:
            case REL_LOGICAL_QUANTIFIER_IS_KIND_OF:
            case REL_LOGICAL_QUANTIFIER_IS_CHILD_OF:
            case REL_LOGICAL_QUANTIFIER_IS_DESCENDENT_OF:
            case REL_CHARACTERISTIC_IS:
            case REL_CHARACTERISTIC_IS_KIND_OF:
            case REL_CHARACTERISTIC_IS_CHILD_OF:
            case REL_CHARACTERISTIC_IS_DESCENDENT_OF:
            case REL_REFINABILITY_IS:
            case REL_REFINABILITY_IS_KIND_OF:
            case REL_REFINABILITY_IS_CHILD_OF:
            case REL_REFINABILITY_IS_DESCENDENT_OF:
            case REL_DESTINATION_IS:
            case REL_DESTINATION_IS_KIND_OF:
            case REL_DESTINATION_IS_CHILD_OF:
            case REL_DESTINATION_IS_DESCENDENT_OF:
                return ComputeType.RELATIONSHIP;
            case V1_IS:
            case V2_IS:
            case ADDED_CONCEPT:
            case CHANGED_CONCEPT_STATUS:
            case CHANGED_CONCEPT_DEFINED:
                return ComputeType.CONCEPT;
            case ADDED_DESCRIPTION:
            case CHANGED_DESCRIPTION_CASE:
            case CHANGED_DESCRIPTION_LANGUAGE:
            case CHANGED_DESCRIPTION_STATUS:
            case CHANGED_DESCRIPTION_TERM:
            case CHANGED_DESCRIPTION_TYPE:
                return ComputeType.DESCRIPTION;
            case ADDED_RELATIONSHIP:
            case CHANGED_RELATIONSHIP_CHARACTERISTIC:
            case CHANGED_RELATIONSHIP_GROUP:
            case CHANGED_RELATIONSHIP_REFINABILITY:
            case CHANGED_RELATIONSHIP_STATUS:
            case CHANGED_RELATIONSHIP_TYPE:
                return ComputeType.RELATIONSHIP;
            default:
                throw new RuntimeException("Can't handle queryToken: " + groupingToken);
        }
    }
    
        public static ComputeType getTypeFromGrouping(ConceptChronicleBI groupingToken) {
        GROUPING_TYPE grouping = null;
        for (GROUPING_TYPE token : GROUPING_TYPE.values()) {
            if (groupingToken.getConceptNid() == token.getNid()) {
                grouping = token;
                break;
            }
        }

        if (grouping == null) {
            throw new RuntimeException("Unknown query type : " + groupingToken);
        }
        switch (grouping) {
            case OR:
            case AND:
                return ComputeType.CONCEPT;
            case CONCEPT_CONTAINS_REL:
            case NOT_CONCEPT_CONTAINS_REL:
                return ComputeType.RELATIONSHIP;
            case NOT_CONCEPT_CONTAINS_DESC:
            case CONCEPT_CONTAINS_DESC:
                return ComputeType.DESCRIPTION;
            case V1:
            case V2:
                return ComputeType.CONCEPT;
            default:
                throw new RuntimeException("Can't handle queryToken: " + groupingToken);
        }
    }
}
