package org.ihtsdo.concept.component.relationship;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.db.util.NidPair;
import org.ihtsdo.db.util.NidPairForRel;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Relationship extends ConceptComponent<RelationshipRevision, Relationship>
        implements I_RelVersioned<RelationshipRevision>,
        I_RelPart<RelationshipRevision>,
        RelationshipAnalogBI<RelationshipRevision> {

    private static int classifierAuthorNid = Integer.MIN_VALUE;
    public static int getClassifierAuthorNid() {
        if (classifierAuthorNid == Integer.MIN_VALUE) {
            try {
                classifierAuthorNid = org.dwfa.cement.ArchitectonicAuxiliary.Concept.SNOROCKET.localize().getNid();
            } catch (IOException ex) {
               throw new RuntimeException(ex);
            } catch (TerminologyException ex) {
                throw new RuntimeException(ex);
           }
        }
        return classifierAuthorNid;
    }
    
    public class Version
            extends ConceptComponent<RelationshipRevision, Relationship>.Version
            implements I_RelTuple<RelationshipRevision>,
            I_RelPart<RelationshipRevision>,
            RelationshipAnalogBI<RelationshipRevision> {

        public Version() {
            super();
        }

        public Version(int index) {
            super(index);
        }

        @Override
        public int getC1Id() {
            return getEnclosingConcept().getNid();
        }

        @Override
        public int getC2Id() {
            return c2Nid;
        }

        @Override
        public boolean isInferred() {
            if (index >= 0) {
                return revisions.get(index).isInferred();
            }
            return Relationship.this.isInferred();
        }

        @Override
        public boolean isStated() {
            return !isInferred();
        }

        @Override
        public int getCharacteristicId() {
            if (index >= 0) {
                return revisions.get(index).getCharacteristicId();
            }
            return getCharacteristicNid();
        }

        @Override
        public int getGroup() {
            if (index >= 0) {
                return revisions.get(index).getGroup();
            }
            return group;
        }

        @Override
        @Deprecated
        public int getRefinabilityId() {
            if (index >= 0) {
                return revisions.get(index).getRefinabilityId();
            }
            return getRefinabilityNid();
        }

        @Override
        public int getRefinabilityNid() {
            if (index >= 0) {
                return revisions.get(index).getRefinabilityNid();
            }
            return Relationship.this.getRefinabilityNid();
        }

        @Override
        public int getCharacteristicNid() {
            if (index >= 0) {
                return revisions.get(index).getCharacteristicNid();
            }
            return Relationship.this.getCharacteristicNid();
        }

        @Override
        public int getDestinationNid() {
            return Relationship.this.c2Nid;
        }

        @Override
        public int getOriginNid() {
            return Relationship.this.enclosingConceptNid;
        }

        @Override
        public int getRelId() {
            return nid;
        }

        @Override
        public I_RelVersioned getRelVersioned() {
            return Relationship.this;
        }

        @Override
        @Deprecated
        public void setCharacteristicId(int characteristicId) {
            if (index >= 0) {
                revisions.get(index).setCharacteristicId(characteristicId);
            } else {
                Relationship.this.setCharacteristicId(characteristicId);
            }
        }

        @Override
        public void setCharacteristicNid(int characteristicNid) {
            if (index >= 0) {
                revisions.get(index).setCharacteristicNid(characteristicNid);
            } else {
                Relationship.this.setCharacteristicNid(characteristicNid);
            }
        }

        @Override
        public void setDestinationNid(int destNid) throws PropertyVetoException {
            if (Relationship.this.getTime() == Long.MAX_VALUE) {
                Relationship.this.setDestinationNid(destNid);
            } else {
                throw new UnsupportedOperationException("Relationship.this.getTime() != Long.MAX_VALUE");
            }
        }

        @Override
        public void setRefinabilityNid(int refinabilityNid) {
            if (index >= 0) {
                revisions.get(index).setRefinabilityNid(refinabilityNid);
            } else {
                Relationship.this.setRefinabilityNid(refinabilityNid);
            }
        }

        @Override
        public void setGroup(int group) {
            if (index >= 0) {
                revisions.get(index).setGroup(group);
            } else {
                Relationship.this.setGroup(group);
            }
        }

        @Override
        public void setRefinabilityId(int refinabilityId) {
            if (index >= 0) {
                revisions.get(index).setRefinabilityId(refinabilityId);
            } else {
                Relationship.this.setRefinabilityId(refinabilityId);
            }
        }

        @Override
        public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
            throw new UnsupportedOperationException();
        }

        public int getTypeNid() {
            if (index >= 0) {
                assert revisions.get(index).getTypeNid() != Integer.MAX_VALUE : Relationship.this;
                return revisions.get(index).getTypeNid();
            } else {
                assert Relationship.this.typeNid != Integer.MAX_VALUE : Relationship.this;
                return Relationship.this.typeNid;
            }
        }

        @Override
        @Deprecated
        public int getTypeId() {
            return getTypeNid();
        }

        public Concept getType() throws IOException {
            return Bdb.getConcept(getTypeNid());
        }

        @Override
        @Deprecated
        public void setTypeId(int type) {
            if (index >= 0) {
                revisions.get(index).setTypeNid(type);
            } else {
                Relationship.this.setTypeNid(type);
            }
        }

        @Override
        public Relationship.Version getVersion(ViewCoordinate c)
                throws ContraditionException {
            return Relationship.this.getVersion(c);
        }

        @Override
        public Collection<Relationship.Version> getVersions(
                ViewCoordinate c) {
            return Relationship.this.getVersions(c);
        }

        public List<? extends Version> getVersions() {
            return Relationship.this.getVersions();
        }

        public void setTypeNid(int type) {
            if (index >= 0) {
                revisions.get(index).setTypeNid(type);
            } else {
                Relationship.this.setTypeNid(type);
            }

        }

        public Relationship getFixedPart() {
            return Relationship.this;
        }

        public ArrayIntList getVariableVersionNids() {
            if (index >= 0) {
                ArrayIntList resultList = new ArrayIntList(7);
                resultList.add(getCharacteristicId());
                resultList.add(getRefinabilityId());
                resultList.add(getTypeNid());
                resultList.add(getC1Id());
                resultList.add(getC2Id());
                return resultList;
            }
            return Relationship.this.getVariableVersionNids();
        }

        @Override
        public RelationshipRevision makeAnalog(int statusNid, int pathNid, long time) {
             if (index >= 0) {
                RelationshipRevision rev = revisions.get(index);
                if (rev.getTime() == Long.MAX_VALUE && rev.getPathNid() == pathNid) {
                    rev.setStatusNid(statusNid);
                    return rev;
                }
                return rev.makeAnalog(statusNid, pathNid, time);
            } else {
                return Relationship.this.makeAnalog(statusNid, pathNid, time);
            }
        }

        @Override
        public RelationshipRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
            RelationshipRevision newR;
            if (index >= 0) {
                RelationshipRevision rev = revisions.get(index);
                if (rev.getTime() == Long.MAX_VALUE && rev.getPathNid() == pathNid) {
                    rev.setStatusNid(statusNid);
                    rev.setAuthorNid(authorNid);
                    newR = rev;
                } else {
                    newR = rev.makeAnalog(statusNid, authorNid, pathNid, time);
                }
            } else {
                newR = Relationship.this.makeAnalog(statusNid, authorNid, pathNid, time);
            }
            return newR;
        }

        public RelationshipRevision makeAnalog() {
            if (index >= 0) {
                RelationshipRevision rev = revisions.get(index);
                return new RelationshipRevision(rev, Relationship.this);
            }
            return new RelationshipRevision(Relationship.this);
        }

        @Override
        public I_RelPart getMutablePart() {
            return (I_RelPart) super.getMutablePart();
        }

        @Override
        @Deprecated
        public RelationshipRevision duplicate() {
            throw new UnsupportedOperationException("Use makeAnalog instead");
        }

        public int getC1Nid() {
            return getEnclosingConcept().getNid();
        }

        public int getC2Nid() {
            return c2Nid;
        }
    }
    private static VersionComputer<Relationship.Version> computer =
            new VersionComputer<Relationship.Version>();
    private int c2Nid;
    private int characteristicNid;
    private int group;
    private int refinabilityNid;
    private int typeNid;

    public Relationship(Concept enclosingConcept,
            TupleInput input) throws IOException {
        super(enclosingConcept.getNid(),
                input);
    }

    public Relationship(TkRelationship eRel, Concept enclosingConcept) throws IOException {
        super(eRel, enclosingConcept.getNid());
        c2Nid = Bdb.uuidToNid(eRel.getC2Uuid());
        setCharacteristicNid(Bdb.uuidToNid(eRel.getCharacteristicUuid()));
        group = eRel.getRelGroup();
        setRefinabilityNid(Bdb.uuidToNid(eRel.getRefinabilityUuid()));
        setTypeNid(Bdb.uuidToNid(eRel.getTypeUuid()));
        primordialSapNid = Bdb.getSapNid(eRel);
        if (eRel.getRevisionList() != null) {
            revisions = new CopyOnWriteArrayList<RelationshipRevision>();
            for (TkRelationshipRevision erv : eRel.getRevisionList()) {
                revisions.add(new RelationshipRevision(erv, this));
            }
        }
    }

    public Relationship() {
        super();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append("src:");
        ConceptComponent.addNidToBuffer(buf, getEnclosingConcept().getNid());
        buf.append(" t:");
        ConceptComponent.addNidToBuffer(buf, getTypeNid());
        buf.append(" dest:");
        ConceptComponent.addNidToBuffer(buf, c2Nid);
        buf.append(" c:");
        ConceptComponent.addNidToBuffer(buf, getCharacteristicNid());
        buf.append(" g:" + group);
        buf.append(" r:");
        ConceptComponent.addNidToBuffer(buf, getRefinabilityNid());
        buf.append(" ");
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean fieldsEqual(ConceptComponent<RelationshipRevision, Relationship> obj) {
        if (Relationship.class.isAssignableFrom(obj.getClass())) {
            Relationship another = (Relationship) obj;
            if (this.c2Nid != another.c2Nid) {
                return false;
            }
            if (this.getCharacteristicNid() != another.getCharacteristicNid()) {
                return false;
            }
            if (this.group != another.group) {
                return false;
            }
            if (this.getRefinabilityNid() != another.getRefinabilityNid()) {
                return false;
            }
            if (this.getTypeNid() != another.getTypeNid()) {
                return false;
            }
            return conceptComponentFieldsEqual(another);
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
    public String validate(Relationship another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();

        if (this.c2Nid != another.c2Nid) {
            buf.append("\tRelationship.initialCaseSignificant not equal: \n"
                    + "\t\tthis.c2Nid = " + this.c2Nid + "\n"
                    + "\t\tanother.c2Nid = " + another.c2Nid + "\n");
        }
        if (this.getCharacteristicNid() != another.getCharacteristicNid()) {
            buf.append("\tRelationship.characteristicNid not equal: \n"
                    + "\t\tthis.characteristicNid = " + this.getCharacteristicNid() + "\n"
                    + "\t\tanother.characteristicNid = " + another.getCharacteristicNid() + "\n");
        }
        if (this.group != another.group) {
            buf.append("\tRelationship.group not equal: \n"
                    + "\t\tthis.group = " + this.group + "\n"
                    + "\t\tanother.group = " + another.group + "\n");
        }
        if (this.getRefinabilityNid() != another.getRefinabilityNid()) {
            buf.append("\tRelationship.refinabilityNid not equal: \n"
                    + "\t\tthis.refinabilityNid = " + this.getRefinabilityNid() + "\n"
                    + "\t\tanother.refinabilityNid = " + another.getRefinabilityNid() + "\n");
        }
        if (this.getTypeNid() != another.getTypeNid()) {
            buf.append("\tRelationship.typeNid not equal: \n"
                    + "\t\tthis.typeNid = " + this.getTypeNid() + "\n"
                    + "\t\tanother.typeNid = " + another.getTypeNid() + "\n");
        }

        // Compare the parents
        buf.append(super.validate(another));

        return buf.toString();
    }

    @Override
    public void readFromBdb(TupleInput input) {
        // nid, list size, and conceptNid are read already by the binder...
        c2Nid = input.readInt();
        setCharacteristicNid(input.readInt());
        group = input.readInt();
        setRefinabilityNid(input.readInt());
        setTypeNid(input.readInt());
        int additionalVersionCount = input.readShort();
        if (additionalVersionCount > 0) {
            revisions = new CopyOnWriteArrayList<RelationshipRevision>();
            for (int i = 0; i < additionalVersionCount; i++) {
                revisions.add(new RelationshipRevision(input, this));
            }
        }
    }

    @Override
    public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        //
        List<RelationshipRevision> partsToWrite = new ArrayList<RelationshipRevision>();
        if (revisions != null) {
            for (RelationshipRevision p : revisions) {
                if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid
                        && p.getTime() != Long.MIN_VALUE) {
                    partsToWrite.add(p);
                }
            }
        }
        // Start writing
        // c1Nid is the enclosing concept, does not need to be written.
        output.writeInt(c2Nid);
        output.writeInt(getCharacteristicNid());
        output.writeInt(group);
        output.writeInt(getRefinabilityNid());
        output.writeInt(getTypeNid());
        output.writeShort(partsToWrite.size());

        NidPairForRel npr = NidPair.getTypeNidRelNidPair(typeNid, nid);
        Bdb.addXrefPair(c2Nid, npr);

        for (RelationshipRevision p : partsToWrite) {
            if (p.getTypeNid() != typeNid) {
                npr = NidPair.getTypeNidRelNidPair(p.getTypeNid(), nid);
                Bdb.addXrefPair(c2Nid, npr);
            }
            p.writePartToBdb(output);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (Relationship.class.isAssignableFrom(obj.getClass())) {
            Relationship another = (Relationship) obj;
            return nid == another.nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[]{nid, c2Nid, enclosingConceptNid});
    }

    @Override
    public boolean addRetiredRec(int[] releases, int retiredStatusId) {
        throw new UnsupportedOperationException();
    }

    public List<? extends I_RelTuple> getSpecifiedVersions(I_ConfigAceFrame frameConfig)
            throws TerminologyException, IOException {
        List<Relationship.Version> specifiedVersions = new ArrayList<Relationship.Version>();
        computer.addSpecifiedVersions(frameConfig.getAllowedStatus(), frameConfig.getViewPositionSetReadOnly(), specifiedVersions,
                getTuples(), frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy());


        return specifiedVersions;
    }

    @Override
    public List<? extends I_RelTuple> getSpecifiedVersions(NidSetBI allowedStatus,
            PositionSetBI positions, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager)
            throws TerminologyException, IOException {
        List<Relationship.Version> specifiedVersions = new ArrayList<Relationship.Version>();
        computer.addSpecifiedVersions(allowedStatus, positions, specifiedVersions,
                getTuples(), precedencePolicy, contradictionManager);


        return specifiedVersions;
    }

    @Deprecated
    public void addTuples(NidSetBI allowedTypes,
            List<I_RelTuple> returnRels,
            boolean addUncommitted,
            boolean returnConflictResolvedLatestState)
            throws TerminologyException, IOException {
        throw new UnsupportedOperationException("Use a method that specified the config");
    }

    public Collection<Relationship.Version> getVersions(NidSetBI allowedStatus,
            NidSetBI allowedTypes, PositionSetBI viewPositions,
            Precedence precedence, ContradictionManagerBI contradictionMgr) {
        List<Version> returnTuples = new ArrayList<Version>(2);
        computer.addSpecifiedVersions(allowedStatus, allowedTypes, viewPositions,
                returnTuples, getVersions(), precedence, contradictionMgr);
        return returnTuples;
    }

    public boolean addVersion(I_RelPart part) {
        this.versions = null;
        return super.addRevision((RelationshipRevision) part);
    }

    public boolean addPart(RelationshipRevision part) {
        return revisions.add(part);
    }

    @Override
    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getC1Id() {
        return enclosingConceptNid;
    }

    @Override
    public int getC2Id() {
        return c2Nid;
    }

    @Override
    public Version getFirstTuple() {
        return getTuples().get(0);
    }

    @Override
    public Version getLastTuple() {
        List<Version> vList = getTuples();
        return vList.get(vList.size() - 1);
    }

    @Override
    public int getRelId() {
        return nid;
    }
    List<Version> versions;

    public List<Version> getTuples() {
        return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
    }

    public List<Version> getVersions() {
        if (versions == null) {
            int count = 1;
            if (revisions != null) {
                count = count + revisions.size();
            }
            ArrayList<Version> list = new ArrayList<Version>(count);
            if (getTime() != Long.MIN_VALUE) {
                list.add(new Version());
            }
            if (revisions != null) {
                for (int i = 0; i < revisions.size(); i++) {
                    if (revisions.get(i).getTime() != Long.MIN_VALUE) {
                        list.add(new Version(i));
                    }
                }
            }
            versions = list;
        }
        return versions;
    }

    public List<Version> getVersions(ContradictionManagerBI contradictionManager) {
        // TODO implement conflict resolution
        return getTuples();
    }

    public List<Version> getTuples(ContradictionManagerBI contradictionManager) {
        // TODO implement conflict resolution
        return getTuples();
    }

    @Override
    public UniversalAceRelationship getUniversal() throws IOException,
            TerminologyException {
        UniversalAceRelationship universal = new UniversalAceRelationship(
                getEnclosingConcept().getUidsForComponent(nid),
                getEnclosingConcept().getUUIDs(),
                Bdb.getConceptDb().getConcept(c2Nid).getUUIDs(),
                revisions.size());
        for (Version part : getVersions()) {
            UniversalAceRelationshipPart universalPart = new UniversalAceRelationshipPart();
            universalPart.setPathId(Bdb.getConceptDb().getConcept(part.getPathNid()).getUUIDs());
            universalPart.setStatusId(Bdb.getConceptDb().getConcept(part.getStatusNid()).getUUIDs());
            universalPart.setCharacteristicId(Bdb.getConceptDb().getConcept(part.getCharacteristicId()).getUUIDs());
            universalPart.setGroup(part.getGroup());
            universalPart.setRefinabilityId(Bdb.getConceptDb().getConcept(part.getRefinabilityNid()).getUUIDs());
            universalPart.setTypeId(Bdb.getConceptDb().getConcept(part.getTypeNid()).getUUIDs());
            universalPart.setTime(part.getTime());
            universal.addVersion(universalPart);
        }
        return universal;
    }

    @Override
    public void setC2Id(int destNid) {
        this.c2Nid = destNid;
        modified();
    }

    @Override
    public boolean promote(PositionBI viewPosition,
            PathSetReadOnly pomotionPaths, NidSetBI allowedStatus, Precedence precedence)
            throws IOException, TerminologyException {
        int viewPathId = viewPosition.getPath().getConceptNid();
        Collection<Version> matchingTuples = computer.getSpecifiedVersions(allowedStatus,
                viewPosition,
                getVersions(), precedence, null);
        boolean promotedAnything = false;
        for (PathBI promotionPath : pomotionPaths) {
            for (Version v : matchingTuples) {
                if (v.getPathNid() == viewPathId) {

                    RelationshipRevision revision = v.makeAnalog(v.getStatusNid(),
                            promotionPath.getConceptNid(), Long.MAX_VALUE);
                    addRevision(revision);
                    promotedAnything = true;
                }
            }
        }
        return promotedAnything;
    }

    @Override
    public void addTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions, List<I_RelTuple> relTupleList,
            Precedence precedencePolicy, ContradictionManagerBI contradictionManager) {
        List<Version> tuplesToReturn = new ArrayList<Version>();
        computer.addSpecifiedVersions(allowedStatus, allowedTypes, positions, tuplesToReturn,
                getVersions(), precedencePolicy, contradictionManager);
        relTupleList.addAll(tuplesToReturn);
    }

    @Override
    public boolean addVersionNoRedundancyCheck(I_RelPart rel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCharacteristicId() {
        return getCharacteristicNid();
    }

    @Override
    public int getGroup() {
        return group;
    }

    @Override
    public int getRefinabilityId() {
        return getRefinabilityNid();
    }

    @Override
    public void setCharacteristicId(int characteristicNid) {
        this.setCharacteristicNid(characteristicNid);
        modified();
    }

    @Override
    public void setGroup(int group) {
        this.group = group;
        modified();
    }

    @Override
    public void setRefinabilityId(int refinabilityId) {
        this.setRefinabilityNid(refinabilityId);
        modified();
    }

    @Override
    public int getTypeId() {
        return this.getTypeNid();
    }

    @Override
    public void setTypeId(int typeNid) {
        this.setTypeNid(typeNid);
        modified();
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList nidList = new ArrayIntList(7);
        nidList.add(enclosingConceptNid);
        nidList.add(c2Nid);
        nidList.add(getCharacteristicNid());
        nidList.add(getRefinabilityNid());
        nidList.add(getTypeNid());
        return nidList;
    }

    @Override
    public RelationshipRevision makeAnalog(int statusNid, int pathNid, long time) {
        RelationshipRevision newR = new RelationshipRevision(this, statusNid, Terms.get().getAuthorNid(), pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public RelationshipRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        RelationshipRevision newR = new RelationshipRevision(this, statusNid, authorNid, pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public I_RelPart duplicate() {
        throw new UnsupportedOperationException("Use makeAnalog instead");
    }

    @Override
    public Relationship getFixedPart() {
        return this;
    }

    @Override
    public Relationship getMutablePart() {
        return this;
    }

    @Override
    public List<? extends I_RelPart> getMutableParts() {
        return getVersions();
    }

    @Override
    public void clearVersions() {
        versions = null;
    }

    @Override
    public void setCharacteristicNid(int characteristicNid) {
        this.characteristicNid = characteristicNid;
    }

    @Override
    public int getCharacteristicNid() {
        return characteristicNid;
    }

    @Override
    public void setRefinabilityNid(int refinabilityNid) {
        this.refinabilityNid = refinabilityNid;
    }

    @Override
    public int getRefinabilityNid() {
        return refinabilityNid;
    }

    @Override
    public void setTypeNid(int typeNid) {
        this.typeNid = typeNid;
    }

    @Override
    public int getTypeNid() {
        return typeNid;
    }

    @Override
    public boolean hasExtensions() throws IOException {
        return getEnclosingConcept().hasExtensionsForComponent(nid);
    }

    public boolean everWasType(int typeNid) {
        if (this.typeNid == typeNid) {
            return true;
        }
        if (revisions != null) {
            for (RelationshipRevision rv : revisions) {
                if (rv.getTypeNid() == typeNid) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setDestinationNid(int nid) throws PropertyVetoException {
        this.c2Nid = nid;
    }

    @Override
    public int getDestinationNid() {
        return c2Nid;
    }

    @Override
    public int getOriginNid() {
        return enclosingConceptNid;
    }

    @Override
    public Relationship.Version getVersion(ViewCoordinate c)
            throws ContraditionException {
        List<Relationship.Version> vForC = getVersions(c);
        if (vForC.isEmpty()) {
            return null;
        }
        if (vForC.size() > 1) {
            throw new ContraditionException(vForC.toString());
        }
        return vForC.get(0);
    }

    @Override
    public List<Relationship.Version> getVersions(ViewCoordinate c) {
        List<Version> returnValues = new ArrayList<Version>(2);
        computer.addSpecifiedRelVersions(returnValues,
                getVersions(),
                c);
        return returnValues;
    }

    @Override
    public String toUserString() {
        StringBuffer buf = new StringBuffer();
        ConceptComponent.addTextToBuffer(buf, typeNid);
        buf.append(": ");
        ConceptComponent.addTextToBuffer(buf, c2Nid);
        return buf.toString();
    }
    
    
    @Override
    public boolean isInferred() {
        return getAuthorNid() == Relationship.getClassifierAuthorNid();
    }

    @Override
    public boolean isStated() {
        return !isInferred();
    }

}
