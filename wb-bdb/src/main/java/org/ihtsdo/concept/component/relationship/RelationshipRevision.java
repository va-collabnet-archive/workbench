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
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import org.ihtsdo.db.bdb.BdbTerminologyStore;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;

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
        super(Bdb.uuidToNid(erv.getStatusUuid()),
                erv.getTime(),
                Bdb.uuidToNid(erv.getAuthorUuid()),
                Bdb.uuidToNid(erv.getModuleUuid()),
                Bdb.uuidToNid(erv.getPathUuid()),  
                primordialRel);
        this.characteristicNid = Bdb.uuidToNid(erv.getCharacteristicUuid());
        this.group = erv.getGroup();
        this.refinabilityNid = Bdb.uuidToNid(erv.getRefinabilityUuid());
        this.typeNid = Bdb.uuidToNid(erv.getTypeUuid());
        this.sapNid = Bdb.getSapNid(erv);
    }

    public RelationshipRevision(TupleInput input, Relationship primordialRel) {
        super(input.readInt(), primordialRel);
        this.characteristicNid = input.readInt();
        this.group = input.readSortedPackedInt();
        this.refinabilityNid = input.readInt();
        this.typeNid = input.readInt();
    }

    public RelationshipRevision(I_RelPart another, int statusNid, long time, int authorNid, int moduleNid, int pathNid,
            Relationship primordialRel) {
        super(statusNid, time, authorNid, moduleNid, pathNid, primordialRel);
        this.characteristicNid = another.getCharacteristicId();
        this.group = another.getGroup();
        this.refinabilityNid = another.getRefinabilityId();
        this.typeNid = another.getTypeNid();
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        allNids.add(primordialComponent.getTargetNid());
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
    public RelationshipRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {

        if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
            this.setStatusNid(statusNid);
            this.setAuthorNid(authorNid);
            this.setModuleNid(moduleNid);

            return this;
        }

        RelationshipRevision newR = new RelationshipRevision(this, statusNid,
                time, authorNid, moduleNid, pathNid, this.primordialComponent);

        this.primordialComponent.addRevision(newR);

        return newR;
    }

    @Override
    public RelationshipCAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB {
        TkRelationshipType relType = null;
        if (getCharacteristicNid() == SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid()
                || getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid()) {
            throw new InvalidCAB("Inferred relationships can not be used to make blueprints");
        } else if (getCharacteristicNid() == SnomedMetadataRf1.STATED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid()
                || getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid()) {
            relType = TkRelationshipType.STATED_HIERARCHY;
        }
        RelationshipCAB relBp = new RelationshipCAB(getSourceNid(),
                getTypeNid(),
                getTargetNid(),
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
        ConceptComponent.addTextToBuffer(buf, primordialComponent.getTargetNid());

        return buf.toString();
    }

    @Override
    public void writeFieldsToBdb(TupleOutput output) {
        output.writeInt(characteristicNid);
        output.writeSortedPackedInt(group);
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
    public int getTargetNid() {
        return primordialComponent.getTargetNid();
    }

    @Override
    public int getGroup() {
        return group;
    }

    @Override
    public int getSourceNid() {
        return primordialComponent.getSourceNid();
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
    public boolean isInferred() throws IOException {
        if(getAuthorNid() == Relationship.getClassifierAuthorNid()){
            return true;
        }else if(BdbTerminologyStore.isIsReleaseFormatSetup()){
            return getCharacteristicNid() == SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID();
        }
        return false;
    }

    @Override
    public boolean isStated() throws IOException {
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
    public void setTargetNid(int nid) throws PropertyVetoException {
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
