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

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TestComponent;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidListBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;
import org.ihtsdo.tk.contradiction.FoundContradictionVersions;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;

public class ConceptBeanForTree implements I_GetConceptDataForTree, Comparable<ConceptBeanForTree> {
   List<DefaultMutableTreeNode> extraParentNodes = new ArrayList<DefaultMutableTreeNode>();
   I_GetConceptData             bean;

    public Collection<Integer> getAllNids() throws IOException {
        return bean.getAllNids();
    }

    public ConceptChronicleBI getEnclosingConcept() {
        return bean.getEnclosingConcept();
    }
   private I_ConfigAceFrame     config;
   int                          parentDepth;
   private boolean              parentOpened;
   int                          relId;
   private boolean              secondaryParentNode;

   //~--- constructors --------------------------------------------------------

   public ConceptBeanForTree(I_GetConceptData bean, int relId, int parentDepth, boolean secondaryParentNode,
                             I_ConfigAceFrame config) {
      super();
      this.bean                = bean;
      this.relId               = relId;
      this.parentDepth         = parentDepth;
      this.secondaryParentNode = secondaryParentNode;
      this.config              = config;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException {
      return bean.addAnnotation(annotation);
   }

   @Override
   public void cancel() throws IOException {
      bean.cancel();
   }

   @Override
   public boolean commit(ChangeSetGenerationPolicy changeSetPolicy,
                         ChangeSetGenerationThreadingPolicy changeSetWriterThreading)
           throws IOException {
      return bean.commit(changeSetPolicy, changeSetWriterThreading);
   }
   
   @Override
   public boolean commit(ChangeSetGenerationPolicy changeSetPolicy,
                         ChangeSetGenerationThreadingPolicy changeSetWriterThreading,
                         boolean writeAdjudication)
           throws IOException {
      return bean.commit(changeSetPolicy, changeSetWriterThreading, writeAdjudication);
   }

   @Override
   public int compareTo(ConceptBeanForTree o) {
      return bean.getConceptNid() - o.bean.getConceptNid();
   }

   @Override
   public boolean everHadSrcRelOfType(int typeNid) throws IOException {
      return bean.everHadSrcRelOfType(typeNid);
   }

   @Override
   public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws Exception {
      return bean.makeAdjudicationAnalogs(ec, vc);
   }

   public boolean promote(I_Position viewPosition, PathSetReadOnly pomotionPaths, I_IntSet allowedStatus,
                          Precedence precedence, int authorNid)
           throws IOException, TerminologyException {
      return bean.promote(viewPosition, pomotionPaths, allowedStatus, precedence, authorNid);
   }

   public boolean promote(PositionBI viewPosition, PathSetReadOnly pomotionPaths, I_IntSet allowedStatus,
                          Precedence precedence, int authorNid)
           throws IOException, TerminologyException {
      return bean.promote(viewPosition, pomotionPaths, allowedStatus, precedence, authorNid);
   }

   @Override
   public boolean promote(PositionBI viewPosition, PathSetReadOnly pomotionPaths, NidSetBI allowedStatus,
                          Precedence precedence, int authorNid)
           throws IOException, TerminologyException {
      return bean.promote(viewPosition, pomotionPaths, allowedStatus, precedence, authorNid);
   }

   public boolean promote(I_TestComponent test, I_Position viewPosition, PathSetReadOnly pomotionPaths,
                          I_IntSet allowedStatus, Precedence precedence, int authorNid)
           throws IOException, TerminologyException {
      return bean.promote(test, viewPosition, pomotionPaths, allowedStatus, precedence, authorNid);
   }

   @Override
   public boolean promote(I_TestComponent test, I_Position viewPosition, PathSetReadOnly pomotionPaths,
                          NidSetBI allowedStatus, Precedence precedence, int authorNid)
           throws IOException, TerminologyException {
      return bean.promote(test, viewPosition, pomotionPaths, allowedStatus, precedence, authorNid);
   }

   @Override
   public String toLongString() {
      return bean.toLongString();
   }

   @Override
   public String toString() {
      return bean.toString();
   }

   @Override
   public String toUserString() {
      return bean.toUserString();
   }

   //~--- get methods ---------------------------------------------------------

   public static ConceptBeanForTree get(int conceptId, int relId, int parentDepth,
           boolean secondaryParentNode, I_ConfigAceFrame config)
           throws TerminologyException, IOException {
      I_GetConceptData bean = Terms.get().getConcept(conceptId);

      return new ConceptBeanForTree(bean, relId, parentDepth, secondaryParentNode, config);
   }

   public Collection<? extends IdBI> getAdditionalIds() throws IOException {
      return bean.getAdditionalIds();
   }

   public Collection<? extends IdBI> getAllIds() throws IOException {
      return bean.getAllIds();
   }

   @Override
   public Set<Integer> getAllStampNids() throws IOException {
      return bean.getAllStampNids();
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException {
      return bean.getAnnotations();
   }

   @Override
   public Set<? extends I_ConceptAttributeTuple> getCommonConceptAttributeTuples(I_ConfigAceFrame config)
           throws IOException, TerminologyException {
      return bean.getCommonConceptAttributeTuples(config);
   }

   @Override
   public Set<? extends I_DescriptionTuple> getCommonDescTuples(I_ConfigAceFrame config) throws IOException {
      return bean.getCommonDescTuples(config);
   }

   @Override
   public Set<? extends I_RelTuple> getCommonRelTuples(I_ConfigAceFrame config)
           throws IOException, TerminologyException {
      return bean.getCommonRelTuples(config);
   }

   @Override
   public ConceptAttributeChronicleBI getConceptAttributes() throws IOException {
      return bean.getConAttrs();
   }

   @Override
   public List<? extends I_ConceptAttributeTuple> getConceptAttributeTuples(Precedence precedencePolicy,
           ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getConceptAttributeTuples(precedencePolicy, contradictionManager);
   }

   public List<? extends I_ConceptAttributeTuple> getConceptAttributeTuples(I_IntSet allowedStatus,
           PositionSetReadOnly positions, Precedence precedencePolicy,
           ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getConceptAttributeTuples(allowedStatus, positions, precedencePolicy, contradictionManager);
   }

   @Override
   public List<? extends I_ConceptAttributeTuple> getConceptAttributeTuples(NidSetBI allowedStatus,
           PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getConceptAttributeTuples(allowedStatus, positions, precedencePolicy, contradictionManager);
   }

   @Override
   public List<? extends I_ConceptAttributeTuple> getConceptAttributeTuples(NidSetBI allowedStatus,
           PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager,
           long cuttoffTime)
           throws IOException, TerminologyException {
      return bean.getConceptAttributeTuples(allowedStatus, positions, precedencePolicy, contradictionManager,
              cuttoffTime);
   }

   @Override
   public I_ConceptAttributeVersioned getConAttrs() throws IOException {
      return bean.getConAttrs();
   }

   @Override
   public int getConceptNid() {
      return bean.getConceptNid();
   }

   @Override
   public I_GetConceptData getCoreBean() {
      return bean;
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz)
           throws IOException {
      return bean.getAnnotationsActive(xyz);
   }

   public Collection<? extends RefexVersionBI<?>> getAnnotationMembersActive(ViewCoordinate xyz, int refexNid)
           throws IOException {
      return bean.getAnnotationMembersActive(xyz, refexNid);
   }

   public Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate xyz)
           throws IOException {
      return bean.getActiveAnnotations(xyz);
   }

   public Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate xyz, int refexNid)
           throws IOException {
      return bean.getActiveAnnotations(xyz, refexNid);
   }

