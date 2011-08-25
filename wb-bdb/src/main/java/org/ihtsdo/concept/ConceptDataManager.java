/**
 * 
 */
package org.ihtsdo.concept;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.component.DataVersionBinder;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.image.Image;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.I_GetNidData;
import org.ihtsdo.db.util.NidPair;
import org.ihtsdo.db.util.NidPairForRel;

import com.sleepycat.bind.tuple.TupleInput;
import org.ihtsdo.concept.component.AnnotationIndexBinder;
import org.ihtsdo.concept.component.AnnotationStyleBinder;
import org.ihtsdo.tk.api.NidSetBI;

/**
 * File format:<br>
 * 
 * @author kec
 * 
 */
public abstract class ConceptDataManager implements I_ManageConceptData {

    public class AddDescriptionSet extends ConcurrentSkipListSet<Description> {

        private static final long serialVersionUID = 1L;

        public AddDescriptionSet(Collection<? extends Description> c) {
            super(new ComponentComparator());
            for (Description d : c) {
                addDirect(d);
            }
        }

        @Override
        public boolean add(Description e) {
            try {
                boolean returnValue = super.add(e);
                processNewDesc(e);
                return returnValue;
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }

        public final boolean addDirect(Description e) {
            return super.add(e);
        }
    }

    public class AddSrcRelSet extends ConcurrentSkipListSet<Relationship> {

        private static final long serialVersionUID = 1L;

        public AddSrcRelSet(Collection<? extends Relationship> c) {
            super(new ComponentComparator());
            for (Relationship r : c) {
                addDirect(r);
            }
        }

        @Override
        public boolean add(Relationship e) {
            try {
                if (e == null) {
                    AceLog.getAppLog().info("found it");
                }
                assert e != null : "Relationship is null processing:\n" + this;
                boolean returnValue = super.add(e);
                processNewRel(e);
                return returnValue;
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }

        public final boolean addDirect(Relationship e) {
            return super.add(e);
        }
    }

    public class AddImageSet extends ConcurrentSkipListSet<Image> {

        private static final long serialVersionUID = 1L;

        public AddImageSet(Collection<? extends Image> c) {
            super(new ComponentComparator());
            for (Image i : c) {
                addDirect(i);
            }
        }

        @Override
        public boolean add(Image e) {
            try {
                boolean returnValue = super.add(e);
                processNewImage(e);
                return returnValue;
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }

        public final boolean addDirect(Image e) {
            return super.add(e);
        }
    }

    public class AddMemberSet extends ConcurrentSkipListSet<RefsetMember<?, ?>> {

        private static final long serialVersionUID = 1L;

        public AddMemberSet(Collection<? extends RefsetMember<?, ?>> c) {
            super(new ComponentComparator());
            for (RefsetMember m : c) {
                addDirect(m);
            }
        }

        @Override
        public boolean add(RefsetMember<?, ?> e) {
            try {
                assert e != null : "Trying to add a null refset member to: " + this;
                boolean returnValue = super.add(e);
                processNewRefsetMember(e);
                return returnValue;
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }

        public final boolean addDirect(RefsetMember<?, ?> e) {
            return super.add(e);
        }
    }

    public class SetModifiedWhenChangeSet extends ConcurrentSkipListSet<NidPair> {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public SetModifiedWhenChangeSet() {
            super();
        }

        public SetModifiedWhenChangeSet(Collection<NidPair> c) {
            super(c);
        }

        public SetModifiedWhenChangeSet(NidPair[] toCopyIn) {
            super(Arrays.asList(toCopyIn));
        }

        @Override
        public boolean add(NidPair e) {
            boolean returnValue = super.add(e);
            modified();
            return returnValue;
        }

        @Override
        public boolean addAll(Collection<? extends NidPair> c) {
            boolean returnValue = super.addAll(c);
            modified();
            return returnValue;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        public synchronized boolean forget(NidPair pair) {
            boolean removed = super.remove(pair);
            if (removed) {
                modified();
            }
            return removed;
        }

        @Override
        public boolean remove(Object o) {
            return forget((NidPair) o);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }
    }
    /**
     * When the number of refset members are greater than this value, use a map
     * for looking up members instead of iterating through a list.
     */
    protected static int useMemberMapThreshold = 15;
    protected Concept enclosingConcept;
    protected I_GetNidData nidData;

    public ConceptDataManager(I_GetNidData nidData) throws IOException {
        super();
        this.nidData = nidData;
        this.lastChange = getDataVersion();
        this.lastWrite = this.lastChange;
    }

    @Override
    public void resetNidData() {
        this.nidData.reset();
    }

