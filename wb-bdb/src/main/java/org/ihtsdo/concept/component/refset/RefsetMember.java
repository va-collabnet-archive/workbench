package org.ihtsdo.concept.component.refset;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.db.util.NidPair;
import org.ihtsdo.db.util.NidPairForRefset;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexAnalogBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.TerminologySnapshotDI;

public abstract class RefsetMember<R extends RefsetRevision<R, C>, C extends RefsetMember<R, C>>
        extends ConceptComponent<R, C>
        implements I_ExtendByRef,
        RefexChronicleBI<R>,
        RefexAnalogBI<R> {

    public int referencedComponentNid;
    public int refsetNid;

    public class Version
            extends ConceptComponent<R, C>.Version
            implements I_ExtendByRefVersion<R>, I_ExtendByRefPart<R>, RefexAnalogBI<R> {

        public Version() {
            super();
        }

        public Version(int index) {
            super(index);
        }

        @Override
        public RefsetMember getPrimordialVersion() {
            return RefsetMember.this;
        }

        public RefexCAB getRefexEditSpec() throws IOException {
            if (index >= 0) {
                return revisions.get(index).getRefexEditSpec();
            } else {
                return revisions.get(index).getRefexEditSpec();
            }
        }

        @Override
        public int getCollectionNid() {
            return refsetNid;
        }

        @Override
        public ArrayIntList getVariableVersionNids() {
            if (index >= 0) {
                return revisions.get(index).getVariableVersionNids();
            } else {
                return RefsetMember.this.getVariableVersionNids();
            }
        }

        @Override
        public RefsetRevision<?, ?> makeAnalog(int statusNid, int pathNid, long time) {
            if (index >= 0) {
                return revisions.get(index).makeAnalog(statusNid, pathNid, time);
            }
            return (RefsetRevision<?, ?>) RefsetMember.this.makeAnalog(statusNid, pathNid, time);
        }

        @Override
        public R makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
            if (index >= 0) {
                return revisions.get(index).makeAnalog(statusNid, authorNid, pathNid, time);
            }
            return (R) RefsetMember.this.makeAnalog(statusNid, authorNid, pathNid, time);
        }

        public R makeAnalog() {
            if (index >= 0) {
                return revisions.get(index).makeAnalog();
            }
            return (R) RefsetMember.this.makeAnalog();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void addVersion(I_ExtendByRefPart<R> part) {
            versions = null;
            RefsetMember.this.addRevision((R) part);
        }

        @Override
        public int getComponentId() {
            return referencedComponentNid;
        }

        @Override
        public I_ExtendByRef getCore() {
            return RefsetMember.this;
        }

        @Override
        public int getMemberId() {
            return nid;
        }

        @Override
        public int getRefsetId() {
            return refsetNid;
        }

        @Override
        @Deprecated
        public int getStatus() {
            if (index >= 0) {
                return revisions.get(index).getStatus();
            }
            return RefsetMember.this.getStatusId();
        }

        @Override
        public int getTypeId() {
            return RefsetMember.this.getTypeNid();
        }

        public int getTypeNid() {
            return RefsetMember.this.getTypeNid();
        }

        public List<? extends I_ExtendByRefPart<R>> getVersions() {
            return RefsetMember.this.getVersions();
        }

        public void setCollectionNid(int collectionNid) throws PropertyVetoException {
            RefsetMember.this.setCollectionNid(collectionNid);
        }

        @Override
        @Deprecated
        public void setStatus(int idStatus) {
            if (index >= 0) {
                revisions.get(index).setStatus(idStatus);
            } else {
                RefsetMember.this.setStatusId(idStatus);
            }
        }

        @Override
        public UniversalAceExtByRefPart getUniversalPart()
                throws TerminologyException, IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public I_ExtendByRefPart<R> makePromotionPart(PathBI promotionPath) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int compareTo(I_ExtendByRefPart<R> o) {
            if (Version.class.isAssignableFrom(o.getClass())) {
                Version another = (Version) o;
                if (getSapNid() != another.getSapNid()) {
                    return this.getSapNid() - another.getSapNid();
                }
                return this.index - another.index;
            }
            return this.toString().compareTo(o.toString());
        }

        @Override
        public I_ExtendByRefPart<R> duplicate() {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("unchecked")
        @Override
        public I_ExtendByRefPart<R> getMutablePart() {
            return (I_ExtendByRefPart<R>) super.getMutablePart();
        }

        public TkRefsetAbstractMember<?> getERefsetMember() throws TerminologyException, IOException {
            throw new UnsupportedOperationException("subclass must override");
        }

        public TkRevision getERefsetRevision() throws TerminologyException, IOException {
            throw new UnsupportedOperationException("subclass must override");
        }

        @Override
        public RefsetMember<R, C>.Version getVersion(ViewCoordinate c)
                throws ContraditionException {
            return RefsetMember.this.getVersion(c);
        }

        @Override
        public Collection<RefsetMember<R, C>.Version> getVersions(ViewCoordinate c) {
            return RefsetMember.this.getVersions(c);
        }

        @Override
        public int getReferencedComponentNid() {
            return RefsetMember.this.getReferencedComponentNid();
        }

        @Override
        public void setReferencedComponentNid(int componentNid) throws PropertyVetoException {
            RefsetMember.this.setReferencedComponentNid(componentNid);
        }
        
        @Override
        public int hashCodeOfParts() {
        	return 0;
        }

    }

    public RefsetMember(int enclosingConceptNid, TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public abstract R makeAnalog();

    public RefsetMember(TkRefsetAbstractMember<?> refsetMember,
            int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        refsetNid = Bdb.uuidToNid(refsetMember.refsetUuid);
        referencedComponentNid = Bdb.uuidToNid(refsetMember.getComponentUuid());
        primordialSapNid = Bdb.getSapNid(refsetMember);
        assert primordialSapNid != Integer.MAX_VALUE;
        assert referencedComponentNid != Integer.MAX_VALUE;
        assert refsetNid != Integer.MAX_VALUE;
    }

    public RefsetMember() {
        super();
        referencedComponentNid = Integer.MAX_VALUE;
        refsetNid = Integer.MAX_VALUE;
    }

    @SuppressWarnings("unchecked")
    public RefsetMember<R, C> merge(RefsetMember<R, C> component) throws IOException {
        return (RefsetMember<R, C>) super.merge((C) component);
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(" refset:");
        addNidToBuffer(buf, refsetNid);
        buf.append(" type:");
        try {
            buf.append(REFSET_TYPES.nidToType(getTypeNid()));
        } catch (IOException e) {
            buf.append(e.getLocalizedMessage());
        }
        buf.append(" rcNid:");
        addNidToBuffer(buf, referencedComponentNid);
        buf.append(" ");
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean fieldsEqual(ConceptComponent<R, C> obj) {
        if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            RefsetMember<R, C> another = (RefsetMember<R, C>) obj;
            if (this.getTypeNid() != another.getTypeNid()) {
                return false;
            }
            if (membersEqual(obj)) {
                return conceptComponentFieldsEqual(another);
            }
        }
        return false;
    }

    /**
     * Test method to check to see if two objects are equal in all respects.
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures.
     * @throws IOException
     */
    public String validate(RefsetMember<?, ?> another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();

        if (this.referencedComponentNid != another.referencedComponentNid) {
            buf.append("\tRefsetMember.referencedComponentNid not equal: \n"
                    + "\t\tthis.referencedComponentNid = " + this.referencedComponentNid + "\n"
                    + "\t\tanother.referencedComponentNid = " + another.referencedComponentNid + "\n");
        }
        // Compare the parents
        buf.append(super.validate(another));
        return buf.toString();
    }

    protected abstract boolean membersEqual(ConceptComponent<R, C> obj);

    @Override
    public void readFromBdb(TupleInput input) {
        refsetNid = input.readInt();
        referencedComponentNid = input.readInt();
        assert refsetNid != Integer.MAX_VALUE;
        assert referencedComponentNid != Integer.MAX_VALUE;
        readMemberFields(input);
        int additionalVersionCount = input.readShort();
        if (additionalVersionCount > 0) {
            if (revisions == null) {
                revisions = new CopyOnWriteArrayList<R>();
            }
            for (int i = 0; i < additionalVersionCount; i++) {
                R r = readMemberRevision(input);
                if (r.getTime() != Long.MIN_VALUE) {
                    revisions.add(r);
                }
            }
        }
    }

    protected abstract void readMemberFields(TupleInput input);

    protected abstract R readMemberRevision(TupleInput input);

    @Override
    public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        List<RefsetRevision<R, C>> additionalVersionsToWrite = new ArrayList<RefsetRevision<R, C>>();
        if (revisions != null) {
            for (RefsetRevision<R, C> p : revisions) {
                if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid
                        && p.getTime() != Long.MIN_VALUE) {
                    additionalVersionsToWrite.add(p);
                }
            }
        }
        assert refsetNid != Integer.MAX_VALUE;
        assert referencedComponentNid != Integer.MAX_VALUE;
        output.writeInt(refsetNid);
        output.writeInt(referencedComponentNid);
        writeMember(output);
        output.writeShort(additionalVersionsToWrite.size());

        NidPairForRefset npr = NidPair.getRefsetNidMemberNidPair(refsetNid, nid);
        Bdb.addXrefPair(referencedComponentNid, npr);

        for (RefsetRevision<R, C> p : additionalVersionsToWrite) {
            p.writePartToBdb(output);
        }
    }

    protected abstract void writeMember(TupleOutput output);

    @Override
    public boolean promote(PositionBI viewPosition,
            PathSetReadOnly pomotionPaths, NidSetBI allowedStatus, Precedence precedence)
            throws IOException, TerminologyException {
        int viewPathId = viewPosition.getPath().getConceptNid();
        Collection<Version> matchingTuples =
                getVersionComputer().getSpecifiedVersions(allowedStatus,
                viewPosition,
                getVersions(), precedence, null);
        boolean promotedAnything = false;
        for (PathBI promotionPath : pomotionPaths) {
            for (Version v : matchingTuples) {
                if (v.getPathNid() == viewPathId) {
                    RefsetRevision<?, ?> revision = v.makeAnalog(v.getStatusNid(),
                            promotionPath.getConceptNid(), Long.MAX_VALUE);
                    addVersion(revision);
                    promotedAnything = true;
                }
            }
        }
        return promotedAnything;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addVersion(@SuppressWarnings("rawtypes") I_ExtendByRefPart part) {
        versions = null;
        super.addRevision((R) part);
    }

    @Override
    public int getComponentId() {
        return referencedComponentNid;
    }

    @Override
    public int getComponentNid() {
        return referencedComponentNid;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getTypeNid() {
        return getTypeId();
    }

    @Override
    public int getMemberId() {
        return nid;
    }

    @Override
    public int getRefsetId() {
        return refsetNid;
    }

    @Override
    public void setRefsetId(int refsetNid) throws IOException {
        if (getTime() == Long.MAX_VALUE) {
            if (this.refsetNid != refsetNid) {
                this.refsetNid = refsetNid;
            }
        } else {
            throw new UnsupportedOperationException("Cannot change refset unless member is uncommitted...");
        }
    }

    @Override
    public void setTypeId(int typeId) {
        if (typeId != getTypeNid()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public RefsetMember<R, C> getMutablePart() {
        return this;
    }

    public UniversalAceExtByRefPart getUniversalPart()
            throws TerminologyException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends I_ExtendByRefPart<R>> getMutableParts() {
        return getVersions();
    }
    protected List<? extends Version> versions;

    public List<Version> getTuples() {
        return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
    }

    @SuppressWarnings("unchecked")
    public List<? extends Version> getVersions() {
        if (versions == null) {
            int count = 1;
            if (revisions != null) {
                count = count + revisions.size();
            }
            ArrayList<Version> list = new ArrayList<Version>(count);
            list.add(new Version());
            if (revisions != null) {
                for (int i = 0; i < revisions.size(); i++) {
                    list.add(new Version(i));
                }
            }
            versions = list;
        }
        return (List<Version>) versions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (RefsetMember.class.isAssignableFrom(obj.getClass())) {
            RefsetMember<?, ?> another = (RefsetMember<?, ?>) obj;
            return this.referencedComponentNid == another.referencedComponentNid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[]{referencedComponentNid});
    }

    public int getReferencedComponentNid() {
        return referencedComponentNid;
    }

    public void setReferencedComponentNid(int referencedComponentNid) {
        this.referencedComponentNid = referencedComponentNid;
        modified();
    }

    @SuppressWarnings("unchecked")
    public I_ExtendByRefPart<R> makePromotionPart(PathBI promotionPath) {
        return (I_ExtendByRefPart<R>) makeAnalog(getStatusNid(), promotionPath.getConceptNid(), Long.MAX_VALUE);
    }

    public final int compareTo(I_ExtendByRefPart<R> o) {
        return this.toString().compareTo(o.toString());
    }

    protected abstract VersionComputer<RefsetMember<R, C>.Version> getVersionComputer();

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void addTuples(I_IntSet allowedStatus, PositionSetReadOnly positions,
            List<I_ExtendByRefVersion> returnTuples, Precedence precedencePolicy, I_ManageContradiction contradictionManager)
            throws TerminologyException, IOException {
        List<RefsetMember<R, C>.Version> versionsToAdd = new ArrayList<RefsetMember<R, C>.Version>();
        getVersionComputer().addSpecifiedVersions(allowedStatus,
                positions, versionsToAdd, (List<Version>) getVersions(), precedencePolicy, contradictionManager);
        returnTuples.addAll((Collection<? extends I_ExtendByRefVersion>) versionsToAdd);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addTuples(List<I_ExtendByRefVersion> returnTuples,
            Precedence precedencePolicy, I_ManageContradiction contradictionManager)
            throws TerminologyException, IOException {
        List<RefsetMember<R, C>.Version> versionsToAdd = new ArrayList<RefsetMember<R, C>.Version>();
        getVersionComputer().addSpecifiedVersions(Terms.get().getActiveAceFrameConfig().getAllowedStatus(),
                Terms.get().getActiveAceFrameConfig().getViewPositionSetReadOnly(), versionsToAdd,
                (List<Version>) getVersions(), precedencePolicy, contradictionManager);
        returnTuples.addAll((Collection<? extends I_ExtendByRefVersion<R>>) versionsToAdd);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<? extends I_ExtendByRefVersion> getTuples(I_IntSet allowedStatus,
            PositionSetReadOnly positions, Precedence precedencePolicy, I_ManageContradiction contradictionManager)
            throws TerminologyException, IOException {
        List<RefsetMember<R, C>.Version> versionsToAdd = new ArrayList<RefsetMember<R, C>.Version>();
        getVersionComputer().addSpecifiedVersions(allowedStatus,
                positions, versionsToAdd, (List<Version>) getVersions(), precedencePolicy, contradictionManager);
        return versionsToAdd;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<? extends I_ExtendByRefVersion> getTuples(I_ManageContradiction contradictionMgr)
            throws TerminologyException, IOException {
        // TODO Implement contradictionMgr part...
        return getVersions();
    }

    @Override
    public RefsetMember<R, C>.Version getVersion(ViewCoordinate c)
            throws ContraditionException {
        List<RefsetMember<R, C>.Version> vForC = getVersions(c);
        if (vForC.isEmpty()) {
            return null;
        }
        if (vForC.size() > 1) {
            throw new ContraditionException(vForC.toString());
        }
        return vForC.get(0);
    }

    @Override
    public List<RefsetMember<R, C>.Version> getVersions(ViewCoordinate c) {
        List<RefsetMember<R, C>.Version> returnTuples = new ArrayList<RefsetMember<R, C>.Version>(2);
        getVersionComputer().addSpecifiedVersions(c.getAllowedStatusNids(), (NidSetBI) null, c.getPositionSet(),
                returnTuples, getVersions(), c.getPrecedence(), c.getContradictionManager());
        return returnTuples;
    }

    @Override
    public void clearVersions() {
        versions = null;
    }

    @Override
    public boolean hasExtensions() throws IOException {
        return getEnclosingConcept().hasExtensionsForComponent(nid);
    }

    @Override
    public String toUserString() {
        return toString();
    }

    @Override
    public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContraditionException {
        ComponentVersionBI c1Component = snapshot.getConceptVersion(refsetNid);
        return "refex: " + c1Component.toUserString(snapshot);
    }

    @Override
    public int getCollectionNid() {
        return refsetNid;
    }

    @Override
    public void setCollectionNid(int collectionNid) throws PropertyVetoException {
        if (this.refsetNid == Integer.MAX_VALUE
                || this.refsetNid == collectionNid
                || getTime() == Long.MAX_VALUE) {
            if (this.refsetNid != collectionNid) {
                this.refsetNid = collectionNid;
            }
        } else {
            throw new PropertyVetoException("Cannot change refset unless member is uncommitted...", null);
        }
    }

    @Override
    public RefexCAB getRefexEditSpec() throws IOException {
        RefexCAB rcs = new RefexCAB(getTkRefsetType(),
                getReferencedComponentNid(), getRefsetId(), getPrimUuid());
        addSpecProperties(rcs);
        return rcs;
    }

    protected abstract TK_REFSET_TYPE getTkRefsetType();

    protected abstract void addSpecProperties(RefexCAB rcs);

    @Override
    public RefsetMember getPrimordialVersion() {
        return RefsetMember.this;
    }
}
