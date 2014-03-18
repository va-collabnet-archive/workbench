package org.ihtsdo.concept;

//~--- non-JDK imports --------------------------------------------------------
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntList;
import org.dwfa.vodb.types.IntSet;

import org.ihtsdo.cern.colt.map.OpenIntIntHashMap;
import org.ihtsdo.concept.component.relationship.group.RelGroupVersion;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidListBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.constraint.ConstraintBI;
import org.ihtsdo.tk.api.constraint.ConstraintCheckType;
import org.ihtsdo.tk.api.constraint.DescriptionConstraint;
import org.ihtsdo.tk.api.constraint.RelationshipConstraint;
import org.ihtsdo.tk.api.constraint.RelationshipConstraintIncoming;
import org.ihtsdo.tk.api.constraint.RelationshipConstraintOutgoing;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

public class ConceptVersion implements ConceptVersionBI, Comparable<ConceptVersion> {

    private static IntSet classifierCharacteristics;
    //~--- fields --------------------------------------------------------------
    private Concept concept;

    @Override
    public Collection<Integer> getAllNids() throws IOException {
        return concept.getAllNids();
    }
    NidListBI fsnOrder;
    NidListBI preferredOrder;
    NidListBI synonymOrder;
    private ViewCoordinate vc;

    //~--- constructors --------------------------------------------------------
    public ConceptVersion(Concept concept, ViewCoordinate coordinate) {
        super();
        this.concept = concept;
        this.vc = new ViewCoordinate(coordinate);
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException {
        return concept.addAnnotation(annotation);
    }

    @Override
    public ConceptChronicleBI getEnclosingConcept() {
        return concept;
    }
    
    @Override
    public ConceptCB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB { 
        return concept.makeBlueprint(vc);
    }
    
    @Override
    public ConceptCB makeBlueprint() throws IOException, ContradictionException, InvalidCAB { 
        return concept.makeBlueprint(vc);
    }
    
    @Override
    public void cancel() throws IOException {
        concept.cancel();
    }

    private boolean checkConceptVersionConstraint(int cNid, ConceptSpec constraint,
            ConstraintCheckType checkType)
            throws IOException, ContradictionException {
        switch (checkType) {
            case EQUALS:
                return Ts.get().getConceptVersion(vc, cNid).getNid() == constraint.get(vc).getNid();

            case IGNORE:
                return true;

            case KIND_OF:
                return Ts.get().getConceptVersion(vc, cNid).isKindOf(constraint.get(vc));

            default:
                throw new UnsupportedOperationException("Illegal ConstraintCheckType: " + checkType);
        }
    }

    private boolean checkTextConstraint(String text, String constraint, ConstraintCheckType checkType) {
        switch (checkType) {
            case EQUALS:
                return text.equals(constraint);

            case IGNORE:
                return true;

            case REGEX:
                Pattern pattern = Pattern.compile(constraint);
                Matcher matcher = pattern.matcher(text);

                return matcher.find();

            default:
                throw new UnsupportedOperationException("Illegal ConstraintCheckType: " + checkType);
        }
    }

    @Override
    public boolean commit(ChangeSetGenerationPolicy changeSetPolicy,
            ChangeSetGenerationThreadingPolicy changeSetWriterThreading)
            throws IOException {
        return concept.commit(changeSetPolicy, changeSetWriterThreading);
    }
    
    @Override
    public boolean commit(ChangeSetGenerationPolicy changeSetPolicy,
            ChangeSetGenerationThreadingPolicy changeSetWriterThreading,
            boolean writeAdjudication)
            throws IOException {
        return concept.commit(changeSetPolicy, changeSetWriterThreading, writeAdjudication);
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
    public boolean stampIsInRange(int min, int max) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean satisfies(ConstraintBI constraint, ConstraintCheckType subjectCheck,
            ConstraintCheckType propertyCheck, ConstraintCheckType valueCheck)
            throws IOException, ContradictionException {
        if (RelationshipConstraintOutgoing.class.isAssignableFrom(constraint.getClass())) {
            return testRels(constraint, subjectCheck, propertyCheck, valueCheck, getRelationshipsOutgoingActive());
        } else if (RelationshipConstraintIncoming.class.isAssignableFrom(constraint.getClass())) {
            return testRels(constraint, subjectCheck, propertyCheck, valueCheck, getRelationshipsIncomingActive());
        } else if (DescriptionConstraint.class.isAssignableFrom(constraint.getClass())) {
            DescriptionConstraint dc = (DescriptionConstraint) constraint;

            for (DescriptionVersionBI desc : getDescriptionsActive()) {
                if (checkConceptVersionConstraint(desc.getConceptNid(), dc.getConceptSpec(), subjectCheck)
                        && checkConceptVersionConstraint(desc.getTypeNid(), dc.getDescriptionTypeSpec(), propertyCheck)
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
                temp.add(Ts.get().getNidForUuids(SnomedMetadataRf1.DEFINED_RF1.getUuids()));
                temp.add(Ts.get().getNidForUuids(SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids()));
                temp.add(Ts.get().getNidForUuids(SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids()));
                temp.add(SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getConceptNid());
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
            throws IOException, ContradictionException {
        RelationshipConstraint rc = (RelationshipConstraint) constraint;

        for (RelationshipVersionBI rel : rels) {
            if (checkConceptVersionConstraint(rel.getSourceNid(), rc.getSourceSpec(), subjectCheck)
                    && checkConceptVersionConstraint(rel.getTypeNid(), rc.getRelationshipTypeSpec(), propertyCheck)
                    && checkConceptVersionConstraint(rel.getTargetNid(), rc.getTargetSpec(),
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
    public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException {
        if (getDescriptionPreferred() != null) {
            return getDescriptionPreferred().getText();
        }

        return concept.getInitialText();
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public Collection<? extends IdBI> getAdditionalIds() throws IOException {
        return concept.getAdditionalIds();
    }

    @Override
    public Collection<? extends IdBI> getAllIds() throws IOException {
        return concept.getAllIds();
    }

    @Override
    public Set<Integer> getAllNidsForVersion() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Integer> getAllStampNids() throws IOException {
        return concept.getAllStampNids();
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException {
        return concept.getAnnotations();
    }

    @Override
    public int getAuthorNid() {
        try {
            return getConceptAttributes().getAuthorNid();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConceptChronicleBI getChronicle() {
        return concept;
    }

    @Override
    public ConceptAttributeVersionBI getConceptAttributes() throws IOException {
        return concept.getConAttrs();
    }

    @Override
    public ConceptAttributeVersionBI getConceptAttributesActive() throws IOException, ContradictionException {
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
    public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz)
            throws IOException {
        return concept.getAnnotationsActive(xyz);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getAnnotationMembersActive(ViewCoordinate xyz, int refexNid)
            throws IOException {
        return concept.getAnnotationMembersActive(xyz, refexNid);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate xyz)
            throws IOException {
        return getAnnotationsActive(xyz);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getActiveAnnotations(ViewCoordinate xyz, int refexNid)
            throws IOException {
        return getAnnotationMembersActive(xyz, refexNid);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(int refsetNid) throws IOException {
        return concept.getRefexMembersActive(vc, refsetNid);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
            throws IOException {
        return concept.getRefexMembersActive(xyz, refsetNid);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexesActive(ViewCoordinate xyz) throws IOException {
        return concept.getRefexesActive(xyz);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getActiveRefexes(ViewCoordinate xyz, int refsetNid)
            throws IOException {
        return concept.getRefexMembersActive(xyz, refsetNid);
    }

    @Override
    public RefexChronicleBI<?> getRefexMemberActiveForComponent(int componentNid) throws IOException {
        return concept.getRefsetMemberActiveForComponent(vc, componentNid);
    }

    @Override
    public RefexVersionBI<?> getRefsetMemberActiveForComponent(ViewCoordinate vc, int componentNid)
            throws IOException {
        return concept.getRefsetMemberActiveForComponent(vc, componentNid);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getActiveRefsetMembers() throws IOException {
        return concept.getRefsetMembersActive(vc);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefsetMembersActive(ViewCoordinate vc)
            throws IOException {
        return concept.getRefsetMembersActive(vc);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefsetMembersActive(ViewCoordinate vc, Long cutoffTime)
            throws IOException {
        return concept.getRefsetMembersActive(vc, cutoffTime);
    }

    @Override
    public Collection<? extends DescriptionChronicleBI> getDescriptions() throws IOException {
        return concept.getDescs();
    }

    @Override
    public Collection<? extends DescriptionVersionBI> getDescriptionsActive() throws IOException {
        Collection<DescriptionVersionBI> returnValues = new ArrayList<DescriptionVersionBI>();

        for (DescriptionChronicleBI desc : getDescriptions()) {
            returnValues.addAll(desc.getVersions(vc));
        }

        return returnValues;
    }

    @Override
    public Collection<? extends DescriptionVersionBI> getDescriptionsActive(int typeNid) throws IOException {
        return getDescriptionsActive(new IntSet(new int[]{typeNid}));
    }

    @Override
    public Collection<? extends DescriptionVersionBI> getDescriptionsActive(NidSetBI typeNids) throws IOException {
        Collection<DescriptionVersionBI> results = new ArrayList<DescriptionVersionBI>();

        for (DescriptionVersionBI d : getDescriptionsActive()) {
            if (typeNids.contains(d.getTypeNid())) {
                results.add(d);
            }
        }

        return results;
    }

    @Override
    public Collection<? extends DescriptionVersionBI> getDescriptionsFullySpecifiedActive() throws IOException {
        setupFsnOrder();

        return getDescriptionsActive(new IntSet(fsnOrder.getListArray()));
    }

    @Override
    public DescriptionVersionBI getDescriptionFullySpecified() throws IOException, ContradictionException {
        setupFsnOrder();

        return concept.getDescTuple(fsnOrder, vc.getLangPrefList(), vc.getAllowedStatusNids(),
                vc.getPositionSet(), LANGUAGE_SORT_PREF.getPref(vc.getLangSort()),
                vc.getPrecedence(), vc.getContradictionManager());
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexesInactive(ViewCoordinate xyz) throws IOException {
        return concept.getRefexesInactive(xyz);
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
    public Collection<? extends MediaVersionBI> getMediaActive() throws IOException, ContradictionException {
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
            Collection<? extends ConceptVersionBI> parents = getRelationshipsOutgoingTargetConceptsActiveIsa();

            if (parents.isEmpty()) {
                pathList.add(nidPath);
            } else {
                for (ConceptVersionBI parent : parents) {
                    if (parent.getNid() != getNid()) {
                        pathList.addAll(((ConceptVersion) parent).getNidPathsToRoot(new ArrayList(nidPath)));
                    }
                }
            }
        } catch (ContradictionException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }

        return pathList;
    }

    @Override
    public int getPathNid() {
        try {
            return getConceptAttributes().getPathNid();
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
    public Collection<? extends DescriptionVersionBI> getDescriptionsPreferredActive() throws IOException {
        HashSet<DescriptionVersionBI> returnSet = new HashSet<>();
        for(DescriptionVersionBI d : getDescriptionsActive(SnomedMetadataRfx.getDES_SYNONYM_NID())){
            for(RefexVersionBI r : d.getRefexesActive(vc)){
                if(RefexNidVersionBI.class.isAssignableFrom(r.getClass())){
                    RefexNidVersionBI ri = (RefexNidVersionBI) r;
                    if(ri.getNid1()== SnomedMetadataRfx.getDESC_PREFERRED_NID()){
                        returnSet.add(d);
                    }
                }
            }
        }

        return returnSet;
    }

    @Override
    public DescriptionVersionBI getDescriptionPreferred() throws IOException, ContradictionException {
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
    public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
        return concept.getRefexMembers(refsetNid);
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
    public RefexChronicleBI<?> getRefsetMemberForComponent(int componentNid) throws IOException {
        return concept.getRefsetMemberForComponent(componentNid);
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefsetMembers() throws IOException {
        return concept.getRefsetMembers();
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefsetMembersActive() throws IOException {
        return concept.getRefsetMembersActive(vc);
    }

    @Override
    public Collection<? extends RelationshipGroupVersionBI> getRelationshipGroups() throws IOException, ContradictionException {
        ArrayList<RelationshipGroupVersionBI> results = new ArrayList<RelationshipGroupVersionBI>();

        for (RelationshipGroupChronicleBI rgc : concept.getRelationshipOutgoingGroups(vc)) {
            RelationshipGroupVersionBI rgv = new RelGroupVersion(rgc, vc);

            if (rgv.getRelationships().size() > 0) {
                results.add(rgv);
            }
        }

        return results;
    }

    @Override
    public Collection<? extends RelationshipGroupVersionBI> getRelationshipOutgoingGroups(ViewCoordinate vc)
            throws IOException, ContradictionException {
        return concept.getRelationshipOutgoingGroups(vc);
    }

    @Override
    public Collection<? extends RelationshipChronicleBI> getRelationshipsIncoming() throws IOException {
        return concept.getRelationshipsIncoming();
    }

    @Override
    public Collection<? extends RelationshipVersionBI> getRelationshipsIncomingActive()
            throws IOException, ContradictionException {
        Collection<RelationshipVersionBI> returnValues = new ArrayList<RelationshipVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
            returnValues.addAll(rel.getVersions(vc));
        }

        return returnValues;
    }

    @Override
    public Collection<? extends RelationshipVersionBI> getRelationshipsIncomingActiveIsa()
            throws IOException, ContradictionException {
        Collection<RelationshipVersionBI> returnValues = new ArrayList<RelationshipVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
            for (RelationshipVersionBI rv : rel.getVersions(vc)) {
                if (vc.getIsaTypeNids().contains(rv.getTypeNid())) {
                    returnValues.add(rv);
                }
            }
        }

        return returnValues;
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConcepts() throws IOException {
        HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
            for (RelationshipVersionBI relv : rel.getVersions()) {
                ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getSourceNid());

                conceptSet.add(cv);
            }
        }

        return conceptSet;
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConcepts(int typeNid) throws IOException {
        return getRelationshipsIncomingSourceConcepts(new IntSet(new int[]{typeNid}));
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConcepts(NidSetBI typeNids)
            throws IOException {
        HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
            for (RelationshipVersionBI relv : rel.getVersions()) {
                if (typeNids.contains(relv.getTypeNid())) {
                    ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getSourceNid());

                    conceptSet.add(cv);
                }
            }
        }

        return conceptSet;
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConceptsActive()
            throws IOException, ContradictionException {
        HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
            for (RelationshipVersionBI relv : rel.getVersions(vc)) {
                ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getSourceNid());

                conceptSet.add(cv);
            }
        }

        return conceptSet;
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConceptsActive(int typeNid)
            throws IOException, ContradictionException {
        return getRelationshipsIncomingSourceConceptsActive(new IntSet(new int[]{typeNid}));
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConceptsActive(NidSetBI typeNids)
            throws IOException, ContradictionException {
        HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
            for (RelationshipVersionBI relv : rel.getVersions(vc)) {
                if (typeNids.contains(relv.getTypeNid())) {
                    ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getSourceNid());

                    conceptSet.add(cv);
                }
            }
        }

        return conceptSet;
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConceptsActiveIsa()
            throws IOException, ContradictionException {
        HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
            for (RelationshipVersionBI relv : rel.getVersions(vc)) {
                if (vc.getIsaTypeNids().contains(relv.getTypeNid())) {
                    ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getSourceNid());

                    conceptSet.add(cv);
                }
            }
        }

        return conceptSet;
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsIncomingSourceConceptsIsa() throws IOException {
        HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
            for (RelationshipVersionBI relv : rel.getVersions()) {
                if (vc.getIsaTypeNids().contains(relv.getTypeNid())) {
                    ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getSourceNid());

                    conceptSet.add(cv);
                }
            }
        }

        return conceptSet;
    }

    @Override
    public Collection<? extends RelationshipChronicleBI> getRelationshipsOutgoing() throws IOException {
        setupClassifierCharacteristics();

        Collection<? extends RelationshipChronicleBI> allRels = concept.getRelationshipsOutgoing();
        Collection<RelationshipChronicleBI> results =
                new ArrayList<RelationshipChronicleBI>(allRels.size());

        switch (vc.getRelationshipAssertionType()) {
            case INFERRED:
                for (RelationshipChronicleBI rc : allRels) {
                    for (RelationshipVersionBI<?> rv : rc.getVersions()) {
                        if (classifierCharacteristics.contains(rv.getCharacteristicNid())) {
                            results.add(rc);

                            break;
                        }
                    }
                }

                return results;

            case INFERRED_THEN_STATED:
                return allRels;

            case STATED:
                for (RelationshipChronicleBI rc : allRels) {
                    for (RelationshipVersionBI<?> rv : rc.getVersions()) {
                        if (!classifierCharacteristics.contains(rv.getCharacteristicNid())) {
                            results.add(rc);

                            break;
                        }
                    }
                }

                return results;

            default:
                throw new RuntimeException("Can't handle: " + vc.getRelationshipAssertionType());
        }
    }

    @Override
    public Collection<? extends RelationshipVersionBI> getRelationshipsOutgoingActive()
            throws IOException, ContradictionException {
        Collection<RelationshipVersionBI> returnValues = new ArrayList<RelationshipVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
            returnValues.addAll(rel.getVersions(vc));
        }

        return returnValues;
    }

    @Override
    public Collection<? extends RelationshipVersionBI> getRelationshipsOutgoingActiveIsa()
            throws IOException, ContradictionException {
        Collection<RelationshipVersionBI> returnValues = new ArrayList<RelationshipVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
            for (RelationshipVersionBI rv : rel.getVersions(vc)) {
                if (vc.getIsaTypeNids().contains(rv.getTypeNid())) {
                    returnValues.add(rv);
                }
            }
        }

        return returnValues;
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConcepts() throws IOException {
        HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
            for (RelationshipVersionBI relv : rel.getVersions()) {
                ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getTargetNid());

                conceptSet.add(cv);
            }
        }

        return conceptSet;
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConcepts(int typeNid) throws IOException {
        return getRelationshipsOutgoingTargetConcepts(new IntSet(new int[]{typeNid}));
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConcepts(NidSetBI typeNids)
            throws IOException {
        HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
            for (RelationshipVersionBI relv : rel.getVersions()) {
                if (typeNids.contains(relv.getTypeNid())) {
                    ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getTargetNid());

                    conceptSet.add(cv);
                }
            }
        }

        return conceptSet;
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConceptsActive()
            throws IOException, ContradictionException {
        HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
            for (RelationshipVersionBI relv : rel.getVersions(vc)) {
                ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getTargetNid());

                conceptSet.add(cv);
            }
        }

        return conceptSet;
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConceptsActive(int typeNid)
            throws IOException, ContradictionException {
        return getRelationshipsOutgoingTargetConceptsActive(new IntSet(new int[]{typeNid}));
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConceptsActive(NidSetBI typeNids)
            throws IOException, ContradictionException {
        HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
            for (RelationshipVersionBI relv : rel.getVersions(vc)) {
                if (typeNids.contains(relv.getTypeNid())) {
                    ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getTargetNid());

                    conceptSet.add(cv);
                }
            }
        }

        return conceptSet;
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConceptsActiveIsa()
            throws IOException, ContradictionException {
        HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
            for (RelationshipVersionBI relv : rel.getVersions(vc)) {
                if (vc.getIsaTypeNids().contains(relv.getTypeNid())) {
                    ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getTargetNid());

                    conceptSet.add(cv);
                }
            }
        }

        return conceptSet;
    }

    @Override
    public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingTargetConceptsIsa() throws IOException {
        HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();

        for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
            for (RelationshipVersionBI relv : rel.getVersions()) {
                if (vc.getIsaTypeNids().contains(relv.getTypeNid())) {
                    ConceptVersionBI cv = Ts.get().getConceptVersion(vc, relv.getTargetNid());

                    conceptSet.add(cv);
                }
            }
        }

        return conceptSet;
    }

    @Override
    public int[] getRelationshipsOutgoingTargetNidsActiveIsa() throws IOException {
        OpenIntIntHashMap nidList = new OpenIntIntHashMap(10);

        for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
            for (RelationshipVersionBI relv : rel.getVersions(vc)) {
                if (vc.getIsaTypeNids().contains(relv.getTypeNid())) {
                    nidList.put(relv.getTargetNid(), relv.getTargetNid());
                }
            }
        }

        return nidList.keys().elements();
    }

    @Override
    public int getStampNid() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getStatusNid() {
        try {
            return getConceptAttributes().getStatusNid();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public int getModuleNid() {
         try {
            return getConceptAttributes().getModuleNid();
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
            return getConceptAttributes().getTime();
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
    public boolean hasAnnotationMemberActive(int refsetNid) throws IOException {
        return concept.hasAnnotationMemberActive(vc, refsetNid);
    }

    @Override
    public boolean hasChildren() throws IOException, ContradictionException {
        Collection<? extends RelationshipVersionBI> children = this.getRelationshipsIncomingActive();

        if (children.isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean hasAnnotationMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
        return concept.hasAnnotationMemberActive(xyz, refsetNid);
    }

    @Override
    public boolean hasRefexMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
        return concept.hasRefexMemberActive(xyz, refsetNid);
    }

    @Override
    public boolean hasRefsetMemberActiveForComponent(ViewCoordinate vc, int componentNid) throws IOException {
        return concept.hasRefsetMemberActiveForComponent(vc, componentNid);
    }

    @Override
    public boolean hasHistoricalRelationships() throws IOException, ContradictionException {
        boolean history = false;
        ConceptSpec[] historicalTypes = HistoricalRelType.getHistoricalTypes();
        Collection<? extends RelationshipChronicleBI> outRels = getRelationshipsOutgoing();
        ViewCoordinate c = this.getViewCoordinate();
        I_TermFactory tf = Terms.get();

        if (outRels != null) {
            for (ConceptSpec historicalType : historicalTypes) {
                for (RelationshipChronicleBI outRel : outRels) {
                    RelationshipVersionBI<?> vOutRel = outRel.getVersion(c);

                    if (vOutRel != null) {
                        int typeNid = vOutRel.getTypeNid();
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
    public boolean hasRefexMemberActive(int refsetNid) throws IOException {
        return concept.hasRefexMemberActive(vc, refsetNid);
    }

    @Override
    public boolean hasRefsetMemberActiveForComponent(int componentNid) throws IOException {
        return concept.hasRefsetMemberActiveForComponent(vc, componentNid);
    }

    @Override
    public boolean isActive() throws IOException {
        try {
            if (getConceptAttributesActive() == null) {
                return false;
            }

            return true;
        } catch (ContradictionException ex) {
            for (ConceptAttributeVersionBI version : concept.getConceptAttributes().getVersions(vc)) {
                if (vc.getAllowedStatusNids().contains(version.getStatusNid())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isActive(NidSetBI allowedStatusNids) throws IOException {

        ViewCoordinate tempVc = new ViewCoordinate(vc);
        tempVc.getAllowedStatusNids().clear();
        tempVc.getAllowedStatusNids().addAll(allowedStatusNids.getSetValues());

        try {
            if (concept.getConceptAttributes().getVersion(tempVc) == null) {
                return false;
            }

            if (allowedStatusNids == null || allowedStatusNids.size() == 0) {
                return true;
            }
            return allowedStatusNids.contains(getConceptAttributesActive().getStatusNid());
        } catch (ContradictionException ex) {
            for (ConceptAttributeVersionBI version : concept.getConceptAttributes().getVersions(tempVc)) {
                if (allowedStatusNids.contains(version.getStatusNid())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isActive(ViewCoordinate vc) throws IOException {
        return isActive(vc.getAllowedStatusNids());
    }

    @Override
    public boolean isAnnotationStyleRefex() throws IOException {
        return concept.isAnnotationStyleRefex();
    }
    
    @Override
    public boolean isAnnotationIndex() throws IOException {
        return concept.isAnnotationIndex();
    }

    @Override
    public boolean isBaselineGeneration() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isChildOf(ConceptVersionBI possibleParent) throws IOException {
        for (int nid : getRelationshipsOutgoingTargetNidsActiveIsa()) {
            if (nid == possibleParent.getNid()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isKindOf(ConceptVersionBI possibleKind) throws IOException, ContradictionException {
        return Ts.get().isKindOf(getNid(), possibleKind.getNid(), vc);
    }

    @Override
    public boolean isLeaf() throws IOException {
        try {
            return Ts.get().getPossibleChildren(concept.nid, vc).length == 0;
        } catch (ContradictionException ex) {
           throw new IOException(ex);
        }
    }

    // TODO
    @Override
    public boolean isMember(int collectionNid) throws IOException {
        boolean isMember = false;

        try {
            Collection<? extends RefexChronicleBI<?>> refexes =
                    concept.getConceptAttributes().getRefexesActive(vc);

            if (refexes != null) {
                for (RefexChronicleBI<?> refex : refexes) {
                    if (refex.getRefexNid() == collectionNid) {
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
    
    @Override
    public void setAnnotationIndex(boolean annotationIndex) throws IOException {
        concept.setAnnotationIndex(annotationIndex);
    }

    @Override
    public boolean versionsEqual(ViewCoordinate vc1, ViewCoordinate vc2, Boolean compareAuthoring) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Integer> getAllNidsForStamps(Set<Integer> sapNids) throws IOException {
        return concept.getAllNidsForStamps(sapNids);
    }
}