    private long getDataVersion() throws IOException {
        TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
        long dataVersion = Long.MIN_VALUE;
        if (readOnlyInput.available() > 0) {
            dataVersion = checkFormatAndVersion(readOnlyInput);
        }
        TupleInput readWriteInput = nidData.getMutableTupleInput();
        if (readWriteInput.available() > 0) {
            dataVersion = checkFormatAndVersion(readWriteInput);
        }
        return dataVersion;
    }
    protected long lastChange = Long.MIN_VALUE;
    protected long lastWrite = Long.MIN_VALUE;
    protected long lastExtinctRemoval = Long.MIN_VALUE;

    @Override
    public void modified() {
        lastChange = Bdb.gVersion.incrementAndGet();
    }

    @Override
    public void modified(long sequence) {
        lastChange = sequence;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getNid()
     */
    @Override
    public int getNid() {
        return enclosingConcept.getNid();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ihtsdo.db.bdb.concept.I_ManageConceptData#getReadWriteDataVersion()
     */
    @Override
    public int getReadWriteDataVersion() throws InterruptedException,
            ExecutionException, IOException {
        DataVersionBinder binder = DataVersionBinder.getBinder();
        return binder.entryToObject(nidData.getMutableTupleInput());
    }

    public boolean getIsAnnotationStyleRefset() throws IOException {
        AnnotationStyleBinder binder = AnnotationStyleBinder.getBinder();
        TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
        boolean isAnnotationStyle = false;
        if (readOnlyInput.available() > 0) {
            isAnnotationStyle = binder.entryToObject(readOnlyInput);
        }
        TupleInput readWriteInput = nidData.getMutableTupleInput();
        if (readWriteInput.available() > 0) {
            isAnnotationStyle = binder.entryToObject(readWriteInput);
        }
        return isAnnotationStyle;
    }

        public boolean getIsAnnotationStyleIndex() throws IOException {
        AnnotationIndexBinder binder = AnnotationIndexBinder.getBinder();
        TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
        boolean isIndex = false;
        if (readOnlyInput.available() > 0) {
            isIndex = binder.entryToObject(readOnlyInput);
        }
        TupleInput readWriteInput = nidData.getMutableTupleInput();
        if (readWriteInput.available() > 0) {
            isIndex = binder.entryToObject(readWriteInput);
        }
        return isIndex;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getDestRels()
     */
    @Override
    public List<Relationship> getDestRels() throws IOException {
        // Need to make sure there are no pending db writes prior calling this method. 
        BdbCommitManager.waitTillWritesFinished();

        List<Relationship> destRels = new ArrayList<Relationship>();
        for (NidPairForRel pair : Bdb.getDestRelPairs(enclosingConcept.getNid())) {
            int relNid = pair.getRelNid();
            int conceptNid = Bdb.getNidCNidMap().getCNid(relNid);
            Concept c = Bdb.getConceptForComponent(conceptNid);
            if (c != null) {
                Relationship r = c.getRelationship(relNid);
                if (r != null) {
                    destRels.add(r);
                }
            }
        }
        return destRels;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getDestRels()
     */
    @Override
    public List<Relationship> getDestRels(NidSetBI allowedTypes) throws IOException {
        // Need to make sure there are no pending db writes prior calling this method. 
        BdbCommitManager.waitTillWritesFinished();

        List<Relationship> destRels = new ArrayList<Relationship>();
        for (NidPairForRel pair : Bdb.getDestRelPairs(enclosingConcept.getNid())) {
            if (allowedTypes.contains(pair.getTypeNid())) {
                int relNid = pair.getRelNid();
                int conceptNid = Bdb.getNidCNidMap().getCNid(relNid);
                Concept c = Bdb.getConceptForComponent(conceptNid);
                if (c != null) {
                    Relationship r = c.getRelationship(relNid);
                    if (r != null) {
                        destRels.add(r);
                    }
                }
            }
        }
        return destRels;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
     * .component.description.Description)
     */
    @Override
    public void add(Description desc) throws IOException {
        getDescriptions().addDirect(desc);
        getDescNids().add(desc.nid);
        modified();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
     * .component.relationship.Relationship)
     */
    @Override
    public void add(Relationship rel) throws IOException {
        getSourceRels().addDirect(rel);
        getSrcRelNids().add(rel.nid);
        modified();
    }

    void processNewRel(Relationship rel) throws IOException {
        assert rel != null : "rel is null: " + this;
        assert rel.nid != 0 : "relNid is 0: " + this;
        assert rel.getTypeId() != 0 : "relTypeNid is 0: " + this;
        assert Bdb.getConceptForComponent(rel.nid) != null : "No concept for component: "
                + rel.nid
                + "\nsourceConcept: "
                + this.enclosingConcept.toLongString()
                + "\ndestConcept: "
                + Concept.get(rel.getC2Id()).toLongString();
        Bdb.addXrefPair(rel.getC2Id(),
                NidPair.getTypeNidRelNidPair(rel.getTypeNid(),
                rel.getNid()));
        getSrcRelNids().add(rel.nid);
        modified();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
     * .component.image.Image)
     */
    @Override
    public void add(Image img) throws IOException {
        getImages().addDirect(img);
        getImageNids().add(img.nid);
        modified();
    }

    void processNewImage(Image img) throws IOException {
        assert img.nid != 0 : "imgNid is 0: " + this;
        getImageNids().add(img.nid);
        modified();
    }

    public abstract boolean hasComponent(int nid) throws IOException;

    void processNewRefsetMember(RefsetMember<?, ?> refsetMember)
            throws IOException {
        assert refsetMember != null : "refsetMember is null: " + this;
        assert refsetMember.nid != 0 : "memberNid is 0: " + this;
        assert refsetMember.getComponentId() != 0 : "componentNid is 0: "
                + this;
        assert refsetMember.enclosingConceptNid != 0 : "refsetNid is 0: "
                + this;
        if (!isAnnotationStyleRefex()) {
            getMemberNids().add(refsetMember.nid);
            addToMemberMap(refsetMember);
            modified();
            Bdb.addXrefPair(refsetMember.getReferencedComponentNid(),
                    NidPair.getRefsetNidMemberNidPair(
                    refsetMember.getRefsetId(),
                    refsetMember.getNid()));
        }
    }

    protected abstract void addToMemberMap(RefsetMember<?, ?> refsetMember);

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getAllNids()
     */
    @Override
    public Collection<Integer> getAllNids() throws IOException {
        Collection<Integer> descNids = getDescNids();
        Collection<Integer> srcRelNids = getSrcRelNids();
        Collection<Integer> imgNids = getImageNids();
        Collection<Integer> memberNids = new ArrayList<Integer>(0);
        if (!isAnnotationStyleSet()) {
            memberNids = getMemberNids();
        }

        int size = 1 + descNids.size() + srcRelNids.size() + imgNids.size()
                + memberNids.size();

        ArrayList<Integer> allContainedNids = new ArrayList<Integer>(size);
        allContainedNids.add(enclosingConcept.getNid());
        assert enclosingConcept.getNid() != 0;
        assert !descNids.contains(0);
        allContainedNids.addAll(descNids);
        assert !srcRelNids.contains(0);
        allContainedNids.addAll(srcRelNids);
        assert !imgNids.contains(0);
        allContainedNids.addAll(imgNids);
        assert !memberNids.contains(0);
        allContainedNids.addAll(memberNids);
        return allContainedNids;
    }

    @Override
    public byte[] getReadOnlyBytes() throws IOException {
        return nidData.getReadOnlyBytes();
    }

    @Override
    public byte[] getReadWriteBytes() throws IOException {
        return nidData.getReadWriteBytes();
    }

    @Override
    public TupleInput getReadWriteTupleInput() throws IOException {
        return nidData.getMutableTupleInput();
    }

    void processNewDesc(Description e) throws IOException {
        assert e.nid != 0 : "descNid is 0: " + this;
        getDescNids().add(e.nid);
        BdbCommitManager.addUncommittedDescNid(e.nid);
        modified();
    }

    @Override
    public String toString() {
        return enclosingConcept.toLongString();
    }

    @Override
    public long getLastWrite() {
        return lastWrite;
    }

    @Override
    public void setLastWrite(long lastWrite) {
        this.lastWrite = Math.max(this.lastWrite, lastWrite);
    }

    @Override
    public long getLastChange() {
        return lastChange;
    }

    @Override
    public final boolean isUncommitted() {
        if (lastChange > BdbCommitManager.getLastCommit()) {
            return hasUncommittedComponents();
        }
        return false;
    }

    public abstract boolean hasUncommittedComponents();

    @Override
    public final boolean isUnwritten() {
        return lastChange > lastWrite;
    }

    protected long checkFormatAndVersion(TupleInput input)
            throws UnsupportedEncodingException {
        input.mark(128);
        int formatVersion = input.readInt();
        long dataVersion = input.readLong();
        if (formatVersion != OFFSETS.CURRENT_FORMAT_VERSION) {
            throw new UnsupportedEncodingException(
                    "No support for format version: " + formatVersion);
        }
        input.reset();
        return dataVersion;
    }

    @Override
    public boolean isPrimordial() throws IOException {
        return nidData.isPrimordial();
    }
}