package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;

public interface I_GetConceptData extends I_AmTermComponent {

    public I_ConceptAttributeVersioned getConceptAttributes() throws IOException;

    public int getConceptId();

    public List<I_ConceptAttributeTuple> getConceptAttributeTuples(I_IntSet allowedStatus, Set<I_Position> positions)
            throws IOException;

    public List<I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
        Set<I_Position> positions) throws IOException;

    public List<I_ImageTuple> getImageTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions)
            throws IOException;

    /**
     * If you want to get all the is-a relationships for this concept, pass in
     * only is-a relationship types in the allowed types field.
     * 
     * @param allowedStatus
     *            set of allowed status values that the I_RelTuple must conform
     *            with
     * @param allowedTypes
     *            set of allowed relationship types that the I_RelTuple must
     *            conform with
     * @param positions
     *            set of positions used to determine the state of status, and
     *            types for query
     * @param addUncommitted
     *            include uncommitted relationships in the return values
     * @return a list of rel tuples that meet the provided criterion.
     * @throws IOException
     */
    public List<I_RelTuple> getSourceRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
        Set<I_Position> positions, boolean addUncommitted) throws IOException;

    public List<I_RelTuple> getDestRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
        boolean addUncommitted) throws IOException;

    /**
     * If you want to get all the is-a immediate parents for this concept, pass in
     * only is-a relationship types in the allowed types field.
     * @param allowedStatus
     *            set of allowed status values that the I_RelTuple must conform
     *            with
     * @param allowedTypes
     *            set of allowed relationship types that the I_RelTuple must
     *            conform with
     * @param positions
     *            set of positions used to determine the state of status, and
     *            types for query
     * @param addUncommitted
     *            include uncommitted relationships in the return values
     * @return a list of I_GetConceptData that meet the provided criterion.
     * @throws IOException
     */
    public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedStatus, I_IntSet allowedTypes,
        Set<I_Position> positions, boolean addUncommitted) throws IOException;

    public Set<I_GetConceptData> getDestRelOrigins(I_IntSet allowedStatus, I_IntSet allowedTypes,
        Set<I_Position> positions, boolean addUncommitted) throws IOException;

    public List<I_DescriptionVersioned> getDescriptions() throws IOException;

    public List<I_RelVersioned> getDestRels() throws IOException;

    /**
     * 
     * @return all relationships originating at this concept, regardless of type
     *         of status.
     * @throws IOException
     */
    public List<I_RelVersioned> getSourceRels() throws IOException;

    public String getInitialText() throws IOException;

    public boolean isLeaf(I_ConfigAceFrame aceConfig, boolean addUncommitted) throws IOException;

    public List<I_ImageVersioned> getImages() throws IOException;

    public List<UUID> getUids() throws IOException;

    public List<I_DescriptionVersioned> getUncommittedDescriptions();

    public List<I_RelVersioned> getUncommittedSourceRels();

    public I_ConceptAttributeVersioned getUncommittedConceptAttributes();

    public List<I_ImageVersioned> getUncommittedImages();

    public I_IdVersioned getId() throws IOException;

    public I_DescriptionTuple getDescTuple(I_IntList treeDescPreferenceList, I_IntSet allowedStatus,
        Set<I_Position> positions) throws IOException;

    public I_DescriptionTuple getDescTuple(I_IntList treeDescPreferenceList, I_ConfigAceFrame config)
            throws IOException;

    public I_IntSet getUncommittedIds();

    public UniversalAceBean getUniversalAceBean() throws IOException, TerminologyException;

    public boolean isParentOf(I_GetConceptData child, I_IntSet allowedStatus, I_IntSet allowedTypes,
        Set<I_Position> positions, boolean addUncommitted) throws IOException;

    public boolean isParentOfOrEqualTo(I_GetConceptData child, I_IntSet allowedStatus, I_IntSet allowedTypes,
        Set<I_Position> positions, boolean addUncommitted) throws IOException;
}