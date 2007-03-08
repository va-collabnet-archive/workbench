package org.dwfa.ace.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.IntList;
import org.dwfa.ace.IntSet;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.ThinConTuple;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescTuple;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinIdVersioned;
import org.dwfa.vodb.types.ThinImageVersioned;
import org.dwfa.vodb.types.ThinRelTuple;
import org.dwfa.vodb.types.ThinRelVersioned;

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

	public ThinConVersioned getConcept() throws DatabaseException {
		return bean.getConcept();
	}

	public int getConceptId() {
		return bean.getConceptId();
	}

	public List<ThinConTuple> getConceptTuples(IntSet allowedStatus, Set<Position> positions) {
		return bean.getConceptTuples(allowedStatus, positions);
	}

	public List<ThinDescVersioned> getDescriptions() throws DatabaseException {
		return bean.getDescriptions();
	}

	public List<ThinDescTuple> getDescriptionTuples(IntSet allowedStatus, IntSet allowedTypes, Set<Position> positions) throws DatabaseException {
		return bean.getDescriptionTuples(allowedStatus, allowedTypes, positions);
	}

	public List<ThinRelVersioned> getDestRels() throws DatabaseException {
		if (parentDepth > 0) {
			return new ArrayList<ThinRelVersioned>();
		}
		return bean.getDestRels();
	}

	public List<ThinRelTuple> getDestRelTuples(IntSet allowedStatus, 
			IntSet allowedTypes, Set<Position> positions, boolean addUncommitted) throws DatabaseException {
		if (parentDepth > 0) {
			return new ArrayList<ThinRelTuple>();
		}
		return bean.getDestRelTuples(allowedStatus, allowedTypes, positions, addUncommitted);
	}

	public List<ThinImageVersioned> getImages() throws DatabaseException {
		return bean.getImages();
	}

	public String getInitialText() throws DatabaseException {
		return bean.getInitialText();
	}

	public List<ThinRelVersioned> getSourceRels() throws DatabaseException {
		return bean.getSourceRels();
	}

	public List<ThinRelTuple> getSourceRelTuples(IntSet allowedStatus, 
			IntSet allowedTypes, Set<Position> positions, boolean addUncommitted) throws DatabaseException {
		return bean.getSourceRelTuples(allowedStatus, allowedTypes, positions, addUncommitted);
	}

	public List<UUID> getUids() throws DatabaseException {
		return bean.getUids();
	}

	public List<ThinImageVersioned> getUncommittedImages() {
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

	public ThinConVersioned getUncommittedConcept() {
		return bean.getUncommittedConcept();
	}

	public List<ThinDescVersioned> getUncommittedDescriptions() {
		return bean.getUncommittedDescriptions();
	}

	public List<ThinRelVersioned> getUncommittedSourceRels() {
		return bean.getUncommittedSourceRels();
	}

	public ThinIdVersioned getId() throws DatabaseException {
		return bean.getId();
	}

	public ThinDescTuple getDescTuple(AceFrameConfig config) throws DatabaseException {
		return bean.getDescTuple(config.getTreeDescPreferenceList(), config);
	}

	public ThinDescTuple getDescTuple(IntList prefOrder, AceFrameConfig config) throws DatabaseException {
		return bean.getDescTuple(prefOrder, config);
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