   public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
           throws IOException {
      return bean.getRefexMembersActive(xyz, refsetNid);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexesActive(ViewCoordinate xyz) throws IOException {
      return bean.getRefexesActive(xyz);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getActiveRefexes(ViewCoordinate xyz, int refsetNid)
           throws IOException {
      return bean.getActiveRefexes(xyz, refsetNid);
   }

   @Override
   public RefexVersionBI<?> getRefsetMemberActiveForComponent(ViewCoordinate vc, int componentNid)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefsetMembersActive(ViewCoordinate vc)
           throws IOException {
      return bean.getRefsetMembersActive(vc);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefsetMembersActive(ViewCoordinate vc, Long cutoffTime)
           throws IOException {
      return bean.getRefsetMembersActive(vc, cutoffTime);
   }

   @Override
   public Object getDenotation(int identifierScheme) throws IOException, TerminologyException {
      return bean.getDenotation(identifierScheme);
   }

   @Override
   public I_DescriptionTuple getDescTuple(I_ConfigAceFrame config) throws IOException {
      return bean.getDescTuple(config.getTreeDescPreferenceList(), config);
   }

   @Override
   public I_DescriptionTuple getDescTuple(NidListBI prefOrder, I_ConfigAceFrame config) throws IOException {
      return bean.getDescTuple(prefOrder, config);
   }

   public I_DescriptionTuple getDescTuple(NidListBI typePrefOrder, NidListBI langPrefOrder,
           I_IntSet allowedStatus, PositionSetReadOnly positionSet, LANGUAGE_SORT_PREF sortPref,
           Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
           throws IOException {
      return bean.getDescTuple(typePrefOrder, langPrefOrder, allowedStatus, positionSet, sortPref,
                               precedencePolicy, contradictionManager);
   }

   @Override
   public I_DescriptionTuple getDescTuple(NidListBI typePrefOrder, NidListBI langPrefOrder,
           NidSetBI allowedStatus, PositionSetBI positionSet, LANGUAGE_SORT_PREF sortPref,
           Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
           throws IOException {
      return bean.getDescTuple(typePrefOrder, langPrefOrder, allowedStatus, positionSet, sortPref,
                               precedencePolicy, contradictionManager);
   }

   @Override
   public List<? extends I_DescriptionTuple> getDescriptionTuples() throws IOException, TerminologyException {
      return bean.getDescriptionTuples();
   }

   public List<I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
           PositionSetReadOnly positions, boolean returnConflictResolvedLatestState)
           throws IOException {
      return getDescriptionTuples(allowedStatus, allowedTypes, positions, returnConflictResolvedLatestState);
   }

   public List<? extends I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus,
           I_IntSet allowedTypes, PositionSetReadOnly positionSet, Precedence precedencePolicy,
           ContradictionManagerBI contradictionManager)
           throws IOException {
      return bean.getDescriptionTuples(allowedStatus, allowedTypes, positionSet, precedencePolicy,
                                       contradictionManager);
   }

   @Override
   public List<? extends I_DescriptionTuple> getDescriptionTuples(NidSetBI allowedStatus,
           NidSetBI allowedTypes, PositionSetBI positionSet, Precedence precedencePolicy,
           ContradictionManagerBI contradictionManager)
           throws IOException {
      return bean.getDescriptionTuples(allowedStatus, allowedTypes, positionSet, precedencePolicy,
                                       contradictionManager);
   }

   @Override
   public List<? extends I_DescriptionTuple> getDescriptionTuples(NidSetBI allowedStatus,
           NidSetBI allowedTypes, PositionSetBI positionSet, Precedence precedencePolicy,
           ContradictionManagerBI contradictionManager, long cuttoffTime)
           throws IOException {
      return bean.getDescriptionTuples(allowedStatus, allowedTypes, positionSet, precedencePolicy,
                                       contradictionManager, cuttoffTime);
   }

   @Override
   public Collection<? extends I_DescriptionVersioned> getDescs() throws IOException {
      return bean.getDescs();
   }

   @Override
   public Collection<? extends DescriptionChronicleBI> getDescriptions() throws IOException {
      return bean.getDescs();
   }

   @Override
   public I_RelVersioned getDestRel(int relNid) throws IOException {
      return bean.getDestRel(relNid);
   }

   public Set<? extends I_GetConceptData> getDestRelOrigins(I_IntSet allowedTypes)
           throws IOException, TerminologyException {
      return bean.getDestRelOrigins(allowedTypes);
   }

   @Override
   public Set<? extends I_GetConceptData> getDestRelOrigins(NidSetBI allowedTypes)
           throws IOException, TerminologyException {
      return bean.getDestRelOrigins(allowedTypes);
   }

   public Set<? extends I_GetConceptData> getDestRelOrigins(I_IntSet allowedTypes,
           Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getDestRelOrigins(allowedTypes);
   }

   public Set<? extends I_GetConceptData> getDestRelOrigins(I_IntSet allowedStatus, I_IntSet allowedTypes,
           PositionSetReadOnly positions, Precedence precedencePolicy,
           ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getDestRelOrigins(allowedStatus, allowedTypes, positions, precedencePolicy,
                                    contradictionManager);
   }

   @Override
   public Set<? extends I_GetConceptData> getDestRelOrigins(NidSetBI allowedStatus, NidSetBI allowedTypes,
           PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getDestRelOrigins(allowedStatus, allowedTypes, positions, precedencePolicy,
                                    contradictionManager);
   }

   public List<? extends I_RelTuple> getDestRelTuples(I_IntSet allowedTypes, Precedence precedencePolicy,
           ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getDestRelTuples(allowedTypes, precedencePolicy, contradictionManager);
   }

   @Override
   public List<? extends I_RelTuple> getDestRelTuples(NidSetBI allowedTypes, Precedence precedencePolicy,
           ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getDestRelTuples(allowedTypes, precedencePolicy, contradictionManager);
   }

   public List<? extends I_RelTuple> getDestRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
           PositionSetReadOnly positions, Precedence precedencePolicy,
           ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getDestRelTuples(allowedStatus, allowedTypes, positions, precedencePolicy,
                                   contradictionManager);
   }

   @Override
   public List<? extends I_RelTuple> getDestRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
           PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getDestRelTuples(allowedStatus, allowedTypes, positions, precedencePolicy,
                                   contradictionManager);
   }

   @Override
   public List<? extends I_RelTuple> getDestRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
           PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager,
           int classifierNid, RelAssertionType relAssertionType)
           throws IOException, TerminologyException {
      return bean.getDestRelTuples(allowedStatus, allowedTypes, positions, precedencePolicy,
                                   contradictionManager, classifierNid, relAssertionType);
   }

