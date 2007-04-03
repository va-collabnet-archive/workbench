package org.dwfa.ace.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.IntList;
import org.dwfa.ace.IntSet;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Position;

import com.sleepycat.je.DatabaseException;

public class ConceptBeanForTree implements I_GetConceptDataForTree {
	ConceptBean bean;
	int parentDepth;
	List<DefaultMutableTreeNode> extraParentNodes  = new ArrayList<DefaultMutableTreeNode>();
	private boolean parentOpened;
	private boolean secondaryParentNode;
	
	public static ConceptBeanForTree get(int conceptId, int parentDepth, 
			boolean secondaryParentNode) {
		ConceptBean bean = ConceptBean.get(conceptId);
		return new ConceptBeanForTree(bean, parentDepth, secondaryParentNode);
	}

	private ConceptBeanForTree(ConceptBean bean, int parentDepth,
			boolean secondaryParentNode) {
		super();
		this.bean = bean;
		this.parentDepth = parentDepth;
		this.secondaryParentNode = secondaryParentNode;
	}

	public I_ConceptAttributeVersioned getConceptAttributes() throws DatabaseException {
		return bean.getConceptAttributes();
	}

	public int getConceptId() {
		return bean.getConceptId();
	}

	public List<I_ConceptAttributeTuple> getConceptTuples(IntSet allowedStatus, Set<Position> positions) throws DatabaseException {
		return bean.getConceptTuples(allowedStatus, positions);
	}

	public List<I_DescriptionVersioned> getDescriptions() throws DatabaseException {
		return bean.getDescriptions();
	}

	public List<I_DescriptionTuple> getDescriptionTuples(IntSet allowedStatus, IntSet allowedTypes, Set<Position> positions) throws DatabaseException {
		return bean.getDescriptionTuples(allowedStatus, allowedTypes, positions);
	}

	public List<I_RelVersioned> getDestRels() throws DatabaseException {
		if (parentDepth > 0) {
			return new ArrayList<I_RelVersioned>();
		}
		return bean.getDestRels();
	}

	public List<I_RelTuple> getDestRelTuples(IntSet allowedStatus, 
			IntSet allowedTypes, Set<Position> positions, boolean addUncommitted) throws DatabaseException {
		if (parentDepth > 0) {
			return new ArrayList<I_RelTuple>();
		}
		return bean.getDestRelTuples(allowedStatus, allowedTypes, positions, addUncommitted);
	}

	public List<I_ImageVersioned> getImages() throws DatabaseException {
		return bean.getImages();
	}

	public String getInitialText() throws DatabaseException {
		return bean.getInitialText();
	}

	public List<I_RelVersioned> getSourceRels() throws DatabaseException {
		return bean.getSourceRels();
	}

	public List<I_RelTuple> getSourceRelTuples(IntSet allowedStatus, 
			IntSet allowedTypes, Set<Position> positions, boolean addUncommitted) throws DatabaseException {
		return bean.getSourceRelTuples(allowedStatus, allowedTypes, positions, addUncommitted);
	}

	public List<UUID> getUids() throws DatabaseException {
		return bean.getUids();
	}

	public List<I_ImageVersioned> getUncommittedImages() {
		return bean.getUncommittedImages();
	}

	public boolean isLeaf(AceFrameConfig aceConfig, boolean addUncommitted) throws DatabaseException {
		if (parentDepth > 0) {
			return true;
		}
		return bean.isLeaf(aceConfig, addUncommitted);
	}

	public String toString() {
		return bean.toString();
	}

	public int getParentDepth() {
		return parentDepth;
	}

	public boolean isParentOpened() {
		return parentOpened;
	}

	public boolean isSecondaryParentNode() {
		return secondaryParentNode;
	}

	public void setParentOpened(boolean opened) {
		this.parentOpened = opened;
		
	}

	public ConceptBean getCoreBean() {
		return bean;
	}

	public List<DefaultMutableTreeNode> getExtraParentNodes() {
		return extraParentNodes;
	}

	public I_ConceptAttributeVersioned getUncommittedConceptAttributes() {
		return bean.getUncommittedConceptAttributes();
	}

	public List<I_DescriptionVersioned> getUncommittedDescriptions() {
		return bean.getUncommittedDescriptions();
	}

	public List<I_RelVersioned> getUncommittedSourceRels() {
		return bean.getUncommittedSourceRels();
	}

	public I_IdVersioned getId() throws DatabaseException {
		return bean.getId();
	}

	public I_DescriptionTuple getDescTuple(AceFrameConfig config) throws DatabaseException {
		return bean.getDescTuple(config.getTreeDescPreferenceList(), config);
	}

	public I_DescriptionTuple getDescTuple(IntList prefOrder, AceFrameConfig config) throws DatabaseException {
		return bean.getDescTuple(prefOrder, config);
	}

	public IntSet getUncommittedIds() {
		return bean.getUncommittedIds();
	}
	
	/*

	@Override
	public boolean equals(Object another) {
		return bean.equals(another);
	}

	@Override
	public int hashCode() {
		return bean.hashCode();
	}
	*/
	
}
