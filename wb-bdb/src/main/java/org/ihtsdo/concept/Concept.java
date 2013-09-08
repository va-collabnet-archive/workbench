package org.ihtsdo.concept;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_TestComponent;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.exceptions.ToIoException;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.description.Description.Version;
import org.ihtsdo.concept.component.description.DescriptionRevision;
import org.ihtsdo.concept.component.image.Image;
import org.ihtsdo.concept.component.processor.AdjudicationAnalogCreator;
import org.ihtsdo.concept.component.processor.VersionFlusher;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetMemberFactory;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.concept.component.relationship.group.RelGroupChronicle;
import org.ihtsdo.concept.component.relationship.group.RelGroupVersion;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.BdbMemoryMonitor.LowMemoryListener;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.db.change.ChangeNotifier;
import org.ihtsdo.db.util.NidPair;
import org.ihtsdo.db.util.NidPairForRefex;
import org.ihtsdo.db.util.ReferenceType;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.lucene.LuceneManager.LuceneSearchType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidList;
import org.ihtsdo.tk.api.NidListBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.ProcessComponentChronicleBI;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.contradiction.IdentifyAllContradictionStrategy;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate.LANGUAGE_SORT;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;
import org.ihtsdo.tk.contradiction.ContradictionResult;
import org.ihtsdo.tk.contradiction.FoundContradictionVersions;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.hash.Hashcode;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;

import jsr166y.ConcurrentReferenceHashMap;
//~--- JDK imports ------------------------------------------------------------

public class Concept implements I_Transact, I_GetConceptData, ConceptChronicleBI, Comparable<Concept> {

    public static ReferenceType refType = ReferenceType.WEAK;
    private static int fsXmlDescNid = Integer.MIN_VALUE;
    private static int fsDescNid = Integer.MIN_VALUE;
    public static ConcurrentReferenceHashMap<Integer, Object> componentsCRHM;
    public static ConcurrentReferenceHashMap<Integer, Concept> conceptsCRHM;
    private static NidSet rf1LangRefexNidSet;
    private static NidSet rf2LangRefexNidSet;
    private static List<TkRefexAbstractMember<?>> unresolvedAnnotations;

    //~--- static initializers -------------------------------------------------
    static {
        Bdb.addMemoryMonitorListener(new ConceptLowMemoryListener());
        init();
    }
    //~--- fields --------------------------------------------------------------
    private boolean canceled = false;
    private boolean removeInvalidXrefs = false;
    NidSetBI allowedStatus;
    NidSetBI allowedTypes;
    ContradictionManagerBI contradictionManager;
    private I_ManageConceptData data;
    protected int hashCode;
    protected int nid;
    PositionSetBI positions;
    Precedence precedencePolicy;

    //~--- constructors --------------------------------------------------------
    private Concept(int nid) throws IOException {
        super();
        assert nid != Integer.MAX_VALUE : "nid == Integer.MAX_VALUE";
        this.nid = nid;
        this.hashCode = Hashcode.compute(nid);

        switch (refType) {
            case SOFT:
            case WEAK:
                data = new ConceptDataSimpleReference(this);

                break;

            case STRONG:
                throw new UnsupportedOperationException();

            default:
                throw new UnsupportedOperationException("Can't handle reference type: " + refType);
        }

        if (Bdb.watchList.containsKey(nid)) {
            AceLog.getAppLog().info("$$$$$$$$$$$$$$ Constructing concept: " + nid + " $$$$$$$$$$$$$$");
        }
    }

