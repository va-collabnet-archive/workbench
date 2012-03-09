package org.ihtsdo.concept.component.relationship;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.Terms;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RelCAB;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelType;

public class RelationshipRevision extends Revision<RelationshipRevision, Relationship>
        implements I_RelPart<RelationshipRevision>, RelationshipAnalogBI<RelationshipRevision> {

    private int characteristicNid;
    private int group;
    private int refinabilityNid;
    private int typeNid;

    //~--- constructors --------------------------------------------------------
    public RelationshipRevision() {
        super();
    }

    public RelationshipRevision(Relationship primordialRel) {
        super(primordialRel.primordialSapNid, primordialRel);
        this.characteristicNid = primordialRel.getCharacteristicNid();
        this.group = primordialRel.getGroup();
        this.refinabilityNid = primordialRel.getRefinabilityNid();
        this.typeNid = primordialRel.getTypeNid();
    }

    public RelationshipRevision(int statusAtPositionNid, Relationship primordialRel) {
        super(statusAtPositionNid, primordialRel);
    }

    public RelationshipRevision(RelationshipRevision another, Relationship primordialRel) {
        super(another.sapNid, primordialRel);
        this.characteristicNid = another.characteristicNid;
        this.group = another.group;
        this.refinabilityNid = another.refinabilityNid;
        this.typeNid = another.typeNid;
    }

    public RelationshipRevision(TkRelationshipRevision erv, Relationship primordialRel) {
        super(Bdb.uuidToNid(erv.getStatusUuid()), Bdb.uuidToNid(erv.getAuthorUuid()),
                Bdb.uuidToNid(erv.getPathUuid()), erv.getTime(), primordialRel);
        this.characteristicNid = Bdb.uuidToNid(erv.getCharacteristicUuid());
        this.group = erv.getGroup();
        this.refinabilityNid = Bdb.uuidToNid(erv.getRefinabilityUuid());
        this.typeNid = Bdb.uuidToNid(erv.getTypeUuid());
        this.sapNid = Bdb.getSapNid(erv);
    }

    public RelationshipRevision(TupleInput input, Relationship primordialRel) {
        super(input.readInt(), primordialRel);
        this.characteristicNid = input.readInt();
        this.group = input.readInt();
        this.refinabilityNid = input.readInt();
        this.typeNid = input.readInt();
    }

    public RelationshipRevision(I_RelPart another, int statusNid, int authorNid, int pathNid, long time,
            Relationship primordialRel) {
        super(statusNid, authorNid, pathNid, time, primordialRel);
        this.characteristicNid = another.getCharacteristicId();
        this.group = another.getGroup();
        this.refinabilityNid = another.getRefinabilityId();
        this.typeNid = another.getTypeNid();
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        allNids.add(primordialComponent.getDestinationNid());
        allNids.add(characteristicNid);
        allNids.add(refinabilityNid);
        allNids.add(typeNid);
    }

    @Override
    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RelationshipRevision duplicate() {
        return new RelationshipRevision(this, primordialComponent);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (RelationshipRevision.class.isAssignableFrom(obj.getClass())) {
            RelationshipRevision another = (RelationshipRevision) obj;

            return this.sapNid == another.sapNid;
        }

        return false;
    }

    @Override
    public RelationshipRevision makeAnalog(int statusNid, int pathNid, long time) {
        if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
            this.setStatusNid(statusNid);

            return this;
        }

        RelationshipRevision newR = new RelationshipRevision(this.primordialComponent, statusNid,
                Terms.get().getAuthorNid(), pathNid, time, this.primordialComponent);

        this.primordialComponent.addRevision(newR);

        return newR;
    }

    @Override
    public RelationshipRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {

        if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
            this.setStatusNid(statusNid);
            this.setAuthorNid(authorNid);

            return this;
        }

        RelationshipRevision newR = new RelationshipRevision(this,
                statusNid, authorNid, pathNid, time, this.primordialComponent);

        this.primordialComponent.addRevision(newR);

        return newR;
    }

    @Override
    public RelCAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB {
        TkRelType relType = null;
        if (getCharacteristicNid() == SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid()
                || getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid()) {
            throw new InvalidCAB("Inferred relationships can not be used to make blueprints");
        } else if (getCharacteristicNid() == SnomedMetadataRf1.STATED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid()
                || getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid()) {
            relType = TkRelType.STATED_HIERARCHY;
        }
        RelCAB relBp = new RelCAB(getOriginNid(),
                getTypeNid(),
                getDestinationNid(),
                getGroup(),
                relType,
                getVersion(vc),
                vc);
        return relBp;
    }

    @Override
    public boolean readyToWriteRevision() {
        assert characteristicNid != Integer.MAX_VALUE : assertionString();
        assert group != Integer.MAX_VALUE : assertionString();
        assert refinabilityNid != Integer.MAX_VALUE : assertionString();
        assert typeNid != Integer.MAX_VALUE : assertionString();

        return true;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(this.getClass().getSimpleName()).append(":{");
        buf.append("src:");
        ConceptComponent.addNidToBuffer(buf, this.primordialComponent.enclosingConceptNid);
        buf.append(" t:");
        ConceptComponent.addNidToBuffer(buf, typeNid);
        buf.append(" dest:");
        ConceptComponent.addNidToBuffer(buf, this.primordialComponent.getC2Id());
        buf.append(" c:");
        ConceptComponent.addNidToBuffer(buf, this.characteristicNid);
        buf.append(" g:").append(this.group);
        buf.append(" r:");
        ConceptComponent.addNidToBuffer(buf, this.refinabilityNid);
        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    public String toUserString() {
        StringBuffer buf = new StringBuffer();

        ConceptComponent.addTextToBuffer(buf, typeNid);
        buf.append(": ");
        ConceptComponent.addTextToBuffer(buf, primordialComponent.getDestinationNid());

        return buf.toString();
    }

    @Override
    public void writeFieldsToBdb(TupleOutput output) {
        output.writeInt(characteristicNid);
        output.writeInt(group);
        output.writeInt(refinabilityNid);
        output.writeInt(typeNid);
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public int getCharacteristicId() {
        return characteristicNid;
    }

    @Override
    public int getCharacteristicNid() {
        return characteristicNid;
    }

    @Override
    public int getDestinationNid() {
        return primordialComponent.getDestinationNid();
    }

    @Override
    public int getGroup() {
        return group;
    }

    @Override
    public int getOriginNid() {
        return primordialComponent.getOriginNid();
    }

    @Override
    public Relationship getPrimordialVersion() {
        return primordialComponent;
    }

    @Override
    public int getRefinabilityId() {
        return refinabilityNid;
    }

    @Override
    public int getRefinabilityNid() {
        return refinabilityNid;
    }

    @Deprecated
    @Override
    public int getTypeId() {
        return typeNid;
    }

    @Override
    public int getTypeNid() {
        return typeNid;
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList nids = new ArrayIntList(5);

        nids.add(characteristicNid);
        nids.add(refinabilityNid);
        nids.add(typeNid);

        return nids;
    }

    @Override
    public Relationship.Version getVersion(ViewCoordinate c) throws ContradictionException {
        return primordialComponent.getVersion(c);
    }

    @Override
    public Collection<? extends Relationship.Version> getVersions() {
        return ((Relationship) primordialComponent).getVersions();
    }

    @Override
    public Collection<Relationship.Version> getVersions(ViewCoordinate c) {
        return primordialComponent.getVersions(c);
    }

    @Override
    public boolean isInferred() {
        return getAuthorNid() == Relationship.getClassifierAuthorNid();
    }

    @Override
    public boolean isStated() {
        return !isInferred();
    }

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setCharacteristicId(int characteristicNid) {
        this.characteristicNid = characteristicNid;
        modified();
    }

    @Override
    public void setCharacteristicNid(int characteristicNid) {
        this.characteristicNid = characteristicNid;
    }

    @Override
    public void setDestinationNid(int nid) throws PropertyVetoException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGroup(int group) {
        this.group = group;
        modified();
    }

    @Override
    public void setRefinabilityId(int refinabilityNid) {
        this.refinabilityNid = refinabilityNid;
        modified();
    }

    @Override
    public void setRefinabilityNid(int refinabilityNid) {
        this.refinabilityNid = refinabilityNid;
    }

    @Deprecated
    @Override
    public void setTypeId(int typeNid) {
        this.typeNid = typeNid;
        modified();
    }

    @Override
    public void setTypeNid(int typeNid) {
        this.typeNid = typeNid;
        modified();
    }
}