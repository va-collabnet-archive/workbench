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
import java.util.logging.Level;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
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
import org.ihtsdo.db.bdb.I_GetNidData;
import org.ihtsdo.db.bdb.NidDataFromBdb;
import org.ihtsdo.db.bdb.NidDataInMemory;
import org.ihtsdo.db.util.NidPair;

import com.sleepycat.bind.tuple.TupleInput;

public class ConceptDataSimpleReference extends ConceptDataManager {

    private static HashMap<I_ConfigAceFrame, IsLeafBinder> isLeafBinders = new HashMap<I_ConfigAceFrame, IsLeafBinder>();

    private AtomicReference<ConceptAttributes> attributes = new AtomicReference<ConceptAttributes>();
    private AtomicReference<AddSrcRelList> srcRels = new AtomicReference<AddSrcRelList>();
    private AtomicReference<AddDescriptionList> descriptions = new AtomicReference<AddDescriptionList>();
    private AtomicReference<AddImageList> images = new AtomicReference<AddImageList>();
    private AtomicReference<AddMemberList> refsetMembers = new AtomicReference<AddMemberList>();
    private AtomicReference<SetModifiedWhenChangedList> destRelNidTypeNidList =
            new AtomicReference<SetModifiedWhenChangedList>();
    private AtomicReference<SetModifiedWhenChangedList> refsetNidMemberNidForConceptList =
            new AtomicReference<SetModifiedWhenChangedList>();
    private AtomicReference<SetModifiedWhenChangedList> refsetNidMemberNidForDescriptionsList =
            new AtomicReference<SetModifiedWhenChangedList>();
    private AtomicReference<SetModifiedWhenChangedList> refsetNidMemberNidForRelsList =
            new AtomicReference<SetModifiedWhenChangedList>();
    private AtomicReference<SetModifiedWhenChangedList> refsetNidMemberNidForImagesList =
            new AtomicReference<SetModifiedWhenChangedList>();
    private AtomicReference<SetModifiedWhenChangedList> refsetNidMemberNidForRefsetMembersList =
            new AtomicReference<SetModifiedWhenChangedList>();
    private AtomicReference<CopyOnWriteArraySet<Integer>> descNids =
            new AtomicReference<CopyOnWriteArraySet<Integer>>();
    private AtomicReference<CopyOnWriteArraySet<Integer>> srcRelNids =
            new AtomicReference<CopyOnWriteArraySet<Integer>>();
    private AtomicReference<CopyOnWriteArraySet<Integer>> imageNids =
            new AtomicReference<CopyOnWriteArraySet<Integer>>();
    private AtomicReference<CopyOnWriteArraySet<Integer>> memberNids =
            new AtomicReference<CopyOnWriteArraySet<Integer>>();
    private AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>> refsetMembersMap =
            new AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>>();

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
        if (hasUncommittedVersion(attributes.get()) ||
        		hasUncommittedId(attributes.get())) {
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
    	AceLog.getAppLog().info(">>>>>>>>>>> hasUncommittedVersion called");
        if (componentList != null) {
        	AceLog.getAppLog().info(">>>>>>>>>>> hasUncommittedVersion componentList != null");
            for (ConceptComponent<?, ?> cc: componentList) {
                if (hasUncommittedVersion(cc)) {
                	AceLog.getAppLog().info(">>>>>>>>>>> hasUncommittedVersion hasUncommittedVersion(cc)");
                    return true;
                }
                if (hasUncommittedId(cc)) {
                	AceLog.getAppLog().info(">>>>>>>>>>> hasUncommittedVersion hasUncommittedId(cc)");
                    return true;
                }
            }
        }
        AceLog.getAppLog().info(">>>>>>>>>>> hasUncommittedVersion componentList == null");
        return false;
    }

    private boolean hasUncommittedId(ConceptComponent<?, ?> cc) {
    	
    	AceLog.getAppLog().info(">>>>>>>>>>> hasUncommittedId cc = "+cc);
    	AceLog.getAppLog().info(">>>>>>>>>>> hasUncommittedId cc.getAdditionalIdentifierParts() = "+cc.getAdditionalIdentifierParts());
    	
        if (cc != null && cc.getAdditionalIdentifierParts() != null) {
        	AceLog.getAppLog().info(">>>>>>>>>>> hasUncommittedId cc != null && cc.getAdditionalIdentifierParts() != null");
        	for (IdentifierVersion idv: cc.getAdditionalIdentifierParts()) {
        		AceLog.getAppLog().info(">>>>>>>>>>> idv.getTime() = "+idv.getTime());
        		AceLog.getAppLog().info(">>>>>>>>>>> Long.MAX_VALUE = "+Long.MAX_VALUE);
        		if (idv.getTime() == Long.MAX_VALUE) {
        			AceLog.getAppLog().info(">>>>>>>>>>> hasUncommittedId idv.getTime() == Long.MAX_VALUE");
                    return true;
        		}
        	}
        }
        AceLog.getAppLog().info(">>>>>>>>>>> hasUncommittedId cc == null || cc.getAdditionalIdentifierParts() == null");
        return false;
    }


