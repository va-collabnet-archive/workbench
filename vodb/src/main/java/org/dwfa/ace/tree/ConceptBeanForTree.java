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
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;

public class ConceptBeanForTree implements I_GetConceptDataForTree, Comparable<ConceptBeanForTree> {
    ConceptBean bean;
    int relId;
    int parentDepth;
    List<DefaultMutableTreeNode> extraParentNodes = new ArrayList<DefaultMutableTreeNode>();
    private boolean parentOpened;
    private boolean secondaryParentNode;
    private I_ConfigAceFrame config;

    public static ConceptBeanForTree get(int conceptId, int relId, int parentDepth, boolean secondaryParentNode,
            I_ConfigAceFrame config) {
        ConceptBean bean = ConceptBean.get(conceptId);
        return new ConceptBeanForTree(bean, relId, parentDepth, secondaryParentNode, config);
    }

    public ConceptBeanForTree(ConceptBean bean, int relId, int parentDepth, boolean secondaryParentNode,
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
        return bean.getTermComponentId();
    }

    public List<I_ConceptAttributeTuple> getConceptAttributeTuples(I_IntSet allowedStatus, Set<I_Position> positions)
            throws IOException {
        return bean.getConceptAttributeTuples(allowedStatus, positions);
    }

    public List<I_DescriptionVersioned> getDescriptions() throws IOException {
        return bean.getDescriptions();
    }

