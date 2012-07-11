package org.ihtsdo.concept.component.refset;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;

import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.refex.RefexAnalogBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.Set;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

public abstract class RefsetRevision<V extends RefsetRevision<V, C>, C extends RefsetMember<V, C>>
        extends Revision<V, C> implements I_ExtendByRefPart<V>, RefexAnalogBI<V> {

    public RefsetRevision() {
        super();
    }

    public RefsetRevision(int statusAtPositionNid, C primordialComponent) {
        super(statusAtPositionNid, primordialComponent);
    }

    public RefsetRevision(TkRevision eVersion, C member) {
        super(Bdb.uuidToNid(eVersion.getStatusUuid()), eVersion.getTime(), Bdb.uuidToNid(eVersion.getAuthorUuid()),
                 Bdb.uuidToNid(eVersion.getModuleUuid()), Bdb.uuidToNid(eVersion.getPathUuid()),  member);
    }

    public RefsetRevision(TupleInput input, C primordialComponent) {
        super(input, primordialComponent);
    }

    public RefsetRevision(int statusNid, long time, int authorNid, int moduleNid, int pathNid, C primordialComponent) {
        super(statusNid, time, authorNid, moduleNid, pathNid, primordialComponent);
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        allNids.add(primordialComponent.referencedComponentNid);
        allNids.add(primordialComponent.refsetNid);
        addRefsetTypeNids(allNids);
    }

    protected abstract void addRefsetTypeNids(Set<Integer> allNids);

    protected abstract void addSpecProperties(RefexCAB rcs);

    @Override
    public final int compareTo(RefexVersionBI o) {
        return this.toString().compareTo(o.toString());
    }

    @Override
    @Deprecated
    public I_ExtendByRefPart<V> duplicate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (RefsetRevision.class.isAssignableFrom(obj.getClass())) {
            RefsetRevision<?, ?> another = (RefsetRevision<?, ?>) obj;

            if (this.sapNid == another.sapNid) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        return primordialComponent.refexFieldsEqual(another);
    }

    public abstract V makeAnalog();

    public abstract boolean readyToWriteRefsetRevision();

    @Override
    public final boolean readyToWriteRevision() {
        assert readyToWriteRefsetRevision() : assertionString();

        return true;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    public String toUserString() {
        return toString();
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public int getRefexNid() {
        return primordialComponent.refsetNid;
    }

    @Override
    public RefsetMember getPrimordialVersion() {
        return primordialComponent;
    }

    @Override
    public int getReferencedComponentNid() {
        return primordialComponent.getReferencedComponentNid();
    }

    @Override
    public RefexCAB makeBlueprint(ViewCoordinate vc) throws IOException,
            InvalidCAB, ContradictionException {
        RefexCAB rcs = new RefexCAB(getTkRefsetType(),
                Ts.get().getUuidPrimordialForNid(getReferencedComponentNid()),
                getRefexNid(),
                getVersion(vc), vc);

        addSpecProperties(rcs);

        return rcs;
    }

    @Override
    @Deprecated
    public final int getStatus() {
        return getStatusNid();
    }

    protected abstract TK_REFEX_TYPE getTkRefsetType();

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setCollectionNid(int collectionNid) throws PropertyVetoException {
        primordialComponent.setCollectionNid(collectionNid);
    }

    @Override
    public void setReferencedComponentNid(int componentNid) throws PropertyVetoException {
        primordialComponent.setReferencedComponentNid(componentNid);
    }

    @Override
    @Deprecated
    public final void setStatus(int idStatus) {
        throw new UnsupportedOperationException();
    }
}