    private boolean hasUncommittedVersion(ConceptComponent<?, ?> cc) {
        if (cc != null) {
            if (cc.getTime() == Long.MAX_VALUE) {
                return true;
            }
            if (cc.revisions != null) {
                for (Revision<?, ?> r: cc.revisions) {
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
        return srcRels.get();
    }

    public AddDescriptionList getDescriptions() throws IOException {
        if (descriptions.get() == null) {
            descriptions.compareAndSet(null, new AddDescriptionList(getList(new DescriptionBinder(),
                OFFSETS.DESCRIPTIONS, enclosingConcept)));
        }
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
            refsetMembers.compareAndSet(null, new AddMemberList(getList(new RefsetMemberBinder(),
                OFFSETS.REFSET_MEMBERS, enclosingConcept)));
        }
        return refsetMembers.get();
    }

    public AddImageList getImages() throws IOException {
        if (images.get() == null) {
            images.compareAndSet(null, new AddImageList(getList(new ImageBinder(), OFFSETS.IMAGES, enclosingConcept)));
        }
        return images.get();
    }

    public I_GetNidData getNidData() {
        return nidData;
    }

    protected SetModifiedWhenChangedList getArrayIntList(OFFSETS offset) throws IOException {
        SetModifiedWhenChangedList roList = getReadOnlyArrayIntList(offset);
        IntListPairsBinder binder = new IntListPairsBinder();
        binder.setReadOnlyList(roList);
        TupleInput readWriteInput = nidData.getMutableTupleInput();
        if (readWriteInput.available() < OFFSETS.getHeaderSize()) {
            return roList;
        }
        readWriteInput.mark(OFFSETS.getHeaderSize());
        readWriteInput.skipFast(offset.offset);
        int dataOffset = readWriteInput.readInt();
        readWriteInput.reset();
        readWriteInput.skipFast(dataOffset);
        return new SetModifiedWhenChangedList(binder.entryToObject(readWriteInput));
    }

    protected SetModifiedWhenChangedList getReadOnlyArrayIntList(OFFSETS offset) throws IOException {
        TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
        if (readOnlyInput.available() < OFFSETS.getHeaderSize()) {
            return new SetModifiedWhenChangedList();
        }
        readOnlyInput.mark(OFFSETS.getHeaderSize());
        readOnlyInput.skipFast(offset.offset);
        int dataOffset = readOnlyInput.readInt();
        readOnlyInput.reset();
        readOnlyInput.skipFast(dataOffset);
        IntListPairsBinder binder = new IntListPairsBinder();
        return new SetModifiedWhenChangedList(binder.entryToObject(readOnlyInput));
    }

    protected CopyOnWriteArraySet<Integer> getReadOnlyIntSet(OFFSETS offset) throws IOException {
        TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
        if (readOnlyInput.available() < OFFSETS.getHeaderSize()) {
            return new CopyOnWriteArraySet<Integer>();
        }
        readOnlyInput.mark(OFFSETS.getHeaderSize());
        readOnlyInput.skipFast(offset.offset);
        int dataOffset = readOnlyInput.readInt();
        readOnlyInput.reset();
        readOnlyInput.skipFast(dataOffset);
        IntSetBinder binder = new IntSetBinder();
        return binder.entryToObject(readOnlyInput);
    }

    protected CopyOnWriteArraySet<Integer> getMutableIntSet(OFFSETS offset) throws IOException {
        TupleInput mutableInput = nidData.getMutableTupleInput();
        if (mutableInput.available() < OFFSETS.getHeaderSize()) {
            return new CopyOnWriteArraySet<Integer>();
        }
        mutableInput.mark(OFFSETS.getHeaderSize());
        mutableInput.skipFast(offset.offset);
        int dataOffset = mutableInput.readInt();
        mutableInput.reset();
        mutableInput.skipFast(dataOffset);
        IntSetBinder binder = new IntSetBinder();
        return binder.entryToObject(mutableInput);
    }

    protected List<RefsetMember<?, ?>> getRefsetMembers(SetModifiedWhenChangedList members) throws IOException {
        List<RefsetMember<?, ?>> refsetMembers = new ArrayList<RefsetMember<?, ?>>();
        for (NidPair pair : members) {
            int refsetNid = pair.getNid1();
            int memberNid = pair.getNid2();
            Concept refsetConcept = Bdb.getConceptDb().getConcept(refsetNid);
            RefsetMember<?, ?> member = refsetConcept.getRefsetMember(memberNid);
            if (member != null) {
                refsetMembers.add(refsetConcept.getRefsetMember(memberNid));
            } else {
                members.remove(pair);
                Terms.get().addUncommittedNoChecks(enclosingConcept);
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    StringBuffer buff = new StringBuffer();
                    buff.append("Unable to find extension. RefsetNid: ");
                    buff.append(refsetNid);
                    buff.append(" MemberNid: ");
                    buff.append(memberNid);
                    buff.append("\n\nReferenced from concept: ");
                    buff.append(enclosingConcept.toLongString());
                    buff.append("\n\nRefset concept: ");
                    buff.append(refsetConcept.toLongString());
                    AceLog.getAppLog().warning(buff.toString());
                }
            }
        }
        return refsetMembers;
    }

    protected List<RefsetMember<?, ?>> getRefsetMembers(SetModifiedWhenChangedList members, int componentId)
            throws IOException {
        List<RefsetMember<?, ?>> refsetMembers = new ArrayList<RefsetMember<?, ?>>();
        for (NidPair pair : members) {
            int refsetNid = pair.getNid1();
            int memberNid = pair.getNid2();
            Concept refsetConcept = Bdb.getConceptDb().getConcept(refsetNid);
            RefsetMember<?, ?> member = refsetConcept.getRefsetMember(memberNid);
            if (member != null) {
                if (member.getComponentId() == componentId 
                        && member.primordialSapNid >= 0 
                        && member.getTime() != Long.MIN_VALUE) {
                    refsetMembers.add(refsetConcept.getRefsetMember(memberNid));
                }
            } else {
                members.remove(pair);
                Terms.get().addUncommittedNoChecks(enclosingConcept);
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    StringBuffer buff = new StringBuffer();
                    buff.append("Unable to find extension. RefsetNid: ");
                    buff.append(refsetNid);
                    buff.append(" MemberNid: ");
                    buff.append(memberNid);
                    buff.append("\n\nReferenced from concept: ");
                    buff.append(enclosingConcept.toLongString());
                    buff.append("\n\nRefset concept: ");
                    buff.append(refsetConcept.toLongString());
                    AceLog.getAppLog().warning(buff.toString());
                }
            }
        }
        return refsetMembers;
    }

