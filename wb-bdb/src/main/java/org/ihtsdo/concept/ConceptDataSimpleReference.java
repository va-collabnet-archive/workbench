package org.ihtsdo.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.ConceptComponentBinder;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.description.DescriptionBinder;
import org.ihtsdo.concept.component.identifier.IdentifierVersion;
import org.ihtsdo.concept.component.image.Image;
import org.ihtsdo.concept.component.image.ImageBinder;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetMemberBinder;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.concept.component.relationship.RelationshipBinder;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.I_GetNidData;
import org.ihtsdo.db.bdb.NidDataFromBdb;
import org.ihtsdo.db.bdb.NidDataInMemory;
import org.ihtsdo.db.util.NidPairForRel;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;

import com.sleepycat.bind.tuple.TupleInput;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;

public class ConceptDataSimpleReference extends ConceptDataManager {

    private Boolean annotationStyleRefset;
    private AtomicReference<ConceptAttributes> attributes = new AtomicReference<ConceptAttributes>();
    private AtomicReference<AddSrcRelSet> srcRels = new AtomicReference<AddSrcRelSet>();
    private AtomicReference<AddDescriptionSet> descriptions = new AtomicReference<AddDescriptionSet>();
    private AtomicReference<AddImageSet> images = new AtomicReference<AddImageSet>();
    private AtomicReference<AddMemberSet> refsetMembers = new AtomicReference<AddMemberSet>();
    private AtomicReference<ConcurrentSkipListSet<Integer>> descNids =
            new AtomicReference<ConcurrentSkipListSet<Integer>>();
    private AtomicReference<ConcurrentSkipListSet<Integer>> srcRelNids =
            new AtomicReference<ConcurrentSkipListSet<Integer>>();
    private AtomicReference<ConcurrentSkipListSet<Integer>> imageNids =
            new AtomicReference<ConcurrentSkipListSet<Integer>>();
    private AtomicReference<ConcurrentSkipListSet<Integer>> memberNids =
            new AtomicReference<ConcurrentSkipListSet<Integer>>();
    private AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>> refsetMembersMap =
            new AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>>();
    private AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>> refsetComponentMap =
            new AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>>();

    @Override
    public boolean readyToWrite() {
        if (attributes.get() != null) {
            attributes.get().readyToWriteComponent();
        }
        if (srcRels.get() != null) {
            for (Relationship r : srcRels.get()) {
                assert r.readyToWriteComponent();
            }
        }
        if (descriptions.get() != null) {
            for (Description component : descriptions.get()) {
                assert component.readyToWriteComponent();
            }
        }
        if (images.get() != null) {
            for (Image component : images.get()) {
                assert component.readyToWriteComponent();
            }
        }
        if (refsetMembers.get() != null) {
            for (RefsetMember component : refsetMembers.get()) {
                assert component.readyToWriteComponent();
            }
        }
        if (descNids.get() != null) {
            for (Integer component : descNids.get()) {
                assert component != null;
                assert component != Integer.MAX_VALUE;
            }
        }
        if (srcRelNids.get() != null) {
            for (Integer component : srcRelNids.get()) {
                assert component != null;
                assert component != Integer.MAX_VALUE;
            }
        }
        if (imageNids.get() != null) {
            for (Integer component : imageNids.get()) {
                assert component != null;
                assert component != Integer.MAX_VALUE;
            }
        }
        if (memberNids.get() != null) {
            for (Integer component : memberNids.get()) {
                assert component != null;
                assert component != Integer.MAX_VALUE;
            }
        }

        return true;
    }

    public ConceptDataSimpleReference(Concept enclosingConcept) throws IOException {
        super(new NidDataFromBdb(enclosingConcept.getNid()));
        assert enclosingConcept != null : "enclosing concept cannot be null.";
        this.enclosingConcept = enclosingConcept;
    }

    public ConceptDataSimpleReference(Concept enclosingConcept, byte[] roBytes, byte[] mutableBytes) throws IOException {
        super(new NidDataInMemory(roBytes, mutableBytes));
        assert enclosingConcept != null : "enclosing concept cannot be null.";
        this.enclosingConcept = enclosingConcept;
    }

