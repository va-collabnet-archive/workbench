package org.ihtsdo.concept;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntList;
import org.dwfa.vodb.types.IntSet;

import org.ihtsdo.cern.colt.map.OpenIntIntHashMap;
import org.ihtsdo.concept.component.relationship.group.RelGroupVersion;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidListBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.constraint.ConstraintBI;
import org.ihtsdo.tk.api.constraint.ConstraintCheckType;
import org.ihtsdo.tk.api.constraint.DescriptionConstraint;
import org.ihtsdo.tk.api.constraint.RelConstraint;
import org.ihtsdo.tk.api.constraint.RelConstraintIncoming;
import org.ihtsdo.tk.api.constraint.RelConstraintOutgoing;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.binding.snomed.HistoricalRelType;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.contradiction.FoundContradictionVersions;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.ValidationException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConceptVersion implements ConceptVersionBI, Comparable<ConceptVersion> {
   private static IntSet classifierCharacteristics;

   //~--- fields --------------------------------------------------------------

   private Concept        concept;
   NidListBI              fsnOrder;
   NidListBI              preferredOrder;
   NidListBI              synonymOrder;
   private ViewCoordinate vc;

   //~--- constructors --------------------------------------------------------

   public ConceptVersion(Concept concept, ViewCoordinate coordinate) {
      super();
      this.concept = concept;
      this.vc      = new ViewCoordinate(coordinate);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException {
      return concept.addAnnotation(annotation);
   }

   @Override
   public void cancel() throws IOException {
      concept.cancel();
   }

   private boolean checkConceptVersionConstraint(int cNid, ConceptSpec constraint,
           ConstraintCheckType checkType)
           throws IOException {
      switch (checkType) {
      case EQUALS :
         return Ts.get().getConceptVersion(vc, cNid).getNid() == constraint.get(vc).getNid();

      case IGNORE :
         return true;

      case KIND_OF :
         return Ts.get().getConceptVersion(vc, cNid).isKindOf(constraint.get(vc));

      default :
         throw new UnsupportedOperationException("Illegal ConstraintCheckType: " + checkType);
      }
   }

   private boolean checkTextConstraint(String text, String constraint, ConstraintCheckType checkType) {
      switch (checkType) {
      case EQUALS :
         return text.equals(constraint);

      case IGNORE :
         return true;

      case REGEX :
         Pattern pattern = Pattern.compile(constraint);
         Matcher matcher = pattern.matcher(text);

         return matcher.find();

      default :
         throw new UnsupportedOperationException("Illegal ConstraintCheckType: " + checkType);
      }
   }

   @Override
   public boolean commit(ChangeSetGenerationPolicy changeSetPolicy,
                         ChangeSetGenerationThreadingPolicy changeSetWriterThreading)
           throws IOException {
      return concept.commit(changeSetPolicy, changeSetWriterThreading);
   }

   public void commit(ChangeSetPolicy changeSetPolicy, ChangeSetWriterThreading changeSetWriterThreading)
           throws IOException {
      concept.commit(changeSetPolicy, changeSetWriterThreading);
   }

   @Override
   public int compareTo(ConceptVersion o) {
      return getNid() - o.getNid();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ConceptVersion) {
         ConceptVersion another = (ConceptVersion) obj;

         if (concept.nid != another.concept.nid) {
            return false;
         }

         if (vc == another.vc) {
            return true;
         }

         return vc.equals(another.vc);
      }

      return false;
   }

   @Override
   public int hashCode() {
      return concept.hashCode;
   }

   @Override
   public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws Exception {
      return concept.makeAdjudicationAnalogs(ec, vc);
   }

   @Override
   public boolean sapIsInRange(int min, int max) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean satisfies(ConstraintBI constraint, ConstraintCheckType subjectCheck,
                            ConstraintCheckType propertyCheck, ConstraintCheckType valueCheck)
           throws IOException, ContraditionException {
      if (RelConstraintOutgoing.class.isAssignableFrom(constraint.getClass())) {
         return testRels(constraint, subjectCheck, propertyCheck, valueCheck, getRelsOutgoingActive());
      } else if (RelConstraintIncoming.class.isAssignableFrom(constraint.getClass())) {
         return testRels(constraint, subjectCheck, propertyCheck, valueCheck, getRelsIncomingActive());
      } else if (DescriptionConstraint.class.isAssignableFrom(constraint.getClass())) {
         DescriptionConstraint dc = (DescriptionConstraint) constraint;

         for (DescriptionVersionBI desc : getDescsActive()) {
            if (checkConceptVersionConstraint(desc.getConceptNid(), dc.getConceptSpec(), subjectCheck)
                    && checkConceptVersionConstraint(desc.getTypeNid(), dc.getDescTypeSpec(), propertyCheck)
                    && checkTextConstraint(desc.getText(), dc.getText(), valueCheck)) {
               return true;
            }
         }

         return false;
      }

      throw new UnsupportedOperationException("Can't handle constraint of type: " + constraint);
   }

   private static void setupClassifierCharacteristics() {
      if (classifierCharacteristics == null) {
         IntSet temp = new IntSet();

         try {
            temp.add(SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID());
         } catch (ValidationException e) {
            throw new RuntimeException(e);
         } catch (IOException e) {
            throw new RuntimeException(e);
         }

         classifierCharacteristics = temp;
      }
   }

   private void setupFsnOrder() {
      if (fsnOrder == null) {
         IntList newList = new IntList();

         newList.add(ReferenceConcepts.FULLY_SPECIFIED_RF1.getNid());
         newList.add(ReferenceConcepts.FULLY_SPECIFIED_RF2.getNid());
         fsnOrder = newList;
      }
   }

   private void setupPreferredOrder() {
      if (preferredOrder == null) {
         IntList newList = new IntList();

         newList.add(ReferenceConcepts.PREFERRED_ACCEPTABILITY_RF1.getNid());
         newList.add(ReferenceConcepts.PREFERRED_RF1.getNid());
         newList.add(ReferenceConcepts.PREFERRED_ACCEPTABILITY_RF2.getNid());
         newList.add(ReferenceConcepts.SYNONYM_RF1.getNid());
         newList.add(ReferenceConcepts.SYNONYM_RF2.getNid());
         preferredOrder = newList;
      }
   }

   private boolean testRels(ConstraintBI constraint, ConstraintCheckType subjectCheck,
                            ConstraintCheckType propertyCheck, ConstraintCheckType valueCheck,
                            Collection<? extends RelationshipVersionBI> rels)
           throws IOException {
      RelConstraint rc = (RelConstraint) constraint;

      for (RelationshipVersionBI rel : rels) {
         if (checkConceptVersionConstraint(rel.getOriginNid(), rc.getOriginSpec(), subjectCheck)
                 && checkConceptVersionConstraint(rel.getTypeNid(), rc.getRelTypeSpec(), propertyCheck)
                 && checkConceptVersionConstraint(rel.getDestinationNid(), rc.getDestinationSpec(),
                    valueCheck)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public String toLongString() {
      return concept.toLongString();
   }

   @Override
   public String toString() {
      return concept.toString() + "\n\nviewCoordinate:\n" + vc;
   }

   @Override
   public String toUserString() {
      return concept.toString();
   }

   @Override
   public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContraditionException {
      if (getPreferredDescription() != null) {
         return getPreferredDescription().getText();
      }

      return concept.getInitialText();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Set<Integer> getAllSapNids() throws IOException {
      return concept.getAllSapNids();
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException {
      return concept.getAnnotations();
   }

   @Override
   public int getAuthorNid() {
      try {
         return getConAttrs().getAuthorNid();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public ConceptChronicleBI getChronicle() {
      return concept;
   }

   @Override
   public ConAttrVersionBI getConAttrs() throws IOException {
      return concept.getConceptAttributes();
   }

   @Override
   public ConAttrVersionBI getConAttrsActive() throws IOException, ContraditionException {
      return concept.getConceptAttributes().getVersion(vc);
   }

   @Override
   public int getConceptNid() {
      return concept.getConceptNid();
   }

   public Collection<Integer> getConceptNidsAffectedByCommit() throws IOException {
      return concept.getConceptNidsAffectedByCommit();
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(ViewCoordinate xyz)
           throws IOException {
      return concept.getCurrentAnnotations(xyz);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz) throws IOException {
      return concept.getCurrentRefexes(xyz);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz, int refsetNid)
           throws IOException {
      return concept.getCurrentRefexes(xyz, refsetNid);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers() throws IOException {
      return concept.getCurrentRefsetMembers(vc);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers(ViewCoordinate vc)
           throws IOException {
      return concept.getCurrentRefsetMembers(vc);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers(ViewCoordinate vc, Long cutoffTime)
           throws IOException {
      return concept.getCurrentRefsetMembers(vc, cutoffTime);
   }

   @Override
   public Collection<? extends DescriptionChronicleBI> getDescs() throws IOException {
      return concept.getDescriptions();
   }

   @Override
   public Collection<? extends DescriptionVersionBI> getDescsActive() throws IOException {
      Collection<DescriptionVersionBI> returnValues = new ArrayList<DescriptionVersionBI>();

      for (DescriptionChronicleBI desc : getDescs()) {
         returnValues.addAll(desc.getVersions(vc));
      }

      return returnValues;
   }

   @Override
   public Collection<? extends DescriptionVersionBI> getDescsActive(int typeNid) throws IOException {
      return getDescsActive(new IntSet(new int[] { typeNid }));
   }

   @Override
   public Collection<? extends DescriptionVersionBI> getDescsActive(NidSetBI typeNids) throws IOException {
      Collection<DescriptionVersionBI> results = new ArrayList<DescriptionVersionBI>();

      for (DescriptionVersionBI d : getDescsActive()) {
         if (typeNids.contains(d.getTypeNid())) {
            results.add(d);
         }
      }

      return results;
   }

   @Override
   public Collection<? extends DescriptionVersionBI> getFsnDescsActive() throws IOException {
      setupFsnOrder();

      return getDescsActive(new IntSet(fsnOrder.getListArray()));
   }

   @Override
   public DescriptionVersionBI getFullySpecifiedDescription() throws IOException, ContraditionException {
      setupFsnOrder();

      return concept.getDescTuple(fsnOrder, vc.getLangPrefList(), vc.getAllowedStatusNids(),
                                  vc.getPositionSet(), LANGUAGE_SORT_PREF.getPref(vc.getLangSort()),
                                  vc.getPrecedence(), vc.getContradictionManager());
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getInactiveRefexes(ViewCoordinate xyz) throws IOException {
      return concept.getInactiveRefexes(xyz);
   }

   @Override
   public long getLastModificationSequence() {
      return concept.getLastModificationSequence();
   }

   @Override
   public Collection<? extends MediaChronicleBI> getMedia() throws IOException {
      return concept.getImages();
   }

   @Override
   public Collection<? extends MediaVersionBI> getMediaActive() throws IOException, ContraditionException {
      Collection<MediaVersionBI> returnValues = new ArrayList<MediaVersionBI>();

      for (MediaChronicleBI media : getMedia()) {
         returnValues.addAll(media.getVersions(vc));
      }

      return returnValues;
   }

   @Override
   public int getNid() {
      return concept.getNid();
   }

   @Override
   public Collection<List<Integer>> getNidPathsToRoot() throws IOException {
      return getNidPathsToRootNoAdd(new ArrayList<Integer>());
   }

   private Collection<List<Integer>> getNidPathsToRoot(List<Integer> nidPath) throws IOException {
      nidPath.add(this.getNid());

      return getNidPathsToRootNoAdd(nidPath);
   }

   private Collection<List<Integer>> getNidPathsToRootNoAdd(List<Integer> nidPath) throws IOException {
      TreeSet<List<Integer>> pathList = new TreeSet<List<Integer>>(new Comparator<List<Integer>>() {
         @Override
         public int compare(List<Integer> o1, List<Integer> o2) {
            if (o1.size() != o2.size()) {
               return o1.size() - o2.size();
            }

            int size = o1.size();

            for (int i = 0; i < size; i++) {
               if (o1.get(i) != o2.get(i)) {
                  return o1.get(i) - o2.get(i);
               }
            }

            return 0;
         }
      });

      try {
         Collection<? extends ConceptVersionBI> parents = getRelsOutgoingDestinationsActiveIsa();

         if (parents.isEmpty()) {
            pathList.add(nidPath);
         } else {
            for (ConceptVersionBI parent : parents) {
               pathList.addAll(((ConceptVersion) parent).getNidPathsToRoot(new ArrayList(nidPath)));
            }
         }
      } catch (ContraditionException ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }

      return pathList;
   }

   @Override
   public int getPathNid() {
      try {
         return getConAttrs().getPathNid();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public PositionBI getPosition() throws IOException {
      throw new UnsupportedOperationException();
   }

   @Override
   public Set<PositionBI> getPositions() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends DescriptionVersionBI> getPrefDescsActive() throws IOException {
      setupPreferredOrder();

      return getDescsActive(new IntSet(preferredOrder.getListArray()));
   }

   @Override
   public DescriptionVersionBI getPreferredDescription() throws IOException, ContraditionException {
      setupPreferredOrder();

      return concept.getDescTuple(preferredOrder, vc.getLangPrefList(), vc.getAllowedStatusNids(),
                                  vc.getPositionSet(), LANGUAGE_SORT_PREF.getPref(vc.getLangSort()),
                                  vc.getPrecedence(), vc.getContradictionManager());
   }

   @Override
   public UUID getPrimUuid() {
      return concept.getPrimUuid();
   }

   @Override
   public ConceptVersionBI getPrimordialVersion() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
      return concept.getRefexes();
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid) throws IOException {
      return concept.getRefexes(refsetNid);
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefsetMembers() throws IOException {
      return concept.getRefsetMembers();
   }

   @Override
   public Collection<? extends RelGroupVersionBI> getRelGroups() throws IOException, ContraditionException {
      ArrayList<RelGroupVersionBI> results = new ArrayList<RelGroupVersionBI>();

      for (RelGroupChronicleBI rgc : concept.getRelGroups(vc)) {
         RelGroupVersionBI rgv = new RelGroupVersion(rgc, vc);

         if (rgv.getRels().size() > 0) {
            results.add(rgv);
         }
      }

      return results;
   }

   @Override
   public Collection<? extends RelGroupVersionBI> getRelGroups(ViewCoordinate vc)
           throws IOException, ContraditionException {
      return concept.getRelGroups(vc);
   }

   @Override
   public Collection<? extends RelationshipChronicleBI> getRelsIncoming() throws IOException {
      return concept.getRelsIncoming();
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getRelsIncomingActive()
           throws IOException, ContraditionException {
      Collection<RelationshipVersionBI> returnValues = new ArrayList<RelationshipVersionBI>();

      for (RelationshipChronicleBI rel : getRelsIncoming()) {
         returnValues.addAll(rel.getVersions(vc));
      }

      return returnValues;
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getRelsIncomingActiveIsa()
           throws IOException, ContraditionException {
      Collection<RelationshipVersionBI> returnValues = new ArrayList<RelationshipVersionBI>();

      for (RelationshipChronicleBI rel : getRelsIncoming()) {
         for (RelationshipVersionBI rv : rel.getVersions(vc)) {
            if (vc.getIsaTypeNids().contains(rv.getTypeNid())) {
               returnValues.add(rv);
            }
         }
      }

      return returnValues;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsIncomingOrigins() throws IOException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

      for (RelationshipChronicleBI rel : getRelsIncoming()) {
         for (RelationshipVersionBI relv : rel.getVersions()) {
            ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getOriginNid());

            conceptSet.add(cv);
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsIncomingOrigins(int typeNid) throws IOException {
      return getRelsIncomingOrigins(new IntSet(new int[] { typeNid }));
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsIncomingOrigins(NidSetBI typeNids)
           throws IOException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

      for (RelationshipChronicleBI rel : getRelsIncoming()) {
         for (RelationshipVersionBI relv : rel.getVersions()) {
            if (typeNids.contains(relv.getTypeNid())) {
               ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getOriginNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive()
           throws IOException, ContraditionException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

      for (RelationshipChronicleBI rel : getRelsIncoming()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getOriginNid());

            conceptSet.add(cv);
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(int typeNid)
           throws IOException, ContraditionException {
      return getRelsIncomingOriginsActive(new IntSet(new int[] { typeNid }));
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(NidSetBI typeNids)
           throws IOException, ContraditionException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

      for (RelationshipChronicleBI rel : getRelsIncoming()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            if (typeNids.contains(relv.getTypeNid())) {
               ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getOriginNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActiveIsa()
           throws IOException, ContraditionException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

      for (RelationshipChronicleBI rel : getRelsIncoming()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            if (vc.getIsaTypeNids().contains(relv.getTypeNid())) {
               ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getOriginNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsIsa() throws IOException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

      for (RelationshipChronicleBI rel : getRelsIncoming()) {
         for (RelationshipVersionBI relv : rel.getVersions()) {
            if (vc.getIsaTypeNids().contains(relv.getTypeNid())) {
               ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getOriginNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends RelationshipChronicleBI> getRelsOutgoing() throws IOException {
      setupClassifierCharacteristics();

      Collection<? extends RelationshipChronicleBI> allRels = concept.getRelsOutgoing();
      Collection<RelationshipChronicleBI>           results =
         new ArrayList<RelationshipChronicleBI>(allRels.size());

      switch (vc.getRelAssertionType()) {
      case INFERRED :
         for (RelationshipChronicleBI rc : allRels) {
            for (RelationshipVersionBI<?> rv : rc.getVersions()) {
               if (classifierCharacteristics.contains(rv.getCharacteristicNid())) {
                  results.add(rc);

                  break;
               }
            }
         }

         return results;

      case INFERRED_THEN_STATED :
         return allRels;

      case STATED :
         for (RelationshipChronicleBI rc : allRels) {
            for (RelationshipVersionBI<?> rv : rc.getVersions()) {
               if (!classifierCharacteristics.contains(rv.getCharacteristicNid())) {
                  results.add(rc);

                  break;
               }
            }
         }

         return results;

      default :
         throw new RuntimeException("Can't handle: " + vc.getRelAssertionType());
      }
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getRelsOutgoingActive()
           throws IOException, ContraditionException {
      Collection<RelationshipVersionBI> returnValues = new ArrayList<RelationshipVersionBI>();

      for (RelationshipChronicleBI rel : getRelsOutgoing()) {
         returnValues.addAll(rel.getVersions(vc));
      }

      return returnValues;
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getRelsOutgoingActiveIsa()
           throws IOException, ContraditionException {
      Collection<RelationshipVersionBI> returnValues = new ArrayList<RelationshipVersionBI>();

      for (RelationshipChronicleBI rel : getRelsOutgoing()) {
         for (RelationshipVersionBI rv : rel.getVersions(vc)) {
            if (vc.getIsaTypeNids().contains(rv.getTypeNid())) {
               returnValues.add(rv);
            }
         }
      }

      return returnValues;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations() throws IOException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

      for (RelationshipChronicleBI rel : getRelsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions()) {
            ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getDestinationNid());

            conceptSet.add(cv);
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations(int typeNid) throws IOException {
      return getRelsOutgoingDestinations(new IntSet(new int[] { typeNid }));
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations(NidSetBI typeNids)
           throws IOException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

      for (RelationshipChronicleBI rel : getRelsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions()) {
            if (typeNids.contains(relv.getTypeNid())) {
               ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getDestinationNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive()
           throws IOException, ContraditionException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

      for (RelationshipChronicleBI rel : getRelsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getDestinationNid());

            conceptSet.add(cv);
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive(int typeNid)
           throws IOException, ContraditionException {
      return getRelsOutgoingDestinationsActive(new IntSet(new int[] { typeNid }));
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive(NidSetBI typeNids)
           throws IOException, ContraditionException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

      for (RelationshipChronicleBI rel : getRelsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            if (typeNids.contains(relv.getTypeNid())) {
               ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getDestinationNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActiveIsa()
           throws IOException, ContraditionException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

      for (RelationshipChronicleBI rel : getRelsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            if (vc.getIsaTypeNids().contains(relv.getTypeNid())) {
               ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getDestinationNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsIsa() throws IOException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

      for (RelationshipChronicleBI rel : getRelsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions()) {
            if (vc.getIsaTypeNids().contains(relv.getTypeNid())) {
               ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getDestinationNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public int[] getRelsOutgoingDestinationsNidsActiveIsa() throws IOException {
      OpenIntIntHashMap nidList = new OpenIntIntHashMap(10);

      for (RelationshipChronicleBI rel : getRelsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            if (vc.getIsaTypeNids().contains(relv.getTypeNid())) {
               nidList.put(relv.getDestinationNid(), relv.getDestinationNid());
            }
         }
      }

      return nidList.keys().elements();
   }

   @Override
   public int getSapNid() {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public int getStatusNid() {
      try {
         return getConAttrs().getStatusNid();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public Collection<? extends DescriptionVersionBI> getSynonyms() throws IOException {
      if (synonymOrder == null) {
         synonymOrder = new IntList();
         synonymOrder.add(ReferenceConcepts.ACCEPTABLE_ACCEPTABILITY.getNid());
         synonymOrder.add(ReferenceConcepts.SYNONYM_RF1.getNid());
         synonymOrder.add(ReferenceConcepts.SYNONYM_RF2.getNid());
      }

      throw new UnsupportedOperationException();
   }

   @Override
   public long getTime() {
      try {
         return getConAttrs().getTime();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public List<UUID> getUUIDs() {
      return concept.getUUIDs();
   }

   @Override
   public ConceptVersionBI getVersion(ViewCoordinate c) {
      return concept.getVersion(c);
   }

   @Override
   public Collection<? extends ConceptVersionBI> getVersions() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends ConceptVersionBI> getVersions(ViewCoordinate c) {
      return concept.getVersions();
   }

   @Override
   public FoundContradictionVersions getVersionsInContradiction(ViewCoordinate vc) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public ViewCoordinate getViewCoordinate() {
      return vc;
   }

   @Override
   public boolean hasChildren() throws IOException, ContraditionException {
      Collection<? extends RelationshipVersionBI> children = this.getRelsIncomingActive();

      if (children.isEmpty()) {
         return false;
      }

      return true;
   }

   @Override
   public boolean hasHistoricalRels() throws IOException, ContraditionException {
      boolean                                       history         = false;
      ConceptSpec[]                                 historicalTypes = HistoricalRelType.getHistoricalTypes();
      Collection<? extends RelationshipChronicleBI> outRels         = getRelsOutgoing();
      ViewCoordinate                                c               = this.getViewCoordinate();
      I_TermFactory                                 tf              = Terms.get();

      if (outRels != null) {
         for (ConceptSpec historicalType : historicalTypes) {
            for (RelationshipChronicleBI outRel : outRels) {
               RelationshipVersionBI<?> vOutRel = outRel.getVersion(c);

               if (vOutRel != null) {
                  int    typeNid   = vOutRel.getTypeNid();
                  UUID[] compUuids = historicalType.getUuids();

                  for (UUID compUuid : compUuids) {
                     if (tf.nidToUuid(typeNid).compareTo(compUuid) == 0) {
                        history = true;
                     }
                  }
               }
            }
         }
      }

      return history;
   }

   @Override
   public boolean isActive(NidSetBI allowedStatusNids) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean isAnnotationStyleRefex() throws IOException {
      return concept.isAnnotationStyleRefex();
   }

   @Override
   public boolean isBaselineGeneration() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean isChildOf(ConceptVersionBI possibleParent) throws IOException {
      for (int nid : getRelsOutgoingDestinationsNidsActiveIsa()) {
         if (nid == possibleParent.getNid()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isKindOf(ConceptVersionBI possibleKind) throws IOException {
      Concept possibleParent = ((ConceptVersion) possibleKind).concept;

      try {
         return possibleParent.isParentOfOrEqualTo(concept, vc.getAllowedStatusNids(), vc.getIsaTypeNids(),
                 vc.getPositionSet(), vc.getPrecedence(), vc.getContradictionManager());
      } catch (TerminologyException e) {
         throw new IOException(e);
      }
   }

   @Override
   public boolean isLeaf() throws IOException {
      return Ts.get().getPossibleChildren(concept.nid, vc).length == 0;
   }

   // TODO
   @Override
   public boolean isMember(int collectionNid) throws IOException {
      boolean isMember = false;

      try {
         Collection<? extends RefexChronicleBI<?>> refexes =
            concept.getConceptAttributes().getCurrentRefexes(vc);

         if (refexes != null) {
            for (RefexChronicleBI<?> refex : refexes) {
               if (refex.getCollectionNid() == collectionNid) {
                  return true;
               }
            }
         }

         return isMember;
      } catch (Exception e) {
         throw new IOException(e);    // AceLog.getAppLog().alertAndLogException(e);
      }
   }

   @Override
   public boolean isUncommitted() {
      return concept.isUncommitted();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setAnnotationStyleRefex(boolean annotationStyleRefset) {
      concept.setAnnotationStyleRefex(annotationStyleRefset);
   }
}