    @Override
    public void setDestRelNidTypeNidList(List<NidPair> destRelNidTypeNidList) throws IOException {
        this.destRelNidTypeNidList.set(new SetModifiedWhenChangedList(destRelNidTypeNidList));
        enclosingConcept.modified();
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
    public void setRefsetNidMemberNidForConceptList(List<NidPair> refsetNidMemberNidForConceptList) throws IOException {
        this.refsetNidMemberNidForConceptList.set(new SetModifiedWhenChangedList(refsetNidMemberNidForConceptList));
        enclosingConcept.modified();
    }

    @Override
    public void setRefsetNidMemberNidForDescriptionsList(List<NidPair> refsetNidMemberNidForDescriptionsList)
            throws IOException {
        this.refsetNidMemberNidForDescriptionsList.set(new SetModifiedWhenChangedList(
            refsetNidMemberNidForDescriptionsList));
        enclosingConcept.modified();
    }

    @Override
    public void setRefsetNidMemberNidForRelsList(List<NidPair> refsetNidMemberNidForRelsList) throws IOException {
        this.refsetNidMemberNidForRelsList.set(new SetModifiedWhenChangedList(refsetNidMemberNidForRelsList));
        enclosingConcept.modified();
    }

    @Override
    public void setRefsetNidMemberNidForImagesList(List<NidPair> refsetNidMemberNidForImagesList) throws IOException {
        this.refsetNidMemberNidForImagesList.set(new SetModifiedWhenChangedList(refsetNidMemberNidForImagesList));
        enclosingConcept.modified();
    }

    @Override
    public void setRefsetNidMemberNidForRefsetMembersList(List<NidPair> refsetNidMemberNidForRefsetMembersList)
            throws IOException {
        this.refsetNidMemberNidForRefsetMembersList.set(new SetModifiedWhenChangedList(
            refsetNidMemberNidForRefsetMembersList));
        enclosingConcept.modified();
    }

    @Override
    public SetModifiedWhenChangedList getDestRelNidTypeNidList() throws IOException {
        if (destRelNidTypeNidList.get() == null) {
            destRelNidTypeNidList.compareAndSet(null, getArrayIntList(OFFSETS.DEST_REL_NID_TYPE_NIDS));
        }
        return destRelNidTypeNidList.get();
    }

    @Override
    public SetModifiedWhenChangedList getRefsetNidMemberNidForConceptList() throws IOException {
        if (refsetNidMemberNidForConceptList.get() == null) {
            refsetNidMemberNidForConceptList.compareAndSet(null,
                getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_CONCEPT));
        }
        return refsetNidMemberNidForConceptList.get();
    }