    @Override
    public void diet() {
        if (!isUnwritten()) {
            refsetMembersMap.set(null);
            refsetComponentMap.set(null);
            refsetMembers.set(null);
        }
    }

    @Override
    public boolean hasUncommittedComponents() {
        if (hasUncommittedVersion(attributes.get())) {
            return true;
        }
        if (hasUncommittedVersion(srcRels.get())) {
            return true;
        }
        if (hasUncommittedVersion(descriptions.get())) {
            return true;
        }
        if (hasUncommittedVersion(images.get())) {
            return true;
        }
        if (hasUncommittedVersion(refsetMembers.get())) {
            return true;
        }
        return false;
    }

    @Override
    public NidSetBI setCommitTime(long time) {
        NidSet sapNids = new NidSet();
        setCommitTime(attributes.get(), time, sapNids);
        setCommitTime(srcRels.get(), time, sapNids);
        setCommitTime(descriptions.get(), time, sapNids);
        setCommitTime(images.get(), time, sapNids);
        setCommitTime(refsetMembers.get(), time, sapNids);
        return sapNids;
    }

    @Override
    public void cancel() throws IOException {
        cancel(attributes.get());
        cancel(srcRels.get());
        cancel(descriptions.get());
        cancel(images.get());
        cancel(refsetMembers.get());

    }

    private void cancel(
            Collection<? extends ConceptComponent<?, ?>> componentList) throws IOException {
        ArrayList<ConceptComponent<?, ?>> toRemove =
                new ArrayList<ConceptComponent<?, ?>>();
        if (componentList != null) {
            for (ConceptComponent<?, ?> cc : componentList) {
                if (cancel(cc)) {
                    toRemove.add(cc);
                    removeRefsetReferences(cc);
                }
            }
            componentList.removeAll(toRemove);
        }
    }

    private void removeRefsetReferences(ConceptComponent<?, ?> cc) throws IOException {
        for (RefexChronicleBI<?> rc: cc.getRefsetMembers()) {
            Concept refsetCon = Concept.get(rc.getCollectionNid());
            RefsetMember rm = (RefsetMember) rc;
            rm.primordialSapNid = -1;
            BdbCommitManager.writeImmediate(refsetCon);
            BdbCommitManager.addUncommittedNoChecks(refsetCon);
        }
    }

    private boolean cancel(
            ConceptComponent<?, ?> cc) throws IOException {
        if (cc == null) {
            return true;
        }
        // component
        if (cc.getTime() == Long.MAX_VALUE) {
            cc.cancel();
            removeRefsetReferences(cc);
            return true;
        }
        cc.cancel();
        return false;
    }

    private void setCommitTime(
            Collection<? extends ConceptComponent<?, ?>> componentList,
            long time, NidSetBI sapNids) {

        if (componentList != null) {
            for (ConceptComponent<?, ?> cc : componentList) {
                setCommitTime(cc, time, sapNids);
            }
        }
    }

    private void setCommitTime(
            ConceptComponent<?, ?> cc,
            long time, NidSetBI sapNids) {
        // component
        if (cc.getTime() == Long.MAX_VALUE) {
            cc.setTime(time);
            sapNids.add(cc.primordialSapNid);
        }
        if (cc.revisions != null) {
            for (Revision<?, ?> r : cc.revisions) {
                if (r.getTime() == Long.MAX_VALUE) {
                    r.setTime(time);
                    sapNids.add(r.sapNid);
                }
            }
        }
        // id
        if (cc.getAdditionalIdentifierParts() != null) {
            for (IdentifierVersion idv : cc.getAdditionalIdentifierParts()) {
                if (idv.getTime() == Long.MAX_VALUE) {
                    idv.setTime(time);
                    sapNids.add(idv.getSapNid());
                }
            }
        }

        // annotation
        if (cc.annotations != null) {
            for (RefexChronicleBI<?> rc : cc.annotations) {
                RefsetMember<?, ?> rm = (RefsetMember<?, ?>) rc;
                if (rm.getTime() == Long.MAX_VALUE) {
                    rm.setTime(time);
                    sapNids.add(rm.getSapNid());

                }
                if (rm.revisions != null) {
                    for (RefsetRevision<?, ?> rr : rm.revisions) {
                        if (rr.getTime() == Long.MAX_VALUE) {
                            rr.setTime(time);
                            sapNids.add(rr.getSapNid());
                        }
                    }
                }
            }
        }
    }

