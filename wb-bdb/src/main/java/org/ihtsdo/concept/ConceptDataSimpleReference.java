package org.ihtsdo.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.component.ComponentList;
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
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;

import com.sleepycat.bind.tuple.TupleInput;
import java.util.concurrent.ConcurrentSkipListSet;

public class ConceptDataSimpleReference extends ConceptDataManager {

    private static HashMap<I_ConfigAceFrame, IsLeafBinder> isLeafBinders = new HashMap<I_ConfigAceFrame, IsLeafBinder>();
    private AtomicReference<ConceptAttributes> attributes = new AtomicReference<ConceptAttributes>();
    private AtomicReference<AddSrcRelList> srcRels = new AtomicReference<AddSrcRelList>();
    private AtomicReference<AddDescriptionList> descriptions = new AtomicReference<AddDescriptionList>();
    private AtomicReference<AddImageList> images = new AtomicReference<AddImageList>();
    private AtomicReference<AddMemberList> refsetMembers = new AtomicReference<AddMemberList>();
    private AtomicReference<CopyOnWriteArraySet<Integer>> descNids =
            new AtomicReference<CopyOnWriteArraySet<Integer>>();
    private AtomicReference<CopyOnWriteArraySet<Integer>> srcRelNids =
            new AtomicReference<CopyOnWriteArraySet<Integer>>();
    private AtomicReference<CopyOnWriteArraySet<Integer>> imageNids =
            new AtomicReference<CopyOnWriteArraySet<Integer>>();
    private AtomicReference<ConcurrentSkipListSet<Integer>> memberNids =
            new AtomicReference<ConcurrentSkipListSet<Integer>>();
    private AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>> refsetMembersMap =
            new AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>>();
    private AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>> refsetComponentMap =
            new AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>>();