    @Override
    public SetModifiedWhenChangedList getRefsetNidMemberNidForDescriptionsList() throws IOException {
        if (refsetNidMemberNidForDescriptionsList.get() == null) {
            refsetNidMemberNidForDescriptionsList.compareAndSet(null,
                getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_DESCRIPTIONS));
        }
        return refsetNidMemberNidForDescriptionsList.get();
    }

    @Override
    public SetModifiedWhenChangedList getRefsetNidMemberNidForRelsList() throws IOException {
        if (refsetNidMemberNidForRelsList.get() == null) {
            refsetNidMemberNidForRelsList.compareAndSet(null,
                getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_RELATIONSHIPS));
        }
        return refsetNidMemberNidForRelsList.get();
    }

    @Override
    public SetModifiedWhenChangedList getRefsetNidMemberNidForImagesList() throws IOException {
        if (refsetNidMemberNidForImagesList.get() == null) {
            refsetNidMemberNidForImagesList
                .compareAndSet(null, getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_IMAGES));
        }
        return refsetNidMemberNidForImagesList.get();
    }

    @Override
    public SetModifiedWhenChangedList getRefsetNidMemberNidForRefsetMembersList() throws IOException {
        if (refsetNidMemberNidForRefsetMembersList.get() == null) {
            refsetNidMemberNidForRefsetMembersList.compareAndSet(null,
                getArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_REFSETMEMBERS));
        }
        return refsetNidMemberNidForRefsetMembersList.get();
    }

    @Override
    public SetModifiedWhenChangedList getDestRelNidTypeNidListReadOnly() throws IOException {
        return getReadOnlyArrayIntList(OFFSETS.DEST_REL_NID_TYPE_NIDS);
    }

    @Override
    public SetModifiedWhenChangedList getRefsetNidMemberNidForConceptListReadOnly() throws IOException {
        return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_CONCEPT);
    }

    @Override
    public SetModifiedWhenChangedList getRefsetNidMemberNidForDescriptionsListReadOnly() throws IOException {
        return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_DESCRIPTIONS);
    }

    @Override
    public SetModifiedWhenChangedList getRefsetNidMemberNidForRelsListReadOnly() throws IOException {
        return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_RELATIONSHIPS);
    }

    @Override
    public SetModifiedWhenChangedList getRefsetNidMemberNidForRefsetMembersListReadOnly() throws IOException {
        return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_REFSETMEMBERS);
    }

    @Override
    public SetModifiedWhenChangedList getRefsetNidMemberNidForImagesListReadOnly() throws IOException {
        return getReadOnlyArrayIntList(OFFSETS.REFSETNID_MEMBERNID_FOR_IMAGES);
    }

    /**
     * TODO add call for getRefsetMembersForComponent(int refset, int componentNid);
     * 
     * @throws IOException
     */
    @Override
    public List<RefsetMember<?, ?>> getExtensionsForComponent(int componentNid) throws IOException {
        if (componentNid == enclosingConcept.getConceptId()) {
            SetModifiedWhenChangedList conceptMembers = getRefsetNidMemberNidForConceptList();
            return getRefsetMembers(conceptMembers, componentNid);
        } else if (getDescNids().contains(componentNid)) {
            SetModifiedWhenChangedList descMembers = getRefsetNidMemberNidForDescriptionsList();
            return getRefsetMembers(descMembers, componentNid);
        } else if (getSrcRelNids().contains(componentNid)) {
            SetModifiedWhenChangedList srcRelMembers = getRefsetNidMemberNidForRelsList();
            return getRefsetMembers(srcRelMembers, componentNid);
        } else if (getImageNids().contains(componentNid)) {
            SetModifiedWhenChangedList imageMembers = getRefsetNidMemberNidForImagesList();
            return getRefsetMembers(imageMembers, componentNid);
        } // must be in one of the refset members by elimination
        SetModifiedWhenChangedList refsetMembers = getRefsetNidMemberNidForRefsetMembersList();
        return getRefsetMembers(refsetMembers, componentNid);
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
    public CopyOnWriteArraySet<Integer> getSrcRelNidsReadOnly() throws IOException {
        return getReadOnlyIntSet(OFFSETS.SRC_REL_NIDS);
    }

    @Override
    public CopyOnWriteArraySet<Integer> getMemberNids() throws IOException {
        if (memberNids.get() == null) {
            CopyOnWriteArraySet<Integer> temp = new CopyOnWriteArraySet<Integer>(getMemberNidsReadOnly());
            temp.addAll(getMutableIntSet(OFFSETS.MEMBER_NIDS));
            memberNids.compareAndSet(null, temp);
        }
        return memberNids.get();
    }

    @Override
    public CopyOnWriteArraySet<Integer> getMemberNidsReadOnly() throws IOException {
        return getReadOnlyIntSet(OFFSETS.MEMBER_NIDS);
    }

    public SetModifiedWhenChangedList getRefsetNidMemberNidForImagesListRef() {
        return refsetNidMemberNidForImagesList.get();
    }

    public SetModifiedWhenChangedList getRefsetNidMemberNidForRefsetMembersListRef() {
        return refsetNidMemberNidForRefsetMembersList.get();
    }

    public void setRefsetNidMemberNidForRefsetMembersListRef(
            SetModifiedWhenChangedList refsetNidMemberNidForRefsetMembersListRef) {
        this.refsetNidMemberNidForRefsetMembersList.set(refsetNidMemberNidForRefsetMembersListRef);
        enclosingConcept.modified();
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

    private synchronized void setupMemberMap(Collection<RefsetMember<?, ?>> refsetMemberList) {
        if (refsetMembersMap.get() == null) {
            ConcurrentHashMap<Integer, RefsetMember<?, ?>> temp =
                    new ConcurrentHashMap<Integer, RefsetMember<?, ?>>(refsetMemberList.size(), 0.75f, 2);
            for (RefsetMember<?, ?> m : refsetMemberList) {
                temp.put(m.nid, m);
            }
            refsetMembersMap.compareAndSet(null, temp);
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
    public ConceptComponent<?, ?> getComponent(int nid) throws IOException {
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
            for (RefsetMember<?, ?> r : getRefsetMembers()) {
                if (r.getNid() == nid) {
                    return r;
                }
            }
        }
        return null;
    }

    protected void addToMemberMap(RefsetMember<?, ?> refsetMember) {
        if (refsetMembersMap.get() != null) {
            refsetMembersMap.get().put(refsetMember.nid, refsetMember);
        }
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
        if (destRelNidTypeNidList != null && destRelNidTypeNidList.get() != null) {
            I_IntSet destRelTypes = aceConfig.getDestRelTypes();
            for (NidPair pair: destRelNidTypeNidList.get()) {
                int relNid = pair.getNid1();
                int typeId = pair.getNid2();
                if (destRelTypes.contains(typeId)) {
                    try {
                        Concept c = Bdb.getConceptForComponent(relNid);
                        if (c != null) {
                            Relationship r = c.getSourceRel(relNid);
                            if (r != null) {
                                List<I_RelTuple> currentVersions = new ArrayList<I_RelTuple>();
                                r.addTuples(aceConfig.getAllowedStatus(), destRelTypes, aceConfig
                                        .getViewPositionSetReadOnly(), currentVersions, 
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

    @Override
    public boolean hasAttributeExtensions() throws IOException {
        return getRefsetNidMemberNidForConceptList() == null || getRefsetNidMemberNidForConceptList().size() > 0;
    }

    @Override
    public boolean hasDescriptionExtensions() throws IOException {
        return getRefsetNidMemberNidForDescriptionsList() == null || getRefsetNidMemberNidForDescriptionsList().size() > 0;
    }

    @Override
    public boolean hasExtensionExtensions() throws IOException{
        return getRefsetNidMemberNidForRefsetMembersListRef() == null || getRefsetNidMemberNidForRefsetMembersListRef().size() > 0;
    }

    @Override
    public boolean hasExtensionsForComponent(int nid) throws IOException {
        return getExtensionsForComponent(nid).size() > 0;
    }

    @Override
    public boolean hasMediaExtensions() throws IOException {
        return getRefsetNidMemberNidForImagesListRef() == null || getRefsetNidMemberNidForImagesListRef().size() > 0;
    }

    @Override
    public boolean hasRelExtensions() throws IOException {
        return getRefsetNidMemberNidForRelsList() == null || getRefsetNidMemberNidForRelsList().size() > 0;
    }

}