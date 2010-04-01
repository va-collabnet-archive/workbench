package org.ihtsdo.concept.component.identifier;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.I_HandleFutureStatusAtPositionSetup;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.sap.StatusAtPositionBdb;
import org.ihtsdo.etypes.EIdentifier;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class IdentifierVersion implements I_IdPart, I_IdVersion, I_HandleFutureStatusAtPositionSetup {

    private static StatusAtPositionBdb sapBdb = Bdb.getSapDb();

    private transient ConceptComponent<?, ?> conceptComponent;
    private int statusAtPositionNid;
    private int authorityNid;

    protected IdentifierVersion(int statusNid, int pathNid, long time) {
        this.statusAtPositionNid = sapBdb.getSapNid(statusNid, pathNid, time);
    }

    protected IdentifierVersion(int statusNid, int pathNid, long time, IdentifierVersion idVersion) {
        this(statusNid, pathNid, time);
    }

    protected IdentifierVersion(TupleInput input) {
        super();
        statusAtPositionNid = input.readInt();
        authorityNid = input.readInt();

    }

    protected IdentifierVersion(EIdentifier idv) {
        super();
        this.statusAtPositionNid =
                sapBdb.getSapNid(Bdb.uuidToNid(idv.getStatusUuid()), Bdb.uuidToNid(idv.getPathUuid()), idv.getTime());
        this.authorityNid = Bdb.uuidToNid(idv.getAuthorityUuid());
    }

    protected IdentifierVersion() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup#isSetup()
     */
    public boolean isSetup() {
        return statusAtPositionNid != Integer.MAX_VALUE;
    }

    /*
     * (non-Javadoc)
     * @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup#setStatusAtPositionNid(int)
     */
    public void setStatusAtPositionNid(int sapNid) {
        this.statusAtPositionNid = sapNid;
    }

    public abstract ConceptComponent.IDENTIFIER_PART_TYPES getType();

    public final void writeIdPartToBdb(TupleOutput output) {
        output.writeInt(statusAtPositionNid);
        output.writeInt(authorityNid);
        writeSourceIdToBdb(output);
    }

    protected abstract void writeSourceIdToBdb(TupleOutput output);

    @Override
    public int getAuthorityNid() {
        return authorityNid;
    }

    @Override
    public void setAuthorityNid(int sourceNid) {
        this.authorityNid = sourceNid;
    }

    protected ArrayIntList getVariableVersionNids() {
        ArrayIntList nids = new ArrayIntList(3);
        nids.add(authorityNid);
        return nids;
    }

    @Override
    public I_IdPart duplicateIdPart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayIntList getPartComponentNids() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getPathId() {
        return sapBdb.getPathId(statusAtPositionNid);
    }

    @Override
    public int getStatusId() {
        return sapBdb.getStatusId(statusAtPositionNid);
    }

    @Override
    public long getTime() {
        return sapBdb.getTime(statusAtPositionNid);
    }

    @Override
    public int getVersion() {
        return ThinVersionHelper.convert(getTime());
    }

    @Override
    public void setPathId(int pathId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatusId(int statusId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setVersion(int version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public I_Identify getIdentifier() {
        return conceptComponent;
    }

    @Override
    public I_IdPart getMutableIdPart() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<TimePathId> getTimePathSet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<UUID> getUUIDs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final I_Identify getFixedIdPart() {
        return conceptComponent;
    }

    @Override
    public int getNid() {
        return conceptComponent.nid;
    }

    public int getSapNid() {
        return statusAtPositionNid;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("sap:" + statusAtPositionNid);
        buf.append(" conceptComponent:" + conceptComponent);
        buf.append(" authority:");
        ConceptComponent.addNidToBuffer(buf, authorityNid);
        buf.append(" path:");
        ConceptComponent.addNidToBuffer(buf, getPathId());
        buf.append(" tm:");
        buf.append(Revision.fileDateFormat.format(new Date(getTime())));
        buf.append(" status:");
        ConceptComponent.addNidToBuffer(buf, getStatusId());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (IdentifierVersion.class.isAssignableFrom(obj.getClass())) {
            IdentifierVersion another = (IdentifierVersion) obj;
            return this.statusAtPositionNid == another.statusAtPositionNid && this.authorityNid == another.authorityNid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { statusAtPositionNid, authorityNid });
    }

}