   @Override
   public Collection<? extends I_RelVersioned> getDestRels() throws IOException {
      if (parentDepth > 0) {
         return new ArrayList<I_RelVersioned>();
      }

      return bean.getDestRels();
   }

   @Override
   public Collection<? extends I_ExtendByRef> getExtensions() throws IOException, TerminologyException {
      return bean.getExtensions();
   }

   @Override
   public List<DefaultMutableTreeNode> getExtraParentNodes() {
      return extraParentNodes;
   }

   @Override
   public I_Identify getIdentifier() throws IOException {
      return bean.getIdentifier();
   }

   @Override
   public List<? extends I_ImageTuple> getImageTuples() throws IOException, TerminologyException {
      return bean.getImageTuples();
   }

   public List<? extends I_ImageTuple> getImageTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
           PositionSetReadOnly positions, Precedence precedencePolicy,
           ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getImageTuples(allowedStatus, allowedTypes, positions, precedencePolicy,
                                 contradictionManager);
   }

   @Override
   public List<? extends I_ImageTuple> getImageTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
           PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getImageTuples(allowedStatus, allowedTypes, positions, precedencePolicy,
                                 contradictionManager);
   }

   @Override
   public Collection<? extends I_ImageVersioned> getImages() throws IOException {
      return bean.getImages();
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexesInactive(ViewCoordinate xyz) throws IOException {
      return bean.getRefexesInactive(xyz);
   }

   @Override
   public String getInitialText() throws IOException {
      I_DescriptionTuple tuple =
         this.getDescTuple(ConceptBeanForTree.this.config.getShortLabelDescPreferenceList(),
                           ConceptBeanForTree.this.config);

      if (tuple != null) {
         return tuple.getText();
      }

      return bean.getInitialText();
   }

   @Override
   public long getLastModificationSequence() {
      return bean.getLastModificationSequence();
   }

   @Override
   public Collection<? extends MediaChronicleBI> getMedia() throws IOException {
      return bean.getMedia();
   }

   @Override
   public int getNid() {
      return bean.getNid();
   }

   @Override
   public int getParentDepth() {
      return parentDepth;
   }

   @Override
   public Set<PositionBI> getPositions() throws IOException {
      return bean.getPositions();
   }

   @Override
   public I_RepresentIdSet getPossibleChildOfConcepts(I_ConfigAceFrame configFrame) throws IOException, ContradictionException {
      return bean.getPossibleChildOfConcepts(configFrame);
   }

   @Override
   public I_RepresentIdSet getPossibleKindOfConcepts(I_ConfigAceFrame config) throws IOException, ContradictionException {
      return (I_RepresentIdSet) bean.getPossibleKindOfConcepts(config);
   }

   @Override
   public UUID getPrimUuid() {
      return bean.getPrimUuid();
   }

   @Override
   public ConceptVersionBI getPrimordialVersion() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
      return bean.getRefexMembers(refsetNid);
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
      return bean.getRefexes();
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid) throws IOException {
      return bean.getRefexes(refsetNid);
   }

   @Override
   public RefexChronicleBI<?> getRefsetMemberForComponent(int componentNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefsetMembers() throws IOException {
      return bean.getRefsetMembers();
   }

   @Override
   public Collection<? extends RelationshipGroupVersionBI> getRelationshipOutgoingGroups(ViewCoordinate vc)
           throws IOException, ContradictionException {
      return bean.getRelationshipOutgoingGroups(vc);
   }

   @Override
   public int getRelId() {
      return relId;
   }

   @Override
   public Collection<? extends RelationshipChronicleBI> getRelationshipsIncoming() throws IOException {
      return bean.getRelationshipsIncoming();
   }

   @Override
   public Collection<? extends RelationshipChronicleBI> getRelationshipsOutgoing() throws IOException {
      return bean.getRelationshipsOutgoing();
   }

   @Override
   public I_RelVersioned getSourceRel(int relNid) throws IOException {
      return bean.getSourceRel(relNid);
   }

   public Set<? extends I_GetConceptData> getSourceRelTargets(I_IntSet allowedTypes,
           Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getSourceRelTargets(allowedTypes, precedencePolicy, contradictionManager);
   }

   @Override
   public Set<? extends I_GetConceptData> getSourceRelTargets(NidSetBI allowedTypes,
           Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getSourceRelTargets(allowedTypes, precedencePolicy, contradictionManager);
   }

   public Set<? extends I_GetConceptData> getSourceRelTargets(I_IntSet allowedStatus, I_IntSet allowedTypes,
           PositionSetReadOnly positions, Precedence precedencePolicy,
           ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getSourceRelTargets(allowedStatus, allowedTypes, positions, precedencePolicy,
                                      contradictionManager);
   }

   @Override
   public Set<? extends I_GetConceptData> getSourceRelTargets(NidSetBI allowedStatus, NidSetBI allowedTypes,
           PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getSourceRelTargets(allowedStatus, allowedTypes, positions, precedencePolicy,
                                      contradictionManager);
   }
   
   public List<? extends I_RelTuple> getSourceRelTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
           PositionSetReadOnly positions, Precedence precedencePolicy,
           ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getSourceRelTuples(allowedStatus, allowedTypes, positions, precedencePolicy,
                                     contradictionManager);
   }

   @Override
   public List<? extends I_RelTuple> getSourceRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
           PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException {
      return bean.getSourceRelTuples(allowedStatus, allowedTypes, positions, precedencePolicy,
                                     contradictionManager);
   }

   @Override
   public List<? extends I_RelTuple> getSourceRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
           PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager,
           Long cutoffTime)
           throws IOException, TerminologyException {
      return bean.getSourceRelTuples(allowedStatus, allowedTypes, positions, precedencePolicy,
                                     contradictionManager, cutoffTime);
   }

   @Override
   public List<? extends I_RelTuple> getSourceRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
           PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager,
           int classifierNid, RelAssertionType relAssertionType)
           throws IOException, TerminologyException {
      return bean.getSourceRelTuples(allowedStatus, allowedTypes, positions, precedencePolicy,
                                     contradictionManager, classifierNid, relAssertionType);
   }

   public List<? extends I_RelTuple> getSourceRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
           PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager,
           int classifierNid, RelAssertionType relAssertionType, Long cutoffTime)
           throws IOException, TerminologyException {
      return bean.getSourceRelTuples(allowedStatus, allowedTypes, positions, precedencePolicy,
                                     contradictionManager, classifierNid, relAssertionType, cutoffTime);
   }

   @Override
   public Collection<? extends I_RelVersioned> getSourceRels() throws IOException {
      return bean.getSourceRels();
   }

   public int getTermComponentId() {
      return bean.getConceptNid();
   }

   @Override
   public List<UUID> getUUIDs() {
      return bean.getUUIDs();
   }

   @Override
   public List<UUID> getUids() throws IOException {
      return bean.getUids();
   }

   @Override
   public List<I_Identify> getUncommittedIdVersioned() {
      return bean.getUncommittedIdVersioned();
   }

   @Override
   public NidSetBI getUncommittedIds() {
      return bean.getUncommittedIds();
   }

   @Override
   public UniversalAceBean getUniversalAceBean() throws IOException, TerminologyException {
      return bean.getUniversalAceBean();
   }

   @Override
   public ConceptVersionBI getVersion(ViewCoordinate c) throws ContradictionException {
      return bean.getVersion(c);
   }

   @Override
   public Collection<? extends ConceptVersionBI> getVersions() {
      return bean.getVersions();
   }

   @Override
   public Collection<? extends ConceptVersionBI> getVersions(ViewCoordinate c) {
      return bean.getVersions(c);
   }

   @Override
   public FoundContradictionVersions getVersionsInContradiction(ViewCoordinate vc) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean hasAnnotationMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
      return bean.hasAnnotationMemberActive(xyz, refsetNid);
   }

   public boolean hasRefexMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
      return bean.hasRefexMemberActive(xyz, refsetNid);
   }

   @Override
   public boolean hasRefsetMemberActiveForComponent(ViewCoordinate vc, int componentNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean isAnnotationStyleRefex() throws IOException {
      return bean.isAnnotationStyleRefex();
   }
   
   @Override
   public boolean isAnnotationIndex() throws IOException {
      return bean.isAnnotationIndex();
   }

   @Override
   public boolean isCanceled() throws IOException {
      return bean.isCanceled();
   }

   @Override
   public boolean isLeaf(I_ConfigAceFrame aceConfig, boolean addUncommitted) throws IOException {
      if (parentDepth > 0) {
         return true;
      }

      return bean.isLeaf(aceConfig, addUncommitted);
   }

   @Override
   public boolean isParentOf(I_GetConceptData child) throws IOException, TerminologyException, ContradictionException {
      return bean.isParentOf(child);
   }

   public boolean isParentOf(I_GetConceptData child, I_IntSet allowedStatus, I_IntSet allowedTypes,
                             PositionSetReadOnly positions, Precedence precedencePolicy,
                             ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException, ContradictionException {
      return bean.isParentOf(child, allowedStatus, allowedTypes, positions, precedencePolicy,
                             contradictionManager);
   }

   @Override
   public boolean isParentOf(I_GetConceptData child, NidSetBI allowedStatus, NidSetBI allowedTypes,
                             PositionSetBI positions, Precedence precedencePolicy,
                             ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException, ContradictionException {
      return bean.isParentOf(child, allowedStatus, allowedTypes, positions, precedencePolicy,
                             contradictionManager);
   }

   @Override
   public boolean isParentOfOrEqualTo(I_GetConceptData child) throws IOException, TerminologyException, ContradictionException {
      return bean.isParentOfOrEqualTo(child);
   }

   public boolean isParentOfOrEqualTo(I_GetConceptData child, I_IntSet allowedStatus, I_IntSet allowedTypes,
                                      PositionSetReadOnly positions, Precedence precedencePolicy,
                                      ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException, ContradictionException{
      return bean.isParentOfOrEqualTo(child, allowedStatus, allowedTypes, positions, precedencePolicy,
                                      contradictionManager);
   }

   @Override
   public boolean isParentOfOrEqualTo(I_GetConceptData child, NidSetBI allowedStatus, NidSetBI allowedTypes,
                                      PositionSetBI positions, Precedence precedencePolicy,
                                      ContradictionManagerBI contradictionManager)
           throws IOException, TerminologyException, ContradictionException {
      return bean.isParentOfOrEqualTo(child, allowedStatus, allowedTypes, positions, precedencePolicy,
                                      contradictionManager);
   }

   @Override
   public boolean isParentOpened() {
      return parentOpened;
   }

   @Override
   public boolean isSecondaryParentNode() {
      return secondaryParentNode;
   }

   @Override
   public boolean isUncommitted() {
      return bean.isUncommitted();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setAnnotationStyleRefex(boolean annotationSyleRefex) {
      bean.setAnnotationStyleRefex(annotationSyleRefex);
   }
   
   @Override
   public void setAnnotationIndex(boolean annotationIndex) throws IOException {
      bean.setAnnotationIndex(annotationIndex);
   }

   @Override
   public void setParentOpened(boolean opened) {
      this.parentOpened = opened;
   }

    @Override
    public Set<Integer> getAllNidsForStamps(Set<Integer> sapNids) throws IOException {
        return bean.getAllNidsForStamps(sapNids);
    }
}
