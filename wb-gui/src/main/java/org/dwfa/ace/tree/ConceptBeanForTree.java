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
import java.util.Collection;
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
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TestComponent;
import org.dwfa.ace.api.PRECEDENCE;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;

public class ConceptBeanForTree implements I_GetConceptDataForTree, Comparable<ConceptBeanForTree> {
    I_GetConceptData bean;
    public boolean promote(I_TestComponent test, I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus,
			PRECEDENCE precedence) throws IOException, TerminologyException {
		return bean.promote(test, viewPosition, pomotionPaths, allowedStatus,
				precedence);
	}

	public I_RepresentIdSet getPossibleKindOfConcepts(I_ConfigAceFrame config,
			I_ShowActivity activity) throws IOException {
		return bean.getPossibleKindOfConcepts(config, activity);
	}

	public boolean everHadSrcRelOfType(int typeNid) throws IOException {
		return bean.everHadSrcRelOfType(typeNid);
	}

	public Set<? extends I_GetConceptData> getDestRelOrigins(I_IntSet allowedTypes) throws IOException,
            TerminologyException {
        return bean.getDestRelOrigins(allowedTypes);
    }

    public List<? extends I_DescriptionTuple> getDescriptionTuples() throws IOException, TerminologyException {
        return bean.getDescriptionTuples();
    }

    public List<? extends I_ImageTuple> getImageTuples() throws IOException, TerminologyException {
        return bean.getImageTuples();
    }

