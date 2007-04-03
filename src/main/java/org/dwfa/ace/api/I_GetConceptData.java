package org.dwfa.ace.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.IntList;
import org.dwfa.ace.IntSet;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.vodb.types.Position;

import com.sleepycat.je.DatabaseException;

public interface I_GetConceptData extends I_AmTermComponent {

	public I_ConceptAttributeVersioned getConceptAttributes() throws DatabaseException;

	public int getConceptId();

	public List<I_ConceptAttributeTuple> getConceptTuples(IntSet allowedStatus,
			Set<Position> positions) throws DatabaseException;

	public List<I_DescriptionTuple> getDescriptionTuples(IntSet allowedStatus,
			IntSet allowedTypes, Set<Position> positions) throws DatabaseException;

	public List<I_RelTuple> getSourceRelTuples(IntSet allowedStatus,
			IntSet allowedTypes, Set<Position> positions, boolean addUncommitted)
			throws DatabaseException;

	public List<I_RelTuple> getDestRelTuples(IntSet allowedStatus,
			IntSet allowedTypes, Set<Position> positions, boolean addUncommitted)
			throws DatabaseException;

	public List<I_DescriptionVersioned> getDescriptions() throws DatabaseException;

	public List<I_RelVersioned> getDestRels() throws DatabaseException;

	public List<I_RelVersioned> getSourceRels() throws DatabaseException;

	public String getInitialText() throws DatabaseException;

	public boolean isLeaf(AceFrameConfig aceConfig, boolean addUncommitted) throws DatabaseException;

	public List<I_ImageVersioned> getImages() throws DatabaseException;

	public List<UUID> getUids() throws DatabaseException;

	public List<I_DescriptionVersioned> getUncommittedDescriptions();
	
	public List<I_RelVersioned> getUncommittedSourceRels();
	
	public I_ConceptAttributeVersioned getUncommittedConceptAttributes();
	
	public List<I_ImageVersioned> getUncommittedImages();

	public I_IdVersioned getId() throws DatabaseException;

	public I_DescriptionTuple getDescTuple(IntList treeDescPreferenceList, AceFrameConfig config) throws DatabaseException;

	public IntSet getUncommittedIds();
}