    @Override
    public void diet() {
        if (!isUnwritten()) {
            refsetMembersMap.set(null);
            refsetComponentMap.set(null);
            refsetMembers.set(null);
        }
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

    public boolean hasUncommittedComponents() {
        if (hasUncommittedVersion(attributes.get())
                || hasUncommittedId(attributes.get())) {
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

    private boolean hasUncommittedVersion(ComponentList<? extends ConceptComponent<?, ?>> componentList) {
        if (componentList != null) {
            for (ConceptComponent<?, ?> cc : componentList) {
                if (hasUncommittedVersion(cc)) {
                    return true;
                }
                if (hasUncommittedId(cc)) {
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

    public AddSrcRelList getSourceRels() throws IOException {
        if (srcRels.get() == null) {
            srcRels.compareAndSet(null, new AddSrcRelList(getList(new RelationshipBinder(), OFFSETS.SOURCE_RELS,
                    enclosingConcept)));
        }
        handleCanceledComponents();
        return srcRels.get();
    }

    public AddDescriptionList getDescriptions() throws IOException {
        if (descriptions.get() == null) {
            descriptions.compareAndSet(null, new AddDescriptionList(getList(new DescriptionBinder(),
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

    public AddMemberList getRefsetMembers() throws IOException {
        if (refsetMembers.get() == null) {
            refsetMembers.compareAndSet(null, new AddMemberList(getList(new RefsetMemberBinder(enclosingConcept),
                    OFFSETS.REFSET_MEMBERS, enclosingConcept)));
        }
        handleCanceledComponents();
        return refsetMembers.get();
    }

    private void handleCanceledComponents() {
        if (lastExtinctRemoval < BdbCommitManager.getLastCancel()) {
            if (refsetMembers != null
                    && refsetMembers.get() != null
                    && refsetMembers.get().size() > 0) {
                removeCanceledFromList(refsetMembers.get());
            }
            if (descriptions != null
                    && descriptions.get() != null
                    && descriptions.get().size() > 0) {
                removeCanceledFromList(descriptions.get());
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

    private void removeCanceledFromList(List<? extends ConceptComponent<?, ?>> ccList) {
        if (ccList != null) {
            synchronized (ccList) {
                List<Integer> toRemove = new ArrayList<Integer>();
                for (int i = ccList.size() - 1; i > -1; i--) {
                    if (ccList.get(i).getTime() == Long.MIN_VALUE) {
                        toRemove.add(i);
                        ccList.get(i).clearVersions();
                    }
                }
                for (int i : toRemove) {
                    ccList.remove(i);
                }
            }
        }
    }

    public AddImageList getImages() throws IOException {
        if (images.get() == null) {
            images.compareAndSet(null, new AddImageList(getList(new ImageBinder(), OFFSETS.IMAGES, enclosingConcept)));
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
            CopyOnWriteArraySet<Integer> temp = new CopyOnWriteArraySet<Integer>(getDescNidsReadOnly());
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
    public CopyOnWriteArraySet<Integer> getImageNids() throws IOException {
        if (imageNids.get() == null) {
            CopyOnWriteArraySet<Integer> temp = new CopyOnWriteArraySet<Integer>(getImageNidsReadOnly());
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
    public CopyOnWriteArraySet<Integer> getSrcRelNids() throws IOException {
        if (srcRelNids.get() == null) {
            CopyOnWriteArraySet<Integer> temp = new CopyOnWriteArraySet<Integer>(getSrcRelNidsReadOnly());
            temp.addAll(getMutableIntSet(OFFSETS.SRC_REL_NIDS));

            srcRelNids.compareAndSet(null, temp);
        }
        return srcRelNids.get();
    }

    @Override
    public ConcurrentSkipListSet<Integer> getSrcRelNidsReadOnly() throws IOException {
        return getReadOnlyIntSet(OFFSETS.SRC_REL_NIDS);
    }

    @Override
    public ConcurrentSkipListSet<Integer> getMemberNids() throws IOException {
        if (memberNids.get() == null) {
            ConcurrentSkipListSet<Integer> temp = new ConcurrentSkipListSet<Integer>(getMemberNidsReadOnly());
            temp.addAll(getMutableIntSet(OFFSETS.MEMBER_NIDS));
            memberNids.compareAndSet(null, temp);
        }
        return memberNids.get();
    }

    @Override
    public ConcurrentSkipListSet<Integer> getMemberNidsReadOnly() throws IOException {
        return getReadOnlyIntSet(OFFSETS.MEMBER_NIDS);
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
    public ConceptAttributes getConceptAttributesIfChanged() throws IOException {
        return attributes.get();
    }

    @Override
    public ComponentList<Description> getDescriptionsIfChanged() throws IOException {
        return descriptions.get();
    }

    @Override
    public ComponentList<Image> getImagesIfChanged() throws IOException {
        return images.get();
    }

    @Override
    public ComponentList<RefsetMember<?, ?>> getRefsetMembersIfChanged() throws IOException {
        return refsetMembers.get();
    }

    @Override
    public ComponentList<Relationship> getSourceRelsIfChanged() throws IOException {
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

        for (RelGroupChronicleBI group : enclosingConcept.getRelGroups()) {
            if (group.getNid() == nid) {
                return group;
            }
        }
        return null;
    }

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
                            r.addTuples(aceConfig.getAllowedStatus(), 
                                    destRelTypes, 
                                    aceConfig.getViewPositionSetReadOnly(), 
                                    currentVersions,
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
        } else {
            IsLeafBinder leafBinder = isLeafBinders.get(aceConfig);
            if (leafBinder == null) {
                leafBinder = new IsLeafBinder(aceConfig);
                if (isLeafBinders.size() > 5) {
                    isLeafBinders.clear();
                }
                isLeafBinders.put(aceConfig, leafBinder);
            }

            TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
            if (readOnlyInput.available() > OFFSETS.getHeaderSize()) {
                readOnlyInput.mark(OFFSETS.getHeaderSize());
                readOnlyInput.skipFast(OFFSETS.DEST_REL_NID_TYPE_NIDS.offset);
                int dataOffset = readOnlyInput.readInt();
                readOnlyInput.reset();
                readOnlyInput.skipFast(dataOffset);
                isLeaf = leafBinder.entryToObject(readOnlyInput);
            }

            if (isLeaf) {
                TupleInput mutableInput = nidData.getMutableTupleInput();
                if (mutableInput.available() > OFFSETS.getHeaderSize()) {
                    mutableInput.mark(OFFSETS.getHeaderSize());
                    mutableInput.skipFast(OFFSETS.DEST_REL_NID_TYPE_NIDS.offset);
                    int dataOffset = mutableInput.readInt();
                    mutableInput.reset();
                    mutableInput.skipFast(dataOffset);
                    isLeaf = leafBinder.entryToObject(mutableInput);
                }
            }
        }
        return isLeaf;
    }
}