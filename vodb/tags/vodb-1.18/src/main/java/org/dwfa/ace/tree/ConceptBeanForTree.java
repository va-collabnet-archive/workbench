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
package org.dwfa.ace.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;

public class ConceptBeanForTree implements I_GetConceptDataForTree {
	ConceptBean bean;
	int relId;
	int parentDepth;
	List<DefaultMutableTreeNode> extraParentNodes  = new ArrayList<DefaultMutableTreeNode>();
	private boolean parentOpened;
	private boolean secondaryParentNode;
	
	public static ConceptBeanForTree get(int conceptId, int relId, int parentDepth, 
			boolean secondaryParentNode) {
		ConceptBean bean = ConceptBean.get(conceptId);
		return new ConceptBeanForTree(bean, relId, parentDepth, secondaryParentNode);
	}

	public ConceptBeanForTree(ConceptBean bean, int relId, int parentDepth,
			boolean secondaryParentNode) {
		super();
		this.bean = bean;
		this.relId = relId;
		this.parentDepth = parentDepth;
		this.secondaryParentNode = secondaryParentNode;
	}

	public I_ConceptAttributeVersioned getConceptAttributes() throws IOException {
		return bean.getConceptAttributes();
	}

	public int getConceptId() {
		return bean.getConceptId();
	}

	public List<I_ConceptAttributeTuple> getConceptAttributeTuples(I_IntSet allowedStatus, Set<I_Position> positions) throws IOException {
		return bean.getConceptAttributeTuples(allowedStatus, positions);
	}

	public List<I_DescriptionVersioned> getDescriptions() throws IOException {
		return bean.getDescriptions();
	}

	public List<I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions) throws IOException {
		return bean.getDescriptionTuples(allowedStatus, allowedTypes, positions);
	}

	public List<I_RelVersioned> getDestRels() throws IOException {
		if (parentDepth > 0) {
			return new ArrayList<I_RelVersioned>();
		}
		return bean.getDestRels();
	}

	public List<I_RelTuple> getDestRelTuples(I_IntSet allowedStatus, 
			I_IntSet allowedTypes, Set<I_Position> positions, boolean addUncommitted) throws IOException {
		if (parentDepth > 0) {
			return new ArrayList<I_RelTuple>();
		}
		return bean.getDestRelTuples(allowedStatus, allowedTypes, positions, addUncommitted);
	}

	public List<I_ImageVersioned> getImages() throws IOException {
		return bean.getImages();
	}

	public String getInitialText() throws IOException {
		return bean.getInitialText();
	}

	public List<I_RelVersioned> getSourceRels() throws IOException {
		return bean.getSourceRels();
	}

	public List<I_RelTuple> getSourceRelTuples(I_IntSet allowedStatus, 
			I_IntSet allowedTypes, Set<I_Position> positions, boolean addUncommitted) throws IOException {
		return bean.getSourceRelTuples(allowedStatus, allowedTypes, positions, addUncommitted);
	}

	public List<UUID> getUids() throws IOException {
		return bean.getUids();
	}

	public List<I_ImageVersioned> getUncommittedImages() {
		return bean.getUncommittedImages();
	}

	public boolean isLeaf(I_ConfigAceFrame aceConfig, boolean addUncommitted) throws IOException {
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

	public I_IdVersioned getId() throws IOException {
		return bean.getId();
	}

	public I_DescriptionTuple getDescTuple(I_ConfigAceFrame config) throws IOException {
		return bean.getDescTuple(config.getTreeDescPreferenceList(), config);
	}

	public I_DescriptionTuple getDescTuple(I_IntList prefOrder, I_ConfigAceFrame config) throws IOException {
		return bean.getDescTuple(prefOrder, config);
	}

	public I_IntSet getUncommittedIds() {
		return bean.getUncommittedIds();
	}

	public UniversalAceBean getUniversalAceBean() throws IOException, TerminologyException {
		return bean.getUniversalAceBean();
	}

	public Set<I_GetConceptData> getDestRelOrigins(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions, boolean addUncommitted) throws IOException {
		return bean.getDestRelOrigins(allowedStatus, allowedTypes, positions, addUncommitted);
	}

	public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions, boolean addUncommitted) throws IOException {
		return bean.getSourceRelTargets(allowedStatus, allowedTypes, positions, addUncommitted);
	}

	public boolean isParentOf(I_GetConceptData child, I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions, boolean addUncommitted) throws IOException {
		return bean.isParentOf(child, allowedStatus, allowedTypes, positions, addUncommitted);
	}

   public List<I_ImageTuple> getImageTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions) throws IOException {
      return bean.getImageTuples(allowedStatus, allowedTypes, positions);
   }

   public I_DescriptionTuple getDescTuple(I_IntList arg0, I_IntSet arg1, Set<I_Position> arg2) throws IOException {
      return bean.getDescTuple(arg0, arg1, arg2);
   }

   public int getRelId() {
       return relId;
   }

public boolean isParentOfOrEqualTo(I_GetConceptData child, I_IntSet allowedStatus, I_IntSet allowedTypes,
    Set<I_Position> positions, boolean addUncommitted) throws IOException {
    return bean.isParentOfOrEqualTo(child, allowedStatus, allowedTypes, positions, addUncommitted);
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