    public boolean isParentOf(I_GetConceptData child, I_IntSet allowedStatus, I_IntSet allowedTypes,
            PositionSetReadOnly positions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
            throws IOException, TerminologyException {
        return bean.isParentOf(child, allowedStatus, allowedTypes, positions, precedencePolicy, contradictionManager);
    }

    public boolean isParentOf(I_GetConceptData child) throws IOException, TerminologyException {
        return bean.isParentOf(child);
    }

    public boolean isParentOfOrEqualTo(I_GetConceptData child, I_IntSet allowedStatus, I_IntSet allowedTypes,
            PositionSetReadOnly positions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
            throws IOException, TerminologyException {
        return bean.isParentOfOrEqualTo(child, allowedStatus, allowedTypes, positions, precedencePolicy,
            contradictionManager);
    }

    public boolean isParentOfOrEqualTo(I_GetConceptData child) throws IOException, TerminologyException {
        return bean.isParentOfOrEqualTo(child);
    }

    public Set<? extends I_GetConceptData> getDestRelOrigins(I_IntSet allowedTypes, PRECEDENCE precedencePolicy,
            I_ManageContradiction contradictionManager) throws IOException, TerminologyException {
        return bean.getDestRelOrigins(allowedTypes);
    }

    public List<? extends I_ConceptAttributeTuple> getConceptAttributeTuples(I_IntSet allowedStatus,
            PositionSetReadOnly positions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
            throws IOException, TerminologyException {
        return bean.getConceptAttributeTuples(allowedStatus, positions, precedencePolicy, contradictionManager);
    }

    public List<? extends I_ConceptAttributeTuple> getConceptAttributeTuples(PRECEDENCE precedencePolicy,
            I_ManageContradiction contradictionManager) throws IOException, TerminologyException {
        return bean.getConceptAttributeTuples(precedencePolicy, contradictionManager);
    }

    public List<? extends I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            PositionSetReadOnly positionSet, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
            throws IOException {
        return bean.getDescriptionTuples(allowedStatus, allowedTypes, positionSet, precedencePolicy,
            contradictionManager);
    }

    public I_DescriptionTuple getDescTuple(I_IntList typePrefOrder, I_IntList langPrefOrder, I_IntSet allowedStatus,
            PositionSetReadOnly positionSet, LANGUAGE_SORT_PREF sortPref, PRECEDENCE precedencePolicy,
            I_ManageContradiction contradictionManager) throws IOException {
        return bean.getDescTuple(typePrefOrder, langPrefOrder, allowedStatus, positionSet, sortPref, precedencePolicy,
            contradictionManager);
    }

    public Set<? extends I_GetConceptData> getDestRelOrigins(I_IntSet allowedStatus, I_IntSet allowedTypes,
            PositionSetReadOnly positions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
            throws IOException, TerminologyException {
        return bean.getDestRelOrigins(allowedStatus, allowedTypes, positions, precedencePolicy, contradictionManager);
    }

    public List<? extends I_RelTuple> getDestRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            PositionSetReadOnly positions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
            throws IOException, TerminologyException {
        return bean.getDestRelTuples(allowedStatus, allowedTypes, positions, precedencePolicy, contradictionManager);
    }

    public List<? extends I_RelTuple> getDestRelTuples(I_IntSet allowedTypes, PRECEDENCE precedencePolicy,
            I_ManageContradiction contradictionManager) throws IOException, TerminologyException {
        return bean.getDestRelTuples(allowedTypes, precedencePolicy, contradictionManager);
    }

    public List<? extends I_ImageTuple> getImageTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            PositionSetReadOnly positions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
            throws IOException, TerminologyException {
        return bean.getImageTuples(allowedStatus, allowedTypes, positions, precedencePolicy, contradictionManager);
    }

    public Set<? extends I_GetConceptData> getSourceRelTargets(I_IntSet allowedStatus, I_IntSet allowedTypes,
            PositionSetReadOnly positions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
            throws IOException, TerminologyException {
        return bean.getSourceRelTargets(allowedStatus, allowedTypes, positions, precedencePolicy, contradictionManager);
    }

    public Set<? extends I_GetConceptData> getSourceRelTargets(I_IntSet allowedTypes, PRECEDENCE precedencePolicy,
            I_ManageContradiction contradictionManager) throws IOException, TerminologyException {
        return bean.getSourceRelTargets(allowedTypes, precedencePolicy, contradictionManager);
    }

    public List<? extends I_RelTuple> getSourceRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            PositionSetReadOnly positions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager)
            throws IOException, TerminologyException {
        return bean.getSourceRelTuples(allowedStatus, allowedTypes, positions, precedencePolicy, contradictionManager);
    }

    public boolean promote(I_Position viewPosition, PathSetReadOnly pomotionPaths, I_IntSet allowedStatus,
            PRECEDENCE precedence) throws IOException, TerminologyException {
        return bean.promote(viewPosition, pomotionPaths, allowedStatus, precedence);
    }

    public I_RepresentIdSet getPossibleChildOfConcepts(I_ConfigAceFrame configFrame) throws IOException {
        return bean.getPossibleChildOfConcepts(configFrame);
    }

    int relId;
    int parentDepth;
    List<DefaultMutableTreeNode> extraParentNodes = new ArrayList<DefaultMutableTreeNode>();
    private boolean parentOpened;
    private boolean secondaryParentNode;
    private I_ConfigAceFrame config;

    public static ConceptBeanForTree get(int conceptId, int relId, int parentDepth, boolean secondaryParentNode,
            I_ConfigAceFrame config) throws TerminologyException, IOException {
        I_GetConceptData bean = Terms.get().getConcept(conceptId);
        return new ConceptBeanForTree(bean, relId, parentDepth, secondaryParentNode, config);
    }

    public ConceptBeanForTree(I_GetConceptData bean, int relId, int parentDepth, boolean secondaryParentNode,
            I_ConfigAceFrame config) {
        super();
        this.bean = bean;
        this.relId = relId;
        this.parentDepth = parentDepth;
        this.secondaryParentNode = secondaryParentNode;
        this.config = config;
    }

    public I_ConceptAttributeVersioned getConceptAttributes() throws IOException {
        return bean.getConceptAttributes();
    }

    public int getConceptId() {
        return bean.getConceptId();
    }

    public int getTermComponentId() {
        return bean.getConceptId();
    }


    public Collection<? extends I_DescriptionVersioned> getDescriptions() throws IOException {
        return bean.getDescriptions();
    }

    public Collection<? extends I_RelVersioned> getDestRels() throws IOException {
        if (parentDepth > 0) {
            return new ArrayList<I_RelVersioned>();
        }
        return bean.getDestRels();
    }


    public Collection<? extends I_ImageVersioned> getImages() throws IOException {
        return bean.getImages();
    }

    public String getInitialText() throws IOException {
        I_DescriptionTuple tuple = this.getDescTuple(ConceptBeanForTree.this.config.getShortLabelDescPreferenceList(),
            ConceptBeanForTree.this.config);
        if (tuple != null) {
            return tuple.getText();
        }

        return bean.getInitialText();
    }

    public Collection<? extends I_RelVersioned> getSourceRels() throws IOException {
        return bean.getSourceRels();
    }
 
    public List<UUID> getUids() throws IOException {
        return bean.getUids();
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

    public I_GetConceptData getCoreBean() {
        return bean;
    }

    public List<DefaultMutableTreeNode> getExtraParentNodes() {
        return extraParentNodes;
    }

    public I_Identify getIdentifier() throws IOException {
        return bean.getIdentifier();
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

 
    public int getRelId() {
        return relId;
    }

    public List<I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            PositionSetReadOnly positions, boolean returnConflictResolvedLatestState) throws IOException {
        return getDescriptionTuples(allowedStatus, allowedTypes, positions, returnConflictResolvedLatestState);
    }

    public Collection<? extends I_ExtendByRef> getExtensions() throws IOException, TerminologyException {
        return bean.getExtensions();
    }

    public Object getDenotation(int identifierScheme) throws IOException, TerminologyException {
        return bean.getDenotation(identifierScheme);
    }

    public I_RepresentIdSet getPossibleKindOfConcepts(I_ConfigAceFrame config) throws IOException {
        return bean.getPossibleKindOfConcepts(config);
    }

    public int getNid() {
        return bean.getNid();
    }

    public List<I_Identify> getUncommittedIdVersioned() {
        return bean.getUncommittedIdVersioned();
    }

    @Override
    public int compareTo(ConceptBeanForTree o) {
        return bean.getConceptId() - o.bean.getConceptId();
    }

	public I_RelVersioned getDestRel(int relNid) throws IOException {
		return bean.getDestRel(relNid);
	}

	public I_RelVersioned getSourceRel(int relNid) throws IOException {
		return bean.getSourceRel(relNid);
	}

	public String toLongString() {
		return bean.toLongString();
	}

	public boolean isCanceled() throws IOException {
		return bean.isCanceled();
	}

	public Set<? extends I_ConceptAttributeTuple> getCommonConceptAttributeTuples(
			I_ConfigAceFrame config) throws IOException, TerminologyException {
		return bean.getCommonConceptAttributeTuples(config);
	}

	public Set<? extends I_DescriptionTuple> getCommonDescTuples(
			I_ConfigAceFrame config) throws IOException {
		return bean.getCommonDescTuples(config);
	}

	public Set<? extends I_RelTuple> getCommonRelTuples(I_ConfigAceFrame config) throws IOException, TerminologyException {
		return bean.getCommonRelTuples(config);
	}

}