    /**
     * For use in testing/test cases only.
     *
     * @param nid
     * @param editable
     * @param roBytes
     * @param mutableBytes
     * @throws IOException
     */
    protected Concept(int nid, byte[] roBytes, byte[] mutableBytes) throws IOException {
        this.nid = nid;
        this.hashCode = Hashcode.compute(nid);
        data = new ConceptDataSimpleReference(this, roBytes, mutableBytes);

        if (Bdb.watchList.containsKey(nid)) {
            AceLog.getAppLog().info("############  Constructing concept: " + nid + " ############");
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public void abort() throws IOException {
        // TODO...
    }

    @Override
    public boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException {
        return getConAttrs().addAnnotation(annotation);
    }

    public boolean addMemberNid(int nid) throws IOException {
        Set<Integer> memberNids = data.getMemberNids();

        if (!memberNids.contains(nid)) {
            memberNids.add(nid);
            modified();

            return true;
        }

        return false;
    }

    @Override
    public void cancel() throws IOException {
        ChangeNotifier.touchComponents(getConceptNidsAffectedByCommit());
        data.cancel();

        if (BdbCommitManager.forget(getConAttrs())) {
            Bdb.getConceptDb().forget(this);
            canceled = true;
        }
    }

    @Override
    public boolean commit(ChangeSetGenerationPolicy changeSetPolicy,
            ChangeSetGenerationThreadingPolicy changeSetWriterThreading,
            boolean writeAdjudication)
            throws IOException {
        this.modified();

        return BdbCommitManager.commit(this, ChangeSetPolicy.get(changeSetPolicy),
                ChangeSetWriterThreading.get(changeSetWriterThreading),
                writeAdjudication);
    }

    @Override
    public boolean commit(ChangeSetGenerationPolicy changeSetPolicy,
            ChangeSetGenerationThreadingPolicy changeSetWriterThreading)
            throws IOException {
        this.modified();

        return BdbCommitManager.commit(this, ChangeSetPolicy.get(changeSetPolicy),
                ChangeSetWriterThreading.get(changeSetWriterThreading));
    }

    public boolean commit(ChangeSetPolicy changeSetPolicy, ChangeSetWriterThreading changeSetWriterThreading)
            throws IOException {
        return BdbCommitManager.commit(this, changeSetPolicy, changeSetWriterThreading);
    }

    @Override
    public void commit(int version, Set<TimePathId> values) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Concept o) {
        return getNid() - o.getNid();
    }

    private void diet() {
        data.diet();
    }

    public static void disableComponentsCRHM() {
        componentsCRHM = new ConcurrentReferenceHashMap<Integer, Object>(ConcurrentReferenceHashMap.ReferenceType.STRONG,
                ConcurrentReferenceHashMap.ReferenceType.WEAK) {

            @Override
            public Object put(Integer key, Object value) {
                return null;
            }

            @Override
            public void putAll(Map<? extends Integer, ? extends Object> m) {
                // nothing to do;
            }

            @Override
            public Object putIfAbsent(Integer key, Object value) {
                return null;
            }

            @Override
            public boolean replace(Integer key, Object oldValue, Object newValue) {
                return false;
            }

            @Override
            public Object replace(Integer key, Object value) {
                return false;
            }
        };
    }

    public static void enableComponentsCRHM() {
        componentsCRHM = new ConcurrentReferenceHashMap<Integer, Object>(ConcurrentReferenceHashMap.ReferenceType.STRONG,
                ConcurrentReferenceHashMap.ReferenceType.WEAK);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (Concept.class.isAssignableFrom(obj.getClass())) {
            Concept another = (Concept) obj;

            return nid == another.nid;
        }

        return false;
    }

    @Override
    public boolean everHadSrcRelOfType(int typeNid) throws IOException {
        Collection<Relationship> rels = getSourceRels();

        for (Relationship r : rels) {
            if (r.everWasType(typeNid)) {
                return true;
            }
        }

        return false;
    }

    public void flushVersions() throws Exception {
        processComponentChronicles(new VersionFlusher());
    }

    private void formatCollection(StringBuffer buff, Collection<?> list) {
        if ((list != null) && (list.size() > 0)) {
            buff.append("[\n");

            for (Object obj : list) {
                buff.append("   ");
                buff.append(obj);
                buff.append(",\n");
            }

            buff.append("]");
        } else {
            buff.append("[]");
        }
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private static void init() {
        conceptsCRHM = new ConcurrentReferenceHashMap<Integer, Concept>(ConcurrentReferenceHashMap.ReferenceType.STRONG,
                ConcurrentReferenceHashMap.ReferenceType.WEAK);
        componentsCRHM = new ConcurrentReferenceHashMap<Integer, Object>(ConcurrentReferenceHashMap.ReferenceType.STRONG,
                ConcurrentReferenceHashMap.ReferenceType.WEAK);
        unresolvedAnnotations = new ArrayList<TkRefexAbstractMember<?>>();
        fsXmlDescNid = Integer.MIN_VALUE;
        fsDescNid = Integer.MIN_VALUE;
        rf1LangRefexNidSet = new NidSet();
        rf1LangRefexNidSet.add(ReferenceConcepts.FULLY_SPECIFIED_RF1.getNid());
        rf1LangRefexNidSet.add(ReferenceConcepts.PREFERRED_RF1.getNid());
        rf1LangRefexNidSet.add(ReferenceConcepts.SYNONYM_RF1.getNid());
        rf2LangRefexNidSet = new NidSet();
        rf2LangRefexNidSet.add(ReferenceConcepts.FULLY_SPECIFIED_RF2.getNid());
        rf2LangRefexNidSet.add(ReferenceConcepts.SYNONYM_RF2.getNid());
        rf2LangRefexNidSet.add(ReferenceConcepts.FULLY_SPECIFIED_RF1.getNid());
        rf2LangRefexNidSet.add(ReferenceConcepts.PREFERRED_RF1.getNid());
        rf2LangRefexNidSet.add(ReferenceConcepts.SYNONYM_RF1.getNid());
    }

    @Override
    public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws Exception {
        AdjudicationAnalogCreator aac = new AdjudicationAnalogCreator(ec, vc);

        processComponentChronicles(aac);

        return aac.isComponentChanged();
    }

    public static Concept mergeAndWrite(EConcept eConcept, Set<ConceptChronicleBI> indexedAnnotationConcepts) throws IOException {
        int conceptNid = Bdb.uuidToNid(eConcept.getPrimordialUuid());

        assert conceptNid != Integer.MAX_VALUE : "no conceptNid for uuids";

        Concept c = get(conceptNid);

        mergeWithEConcept(eConcept, c, true, indexedAnnotationConcepts);
        BdbCommitManager.addUncommittedNoChecks(c);

        return c;
    }

    /**
     * merge from EConcept eConcept into Concept c
     *
     * @param eConcept
     * @param c
     * @param updateLucene
     * @param indexedAnnotationConcepts
     * @return
     * @throws IOException
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Concept mergeWithEConcept(EConcept eConcept, Concept c, boolean updateLucene, Set<ConceptChronicleBI> indexedAnnotationConcepts)
            throws IOException {
        if (c.isAnnotationStyleRefex() == false) {
            c.setAnnotationStyleRefex(eConcept.isAnnotationStyleRefex());
        }

        if (c.isAnnotationIndex() == false) {
            c.setAnnotationIndex(eConcept.isAnnotationIndexStyleRefex());
        }

        TkConceptAttributes eAttr = eConcept.getConceptAttributes();

        if (eAttr != null) {
            if (c.getConAttrs() == null) {
                setAttributesFromEConcept(c, eAttr);
//Bad patch from Dan to enable refset indexing  https://csfe.aceworkspace.net/sf/go/artf228205?nav=1&_pagenum=1&returnUrlKey=1371093562968
                for (RefexChronicleBI<?> annotation : c.getAnnotations()) {
                    if (annotation instanceof RefsetMember){
                        RefsetMember refsetMember = (RefsetMember)annotation;
                        if (Ts.get().getConceptNidForNid(refsetMember.getRefsetId()) != Integer.MAX_VALUE) {
                            Concept refsetConcept = (Concept) Ts.get().getConceptForNid(refsetMember.getRefsetId());
                            if (refsetConcept.isAnnotationIndex()) {
                                refsetConcept.getData().getMemberNids().add(refsetMember.getNid());
                                indexedAnnotationConcepts.add(refsetConcept);
                            }
                        } else {
                            // Not an error, it the indexed annotation concept does not exist, 
                            // then it is from initial load, and the index will be created later. 
                            //System.out.println("Nid to cNid == Integer.MAX_VALUE; " + refsetMember);
                        }
                    }
                }
//end of bad patch
            } else {
                ConceptAttributes ca = c.getConAttrs();
                ca.merge(new ConceptAttributes(eAttr, c), indexedAnnotationConcepts);
            }
            ChangeNotifier.touch(c.nid, ChangeNotifier.Change.COMPONENT);
        }

        // check if different primordial UUIDs are being merged
        if (c.getPrimUuid().compareTo(UUID.fromString("00000000-0000-0000-c000-000000000046")) != 0
                && c.getUUIDs().contains(eConcept.primordialUuid) == false) {
            if (eConcept.getConceptAttributes() == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("\r\nmergeWithEConcept eConcept.getConceptAttributes() == null");
                sb.append(" insufficient information to add UUID_ADDITIONAL:\t");
                sb.append(c.getPrimUuid());
                sb.append("\t");
                sb.append(c.toUserString());
                sb.append("\tUUID_PRIMORDIAL:\t");
                sb.append(eConcept.getPrimordialUuid().toString());
                if (eConcept.getDescriptions() != null && eConcept.getDescriptions().size() > 0) {
                    sb.append("\t");
                    sb.append(eConcept.getDescriptions().get(0).text);
                }
                sb.append("\r\n");
                AceLog.getAppLog().log(Level.INFO, sb.toString());
            } else {
                TerminologyStoreDI ts = Ts.get();
                int unspecifiedUuidNid = ts.getNidForUuids(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());

                // check if Concept c already has the extra UUID
                if (c.getUUIDs().contains(eConcept.primordialUuid) == false) {
                    // add EConcept eConcept primordial uuid into Concept c additional ids
                    // RF2 Active
                    int idStatusNid = ts.getNidForUuids(UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f"));
                    long idTime = eConcept.getConceptAttributes().time;
                    int idAuthorNid = ts.getNidForUuids(eConcept.getConceptAttributes().authorUuid);
                    int idModuleNid = ts.getNidForUuids(eConcept.getConceptAttributes().moduleUuid);
                    int idPathNid = ts.getNidForUuids(eConcept.getConceptAttributes().pathUuid);

                    c.getConceptAttributes().addUuidId(
                            eConcept.getPrimordialUuid(), 
                            unspecifiedUuidNid, 
                            idStatusNid, 
                            idTime, 
                            idAuthorNid, 
                            idModuleNid, 
                            idPathNid); // STAMP

                    StringBuilder sb = new StringBuilder();
                    sb.append("\r\nmergeWithEConcept SCTID:\t");
                    if (eConcept.getConceptAttributes() != null
                            && eConcept.getConceptAttributes().getEIdentifiers() != null) {
                        List<TkIdentifier> ids = eConcept.getConceptAttributes().getEIdentifiers();
                        for (TkIdentifier tkIdentifier : ids) {
                            UUID authorityUuid = tkIdentifier.getAuthorityUuid();
                            UUID snomedIntIdUuid = UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9");
                            if (authorityUuid.compareTo(snomedIntIdUuid) == 0) {
                                sb.append(((Long) tkIdentifier.getDenotation()).toString());
                                sb.append("\t");
                                break;
                            }
                        }
                    }
                    sb.append("\tUUID_PRIMORDIAL:\t");
                    sb.append(c.getPrimUuid());
                    sb.append("\t");
                    sb.append(c.toUserString());
                    sb.append("\tUUID_ADDITIONAL:\t");
                    sb.append(eConcept.getPrimordialUuid().toString());
                    if (eConcept.getDescriptions() != null && eConcept.getDescriptions().size() > 0) {
                        sb.append("\t");
                        sb.append(eConcept.getDescriptions().get(0).text);
                    }
                    sb.append("\r\n");
                    AceLog.getAppLog().log(Level.INFO, sb.toString());

                    ChangeNotifier.touch(c.nid, ChangeNotifier.Change.COMPONENT);
                }
            }
        }

        if ((eConcept.getDescriptions() != null) && !eConcept.getDescriptions().isEmpty()) {
            if ((c.getDescs() == null) || c.getDescs().isEmpty()) {
                setDescriptionsFromEConcept(eConcept, c);
            } else {
                Set<Integer> currentDNids = c.data.getDescNids();

                for (TkDescription ed : eConcept.getDescriptions()) {
                    int dNid = Bdb.uuidToNid(ed.primordialUuid);

                    if (currentDNids.contains(dNid)) {
                        Description d = c.getDescription(dNid);
                        ChangeNotifier.touch(d.nid, ChangeNotifier.Change.COMPONENT);
                        d.merge(new Description(ed, c), indexedAnnotationConcepts);
                    } else {
                        Description d = new Description(ed, c);
                        ChangeNotifier.touch(d.nid, ChangeNotifier.Change.COMPONENT);
                        c.getDescs().add(d);
                    }
                }
            }
            ChangeNotifier.touch(c.nid, ChangeNotifier.Change.COMPONENT);

            if (updateLucene) {
                LuceneManager.writeToLucene(c.getDescs(), LuceneSearchType.DESCRIPTION);
            }
        }

        if ((eConcept.getRelationships() != null) && !eConcept.getRelationships().isEmpty()) {
            if ((c.getSourceRels() == null) || c.getSourceRels().isEmpty()) {
                setRelationshipsFromEConcept(eConcept, c);
            } else {
                Set<Integer> currentSrcRelNids = c.data.getSrcRelNids();

                for (TkRelationship er : eConcept.getRelationships()) {
                    int rNid = Bdb.uuidToNid(er.primordialUuid);

                    if (currentSrcRelNids.contains(rNid)) {
                        Relationship r = c.getSourceRel(rNid);
                        ChangeNotifier.touch(r.getSourceNid(), ChangeNotifier.Change.REL_ORIGIN);
                        ChangeNotifier.touch(r.getTargetNid(), ChangeNotifier.Change.REL_XREF);
                        r.merge(new Relationship(er, c), indexedAnnotationConcepts);
                    } else {
                        Relationship r = new Relationship(er, c);
                        ChangeNotifier.touch(r.getSourceNid(), ChangeNotifier.Change.REL_ORIGIN);
                        ChangeNotifier.touch(r.getTargetNid(), ChangeNotifier.Change.REL_XREF);
                        c.getSourceRels().add(r);
                    }
                }
            }
            ChangeNotifier.touch(c.nid, ChangeNotifier.Change.COMPONENT);
        }

        if ((eConcept.getImages() != null) && !eConcept.getImages().isEmpty()) {

            if ((c.getImages() == null) || c.getImages().isEmpty()) {
                setImagesFromEConcept(eConcept, c);
            } else {
                Set<Integer> currentImageNids = c.data.getImageNids();

                for (TkMedia eImg : eConcept.getImages()) {
                    int iNid = Bdb.uuidToNid(eImg.primordialUuid);

                    if (currentImageNids.contains(iNid)) {
                        Image img = c.getImage(iNid);

                        img.merge(new Image(eImg, c), indexedAnnotationConcepts);
                    } else {
                        c.getImages().add(new Image(eImg, c));
                    }
                }
            }
        }

        if ((eConcept.getRefsetMembers() != null) && !eConcept.getRefsetMembers().isEmpty()) {
            if (c.isAnnotationStyleRefex()) {
                for (TkRefexAbstractMember<?> er : eConcept.getRefsetMembers()) {
                    // Workflow refsets handled with WfRefsetChangeSetReader
                    if (WorkflowHelper.isWorkflowCapabilityAvailable()) {
                        if (WorkflowHelper.getRefsetUidList().contains(er.refsetUuid)) {
                            continue;
                        }
                    }

                    ConceptComponent cc;
                    Object referencedComponent = Ts.get().getComponent(er.getComponentUuid());

                    if (referencedComponent != null) {
                        if (referencedComponent instanceof Concept) {
                            cc = ((Concept) referencedComponent).getConAttrs();
                        } else {
                            cc = (ConceptComponent) referencedComponent;
                        }

                        RefsetMember r = (RefsetMember) Ts.get().getComponent(er.getPrimordialComponentUuid());

                        if (r == null) {
                            r = RefsetMemberFactory.create(er,
                                    Bdb.getConceptNid(cc.getNid()));
                            cc.addAnnotation(r);
                        } else {
                            r.merge((RefsetMember) RefsetMemberFactory.create(er,
                                    Bdb.getConceptNid(cc.getNid())), indexedAnnotationConcepts);
                        }
                        ChangeNotifier.touchRefexRC(r.getReferencedComponentNid());

                    } else {
                        unresolvedAnnotations.add(er);
                    }
                }
            } else {
                if ((c.getRefsetMembers() == null) || c.getRefsetMembers().isEmpty()) {
                    setRefsetMembersFromEConcept(eConcept, c);
                } else {
                    Set<Integer> currentMemberNids = c.data.getMemberNids();

                    for (TkRefexAbstractMember<?> er : eConcept.getRefsetMembers()) {
                        int rNid = Bdb.uuidToNid(er.primordialUuid);
                        RefsetMember<?, ?> r = c.getRefsetMember(rNid);

                        if (currentMemberNids.contains(rNid) && (r != null)) {
                            r.mergeNoReturn(RefsetMemberFactory.create(er, c.getNid()),
                                    indexedAnnotationConcepts);
                        } else {
                            r = RefsetMemberFactory.create(er, c.getNid());
                            c.getRefsetMembers().add(r);
                        }
                        ChangeNotifier.touchRefexRC(r.getReferencedComponentNid());
                    }
                }
            }
        }

        return c;
    }

    public void modified() {
        data.modified();
    }

    public void modified(long sequence) {
        data.modified(sequence);
    }

    private static Concept populateFromEConcept(EConcept eConcept, Concept c) throws IOException {
        if (eConcept.getConceptAttributes() != null) {
            setAttributesFromEConcept(c, eConcept.getConceptAttributes());
        }

        if (eConcept.getDescriptions() != null) {
            setDescriptionsFromEConcept(eConcept, c);
        }

        if (eConcept.getRelationships() != null) {
            setRelationshipsFromEConcept(eConcept, c);
        }

        if (eConcept.getImages() != null) {
            setImagesFromEConcept(eConcept, c);
        }

        if (eConcept.getRefsetMembers() != null) {
            setRefsetMembersFromEConcept(eConcept, c);
        }

        return c;
    }

    public void processComponentChronicles(ProcessComponentChronicleBI processor) throws Exception {
        if (getConAttrs() != null) {
            processor.process(getConAttrs());
        }

        if (getDescs() != null) {
            for (ComponentChronicleBI cc : getDescs()) {
                processor.process(cc);
            }
        }

        if (getSourceRels() != null) {
            for (ComponentChronicleBI cc : getSourceRels()) {
                processor.process(cc);
            }
        }

        if (getImages() != null) {
            for (ComponentChronicleBI cc : getImages()) {
                processor.process(cc);
            }
        }

        if (getRefsetMembers() != null) {
            for (ComponentChronicleBI cc : getRefsetMembers()) {
                processor.process(cc);
            }
        }
    }

    @Override
    public boolean promote(PositionBI viewPosition, PathSetReadOnly pomotionPaths, NidSetBI allowedStatus,
            Precedence precedence, int authorNid)
            throws IOException, TerminologyException {
        boolean promotedAnything = false;

        if (getConAttrs().promote(viewPosition, pomotionPaths, allowedStatus, precedence, authorNid)) {
            promotedAnything = true;
        }

        for (I_DescriptionVersioned dv : getDescs()) {
            if (dv.promote(viewPosition, pomotionPaths, allowedStatus, precedence, authorNid)) {
                promotedAnything = true;
            }
        }

        for (I_RelVersioned rv : getSourceRels()) {
            if (rv.promote(viewPosition, pomotionPaths, allowedStatus, precedence, authorNid)) {
                promotedAnything = true;
            }
        }

        for (I_ImageVersioned img : getImages()) {
            if (img.promote(viewPosition, pomotionPaths, allowedStatus, precedence, authorNid)) {
                promotedAnything = true;
            }
        }

        return promotedAnything;
    }

    @Override
    public boolean promote(I_TestComponent test, I_Position viewPosition, PathSetReadOnly pomotionPaths,
            NidSetBI allowedStatus, Precedence precedence, int authorNid)
            throws IOException, TerminologyException {
        if (test.result(this, viewPosition, pomotionPaths, allowedStatus, precedence)) {
            return promote(viewPosition, pomotionPaths, allowedStatus, precedence, authorNid);
        }

        return false;
    }

    public boolean readyToWrite() {
        assert nid != Integer.MAX_VALUE : "nid == Integer.MAX_VALUE";
        assert data.readyToWrite() : toLongString();

        return true;
    }

    public static void reset() {
        init();
    }

    public void resetNidData() {
        data.resetNidData();
    }

    public static void resolveUnresolvedAnnotations(List<TkRefexAbstractMember<?>> annotations,
            Set<ConceptChronicleBI> annotatedIndexes) throws IOException {
        unresolvedAnnotations = annotations;

        resolveUnresolvedAnnotations(annotatedIndexes);
    }

    public static void resolveUnresolvedAnnotations(Set<ConceptChronicleBI> annotatedIndexes) throws IOException {
        List<TkRefexAbstractMember<?>> cantResolve = new ArrayList<>();

        for (TkRefexAbstractMember<?> er : unresolvedAnnotations) {
            ConceptComponent cc;
            Object referencedComponent = Ts.get().getComponent(er.getComponentUuid());
            
            if (referencedComponent != null) {
                if (referencedComponent instanceof Concept) {
                    cc = ((Concept) referencedComponent).getConAttrs();
                } else {
                    cc = (ConceptComponent) referencedComponent;
                }

                RefsetMember r = (RefsetMember) Ts.get().getComponent(er.getPrimordialComponentUuid());

                if (r == null) {
                    r = RefsetMemberFactory.create(er, Ts.get().getConceptNidForNid(cc.getNid()));
                    cc.addAnnotation(r);
                } else {
                    r.merge((RefsetMember) RefsetMemberFactory.create(er,
                            Ts.get().getConceptNidForNid(cc.getNid())), annotatedIndexes);
                }
                ChangeNotifier.touchRefexRC(r.getReferencedComponentNid());

            } else {

                // tmp fix hook
                if (!er.getRefexUuid().equals(WorkflowHelper.getWorkflowRefsetUid())
                        && !er.getComponentUuid().equals(WorkflowHelper.getWorkflowRefsetUid())) {
                    cantResolve.add(er);
                } else {
                    AceLog.getAppLog().log(
                            Level.WARNING,
                            ("Unable to add to workflow history refset bad changes set member due to bad refCompUid: "
                            + er.getComponentUuid()));
                }
            }
        }

        if (!cantResolve.isEmpty()) {
            AceLog.getAppLog().alertAndLogException(new Exception("Can't resolve some annotations on import: "
                    + cantResolve));
        }
    }

    /**
     * Returns a longer - more complete - string representation of the object.
     *
     * @return
     */
    @Override
    public String toLongString() {
        StringBuffer buff = new StringBuffer();

        try {
            buff.append("\nConcept: \"");
            buff.append(getInitialText());
            buff.append("\" nid: ");
            buff.append(nid);
            buff.append(" annotationRefset: ");
            buff.append(isAnnotationStyleRefex());
            buff.append(" annotationIndex: ");
            buff.append(isAnnotationIndex());
            buff.append("\n  data version: ");
            buff.append(getDataVersion());
            buff.append("\n write version: ");
            buff.append(getWriteVersion());
            buff.append("\n uncommitted: ");
            buff.append(isUncommitted());
            buff.append("\n unwritten: ");
            buff.append(isUnwritten());
            buff.append("\n attributes: ");
            buff.append(getConAttrs());
            buff.append("\n descriptions: ");
            formatCollection(buff, getDescs());
            buff.append("\n srcRels: ");
            formatCollection(buff, getSourceRels());
            buff.append("\n images: ");
            formatCollection(buff, getImages());

            if (!isAnnotationStyleRefex()) {
                buff.append("\n refset members: ");
                formatCollection(buff, getExtensions());
            }

            buff.append("\n desc nids: ");
            buff.append(data.getDescNids());
            buff.append("\n src rel nids: ");
            buff.append(data.getSrcRelNids());
            buff.append("\n member nids: ");
            buff.append(data.getMemberNids());
            buff.append("\n image nids: ");
            buff.append(data.getImageNids());
            buff.append("\n");
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }

        return buff.toString();
    }

    /*
     * (non-Javadoc) @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        try {
            if (!isCanceled()) {
                return getInitialText();
            }

            return "canceled concept";
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);

            return ex.toString();
        }
    }

    @Override
    public String toUserString() {
        try {
            if (!isCanceled()) {
                return getInitialText();
            }

            return "canceled concept";
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);

            return ex.toString();
        }
    }

    public void updateXrefs() throws IOException {
        for (RefsetMember<?, ?> m : getRefsetMembers()) {
            NidPairForRefex npr = NidPair.getRefexNidMemberNidPair(m.getRefexNid(), m.getNid());
            Bdb.addXrefPair(m.referencedComponentNid, npr);
        }
    }

    //~--- get methods ---------------------------------------------------------
    public static Concept get(EConcept eConcept, Set<ConceptChronicleBI> indexedAnnotationConcepts) throws IOException {
        int conceptNid;
        if (eConcept.getConceptAttributes() != null) {
            conceptNid = Bdb.uuidsToNid(eConcept.getConceptAttributes().getUuids());
        } else {
            conceptNid = Bdb.uuidToNid(eConcept.getPrimordialUuid());
        }

        Bdb.getNidCNidMap().setCNidForNid(conceptNid, conceptNid);
        assert conceptNid != Integer.MAX_VALUE : "no conceptNid for uuids";

        Concept c = get(conceptNid);

        // return populateFromEConcept(eConcept, c);
        try {
            return mergeWithEConcept(eConcept, c, false, indexedAnnotationConcepts);
        } catch (Throwable t) {
            AceLog.getAppLog().severe("Cannot merge with eConcept: \n" + eConcept, t);
        }
        return null;
    }

    public static Concept get(int nid) throws IOException {
        assert nid != Integer.MAX_VALUE : "nid == Integer.MAX_VALUE";

        boolean newConcept = false;
        Concept c = conceptsCRHM.get(nid);

        if (c == null) {
            Concept newC = new Concept(nid);

            c = conceptsCRHM.putIfAbsent(nid, newC);

            if (c == null) {
                c = newC;
                newConcept = true;
            }
        }

        conceptsCRHM.put(nid, c);

        if (Bdb.watchList.containsKey(nid) && newConcept) {
            AceLog.getAppLog().info(c.toLongString());
        }

        return c;
    }

    public static Concept get(int nid, byte[] roBytes, byte[] mutableBytes) throws IOException {
        assert nid != Integer.MAX_VALUE : "nid == Integer.MAX_VALUE";

        Concept c = conceptsCRHM.get(nid);

        if (c == null) {
            Concept newC = new Concept(nid, roBytes, mutableBytes);

            c = conceptsCRHM.putIfAbsent(nid, newC);

            if (c == null) {
                c = newC;
            }
        }

        return c;
    }

    @Override
    public Collection<? extends IdBI> getAdditionalIds() throws IOException {
        return getConceptAttributes().getAdditionalIds();
    }

    @Override
    public Collection<? extends IdBI> getAllIds() throws IOException {
        return getConceptAttributes().getAllIds();
    }

    public Collection<Integer> getAllNids() throws IOException {
        return data.getAllNids();
    }

    public Collection<? extends RelationshipGroupChronicleBI> getAllRelGroups() throws IOException {
        ArrayList<RelationshipGroupChronicleBI> results = new ArrayList<RelationshipGroupChronicleBI>();
        Map<Integer, HashSet<RelationshipChronicleBI>> statedGroupMap = new HashMap<Integer, HashSet<RelationshipChronicleBI>>();
        Map<Integer, HashSet<RelationshipChronicleBI>> inferredGroupMap =
                new HashMap<Integer, HashSet<RelationshipChronicleBI>>();

        for (RelationshipChronicleBI r : getRelationshipsOutgoing()) {

            // Inferred
            for (RelationshipVersionBI rv : r.getVersions()) {
                int group = rv.getGroup();

                if (group > 0) {
                    if (rv.isInferred()) {
                        HashSet<RelationshipChronicleBI> relsInGroup = inferredGroupMap.get(group);

                        if (relsInGroup == null) {
                            relsInGroup = new HashSet<RelationshipChronicleBI>();
                            inferredGroupMap.put(group, relsInGroup);
                        }

                        relsInGroup.add(r);
                    } else {
                        HashSet<RelationshipChronicleBI> relsInGroup = statedGroupMap.get(group);

                        if (relsInGroup == null) {
                            relsInGroup = new HashSet<RelationshipChronicleBI>();
                            statedGroupMap.put(group, relsInGroup);
                        }

                        relsInGroup.add(r);
                    }
                }
            }
        }

        for (Entry<Integer, HashSet<RelationshipChronicleBI>> groupEntry : statedGroupMap.entrySet()) {
            results.add(new RelGroupChronicle(this, groupEntry.getKey(), groupEntry.getValue()));
        }

        for (Entry<Integer, HashSet<RelationshipChronicleBI>> groupEntry : inferredGroupMap.entrySet()) {
            results.add(new RelGroupChronicle(this, groupEntry.getKey(), groupEntry.getValue()));
        }

        return results;
    }

    @Override
    public Set<Integer> getAllStampNids() throws IOException {
        Set<Integer> stamps = new HashSet<Integer>();

        if (getConAttrs() != null) {
            stamps.addAll(getConAttrs().getComponentSapNids());
        }

        if (getDescs() != null) {
            for (Description d : getDescs()) {
                stamps.addAll(d.getComponentSapNids());
            }
        }

        if (getRelationshipsOutgoing() != null) {
            for (Relationship r : getSourceRels()) {
                stamps.addAll(r.getComponentSapNids());
            }
        }

        if (getImages() != null) {
            for (Image i : getImages()) {
                stamps.addAll(i.getComponentSapNids());
            }
        }


        if (!isAnnotationStyleRefex() && getRefsetMembers() != null) {
            for (ConceptComponent i : getRefsetMembers()) {
                stamps.addAll(i.getComponentSapNids());
            }
        }

        return stamps;
    }

    @Override
    public Set<Integer> getAllNidsForStamps(Set<Integer> sapNids) throws IOException {
        Set<Integer> componentNids = new HashSet<Integer>();

        if (getConAttrs() != null) {
            componentNids.addAll(getConAttrs().getComponentNidsForSaps(sapNids));
        }

        if (getDescs() != null) {
            for (Description d : getDescs()) {
                componentNids.addAll(d.getComponentNidsForSaps(sapNids));
            }
        }

        if (getRelationshipsOutgoing() != null) {
            for (Relationship r : getSourceRels()) {
                componentNids.addAll(r.getComponentNidsForSaps(sapNids));
            }
        }

        if (getImages() != null) {
            for (Image i : getImages()) {
                componentNids.addAll(i.getComponentNidsForSaps(sapNids));
            }
        }

        if (getRefsetMembers() != null) {
            for (ConceptComponent i : getRefsetMembers()) {
                componentNids.addAll(i.getComponentNidsForSaps(sapNids));
            }
        }

        return componentNids;
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException {
        return getConAttrs().getAnnotations();
    }

    @Override
    public final Set<I_ConceptAttributeTuple> getCommonConceptAttributeTuples(I_ConfigAceFrame config)
            throws IOException, TerminologyException {
        return ConflictHelper.getCommonConceptAttributeTuples(this, config);
    }

    @Override
    public final Set<I_DescriptionTuple> getCommonDescTuples(I_ConfigAceFrame config) throws IOException {
        return ConflictHelper.getCommonDescTuples(this, config);
    }

    @Override
    public final Set<I_RelTuple> getCommonRelTuples(I_ConfigAceFrame config)
            throws IOException, TerminologyException {
        return ConflictHelper.getCommonRelTuples(this, config);
    }

    public ComponentChronicleBI<?> getComponent(int nid) throws IOException {
        return data.getComponent(nid);
    }

    @Override
    public ConceptAttributes getConceptAttributes() throws IOException {
        return getConAttrs();
    }

    public Collection<ConceptAttributes.Version> getConceptAttrVersions(NidSetBI allowedStatus,
            PositionSetBI viewPositions, Precedence precedence, ContradictionManagerBI contradictionMgr)
            throws IOException {
        if (isCanceled()) {
            return new ArrayList<ConceptAttributes.Version>();
        }

        List<ConceptAttributes.Version> versions = new ArrayList<ConceptAttributes.Version>(2);

        versions.addAll(getConAttrs().getVersions(allowedStatus, viewPositions, precedence,
                contradictionMgr));

        return versions;
    }

    @Override
    public List<I_ConceptAttributeTuple> getConceptAttributeTuples(Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return getConceptAttributeTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(),
                precedencePolicy, contradictionManager);
    }

    @Override
    public List<I_ConceptAttributeTuple> getConceptAttributeTuples(NidSetBI allowedStatus,
            PositionSetBI positionSet, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException {
        List<I_ConceptAttributeTuple> returnTuples = new ArrayList<I_ConceptAttributeTuple>();
        ConceptAttributes attr = getConAttrs();

        if (attr != null) {
            attr.addTuples(allowedStatus, positionSet, returnTuples, precedencePolicy, contradictionManager);
        }

        return returnTuples;
    }

    @Override
    public List<I_ConceptAttributeTuple> getConceptAttributeTuples(NidSetBI allowedStatus,
            PositionSetBI positionSet, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager, long cuttoffTime)
            throws IOException, TerminologyException {
        List<I_ConceptAttributeTuple> returnTuples = new ArrayList<I_ConceptAttributeTuple>();
        ConceptAttributes attr = getConAttrs();

        if (attr != null) {
            attr.addTuples(allowedStatus, positionSet, returnTuples, precedencePolicy, contradictionManager,
                    cuttoffTime);
        }

        return returnTuples;
    }

    @Override
    public ConceptAttributes getConAttrs() throws IOException {
        if (data != null) {
            return data.getConceptAttributes();
        }

        return null;
    }

    public ArrayList<ConceptAttributes> getConceptAttributesList() throws IOException {
        ArrayList<ConceptAttributes> returnList = new ArrayList<ConceptAttributes>(1);

        returnList.add(getConAttrs());

        return returnList;
    }

    @Override
    public int getConceptNid() {
        return nid;
    }

    public Collection<Integer> getConceptNidsAffectedByCommit() throws IOException {
        return data.getConceptNidsAffectedByCommit();
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate vc)
            throws IOException {
        return getConAttrs().getAnnotationsActive(vc);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getAnnotationMembersActive(ViewCoordinate xyz, int refexNid)
            throws IOException {
        if (getConAttrs() != null) {
            return getConAttrs().getAnnotationMembersActive(xyz, refexNid);
        }

        return Collections.EMPTY_LIST;
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
    public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
            throws IOException {
        if (getConAttrs() != null) {
            return getConAttrs().getRefexMembersActive(xyz, refsetNid);
        }

        return new ArrayList<RefexVersionBI<?>>(0);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexesActive(ViewCoordinate xyz) throws IOException {
        if (getConAttrs() != null) {
            return getConAttrs().getRefexesActive(xyz);
        }

        return new ArrayList<RefexVersionBI<?>>(0);
    }

    @Override
    @Deprecated
    public Collection<? extends RefexVersionBI<?>> getActiveRefexes(ViewCoordinate xyz, int refsetNid)
            throws IOException {
        return getRefexMembersActive(xyz, refsetNid);
    }

    @Override
    public RefexVersionBI<?> getRefsetMemberActiveForComponent(ViewCoordinate vc, int componentNid)
            throws IOException {
        if (isCanceled()) {
            return null;
        }

        RefexChronicleBI<?> member = getRefsetMemberForComponent(componentNid);

        if (member != null) {
            for (RefexVersionBI version : member.getVersions(vc)) {
                return version;
            }

        }
        return null;
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefsetMembersActive(ViewCoordinate vc)
            throws IOException {
        Collection<? extends RefexChronicleBI<?>> refexes = getRefsetMembers();
        List<RefexVersionBI<?>> returnValues =
                new ArrayList<RefexVersionBI<?>>(refexes.size());

        for (RefexChronicleBI<?> refex : refexes) {
            for (RefexVersionBI<?> version : refex.getVersions(vc)) {
                returnValues.add(version);
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefsetMembersActive(ViewCoordinate vc, Long cuttoffTime)
            throws IOException {
        ConcurrentSkipListSet<RefsetMember<?, ?>> refsetMembers = getRefsetMembers();
        List<RefexVersionBI<?>> returnValues =
                new ArrayList<RefexVersionBI<?>>(refsetMembers.size());

        for (RefsetMember refex : refsetMembers) {
            for (Object o : refex.getVersions(vc, cuttoffTime)) {
                RefexVersionBI version = (RefexVersionBI) o;

                returnValues.add(version);
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    public I_ManageConceptData getData() {
        return data;
    }

    public long getDataVersion() {
        return data.getLastChange();
    }

    @Override
    public Object getDenotation(int authorityNid) throws IOException, TerminologyException {
        for (I_IdVersion part : getIdentifier().getIdVersions()) {
            if (part.getAuthorityNid() == authorityNid) {
                return part.getDenotation();
            }
        }

        return null;
    }

    @Override
    public Description.Version getDescTuple(NidListBI descTypePreferenceList, I_ConfigAceFrame config)
            throws IOException {
        return (Version) getDescTuple(descTypePreferenceList,
                new NidList(config.getLanguagePreferenceList().getListArray()),
                config.getAllowedStatus(), config.getViewPositionSetReadOnly(),
                config.getLanguageSortPref(), config.getPrecedence(),
                config.getConflictResolutionStrategy());
    }

    @Override
    public I_DescriptionTuple<DescriptionRevision> getDescTuple(NidListBI typePrefOrder,
            NidListBI langPrefOrder, NidSetBI allowedStatus, PositionSetBI positionSet,
            LANGUAGE_SORT_PREF sortPref, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager)
            throws IOException {
        I_DescriptionTuple<DescriptionRevision> result = null;

        switch (sortPref) {
            case LANG_B4_TYPE:
                result = getLangPreferredDesc(getDescriptionTuples(allowedStatus,
                        new NidSet(typePrefOrder.getListArray()), positionSet, precedencePolicy,
                        contradictionManager), typePrefOrder, langPrefOrder, allowedStatus, positionSet, true);

                if (result != null) {
                    return result;
                }

                return getDescTuple(typePrefOrder, langPrefOrder, allowedStatus, positionSet,
                        LANGUAGE_SORT_PREF.TYPE_B4_LANG, precedencePolicy, contradictionManager);

            case TYPE_B4_LANG:
                result = getTypePreferredDesc(getDescriptionTuples(allowedStatus,
                        new NidSet(typePrefOrder.getListArray()), positionSet, precedencePolicy,
                        contradictionManager), typePrefOrder, langPrefOrder, allowedStatus, positionSet, true);

                if (result != null) {
                    return result;
                }

                if ((getDescriptions() != null) && (getDescriptions().size() > 0)) {
                    return (I_DescriptionTuple<DescriptionRevision>) getDescriptions().iterator().next().getVersions().iterator().next();
                }

                return null;

            case LANG_REFEX:
                result = getRefexSpecifiedDesc(getDescriptionTuples(allowedStatus,
                        new NidSet(typePrefOrder.getListArray()), positionSet, precedencePolicy,
                        contradictionManager), typePrefOrder, langPrefOrder, allowedStatus, positionSet);

                if (result != null) {
                    return result;
                }

                return getDescTuple(typePrefOrder, langPrefOrder, allowedStatus, positionSet,
                        LANGUAGE_SORT_PREF.TYPE_B4_LANG, precedencePolicy, contradictionManager);

            case RF2_LANG_REFEX:
                result = getRf2RefexSpecifiedDesc(getDescriptionTuples(allowedStatus,
                        new NidSet(typePrefOrder.getListArray()), positionSet, precedencePolicy,
                        contradictionManager), typePrefOrder, langPrefOrder, allowedStatus, positionSet);

                if (result != null) {
                    return result;
                }

                return getDescTuple(typePrefOrder, langPrefOrder, allowedStatus, positionSet,
                        LANGUAGE_SORT_PREF.LANG_REFEX, precedencePolicy, contradictionManager);

            default:
                throw new IOException("Can't handle sort type: " + sortPref);
        }
    }

    public Description getDescription(int nid) throws IOException {
        if (isCanceled()) {
            return null;
        }

        for (Description d : getDescs()) {
            if (d.getNid() == nid) {
                return d;
            }
        }

        throw new IOException("No description: " + nid + " " + Ts.get().getUuidsForNid(nid) + " found in\n"
                + toLongString());
    }

    @Override
    public List<I_DescriptionTuple<DescriptionRevision>> getDescriptionTuples()
            throws IOException, TerminologyException {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return getDescriptionTuples(config.getAllowedStatus(), config.getDescTypes(),
                config.getViewPositionSetReadOnly(), config.getPrecedence(),
                config.getConflictResolutionStrategy());
    }

    @Override
    public List<I_DescriptionTuple<DescriptionRevision>> getDescriptionTuples(NidSetBI allowedStatus,
            NidSetBI allowedTypes, PositionSetBI positions, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager)
            throws IOException {
        List<I_DescriptionTuple<DescriptionRevision>> returnDescriptions =
                new ArrayList<I_DescriptionTuple<DescriptionRevision>>();

        for (Description desc : getDescs()) {
            desc.addTuples(allowedStatus, allowedTypes, positions, returnDescriptions, precedencePolicy,
                    contradictionManager);
        }

        return returnDescriptions;
    }

    @Override
    public List<I_DescriptionTuple<DescriptionRevision>> getDescriptionTuples(NidSetBI allowedStatus,
            NidSetBI allowedTypes, PositionSetBI positions, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager, long cuttoffTime)
            throws IOException {
        List<I_DescriptionTuple<DescriptionRevision>> returnDescriptions =
                new ArrayList<I_DescriptionTuple<DescriptionRevision>>();

        for (Description desc : getDescs()) {
            desc.addTuples(allowedStatus, allowedTypes, positions, returnDescriptions, precedencePolicy,
                    contradictionManager, cuttoffTime);
        }

        return returnDescriptions;
    }

    public Collection<Description.Version> getDescriptionVersions(NidSetBI allowedStatus,
            NidSetBI allowedTypes, PositionSetBI viewPositions, Precedence precedence,
            ContradictionManagerBI contradictionMgr)
            throws IOException {
        if (isCanceled()) {
            return new ConcurrentSkipListSet<Description.Version>(new ComponentComparator());
        }

        Collection<Description> descriptions = getDescs();
        List<Description.Version> versions = new ArrayList<Version>(descriptions.size());

        for (Description d : descriptions) {
            versions.addAll(d.getVersions(allowedStatus, allowedTypes, viewPositions, precedence,
                    contradictionMgr));
        }

        return versions;
    }

    @Override
    public Collection<Description> getDescs() throws IOException {
        if (isCanceled()) {
            return new ConcurrentSkipListSet<Description>(new ComponentComparator());
        }

        return data.getDescriptions();
    }

    @Override
    public Collection<Description> getDescriptions() throws IOException {
        return getDescs();
    }

    @Override
    public Relationship getDestRel(int relNid) throws IOException {
        return Bdb.getConceptForComponent(relNid).getRelationship(relNid);
    }

    @Deprecated
    @Override
    public Set<Concept> getDestRelOrigins(NidSetBI allowedTypes) throws IOException, TerminologyException {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return getDestRelOrigins(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(),
                config.getPrecedence(), config.getConflictResolutionStrategy());
    }

    @Override
    @Deprecated
    public Set<Concept> getDestRelOrigins(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException {
        Set<Concept> returnValues = new HashSet<Concept>();

        for (I_RelTuple rel
                : getDestRelTuples(allowedStatus, allowedTypes, positions, precedencePolicy,
                contradictionManager)) {
            returnValues.add(Bdb.getConceptDb().getConcept(rel.getC1Id()));
        }

        return returnValues;
    }

    @Override
    public List<? extends I_RelTuple> getDestRelTuples(NidSetBI allowedTypes, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return getSourceRelTuples(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(),
                precedencePolicy, contradictionManager);
    }

    @Override
    public List<I_RelTuple> getDestRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException {
        ViewCoordinate coordinate = new ViewCoordinate(precedencePolicy, positions, allowedStatus,
                allowedTypes, contradictionManager, Integer.MIN_VALUE,
                Terms.get().getActiveAceFrameConfig().getViewCoordinate().getClassifierNid(),
                Terms.get().getActiveAceFrameConfig().getViewCoordinate().getRelationshipAssertionType(),
                null, null);
        List<I_RelTuple> actualValues = new ArrayList<I_RelTuple>();

        for (Relationship rel : getDestRels(coordinate.getIsaTypeNids())) {
            for (Relationship.Version relv : rel.getVersions(coordinate)) {
                if (coordinate.getIsaTypeNids().contains(relv.getTypeNid())) {
                    actualValues.addAll(rel.getVersions(coordinate));
                }
            }
        }

        return actualValues;
    }

    @Override
    public List<Relationship.Version> getDestRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager,
            int classifierNid, RelAssertionType relAssertionType)
            throws IOException {
        ViewCoordinate coordinate = new ViewCoordinate(precedencePolicy, positions, allowedStatus,
                allowedTypes, contradictionManager, Integer.MIN_VALUE, classifierNid,
                relAssertionType, null, null);
        List<Relationship.Version> actualValues = new ArrayList<Relationship.Version>();

        for (Relationship rel : getDestRels(coordinate.getIsaTypeNids())) {
            for (Relationship.Version relv : rel.getVersions(coordinate)) {
                if (coordinate.getIsaTypeNids().contains(relv.getTypeNid())) {
                    actualValues.addAll(rel.getVersions(coordinate));
                }
            }
        }

        return actualValues;
    }

    @Override
    public Collection<Relationship> getDestRels() throws IOException {
        if (isCanceled()) {
            return new ArrayList<Relationship>();
        }

        return data.getDestRels();
    }

    public Collection<Relationship> getDestRels(NidSetBI allowedTypes) throws IOException {
        if (isCanceled()) {
            return new ArrayList<Relationship>();
        }

        return data.getDestRels(allowedTypes);
    }

    public ConceptCB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB {
        ConceptVersion cv = getVersion(vc);
        UUID[] uuidArray = new UUID[cv.getRelationshipsOutgoingTargetConceptsActiveIsa().size()];
        int index = 0;
        for (ConceptVersionBI parent : cv.getRelationshipsOutgoingTargetConceptsActiveIsa()) {
            uuidArray[index] = parent.getPrimUuid();
            index++;
        }
        ConceptCB cab = new ConceptCB(getVersion(vc), UUID.randomUUID());
        return cab;
    }

    @Override
    public Concept getEnclosingConcept() {
        return this;
    }

    public RefsetMember<?, ?> getExtension(int componentNid) throws IOException {
        if (isCanceled()) {
            return null;
        }

        return data.getRefsetMemberForComponent(componentNid);
    }

    @Override
    public Collection<RefsetMember<?, ?>> getExtensions() throws IOException {
        if (isCanceled()) {
            return new ArrayList<RefsetMember<?, ?>>();
        }

        return data.getRefsetMembers();
    }

    @Override
    public I_Identify getIdentifier() throws IOException {
        return getConAttrs();
    }

    public static Concept getIfInMap(int nid) {
        return conceptsCRHM.get(nid);
    }

    public Image getImage(int nid) throws IOException {
        if (isCanceled()) {
            return null;
        }

        for (Image i : data.getImages()) {
            if (i.getNid() == nid) {
                return i;
            }
        }

        return null;
    }

    @Override
    public List<I_ImageTuple> getImageTuples() throws IOException, TerminologyException {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return getImageTuples(config.getAllowedStatus(), null, config.getViewPositionSetReadOnly(),
                config.getPrecedence(), config.getConflictResolutionStrategy());
    }

    @Override
    public List<I_ImageTuple> getImageTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws IOException {
        List<I_ImageTuple> returnTuples = new ArrayList<I_ImageTuple>();

        for (I_ImageVersioned img : getImages()) {
            img.addTuples(allowedStatus, allowedTypes, positions, returnTuples, precedencePolicy,
                    contradictionManager);
        }

        return returnTuples;
    }

    @Override
    public Collection<Image> getImages() throws IOException {
        return data.getImages();
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getRefexesInactive(ViewCoordinate xyz) throws IOException {
        return getConAttrs().getRefexesInactive(xyz);
    }

    @Override
    public String getInitialText() throws IOException {
        if (isCanceled()) {
            return "canceled";
        }

        try {
            if ((AceConfig.config != null) && (AceConfig.config.aceFrames.get(0) != null)) {
                if (AceConfig.config.aceFrames.get(0).getViewPositionSet().iterator().hasNext()) {
                    I_DescriptionTuple tuple =
                            this.getDescTuple(AceConfig.config.aceFrames.get(0).getShortLabelDescPreferenceList(),
                            AceConfig.config.getAceFrames().get(0));

                    if (tuple != null) {
                        return tuple.getText();
                    }

                } else {
                    throw new IndexOutOfBoundsException("No view positions set");
                }
            }

            return getText();
        } catch (IndexOutOfBoundsException e) {
            try {
                return getText();
            } catch (IndexOutOfBoundsException e2) {
                return nid + " has no desc";
            }
        }
    }

    private I_DescriptionTuple getLangPreferredDesc(
            Collection<I_DescriptionTuple<DescriptionRevision>> descriptions, NidListBI typePrefOrder,
            NidListBI langPrefOrder, NidSetBI allowedStatus, PositionSetBI positionSet, boolean tryType)
            throws IOException, ToIoException {
        if (descriptions.size() > 0) {
            if (descriptions.size() > 1) {
                List<I_DescriptionTuple<DescriptionRevision>> matchedList =
                        new ArrayList<I_DescriptionTuple<DescriptionRevision>>();

                if ((langPrefOrder != null) && (langPrefOrder.getListValues() != null)) {
                    for (int langId : langPrefOrder.getListValues()) {
                        for (I_DescriptionTuple d : descriptions) {
                            try {
                                int tupleLangId =
                                        ArchitectonicAuxiliary.getLanguageConcept(d.getLang()).localize().getNid();

                                if (tupleLangId == langId) {
                                    matchedList.add(d);

                                    if (matchedList.size() == 2) {
                                        break;
                                    }
                                }
                            } catch (TerminologyException e) {
                                throw new ToIoException(e);
                            }
                        }

                        if (matchedList.size() > 0) {
                            if (matchedList.size() == 1) {
                                return matchedList.get(0);
                            }

                            if (tryType) {
                                return getTypePreferredDesc(matchedList, typePrefOrder, langPrefOrder, allowedStatus,
                                        positionSet, false);
                            } else {
                                return matchedList.get(0);
                            }
                        }
                    }
                }

                return descriptions.iterator().next();
            } else {
                return descriptions.iterator().next();
            }
        }

        return null;
    }

    @Override
    public long getLastModificationSequence() {
        return data.getLastChange();
    }

    @Override
    public Collection<Image> getMedia() throws IOException {
        return getImages();
    }

    public Collection<Image.Version> getMediaVersions(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI viewPositions, Precedence precedence, ContradictionManagerBI contradictionMgr)
            throws IOException {
        if (isCanceled()) {
            return new ConcurrentSkipListSet<Image.Version>(new ComponentComparator());
        }

        Collection<Image> media = getImages();
        List<Image.Version> versions = new ArrayList<Image.Version>(media.size());

        for (Image m : media) {
            versions.addAll(m.getVersions(allowedStatus, allowedTypes, viewPositions, precedence,
                    contradictionMgr));
        }

        return versions;
    }

    public Collection<Relationship> getNativeSourceRels() throws IOException {
        if (isCanceled()) {
            return new ConcurrentSkipListSet<Relationship>(new ComponentComparator());
        }

        return data.getSourceRels();
    }

    @Override
    public int getNid() {
        return nid;
    }

    @Override
    public Set<PositionBI> getPositions() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public I_RepresentIdSet getPossibleChildOfConcepts(I_ConfigAceFrame config) throws IOException, ContradictionException {
        NidBitSetBI childNidSet = Ts.get().getEmptyNidSet();
        int[] childrenConceptNids = Bdb.getNidCNidMap().getChildrenConceptNids(nid, config.getViewCoordinate());
        for (int childNid : childrenConceptNids) {
            childNidSet.setMember(childNid);
        }
        return (I_RepresentIdSet) childNidSet;
    }

    /**
     *
     * @param relTypes
     * @return
     * @throws IOException
     * @deprecated -- use get dest rel nids instead
     */
    @Deprecated
    public Set<Integer> getPossibleDestRelsOfTypes(NidSetBI relTypes) throws IOException {
        return new HashSet(Arrays.asList(Bdb.nidCidMapDb.getDestRelNids(nid, relTypes)));
    }

    @Override
    public NidBitSetBI getPossibleKindOfConcepts(I_ConfigAceFrame config)
            throws IOException, ContradictionException {
        return getPossibleKindOfConcepts(config.getViewCoordinate());
    }

    public NidBitSetBI getPossibleKindOfConcepts(ViewCoordinate vc)
            throws IOException, ContradictionException {
        return collectPossibleKindOf(vc, nid);
    }

    private NidBitSetBI collectPossibleKindOf(ViewCoordinate vc, int cNid)
            throws IOException, ContradictionException {
        return Bdb.getNidCNidMap().getKindOfNids(cNid, vc);
    }

    private I_DescriptionTuple getPreferredAcceptability(
            Collection<I_DescriptionTuple<DescriptionRevision>> descriptions, int typePrefNid,
            ViewCoordinate vc, int langRefexNid)
            throws IOException {

        // get FSN
        I_DescriptionTuple descOfType = null;

        for (I_DescriptionTuple d : descriptions) {
            if (d.getTypeNid() == typePrefNid) {
                for (RefexVersionBI<?> refex : d.getRefexesActive(vc)) {
                    if (refex.getRefexNid() == langRefexNid) {
                        RefexNidVersionBI<?> langRefex = (RefexNidVersionBI<?>) refex;

                        if ((langRefex.getNid1() == ReferenceConcepts.PREFERRED_ACCEPTABILITY_RF1.getNid())
                                || (langRefex.getNid1()
                                == ReferenceConcepts.PREFERRED_ACCEPTABILITY_RF2.getNid())) {
                            return d;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public UUID getPrimUuid() {
        try {
            if (getConAttrs() != null) {
                return getConAttrs().getPrimUuid();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return UUID.fromString("00000000-0000-0000-C000-000000000046");
    }

    @Override
    public ConceptVersionBI getPrimordialVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
        return getRefexes(refsetNid);
    }

    private I_DescriptionTuple getRefexSpecifiedDesc(
            Collection<I_DescriptionTuple<DescriptionRevision>> descriptions, NidListBI typePrefOrder,
            NidListBI langRefexOrder, NidSetBI allowedStatus, PositionSetBI positionSet)
            throws IOException, ToIoException {
        ViewCoordinate vc = new ViewCoordinate(Precedence.PATH, positionSet, allowedStatus, null,
                new IdentifyAllContradictionStrategy(), Integer.MIN_VALUE, Integer.MIN_VALUE,
                RelAssertionType.STATED, langRefexOrder, LANGUAGE_SORT.LANG_REFEX);

        if (descriptions.size() > 0) {
            if (descriptions.size() > 1) {
                for (int typePrefNid : typePrefOrder.getListArray()) {
                    if ((langRefexOrder != null) && (langRefexOrder.getListValues() != null)) {
                        for (int langRefexNid : langRefexOrder.getListValues()) {
                            if (typePrefNid == ReferenceConcepts.FULLY_SPECIFIED_RF1.getNid()) {
                                I_DescriptionTuple answer = getPreferredAcceptability(descriptions, typePrefNid, vc,
                                        langRefexNid);

                                if (answer != null) {
                                    return answer;
                                }
                            } else {

                                // get Preferred or other
                                I_DescriptionTuple answer = getPreferredAcceptability(descriptions,
                                        ReferenceConcepts.SYNONYM_RF1.getNid(), vc,
                                        langRefexNid);

                                if (answer != null) {
                                    return answer;
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
        return getConAttrs().getRefexes();
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid) throws IOException {
        return getConAttrs().getRefexes(refsetNid);
    }

    public RefsetMember<?, ?> getRefsetMember(int memberNid) throws IOException {
        return data.getRefsetMember(memberNid);
    }

    @Override
    public RefsetMember<?, ?> getRefsetMemberForComponent(int componentNid) throws IOException {
        if (isCanceled()) {
            return null;
        }

        return data.getRefsetMemberForComponent(componentNid);
    }

    @Override
    public ConcurrentSkipListSet<RefsetMember<?, ?>> getRefsetMembers() throws IOException {
        return data.getRefsetMembers();
    }

    @Override
    public Collection<? extends RelationshipGroupVersionBI> getRelationshipOutgoingGroups(ViewCoordinate vc) throws IOException {
        ArrayList<RelationshipGroupVersionBI> results = new ArrayList<RelationshipGroupVersionBI>();

        if (vc.getRelationshipAssertionType() == RelAssertionType.INFERRED_THEN_STATED) {
            ViewCoordinate tempVc = new ViewCoordinate(vc);

            tempVc.setRelationshipAssertionType(RelAssertionType.STATED);
            getRelGroups(tempVc, results);
            tempVc.setRelationshipAssertionType(RelAssertionType.INFERRED);
            getRelGroups(tempVc, results);
        } else {
            getRelGroups(vc, results);
        }

        return results;
    }

    private void getRelGroups(ViewCoordinate vc, ArrayList<RelationshipGroupVersionBI> results) throws IOException {
        Map<Integer, HashSet<RelationshipChronicleBI>> groupMap = new HashMap<Integer, HashSet<RelationshipChronicleBI>>();
        ViewCoordinate tempVc = new ViewCoordinate(vc);

        tempVc.setAllowedStatusNids(null);

        for (RelationshipChronicleBI r : getRelationshipsOutgoing()) {
            for (RelationshipVersionBI rv : r.getVersions(tempVc)) {
                int group = rv.getGroup();

                if (group > 0) {
                    HashSet<RelationshipChronicleBI> relsInGroup = groupMap.get(group);

                    if (relsInGroup == null) {
                        relsInGroup = new HashSet<RelationshipChronicleBI>();
                        groupMap.put(group, relsInGroup);
                    }

                    relsInGroup.add(r);
                }
            }
        }

        for (Entry<Integer, HashSet<RelationshipChronicleBI>> groupEntry : groupMap.entrySet()) {
            results.add(new RelGroupVersion(new RelGroupChronicle(this, groupEntry.getKey(),
                    groupEntry.getValue()), vc));
        }
    }

    public Relationship getRelationship(int relNid) throws IOException {
        for (Relationship r : getNativeSourceRels()) {
            if (r.getNid() == relNid) {
                return r;
            }
        }

        return null;
    }

    @Override
    public Collection<? extends RelationshipChronicleBI> getRelationshipsIncoming() throws IOException {
        return getDestRels();
    }

    @Override
    public Collection<? extends RelationshipChronicleBI> getRelationshipsOutgoing() throws IOException {
        return getSourceRels();
    }

    private I_DescriptionTuple getRf2RefexSpecifiedDesc(
            Collection<I_DescriptionTuple<DescriptionRevision>> descriptions, NidListBI typePrefOrder,
            NidListBI langRefexOrder, NidSetBI allowedStatus, PositionSetBI positionSet)
            throws IOException, ToIoException {
        ViewCoordinate vc = new ViewCoordinate(Precedence.PATH, positionSet, allowedStatus, null,
                new IdentifyAllContradictionStrategy(), Integer.MIN_VALUE, Integer.MIN_VALUE,
                RelAssertionType.STATED, langRefexOrder, LANGUAGE_SORT.RF2_LANG_REFEX);

        if (descriptions.size() > 0) {
            if (descriptions.size() > 1) {
                if ((langRefexOrder != null) && (langRefexOrder.getListValues() != null)) {
                    for (int langRefexNid : langRefexOrder.getListValues()) {
                        for (int typePrefNid : typePrefOrder.getListArray()) {
                            if (typePrefNid == ReferenceConcepts.FULLY_SPECIFIED_RF2.getNid()) {
                                I_DescriptionTuple answer = getPreferredAcceptability(descriptions, typePrefNid, vc,
                                        langRefexNid);

                                if (answer != null) {
                                    return answer;
                                }
                            } else if (typePrefNid == ReferenceConcepts.SYNONYM_RF2.getNid()) {

                                // get Preferred or other
                                I_DescriptionTuple answer = getPreferredAcceptability(descriptions,
                                        ReferenceConcepts.SYNONYM_RF2.getNid(), vc,
                                        langRefexNid);

                                if (answer != null) {
                                    return answer;
                                }
                            }
                        }
                    }
                }

                if ((langRefexOrder != null) && (langRefexOrder.getListValues() != null)) {
                    for (int langRefexNid : langRefexOrder.getListValues()) {
                        for (int typePrefNid : typePrefOrder.getListArray()) {
                            if (typePrefNid == ReferenceConcepts.FULLY_SPECIFIED_RF1.getNid()) {
                                I_DescriptionTuple answer = getPreferredAcceptability(descriptions, typePrefNid, vc,
                                        langRefexNid);

                                if (answer != null) {
                                    return answer;
                                }
                            } else if (typePrefNid == ReferenceConcepts.SYNONYM_RF1.getNid()) {

                                // get Preferred or other
                                I_DescriptionTuple answer = getPreferredAcceptability(descriptions,
                                        ReferenceConcepts.SYNONYM_RF1.getNid(), vc,
                                        langRefexNid);

                                if (answer != null) {
                                    return answer;
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Relationship getSourceRel(int relNid) throws IOException {
        return getRelationship(relNid);
    }

    @Override
    public Set<I_GetConceptData> getSourceRelTargets(NidSetBI allowedTypes, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return getSourceRelTargets(config.getAllowedStatus(), allowedTypes,
                config.getViewPositionSetReadOnly(), precedencePolicy, contradictionManager);
    }

    @Override
    public Set<I_GetConceptData> getSourceRelTargets(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionMgr)
            throws IOException, TerminologyException {
        Set<I_GetConceptData> returnValues = new HashSet<I_GetConceptData>();

        for (I_RelTuple rel
                : getSourceRelTuples(allowedStatus, allowedTypes, positions, precedencePolicy,
                contradictionMgr)) {
            returnValues.add(Concept.get(rel.getC2Id()));
        }

        return returnValues;
    }

    public List<? extends I_RelTuple> getSourceRelTuples(NidSetBI allowedTypes, boolean addUncommitted,
            boolean returnConflictResolvedLatestState)
            throws IOException, TerminologyException {
        throw new UnsupportedOperationException(
                "Use a method that does not require getting the 'active' config");
    }

    @Override
    public List<? extends I_RelTuple> getSourceRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException {
        List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();

        for (I_RelVersioned rel : getSourceRels()) {
            rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, precedencePolicy,
                    contradictionManager);
        }

        return returnRels;
    }

    @Override
    public List<? extends I_RelTuple> getSourceRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager,
            Long cutoffTime)
            throws IOException, TerminologyException {
        List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();

        for (I_RelVersioned rel : getSourceRels()) {
            rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, precedencePolicy,
                    contradictionManager, cutoffTime);
        }

        return returnRels;
    }

    @Override
    public List<? extends I_RelTuple> getSourceRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager,
            int classifierNid, RelAssertionType relAssertionType, Long cutoffTime)
            throws IOException, TerminologyException {
        ViewCoordinate coordinate = new ViewCoordinate(precedencePolicy, positions, allowedStatus,
                allowedTypes, contradictionManager, Integer.MIN_VALUE, classifierNid,
                relAssertionType, null, null);
        List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();

        for (Relationship rel : getSourceRels()) {
            for (Relationship.Version rv : rel.getVersions(coordinate)) {
                rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, precedencePolicy,
                        contradictionManager, cutoffTime);
            }
        }

        return returnRels;
    }

    @Override
    public List<? extends I_RelTuple> getSourceRelTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy, ContradictionManagerBI contradictionManager,
            int classifierNid, RelAssertionType relAssertionType)
            throws IOException, TerminologyException {
        ViewCoordinate coordinate = new ViewCoordinate(precedencePolicy, positions, allowedStatus,
                allowedTypes, contradictionManager, Integer.MIN_VALUE, classifierNid,
                relAssertionType, null, null);
        List<Relationship.Version> actualValues = new ArrayList<Relationship.Version>();

        for (Relationship rel : getSourceRels()) {
            for (Relationship.Version rv : rel.getVersions(coordinate)) {
                if ((allowedTypes == null) || allowedTypes.contains(rv.getTypeNid())) {
                    actualValues.add(rv);
                }
            }
        }

        return actualValues;
    }

    @Override
    public Collection<Relationship> getSourceRels() throws IOException {
        return getNativeSourceRels();
    }

    public Collection<Relationship.Version> getSrcRelVersions(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI viewPositions, Precedence precedence, ContradictionManagerBI contradictionMgr)
            throws IOException {
        if (isCanceled()) {
            return new ArrayList<Relationship.Version>();
        }

        Collection<Relationship> rels = getNativeSourceRels();
        List<Relationship.Version> versions = new ArrayList<Relationship.Version>(rels.size());

        for (Relationship r : rels) {
            versions.addAll(r.getVersions(allowedStatus, allowedTypes, viewPositions, precedence,
                    contradictionMgr));
        }

        return versions;
    }

    /**
     * This method is for creating temporary concepts for unit testing only...
     *
     * @param eConcept
     * @return
     * @throws IOException
     */
    public static Concept getTempConcept(EConcept eConcept) throws IOException {
        int conceptNid = Bdb.uuidToNid(eConcept.getConceptAttributes().getPrimordialComponentUuid());

        assert conceptNid != Integer.MAX_VALUE : "no conceptNid for uuids";

        return populateFromEConcept(eConcept, new Concept(conceptNid));
    }

    private String getText() {
        try {
            if (getDescs().size() > 0) {
                return getDescs().iterator().next().getFirstTuple().getText();
            }
        } catch (IOException ex) {
            AceLog.getAppLog().nonModalAlertAndLogException(ex);
        }

        List<I_DescriptionVersioned> localDesc = getUncommittedDescriptions();

        if (localDesc.isEmpty()) {
            try {
                if (fsDescNid == Integer.MIN_VALUE) {
                    fsDescNid = Terms.get().uuidToNative(
                            ArchitectonicAuxiliary.Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids());
                    fsDescNid = Terms.get().uuidToNative(
                            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
                }

                if (getDescs().size() > 0) {
                    I_DescriptionVersioned desc = getDescs().iterator().next();

                    for (I_DescriptionVersioned<?> d : getDescs()) {
                        for (I_DescriptionPart part : d.getMutableParts()) {
                            if ((part.getTypeNid() == fsDescNid) || (part.getTypeNid() == fsXmlDescNid)) {
                                return part.getText();
                            }
                        }
                    }

                    return desc.getFirstTuple().getText();
                } else {
                    int sequence = nid + Integer.MIN_VALUE;
                    String errString = nid + " (" + sequence + ") " + " has no descriptions " + getUids();

                    getDescs();

                    return errString;
                }
            } catch (Exception ex) {
                AceLog.getAppLog().nonModalAlertAndLogException(ex);
            }
        }

        I_DescriptionVersioned tdv = localDesc.get(0);
        List<? extends I_DescriptionPart> versions = tdv.getMutableParts();
        I_DescriptionPart first = versions.get(0);

        return first.getText();
    }

    private I_DescriptionTuple getTypePreferredDesc(
            Collection<I_DescriptionTuple<DescriptionRevision>> descriptions, NidListBI typePrefOrder,
            NidListBI langPrefOrder, NidSetBI allowedStatus, PositionSetBI positionSet, boolean tryLang)
            throws IOException, ToIoException {
        if (descriptions.size() > 0) {
            if (descriptions.size() > 1) {
                List<I_DescriptionTuple<DescriptionRevision>> matchedList =
                        new ArrayList<I_DescriptionTuple<DescriptionRevision>>();

                for (int typeId : typePrefOrder.getListValues()) {
                    for (I_DescriptionTuple d : descriptions) {
                        if (d.getTypeNid() == typeId) {
                            matchedList.add(d);

                            if (matchedList.size() == 2) {
                                break;
                            }
                        }
                    }

                    if (matchedList.size() > 0) {
                        if (matchedList.size() == 1) {
                            return matchedList.get(0);
                        }

                        if (tryLang) {
                            return getLangPreferredDesc(matchedList, typePrefOrder, langPrefOrder, allowedStatus,
                                    positionSet, false);
                        } else {
                            return matchedList.get(0);
                        }
                    }
                }

                return descriptions.iterator().next();
            } else {
                return descriptions.iterator().next();
            }
        }

        return null;
    }

    @Override
    public List<UUID> getUUIDs() {
        try {
            if (getConAttrs() != null) {
                return getConAttrs().getUUIDs();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ArrayList<UUID>();
    }

    @Deprecated
    @Override
    public List<UUID> getUids() throws IOException {
        if (getConAttrs() != null) {
            return getConAttrs().getUUIDs();
        }

        return new ArrayList<UUID>();
    }

    public List<UUID> getUidsForComponent(int componentNid) throws IOException {
        if (getComponent(componentNid) != null) {
            return getComponent(componentNid).getUUIDs();
        }

        AceLog.getAppLog().alertAndLogException(new Exception("Null component: " + componentNid
                + " for concept: " + this.toLongString()));

        return new ArrayList<UUID>();
    }

    public ConceptAttributes getUncommittedConceptAttributes() {
        return null;
    }

    public List<I_DescriptionVersioned> getUncommittedDescriptions() {
        return Collections.unmodifiableList(new ArrayList<I_DescriptionVersioned>());
    }

    @Override
    public List<I_Identify> getUncommittedIdVersioned() {
        return Collections.unmodifiableList(new ArrayList<I_Identify>());
    }

    @Override
    public NidSetBI getUncommittedIds() {
        return new IntSet();
    }

    public List<I_ImageVersioned> getUncommittedImages() {
        return Collections.unmodifiableList(new ArrayList<I_ImageVersioned>());
    }

    public NidListBI getUncommittedNids() {
        return data.getUncommittedNids();
    }

    public List<I_RelVersioned> getUncommittedSourceRels() {
        return Collections.unmodifiableList(new ArrayList<I_RelVersioned>());
    }

    @Override
    public UniversalAceBean getUniversalAceBean() throws IOException, TerminologyException {
        UniversalAceBean uab = new UniversalAceBean();

        uab.setIdentifier(getIdentifier().getUniversalId());
        uab.setConceptAttributes(getConAttrs().getUniversal());

        for (I_DescriptionVersioned desc : getDescs()) {
            uab.getDescriptions().add(desc.getUniversal());
        }

        for (I_RelVersioned rel : getSourceRels()) {
            uab.getSourceRels().add(rel.getUniversal());
        }

        for (I_ImageVersioned image : getImages()) {
            uab.getImages().add(image.getUniversal());
        }

        return uab;
    }

    @Override
    public ConceptVersion getVersion(ViewCoordinate c) {
        return new ConceptVersion(this, c);
    }

    @Override
    public Collection<? extends ConceptVersionBI> getVersions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<ConceptVersion> getVersions(ViewCoordinate c) {
        ArrayList<ConceptVersion> cvList = new ArrayList<ConceptVersion>(1);

        cvList.add(new ConceptVersion(this, c));

        return cvList;
    }

    @Override
    public FoundContradictionVersions getVersionsInContradiction(ViewCoordinate vc) {
        try {
            ContradictionIdentifier identifier = new ContradictionIdentifier(vc, true);
            ContradictionResult result = identifier.isConceptInConflict(this);

            return new FoundContradictionVersions(result, identifier.getContradictingVersions());
        } catch (Exception e) {
            return null;
        }
    }

    private long getWriteVersion() {
        return data.getLastWrite();
    }

    @Override
    public boolean hasAnnotationMemberActive(ViewCoordinate xyz, int refexNid) throws IOException {
        if (getConAttrs() != null) {
            return getConAttrs().hasAnnotationMemberActive(xyz, refexNid);
        }

        return false;
    }

    @Override
    public boolean hasRefexMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
        if (getConAttrs() != null) {
            return getConAttrs().hasRefexMemberActive(xyz, refsetNid);
        }

        return false;
    }

    @Override
    public boolean hasRefsetMemberActiveForComponent(ViewCoordinate vc, int componentNid) throws IOException {
        if (isCanceled()) {
            return false;
        }

        RefsetMember<?, ?> member = getRefsetMemberForComponent(componentNid);

        if (member != null) {
            for (RefexVersionBI v : member.getVersions(vc)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasExtensionsForComponent(int nid) throws IOException {
        List<NidPairForRefex> refsetPairs = Bdb.getRefsetPairs(nid);

        if ((refsetPairs != null) && (refsetPairs.size() > 0)) {
            return true;
        }

        return false;
    }

    public boolean hasMediaExtensions() throws IOException {
        if ((data.getImageNids() == null) || data.getImageNids().isEmpty()) {
            return false;
        }

        for (int imageNid : data.getImageNids()) {
            if (hasExtensionsForComponent(imageNid)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isAnnotationIndex() throws IOException {
        return data.isAnnotationIndex();
    }

    @Override
    public boolean isAnnotationStyleRefex() throws IOException {
        return data.isAnnotationStyleRefex();
    }

    @Override
    public boolean isCanceled() throws IOException {
        if (!canceled) {
            if ((getConceptAttributes() != null) && (getConceptAttributes().getPrimordialVersion().getTime() == Long.MIN_VALUE)) {
                canceled = true;
            }
        }

        return canceled;
    }

    @Override
    public boolean isLeaf(I_ConfigAceFrame aceConfig, boolean addUncommitted) throws IOException {
        NidSetBI srcRelTypes = aceConfig.getSourceRelTypes();

        if (srcRelTypes.size() > 0) {
            for (Relationship r : getSourceRels()) {
                List<I_RelTuple> currentVersions = new ArrayList<I_RelTuple>();

                r.addTuples(aceConfig.getAllowedStatus(), srcRelTypes, aceConfig.getViewPositionSetReadOnly(),
                        currentVersions, aceConfig.getPrecedence(),
                        aceConfig.getConflictResolutionStrategy());

                if (currentVersions.size() > 0) {
                    return false;
                }
            }
        }
        if (Bdb.getNidCNidMap().getDestRelNids(this.nid, srcRelTypes).length == 0) {
            return true;
        }
        return false;
    }

    public boolean isParentOf(Concept child, ViewCoordinate vc) throws IOException, ContradictionException {
        return Ts.get().isKindOf(child.nid, nid, vc);
    }

    public boolean isParentOfOrEqualTo(Concept child, ViewCoordinate vc)
            throws IOException, ContradictionException {
        if (child == this) {
            return true;
        }

        return isParentOf(child, vc);
    }

    @Override
    public boolean isParentOf(I_GetConceptData child) throws IOException, TerminologyException, ContradictionException {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return isParentOf(child, config.getAllowedStatus(), config.getDestRelTypes(),
                config.getViewPositionSetReadOnly(), config.getPrecedence(),
                config.getConflictResolutionStrategy());
    }

    /**
     *
     * @param child
     * @param allowedStatus
     * @param allowedTypes
     * @param positions
     * @param precedencePolicy
     * @param contradictionManager
     * @return
     * @throws IOException
     * @throws TerminologyException
     * @deprecated -- use Ts.get().isKindOf(childNid, parentNid, viewCoordinate)
     */
    @Deprecated
    @Override
    public boolean isParentOf(I_GetConceptData child, NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException, ContradictionException {
        ViewCoordinate originalViewCoordinate = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
        ViewCoordinate viewCoordinate = new ViewCoordinate(originalViewCoordinate);
        viewCoordinate.setAllowedStatusNids(allowedStatus);
        viewCoordinate.setIsaTypeNids(allowedTypes);
        viewCoordinate.setPositionSet(positions);
        viewCoordinate.setContradictionManager(contradictionManager);
        for (PositionBI p : positions) {
            return Bdb.getNidCNidMap().isKindOf(child.getConceptNid(),
                    this.nid, viewCoordinate);
        }
        return false;
    }

    @Override
    public boolean isParentOfOrEqualTo(I_GetConceptData child) throws
            IOException, TerminologyException, ContradictionException {
        if (child == this) {
            return true;
        }

        return isParentOf(child);
    }

    @Override
    public boolean isParentOfOrEqualTo(I_GetConceptData child, NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager)
            throws IOException, TerminologyException, ContradictionException {
        if (child == this) {
            return true;
        }

        return isParentOf(child, allowedStatus, allowedTypes, positions, precedencePolicy,
                contradictionManager);
    }

    @Override
    public boolean isUncommitted() {
        return data.isUncommitted();
    }

    public boolean isUnwritten() {
        return data.isUnwritten();
    }

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setAnnotationIndex(boolean annotationIndex) throws IOException {
        data.setAnnotationIndex(annotationIndex);
    }

    @Override
    public void setAnnotationStyleRefex(boolean annotationStyleRefset) {
        data.setAnnotationStyleRefset(annotationStyleRefset);
    }

    private static void setAttributesFromEConcept(Concept c, TkConceptAttributes eAttr) throws IOException {
        assert eAttr != null;

        ConceptAttributes attr = new ConceptAttributes(eAttr, c);

        c.data.set(attr);
    }

    public NidSetBI setCommitTime(long time) {
        return data.setCommitTime(time);
    }

    public void setConceptAttributes(ConceptAttributes attributes) throws IOException {
        assert attributes.nid != 0;
        nid = attributes.nid;
        data.set(attributes);
    }

    private static void setDescriptionsFromEConcept(TkConcept eConcept, Concept c) throws IOException {
        for (TkDescription eDesc : eConcept.getDescriptions()) {
            Description desc = new Description(eDesc, c);

            c.data.add(desc);
        }
    }

    private static void setImagesFromEConcept(EConcept eConcept, Concept c) throws IOException {
        for (TkMedia eImage : eConcept.getImages()) {
            Image img = new Image(eImage, c);

            c.data.add(img);
        }
    }

    public void setIsCanceled(boolean isCanceled) {
        canceled = isCanceled;
    }

    public void setLastWrite(long version) {
        data.setLastWrite(version);
    }

    private static void setRefsetMembersFromEConcept(EConcept eConcept, Concept c) throws IOException {
        for (TkRefexAbstractMember<?> eRefsetMember : eConcept.getRefsetMembers()) {
            RefsetMember<?, ?> refsetMember = RefsetMemberFactory.create(eRefsetMember, c.getConceptNid());
            ChangeNotifier.touchRefexRC(refsetMember.getReferencedComponentNid());

            c.data.add(refsetMember);
        }
    }

    private static void setRelationshipsFromEConcept(EConcept eConcept, Concept c) throws IOException {
        for (TkRelationship eRel : eConcept.getRelationships()) {
            Relationship r = new Relationship(eRel, c);
            ChangeNotifier.touchComponent(r.nid);
            ChangeNotifier.touch(r.getSourceNid(), ChangeNotifier.Change.REL_ORIGIN);
            ChangeNotifier.touch(r.getTargetNid(), ChangeNotifier.Change.REL_XREF);

            c.data.add(r);
        }
    }

    //~--- inner classes -------------------------------------------------------
    public static class ConceptLowMemoryListener implements LowMemoryListener {

        @Override
        public void memoryUsageLow(long usedMemory, long maxMemory) {
            double percentageUsed = ((double) usedMemory) / maxMemory;

            AceLog.getAppLog().warning("Memory low. Percent used: " + percentageUsed
                    + " Concept trying to recover memory by dieting concepts. ");
            new Thread(new Diet(maxMemory), "Diet").start();
        }
    }

    public static class Diet implements Runnable {

        long maxMemory;

        //~--- constructors -----------------------------------------------------
        public Diet(long maxMemory) {
            this.maxMemory = maxMemory;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public void run() {
            System.gc();

            double usedMemory = maxMemory - Runtime.getRuntime().freeMemory();
            double percentageUsed = ((double) usedMemory) / maxMemory;

            if (percentageUsed > 0.85) {
                for (int cNid : conceptsCRHM.keySet()) {
                    Concept c = conceptsCRHM.get(cNid);

                    if (c != null) {
                        c.diet();
                    }
                }

                usedMemory = maxMemory - Runtime.getRuntime().freeMemory();
                percentageUsed = ((double) usedMemory) / maxMemory;

                if (percentageUsed > 0.85) {
                    usedMemory = maxMemory - Runtime.getRuntime().freeMemory();
                    percentageUsed = ((double) usedMemory) / maxMemory;
                    AceLog.getAppLog().info("Concept Diet + KindOfComputer.trimCache() finished recover memory. "
                            + "Percent used: " + percentageUsed);
                } else {
                    AceLog.getAppLog().info("Concept Diet finished recover memory. " + "Percent used: "
                            + percentageUsed);
                }
            } else {
                usedMemory = maxMemory - Runtime.getRuntime().freeMemory();
                percentageUsed = ((double) usedMemory) / maxMemory;
                AceLog.getAppLog().info("GC ONLY Diet finished recover memory. " + "Percent used: "
                        + percentageUsed);
            }
        }
    }

    public static List<TkRefexAbstractMember<?>> getUnresolvedAnnotations() {
        return unresolvedAnnotations;
    }
}