    private boolean hasUncommittedVersion(Collection<? extends ConceptComponent<?, ?>> componentList) {
        if (componentList != null) {
            for (ConceptComponent<?, ?> cc : componentList) {
                if (hasUncommittedVersion(cc)) {
                    return true;
                }
                if (hasUncommittedId(cc)) {
                    return true;
                }
                if (hasUncommittedAnnotation(cc)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasUncommittedAnnotation(ConceptComponent<?, ?> cc) {
        if (cc != null && cc.annotations != null) {
            for (RefexChronicleBI<?> rmc : cc.annotations) {
                if (rmc.isUncommitted()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasUncommittedId(ConceptComponent<?, ?> cc) {
        if (cc != null && cc.getAdditionalIdentifierParts() != null) {
            for (IdentifierVersion idv : cc.getAdditionalIdentifierParts()) {
                if (idv.getTime() == Long.MAX_VALUE) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasUncommittedVersion(ConceptComponent<?, ?> cc) {
        if (cc != null) {
            if (cc.getTime() == Long.MAX_VALUE) {
                return true;
            }
            if (cc.revisions != null) {
                for (Revision<?, ?> r : cc.revisions) {
                    if (r.getTime() == Long.MAX_VALUE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public AddSrcRelSet getSourceRels() throws IOException {
        if (srcRels.get() == null) {
            srcRels.compareAndSet(null, new AddSrcRelSet(getList(new RelationshipBinder(), OFFSETS.SOURCE_RELS,
                    enclosingConcept)));
        }
        handleCanceledComponents();
        return srcRels.get();
    }

    @Override
    public AddDescriptionSet getDescriptions() throws IOException {
        if (descriptions.get() == null) {
            descriptions.compareAndSet(null, new AddDescriptionSet(getList(new DescriptionBinder(),
                    OFFSETS.DESCRIPTIONS, enclosingConcept)));
        }
        handleCanceledComponents();
        return descriptions.get();
    }

    private <C extends ConceptComponent<V, C>, V extends Revision<V, C>> ArrayList<C> getList(
            ConceptComponentBinder<V, C> binder, OFFSETS offset, Concept enclosingConcept) throws IOException {
        binder.setupBinder(enclosingConcept);
        ArrayList<C> componentList;
        TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
        if (readOnlyInput.available() > 0) {
            checkFormatAndVersion(readOnlyInput);
            readOnlyInput.mark(128);
            readOnlyInput.skipFast(offset.offset);
            int listStart = readOnlyInput.readInt();
            readOnlyInput.reset();
            readOnlyInput.skipFast(listStart);
            componentList = binder.entryToObject(readOnlyInput);
        } else {
            componentList = new ArrayList<C>();
        }
        assert componentList != null;
        binder.setTermComponentList(componentList);
        TupleInput readWriteInput = nidData.getMutableTupleInput();
        if (readWriteInput.available() > 0) {
            checkFormatAndVersion(readWriteInput);
            readWriteInput.mark(128);
            readWriteInput.skipFast(offset.offset);
            int listStart = readWriteInput.readInt();
            readWriteInput.reset();
            readWriteInput.skipFast(listStart);
            componentList = binder.entryToObject(readWriteInput);
        }
        return componentList;
    }

    private Collection<RefsetMember<?, ?>> getList(RefsetMemberBinder binder, OFFSETS offset, Concept enclosingConcept)
            throws IOException {
        binder.setupBinder(enclosingConcept);
        Collection<RefsetMember<?, ?>> componentList;
        TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
        if (readOnlyInput.available() > 0) {
            checkFormatAndVersion(readOnlyInput);
            readOnlyInput.mark(128);
            readOnlyInput.skipFast(offset.offset);
            int listStart = readOnlyInput.readInt();
            readOnlyInput.reset();
            readOnlyInput.skipFast(listStart);
            componentList = binder.entryToObject(readOnlyInput);
        } else {
            componentList = new ArrayList<RefsetMember<?, ?>>();
        }
        assert componentList != null;
        binder.setTermComponentList(componentList);
        TupleInput readWriteInput = nidData.getMutableTupleInput();
        if (readWriteInput.available() > 0) {
            readWriteInput.mark(128);
            checkFormatAndVersion(readWriteInput);
            readWriteInput.reset();
            readWriteInput.skipFast(offset.offset);
            int listStart = readWriteInput.readInt();
            readWriteInput.reset();
            readWriteInput.skipFast(listStart);
            componentList = binder.entryToObject(readWriteInput);
        }
        return componentList;
    }

    @Override
    public AddMemberSet getRefsetMembers() throws IOException {
        if (refsetMembers.get() == null) {
            refsetMembers.compareAndSet(null, new AddMemberSet(getList(new RefsetMemberBinder(enclosingConcept),
                    OFFSETS.REFSET_MEMBERS, enclosingConcept)));
        }
        handleCanceledComponents();
        return refsetMembers.get();
    }

    @SuppressWarnings("unchecked")
    private void handleCanceledComponents() {
        if (lastExtinctRemoval < BdbCommitManager.getLastCancel()) {
            if (refsetMembers != null
                    && refsetMembers.get() != null
                    && refsetMembers.get().size() > 0) {
                List<RefsetMember<?, ?>> removed =
                        (List<RefsetMember<?, ?>>) removeCanceledFromList(refsetMembers.get());
                if (refsetMembersMap.get() != null
                        || refsetComponentMap.get() != null) {
                    Map<Integer, ?> memberMap = refsetMembersMap.get();
                    Map<Integer, ?> componentMap = refsetComponentMap.get();
                    for (RefsetMember<?, ?> cc : removed) {
                        if (memberMap != null) {
                            memberMap.remove(cc.getNid());
                        }
                        if (componentMap != null) {
                            componentMap.remove(cc.getComponentNid());
                        }
                    }
                }
            }
            if (descriptions != null
                    && descriptions.get() != null
                    && descriptions.get().size() > 0) {
                AddDescriptionSet descList = descriptions.get();
                removeCanceledFromList(descList);
            }
            if (images != null
                    && images.get() != null
                    && images.get().size() > 0) {
                removeCanceledFromList(images.get());
            }
            if (srcRels != null
                    && srcRels.get() != null
                    && srcRels.get().size() > 0) {
                removeCanceledFromList(srcRels.get());
            }
            lastExtinctRemoval = Bdb.gVersion.incrementAndGet();
        }
    }

    private List<? extends ConceptComponent<?, ?>> removeCanceledFromList(Collection<? extends ConceptComponent<?, ?>> ccList) {
        List<ConceptComponent<?, ?>> toRemove = new ArrayList<ConceptComponent<?, ?>>();
        if (ccList != null) {
            synchronized (ccList) {
                for (ConceptComponent<?, ?> cc : ccList) {
                    if (cc.getTime() == Long.MIN_VALUE) {
                        toRemove.add(cc);
                        cc.clearVersions();
                        Concept.componentsCRHM.remove(cc.getNid());
                    } else {
                        if (cc.revisions != null) {
                            List<Revision<?, ?>> revisionToRemove = new ArrayList<Revision<?, ?>>();
                            for (Revision<?, ?> r : cc.revisions) {
                                if (r.getTime() == Long.MIN_VALUE) {
                                    cc.clearVersions();
                                    revisionToRemove.add(r);
                                }
                            }
                            for (Revision<?, ?> r : revisionToRemove) {
                                cc.revisions.remove(r);
                            }
                        }
                    }
                }

                ccList.removeAll(toRemove);
            }
        }
        return toRemove;
    }

    @Override
    public AddImageSet getImages() throws IOException {
        if (images.get() == null) {
            images.compareAndSet(null, new AddImageSet(getList(new ImageBinder(), OFFSETS.IMAGES, enclosingConcept)));
        }
        handleCanceledComponents();
        return images.get();
    }

    public I_GetNidData getNidData() {
        return nidData;
    }

    protected ConcurrentSkipListSet<Integer> getReadOnlyIntSet(OFFSETS offset) throws IOException {
        TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
        if (readOnlyInput.available() < OFFSETS.getHeaderSize()) {
            return new ConcurrentSkipListSet<Integer>();
        }
        readOnlyInput.mark(OFFSETS.getHeaderSize());
        readOnlyInput.skipFast(offset.offset);
        int dataOffset = readOnlyInput.readInt();
        readOnlyInput.reset();
        readOnlyInput.skipFast(dataOffset);
        IntSetBinder binder = new IntSetBinder();
        return binder.entryToObject(readOnlyInput);
    }

    protected ConcurrentSkipListSet<Integer> getMutableIntSet(OFFSETS offset) throws IOException {
        TupleInput mutableInput = nidData.getMutableTupleInput();
        if (mutableInput.available() < OFFSETS.getHeaderSize()) {
            return new ConcurrentSkipListSet<Integer>();
        }
        mutableInput.mark(OFFSETS.getHeaderSize());
        mutableInput.skipFast(offset.offset);
        int dataOffset = mutableInput.readInt();
        mutableInput.reset();
        mutableInput.skipFast(dataOffset);
        IntSetBinder binder = new IntSetBinder();
        return binder.entryToObject(mutableInput);
    }

    @Override
    public void set(ConceptAttributes attr) throws IOException {
        if (attributes.get() != null) {
            throw new IOException("Attributes is already set. Please modify the exisiting attributes object.");
        }
        if (!attributes.compareAndSet(null, attr)) {
            throw new IOException("Attributes is already set. Please modify the exisiting attributes object.");
        }
        enclosingConcept.modified();
    }

    @Override
    public Set<Integer> getDescNids() throws IOException {
        if (descNids.get() == null) {
            ConcurrentSkipListSet<Integer> temp = new ConcurrentSkipListSet<Integer>(getDescNidsReadOnly());
            temp.addAll(getMutableIntSet(OFFSETS.DESC_NIDS));
            descNids.compareAndSet(null, temp);
        }
        return descNids.get();
    }

    @Override
    public Set<Integer> getDescNidsReadOnly() throws IOException {
        return getReadOnlyIntSet(OFFSETS.DESC_NIDS);
    }

    @Override
    public Set<Integer> getImageNids() throws IOException {
        if (imageNids.get() == null) {
            ConcurrentSkipListSet<Integer> temp = new ConcurrentSkipListSet<Integer>(getImageNidsReadOnly());
            temp.addAll(getMutableIntSet(OFFSETS.IMAGE_NIDS));
            imageNids.compareAndSet(null, temp);
        }
        return imageNids.get();
    }

    @Override
    public Set<Integer> getImageNidsReadOnly() throws IOException {
        return getReadOnlyIntSet(OFFSETS.IMAGE_NIDS);
    }

    @Override
    public Set<Integer> getSrcRelNids() throws IOException {
        if (srcRelNids.get() == null) {
            ConcurrentSkipListSet<Integer> temp = new ConcurrentSkipListSet<Integer>(getSrcRelNidsReadOnly());
            temp.addAll(getMutableIntSet(OFFSETS.SRC_REL_NIDS));

            srcRelNids.compareAndSet(null, temp);
        }
        return srcRelNids.get();
    }

    @Override
    public Set<Integer> getSrcRelNidsReadOnly() throws IOException {
        return getReadOnlyIntSet(OFFSETS.SRC_REL_NIDS);
    }

    @Override
    public Set<Integer> getMemberNids() throws IOException {
        if (memberNids.get() == null) {
            ConcurrentSkipListSet<Integer> temp = new ConcurrentSkipListSet<Integer>(getMemberNidsReadOnly());
            temp.addAll(getMutableIntSet(OFFSETS.MEMBER_NIDS));
            memberNids.compareAndSet(null, temp);
        }
        return memberNids.get();
    }

    @Override
    public Set<Integer> getMemberNidsReadOnly() throws IOException {
        return getReadOnlyIntSet(OFFSETS.MEMBER_NIDS);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
     * .component.refset.RefsetMember)
     */
    @Override
    public void add(RefsetMember<?, ?> refsetMember) throws IOException {
        getRefsetMembers().addDirect(refsetMember);
        getMemberNids().add(refsetMember.nid);
        addToMemberMap(refsetMember);
        modified();
    }

    @Override
    public RefsetMember<?, ?> getRefsetMember(int memberNid) throws IOException {
        Collection<RefsetMember<?, ?>> refsetMemberList = getRefsetMembers();
        if (refsetMemberList.size() < useMemberMapThreshold) {
            for (RefsetMember<?, ?> member : refsetMemberList) {
                if (member.nid == memberNid) {
                    return member;
                }
            }
            return null;
        }
        if (refsetMembersMap.get() == null) {
            setupMemberMap(refsetMemberList);
        }
        return refsetMembersMap.get().get(memberNid);
    }

    @Override
    public RefsetMember<?, ?> getRefsetMemberForComponent(int componentNid) throws IOException {
        Collection<RefsetMember<?, ?>> refsetMemberList = getRefsetMembers();
        if (refsetMemberList.size() < useMemberMapThreshold) {
            for (RefsetMember<?, ?> member : refsetMemberList) {
                if (member.getComponentNid() == componentNid) {
                    return member;
                }
            }
            return null;
        }
        if (refsetComponentMap.get() == null) {
            setupMemberMap(refsetMemberList);
        }
        return refsetComponentMap.get().get(componentNid);
    }
    private ReentrantLock memberMapLock = new ReentrantLock();

    private void setupMemberMap(Collection<RefsetMember<?, ?>> refsetMemberList) {
        memberMapLock.lock();
        try {
            if (refsetMembersMap.get() == null
                    || refsetComponentMap.get() == null) {
                ConcurrentHashMap<Integer, RefsetMember<?, ?>> memberMap =
                        new ConcurrentHashMap<Integer, RefsetMember<?, ?>>(refsetMemberList.size(), 0.75f, 2);
                ConcurrentHashMap<Integer, RefsetMember<?, ?>> componentMap =
                        new ConcurrentHashMap<Integer, RefsetMember<?, ?>>(refsetMemberList.size(), 0.75f, 2);
                for (RefsetMember<?, ?> m : refsetMemberList) {
                    memberMap.put(m.nid, m);
                    componentMap.put(m.getComponentNid(), m);
                }
                refsetMembersMap.set(memberMap);
                refsetComponentMap.set(componentMap);
            }
        } finally {
            memberMapLock.unlock();
        }
    }

    @Override
    protected void addToMemberMap(RefsetMember<?, ?> refsetMember) {
        memberMapLock.lock();
        try {
            if (refsetMembersMap.get() != null) {
                refsetMembersMap.get().put(refsetMember.nid, refsetMember);
            }
            if (refsetComponentMap.get() != null) {
                refsetComponentMap.get().put(refsetMember.getComponentNid(), refsetMember);
            }
        } finally {
            memberMapLock.unlock();
        }
    }

    @Override
    public Collection<Description> getDescriptionsIfChanged() throws IOException {
        return descriptions.get();
    }

    @Override
    public Collection<Image> getImagesIfChanged() throws IOException {
        return images.get();
    }

    @Override
    public Collection<RefsetMember<?, ?>> getRefsetMembersIfChanged() throws IOException {
        return refsetMembers.get();
    }

    @Override
    public Collection<Relationship> getSourceRelsIfChanged() throws IOException {
        return srcRels.get();
    }

    @Override
    public ComponentChroncileBI<?> getComponent(int nid) throws IOException {
        if (getConceptAttributes() != null && getConceptAttributes().nid == nid) {
            return getConceptAttributes();
        }

        if (getDescNids().contains(nid)) {
            for (Description d : getDescriptions()) {
                if (d.getNid() == nid) {
                    return d;
                }
            }
        }
        if (getSrcRelNids().contains(nid)) {
            for (Relationship r : getSourceRels()) {
                if (r.getNid() == nid) {
                    return r;
                }
            }
        }
        if (getImageNids().contains(nid)) {
            for (Image i : getImages()) {
                if (i.getNid() == nid) {
                    return i;
                }
            }
        }
        if (getMemberNids().contains(nid)) {
            return getRefsetMember(nid);
        }

        for (RelGroupChronicleBI group : enclosingConcept.getAllRelGroups()) {
            if (group.getNid() == nid) {
                return group;
            }
        }
        ComponentChroncileBI<?> cc = null;

        //recursive search through all annotations...
        if (getConceptAttributes() != null) {
            cc = getAnnotation(getConceptAttributes().annotations, nid);
            if (cc != null) {
                return cc;
            }
        }

        if (getDescriptions() != null) {
            for (Description d : getDescriptions()) {
                cc = getAnnotation(d.annotations, nid);
                if (cc != null) {
                    return cc;
                }
            }
        }

        if (getSourceRels() != null) {
            for (Relationship r : getSourceRels()) {
                cc = getAnnotation(r.annotations, nid);
                if (cc != null) {
                    return cc;
                }
            }
        }

        if (getImages() != null) {
            for (Image i : getImages()) {
                cc = getAnnotation(i.annotations, nid);
                if (cc != null) {
                    return cc;
                }
            }
        }

        if (getRefsetMembers() != null) {
            for (RefsetMember r : getRefsetMembers()) {
                cc = getAnnotation(r.annotations, nid);
                if (cc != null) {
                    return cc;
                }
            }
        }





        return null;
    }

    private ComponentChroncileBI<?> getAnnotation(
            Collection<? extends RefexChronicleBI<?>> annotations, int nid) throws IOException {
        if (annotations == null) {
            return null;
        }
        for (RefexChronicleBI<?> annotation : annotations) {
            if (annotation.getNid() == nid) {
                return annotation;
            }
            ComponentChroncileBI<?> cc = getAnnotation(annotation.getAnnotations(), nid);
            if (cc != null) {
                return cc;
            }
        }
        return null;
    }

    @Override
    public boolean hasComponent(int nid) throws IOException {
        if (getNid() == nid) {
            return true;
        }
        if (getDescNids().contains(nid)) {
            return true;
        }
        if (getSrcRelNids().contains(nid)) {
            return true;
        }
        if (getImageNids().contains(nid)) {
            return true;
        }
        if (getMemberNids().contains(nid)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isLeafByDestRels(I_ConfigAceFrame aceConfig) throws IOException {
        boolean isLeaf = true;
        I_IntSet destRelTypes = aceConfig.getDestRelTypes();
        List<NidPairForRel> relPairs = Bdb.xref.getDestRelPairs(enclosingConcept.getNid(), destRelTypes);
        if (relPairs != null) {
            for (NidPairForRel pair : relPairs) {
                int relNid = pair.getRelNid();
                try {
                    Concept c = Bdb.getConceptForComponent(relNid);
                    if (c != null) {
                        Relationship r = c.getSourceRel(relNid);
                        if (r != null) {
                            List<I_RelTuple> currentVersions = new ArrayList<I_RelTuple>();
                            r.addTuples(aceConfig.getAllowedStatus(), destRelTypes, aceConfig.getViewPositionSetReadOnly(), currentVersions,
                                    aceConfig.getPrecedence(),
                                    aceConfig.getConflictResolutionStrategy());
                            if (currentVersions.size() > 0) {
                                return false;
                            }
                        }
                    }
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }

            }
        }
        return isLeaf;
    }

    @Override
    public boolean isAnnotationStyleRefset() throws IOException {
        if (annotationStyleRefset == null) {
            annotationStyleRefset = getIsAnnotationStyleRefset();
        }
        return annotationStyleRefset;
    }

    @Override
    public void setAnnotationStyleRefset(boolean annotationStyleRefset) {
        modified();
        this.annotationStyleRefset = annotationStyleRefset;
    }

    @Override
    public ConceptAttributes getConceptAttributes() throws IOException {
        if (attributes.get() == null) {
            ArrayList<ConceptAttributes> components =
                    getList(new ConceptAttributesBinder(), OFFSETS.ATTRIBUTES, enclosingConcept);
            if (components != null && components.size() > 0) {
                attributes.compareAndSet(null, components.get(0));
            }

        }
        return attributes.get();
    }

    @Override
    public ConceptAttributes getConceptAttributesIfChanged() throws IOException {
        return attributes.get();
    }
}
