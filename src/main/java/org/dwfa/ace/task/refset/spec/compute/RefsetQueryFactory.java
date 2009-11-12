package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.lucene.queryParser.ParseException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class RefsetQueryFactory {

    private final static int REL = 1;
    private final static int DESC = 2;
    private final static int CONCEPT = 3;

    public static RefsetSpecQuery createQuery(I_ConfigAceFrame configFrame, I_TermFactory termFactory,
            I_GetConceptData refsetSpec, I_GetConceptData refset) throws IOException, TerminologyException,
            ParseException {

        // create tree object that corresponds to the database's refset spec
        List<I_ThinExtByRefVersioned> extensions =
                LocalVersionedTerminology.get().getAllExtensionsForComponent(refsetSpec.getConceptId(), true);
        HashMap<Integer, DefaultMutableTreeNode> extensionMap = new HashMap<Integer, DefaultMutableTreeNode>();
        HashSet<Integer> fetchedComponents = new HashSet<Integer>();
        fetchedComponents.add(refsetSpec.getConceptId());
        addExtensionsToMap(extensions, extensionMap, fetchedComponents);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(refsetSpec);
        for (DefaultMutableTreeNode extNode : extensionMap.values()) {
            I_ThinExtByRefVersioned ext = (I_ThinExtByRefVersioned) extNode.getUserObject();
            if (ext.getComponentId() == refsetSpec.getConceptId()) {
                root.add(extNode);
            } else {
                extensionMap.get(ext.getComponentId()).add(extNode);
            }
        }

        // create refset spec query
        I_GetConceptData orConcept = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_OR_GROUPING.getUids());

        RefsetSpecQuery query = new RefsetSpecQuery(orConcept);
        query = processNode(root, query, CONCEPT, configFrame, termFactory);
        return query;

    }

    /**
     * Create a "possible" query that contains all historical clauses to iterate over, even retired clauses.
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
    public static RefsetSpecQuery createPossibleQuery(I_ConfigAceFrame configFrame, I_TermFactory termFactory,
            I_GetConceptData refsetSpec, I_GetConceptData refset) throws IOException, TerminologyException,
            ParseException {

        // create tree object that corresponds to the database's refset spec
        List<I_ThinExtByRefVersioned> extensions =
                LocalVersionedTerminology.get().getAllExtensionsForComponent(refsetSpec.getConceptId(), true);
        HashMap<Integer, DefaultMutableTreeNode> extensionMap = new HashMap<Integer, DefaultMutableTreeNode>();
        HashSet<Integer> fetchedComponents = new HashSet<Integer>();
        fetchedComponents.add(refsetSpec.getConceptId());
        addExtensionsToMap(extensions, extensionMap, fetchedComponents);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(refsetSpec);
        for (DefaultMutableTreeNode extNode : extensionMap.values()) {
            I_ThinExtByRefVersioned ext = (I_ThinExtByRefVersioned) extNode.getUserObject();
            if (ext.getComponentId() == refsetSpec.getConceptId()) {
                root.add(extNode);
            } else {
                extensionMap.get(ext.getComponentId()).add(extNode);
            }
        }

        // create refset spec query
        I_GetConceptData orConcept = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_OR_GROUPING.getUids());

        RefsetSpecQuery query = new RefsetSpecQuery(orConcept);
        query = processNode(root, query, CONCEPT, configFrame, termFactory);
        return query;

    }

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
    private static RefsetSpecQuery processNode(DefaultMutableTreeNode node, RefsetSpecQuery query, int type,
            I_ConfigAceFrame configFrame, I_TermFactory termFactory) throws IOException, TerminologyException,
            ParseException {

        if (query == null) {
            throw new TerminologyException("Invalid refset spec : null query item used.");
        }

        int childCount = node.getChildCount();

        for (int i = 0; i < childCount; i++) {

            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);

            // determine type of current child
            I_ThinExtByRefVersioned currExt = (I_ThinExtByRefVersioned) childNode.getUserObject();

            boolean addUncommitted = true;
            boolean returnConflictResolvedLatestState = true;
            List<I_ThinExtByRefTuple> extensions =
                    currExt.getTuples(configFrame.getAllowedStatus(), configFrame.getViewPositionSet(), addUncommitted,
                        returnConflictResolvedLatestState);
            if (extensions.size() > 0) {
                I_ThinExtByRefPart thinPart = extensions.get(0).getPart();

                if (thinPart instanceof I_ThinExtByRefPartConceptConceptConcept) {

                    // structural query e.g. true : is-concept : height
                    I_ThinExtByRefPartConceptConceptConcept part = (I_ThinExtByRefPartConceptConceptConcept) thinPart;

                    I_GetConceptData truthToken = termFactory.getConcept(part.getC1id());
                    I_GetConceptData groupingToken = termFactory.getConcept(part.getC2id());
                    I_GetConceptData constraint = termFactory.getConcept(part.getC3id());

                    switch (type) {
                    case (CONCEPT):
                        query.addConceptStatement(getNegation(truthToken, termFactory), groupingToken, constraint);
                        break;
                    case (DESC):
                        query.addDescStatement(getNegation(truthToken, termFactory), groupingToken, constraint);
                        break;
                    case (REL):
                        query.addRelStatement(getNegation(truthToken, termFactory), groupingToken, constraint);
                        break;
                    default:
                        throw new TerminologyException("Unknown type: " + groupingToken.getInitialText());
                    }
                } else if (thinPart instanceof I_ThinExtByRefPartConceptConcept) {

                    // logical OR, AND, CONCEPT-CONTAINS-REL, or
                    // CONCEPT-CONTAINS-DESC.
                    I_ThinExtByRefPartConceptConcept part = (I_ThinExtByRefPartConceptConcept) thinPart;

                    I_GetConceptData truthToken = termFactory.getConcept(part.getC1id());
                    I_GetConceptData groupingToken = termFactory.getConcept(part.getC2id());

                    boolean negate = getNegation(truthToken, termFactory);

                    // add subquery
                    RefsetSpecQuery subquery = query.addSubquery(groupingToken);

                    int subtype = getType(groupingToken, termFactory);
                    if (subtype == -1) {
                        subtype = type;
                    }

                    // process each grandchild
                    if (!childNode.isLeaf()) {
                        processNode(childNode, subquery, subtype, configFrame, termFactory);
                    }
                    if (negate) {
                        subquery.negateQuery();
                    }

                } else if (thinPart instanceof I_ThinExtByRefPartString) {
                    // ignore - comments refset
                } else if (thinPart instanceof I_ThinExtByRefPartConcept) {
                    // ignore - Not part of spec...
                } else {
                    throw new TerminologyException("Unknown extension type : " + thinPart.getClass());
                }
            }
        }
        return query;
    }

    /**
     * Processes a node in our refset spec tree structure. For each child of the
     * node, we recursively determine the refset type (corresponds to a
     * sub-query or statement). This includes checking any grandchildren,
     * great-grandchildren etc.
     * 
     * Same as processNode except it does not do conflict resolution, and provides a
     * null position set to return all tuples...
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
    private static RefsetSpecQuery processPossibleNode(DefaultMutableTreeNode node, RefsetSpecQuery query, int type,
            I_ConfigAceFrame configFrame, I_TermFactory termFactory) throws IOException, TerminologyException,
            ParseException {

        if (query == null) {
            throw new TerminologyException("Invalid refset spec : null query item used.");
        }

        int childCount = node.getChildCount();

        for (int i = 0; i < childCount; i++) {

            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);

            // determine type of current child
            I_ThinExtByRefVersioned currExt = (I_ThinExtByRefVersioned) childNode.getUserObject();

            boolean addUncommitted = true;
            boolean returnConflictResolvedLatestState = false;
            List<I_ThinExtByRefTuple> extensions =
                    currExt.getTuples(configFrame.getAllowedStatus(), null, addUncommitted,
                        returnConflictResolvedLatestState);
            if (extensions.size() > 0) {
                System.out.println("************************Extensions size: " + extensions.size());
                System.out.println("Extensions : " + extensions);
                I_ThinExtByRefPart thinPart = extensions.get(0).getPart();

                if (thinPart instanceof I_ThinExtByRefPartConceptConceptConcept) {

                    // structural query e.g. true : is-concept : height
                    I_ThinExtByRefPartConceptConceptConcept part = (I_ThinExtByRefPartConceptConceptConcept) thinPart;

                    I_GetConceptData truthToken = termFactory.getConcept(part.getC1id());
                    I_GetConceptData groupingToken = termFactory.getConcept(part.getC2id());
                    I_GetConceptData constraint = termFactory.getConcept(part.getC3id());

                    switch (type) {
                    case (CONCEPT):
                        query.addConceptStatement(getNegation(truthToken, termFactory), groupingToken, constraint);
                        break;
                    case (DESC):
                        query.addDescStatement(getNegation(truthToken, termFactory), groupingToken, constraint);
                        break;
                    case (REL):
                        query.addRelStatement(getNegation(truthToken, termFactory), groupingToken, constraint);
                        break;
                    default:
                        throw new TerminologyException("Unknown type: " + groupingToken.getInitialText());
                    }
                } else if (thinPart instanceof I_ThinExtByRefPartConceptConcept) {

                    // logical OR, AND, CONCEPT-CONTAINS-REL, or
                    // CONCEPT-CONTAINS-DESC.
                    I_ThinExtByRefPartConceptConcept part = (I_ThinExtByRefPartConceptConcept) thinPart;

                    I_GetConceptData truthToken = termFactory.getConcept(part.getC1id());
                    I_GetConceptData groupingToken = termFactory.getConcept(part.getC2id());

                    boolean negate = getNegation(truthToken, termFactory);

                    // add subquery
                    RefsetSpecQuery subquery = query.addSubquery(groupingToken);

                    int subtype = getType(groupingToken, termFactory);
                    if (subtype == -1) {
                        subtype = type;
                    }

                    // process each grandchild
                    if (!childNode.isLeaf()) {
                        processPossibleNode(childNode, subquery, subtype, configFrame, termFactory);
                    }
                    if (negate) {
                        subquery.negateQuery();
                    }

                } else if (thinPart instanceof I_ThinExtByRefPartString) {
                    // ignore - comments refset
                } else if (thinPart instanceof I_ThinExtByRefPartConcept) {
                    // ignore - Not part of spec...
                } else {
                    throw new TerminologyException("Unknown extension type : " + thinPart.getClass());
                }
            }
        }
        return query;
    }

    private static int getType(I_GetConceptData groupingToken, I_TermFactory termFactory) throws TerminologyException,
            IOException {
        if (termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING.getUids()).equals(
            groupingToken)) {
            return REL;
        } else if (termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING.getUids()).equals(
            groupingToken)) {
            return DESC;
        } else {
            return -1;
        }
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
     * @param extensions
     * @param extensionMap
     * @param fetchedComponents
     * @throws IOException
     */
    public static void addExtensionsToMap(List<I_ThinExtByRefVersioned> extensions,
            HashMap<Integer, DefaultMutableTreeNode> extensionMap, HashSet<Integer> fetchedComponents)
            throws IOException {
        for (I_ThinExtByRefVersioned ext : extensions) {
            extensionMap.put(ext.getMemberId(), new DefaultMutableTreeNode(ext));
            if (fetchedComponents.contains(ext.getMemberId()) == false) {
                fetchedComponents.add(ext.getMemberId());
                addExtensionsToMap(LocalVersionedTerminology.get()
                    .getAllExtensionsForComponent(ext.getMemberId(), true), extensionMap, fetchedComponents);
            }
        }
    }
}
