package org.dwfa.vodb.types;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.IntList;
import org.dwfa.ace.IntSet;
import org.dwfa.ace.config.AceFrameConfig;

import com.sleepycat.je.DatabaseException;

public interface I_GetConceptData {

	public ThinConVersioned getConcept() throws DatabaseException;

	public int getConceptId();

	public List<ThinConTuple> getConceptTuples(IntSet allowedStatus,
			Set<Position> positions);

	public List<ThinDescTuple> getDescriptionTuples(IntSet allowedStatus,
			IntSet allowedTypes, Set<Position> positions) throws DatabaseException;

	public List<ThinRelTuple> getSourceRelTuples(IntSet allowedStatus,
			IntSet allowedTypes, Set<Position> positions, boolean addUncommitted)
			throws DatabaseException;

	public List<ThinRelTuple> getDestRelTuples(IntSet allowedStatus,
			IntSet allowedTypes, Set<Position> positions, boolean addUncommitted)
			throws DatabaseException;

	public List<ThinDescVersioned> getDescriptions() throws DatabaseException;

	public List<ThinRelVersioned> getDestRels() throws DatabaseException;

	public List<ThinRelVersioned> getSourceRels() throws DatabaseException;

	public String getInitialText() throws DatabaseException;

	public boolean isLeaf(AceFrameConfig aceConfig, boolean addUncommitted) throws DatabaseException;

	public List<ThinImageVersioned> getImages() throws DatabaseException;

	public List<UUID> getUids() throws DatabaseException;

	public List<ThinDescVersioned> getUncommittedDescriptions();
	
	public List<ThinRelVersioned> getUncommittedSourceRels();
	
	public ThinConVersioned getUncommittedConcept();
	
	public List<ThinImageVersioned> getUncommittedImages();

	public ThinIdVersioned getId() throws DatabaseException;

	public ThinDescTuple getDescTuple(IntList treeDescPreferenceList, AceFrameConfig config) throws DatabaseException;

}