    public List<I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions) throws IOException {
        return bean.getDescriptionTuples(allowedStatus, allowedTypes, positions);
    }

    public List<I_RelVersioned> getDestRels() throws IOException {
        if (parentDepth > 0) {
            return new ArrayList<I_RelVersioned>();
        }
        return bean.getDestRels();
    }

    public List<I_RelTuple> getDestRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
            boolean addUncommitted) throws IOException {
        if (parentDepth > 0) {
            return new ArrayList<I_RelTuple>();
        }
        return bean.getDestRelTuples(allowedStatus, allowedTypes, positions, addUncommitted);
    }

    public List<I_ImageVersioned> getImages() throws IOException {
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

    public List<I_RelVersioned> getSourceRels() throws IOException {
        return bean.getSourceRels();
    }

    public List<I_RelTuple> getSourceRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted) throws IOException {
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

    public Set<I_GetConceptData> getDestRelOrigins(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted) throws IOException {
        return bean.getDestRelOrigins(allowedStatus, allowedTypes, positions, addUncommitted);
    }

    public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted) throws IOException {
        return bean.getSourceRelTargets(allowedStatus, allowedTypes, positions, addUncommitted);
    }

    public boolean isParentOf(I_GetConceptData child, I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted) throws IOException {
        return bean.isParentOf(child, allowedStatus, allowedTypes, positions, addUncommitted);
    }

    public List<I_ImageTuple> getImageTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions)
            throws IOException {
        return bean.getImageTuples(allowedStatus, allowedTypes, positions);
    }

    public I_DescriptionTuple getDescTuple(I_IntList typePrefOrder, I_IntList langPrefOrder, I_IntSet allowedStatus,
            Set<I_Position> positionSet, LANGUAGE_SORT_PREF sortPref) throws IOException {
        return bean.getDescTuple(typePrefOrder, langPrefOrder, allowedStatus, positionSet, sortPref);
    }

    public int getRelId() {
        return relId;
    }

    public boolean isParentOfOrEqualTo(I_GetConceptData child, I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted) throws IOException {
        return bean.isParentOfOrEqualTo(child, allowedStatus, allowedTypes, positions, addUncommitted);
    }

    public List<I_ConceptAttributeTuple> getConceptAttributeTuples(I_IntSet allowedStatus, Set<I_Position> positions,
            boolean returnConflictResolvedLatestState) throws IOException {

        return bean.getConceptAttributeTuples(allowedStatus, positions, returnConflictResolvedLatestState);
    }

    public List<I_ConceptAttributeTuple> getConceptAttributeTuples(boolean returnConflictResolvedLatestState)
            throws IOException, TerminologyException {
        return bean.getConceptAttributeTuples(returnConflictResolvedLatestState);
    }

    public List<I_ConceptAttributeTuple> getConceptAttributeTuples(I_IntSet allowedStatus, Set<I_Position> positionSet,
            boolean addUncommitted, boolean returnConflictResolvedLatestState) throws IOException, TerminologyException {
        return bean.getConceptAttributeTuples(allowedStatus, positionSet, addUncommitted,
            returnConflictResolvedLatestState);
    }

    public List<I_DescriptionTuple> getDescriptionTuples(boolean returnConflictResolvedLatestState) throws IOException,
            TerminologyException {
        return bean.getDescriptionTuples(returnConflictResolvedLatestState);
    }

    public List<I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positionSet, boolean addUncommitted, boolean returnConflictResolvedLatestState)
            throws IOException, TerminologyException {
        return bean.getDescriptionTuples(allowedStatus, allowedTypes, positionSet, addUncommitted,
            returnConflictResolvedLatestState);
    }

    public List<I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean returnConflictResolvedLatestState) throws IOException {
        return getDescriptionTuples(allowedStatus, allowedTypes, positions, returnConflictResolvedLatestState);
    }

    public Set<I_GetConceptData> getDestRelOrigins(I_IntSet allowedTypes, boolean addUncommitted,
            boolean returnConflictResolvedLatestState) throws IOException, TerminologyException {
        return bean.getDestRelOrigins(allowedTypes, addUncommitted, returnConflictResolvedLatestState);
    }

    public Set<I_GetConceptData> getDestRelOrigins(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted, boolean returnConflictResolvedLatestState)
            throws IOException, TerminologyException {
        return bean.getDestRelOrigins(allowedStatus, allowedTypes, positions, addUncommitted,
            returnConflictResolvedLatestState);
    }

    public List<I_RelTuple> getDestRelTuples(I_IntSet allowedTypes, boolean addUncommitted,
            boolean returnConflictResolvedLatestState) throws IOException, TerminologyException {
        return bean.getDestRelTuples(allowedTypes, addUncommitted, returnConflictResolvedLatestState);
    }

    public List<I_RelTuple> getDestRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
            boolean addUncommitted, boolean returnConflictResolvedLatestState) throws IOException, TerminologyException {
        return bean.getDestRelTuples(allowedStatus, allowedTypes, positions, addUncommitted,
            returnConflictResolvedLatestState);
    }

    public List<I_ImageTuple> getImageTuples(boolean returnConflictResolvedLatestState) throws IOException,
            TerminologyException {
        return bean.getImageTuples(returnConflictResolvedLatestState);
    }

    public List<I_ImageTuple> getImageTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positions,
            boolean returnConflictResolvedLatestState) throws IOException {
        return bean.getImageTuples(allowedStatus, allowedTypes, positions, returnConflictResolvedLatestState);
    }

    public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedTypes, boolean addUncommitted,
            boolean returnConflictResolvedLatestState) throws IOException, TerminologyException {
        return bean.getSourceRelTargets(allowedTypes, addUncommitted, returnConflictResolvedLatestState);
    }

    public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted, boolean returnConflictResolvedLatestState)
            throws IOException, TerminologyException {
        return bean.getSourceRelTargets(allowedStatus, allowedTypes, positions, addUncommitted,
            returnConflictResolvedLatestState);
    }

    public List<I_RelTuple> getSourceRelTuples(I_IntSet allowedTypes, boolean addUncommitted,
            boolean returnConflictResolvedLatestState) throws IOException, TerminologyException {
        return bean.getSourceRelTuples(allowedTypes, addUncommitted, returnConflictResolvedLatestState);
    }

    public List<I_RelTuple> getSourceRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
            Set<I_Position> positions, boolean addUncommitted, boolean returnConflictResolvedLatestState)
            throws IOException, TerminologyException {
        return bean.getSourceRelTuples(allowedStatus, allowedTypes, positions, addUncommitted,
            returnConflictResolvedLatestState);
    }

    public boolean isParentOf(I_GetConceptData child, boolean addUncommitted) throws IOException, TerminologyException {
        return bean.isParentOf(child, addUncommitted);
    }

    public boolean isParentOfOrEqualTo(I_GetConceptData child, boolean addUncommitted) throws IOException,
            TerminologyException {
        return bean.isParentOfOrEqualTo(child, addUncommitted);
    }

    public List<I_ThinExtByRefVersioned> getExtensions() throws IOException, TerminologyException {
        return bean.getExtensions();
    }

    public Object getId(int identifierScheme) throws IOException, TerminologyException {
        return bean.getId(identifierScheme);
    }

    public I_RepresentIdSet getPossibleKindOfConcepts(I_ConfigAceFrame config) throws IOException {
        return bean.getPossibleKindOfConcepts(config);
    }

    public boolean promote(I_Position viewPosition, Set<I_Path> pomotionPaths, I_IntSet allowedStatus)
            throws IOException, TerminologyException {
        return bean.promote(viewPosition, pomotionPaths, allowedStatus);
    }

    public int getNid() {
        return bean.getNid();
    }

    public List<I_IdVersioned> getUncommittedIdVersioned() {
        return bean.getUncommittedIdVersioned();
    }

    @Override
    public int compareTo(ConceptBeanForTree o) {
        return bean.getConceptId() - o.bean.getConceptId();
    }

}
