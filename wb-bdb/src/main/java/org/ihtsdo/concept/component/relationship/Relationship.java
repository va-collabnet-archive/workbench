package org.ihtsdo.concept.component.relationship;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.ihtsdo.db.bdb.BdbTerminologyStore;
import org.ihtsdo.db.change.ChangeNotifier;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;

public class Relationship extends ConceptComponent<RelationshipRevision, Relationship>
        implements I_RelVersioned<RelationshipRevision>, I_RelPart<RelationshipRevision>,
        RelationshipAnalogBI<RelationshipRevision> {

    private static int classifierAuthorNid = Integer.MIN_VALUE;
    private static VersionComputer<Relationship.Version> computer =
            new VersionComputer<>();
    //~--- fields --------------------------------------------------------------
    private int c2Nid;
    private int characteristicNid;
    private int group;
    private int refinabilityNid;
    private int typeNid;
    List<Version> versions;

    //~--- constructors --------------------------------------------------------
    public Relationship() {
        super();
    }

    public Relationship(Concept enclosingConcept, TupleInput input) throws IOException {
        super(enclosingConcept.getNid(), input);
    }

    public Relationship(TkRelationship eRel, Concept enclosingConcept) throws IOException {
        super(eRel, enclosingConcept.getNid());
        c2Nid = Bdb.uuidToNid(eRel.getRelationshipTargetUuid());
        characteristicNid = Bdb.uuidToNid(eRel.getCharacteristicUuid());
        group = eRel.getRelationshipGroup();
        refinabilityNid = Bdb.uuidToNid(eRel.getRefinabilityUuid());
        typeNid = Bdb.uuidToNid(eRel.getTypeUuid());
        primordialSapNid = Bdb.getSapNid(eRel);

        if (eRel.getRevisionList() != null) {
            revisions = new RevisionSet<>(primordialSapNid);

            for (TkRelationshipRevision erv : eRel.getRevisionList()) {
                revisions.add(new RelationshipRevision(erv, this));
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        allNids.add(c2Nid);
        allNids.add(characteristicNid);
        allNids.add(refinabilityNid);
        allNids.add(typeNid);
    }

    public boolean addPart(RelationshipRevision part) {
        return revisions.add(part);
    }

    @Override
    public boolean addRetiredRec(int[] releases, int retiredStatusId) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void addTuples(NidSetBI allowedTypes, List<I_RelTuple> returnRels, boolean addUncommitted,
            boolean returnConflictResolvedLatestState)
            throws TerminologyException, IOException {
        throw new UnsupportedOperationException("Use a method that specified the config");
    }

    @Override
    public void addTuples(NidSetBI allowedStatus, NidSetBI allowedTypes, PositionSetBI positions,
            List<I_RelTuple> relTupleList, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager) {
        List<Version> tuplesToReturn = new ArrayList<Version>();

        computer.addSpecifiedRelVersions(allowedStatus, allowedTypes, positions, tuplesToReturn, getVersions(),
                precedencePolicy, contradictionManager);
        relTupleList.addAll(tuplesToReturn);
    }

    @Override
    public void addTuples(NidSetBI allowedStatus, NidSetBI allowedTypes, PositionSetBI positions,
            List<I_RelTuple> relTupleList, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager, Long cutoffTime) {
        List<Version> tuplesToReturn = new ArrayList<Version>();

        computer.addSpecifiedVersions(allowedStatus, allowedTypes, positions, tuplesToReturn, getVersions(),
                precedencePolicy, contradictionManager, cutoffTime);
        relTupleList.addAll(tuplesToReturn);
    }

    @Override
    public boolean addVersion(I_RelPart part) {
        this.versions = null;

        boolean returnValue = super.addRevision((RelationshipRevision) part);
        return returnValue;

    }
    @Override
    protected void addRevisionHook(boolean returnValue, RelationshipRevision r) {
        ChangeNotifier.touchRelTarget(getTargetNid());
        ChangeNotifier.touchRelOrigin(getSourceNid());
    }
    @Override
    public boolean addVersionNoRedundancyCheck(I_RelPart rel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearVersions() {
        versions = null;
        clearAnnotationVersions();
    }

    @Override
    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public I_RelPart duplicate() {
        throw new UnsupportedOperationException("Use makeAnalog instead");
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

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[]{nid, c2Nid, enclosingConceptNid});
    }

    @Override
    public RelationshipRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
        RelationshipRevision newR = new RelationshipRevision(this, statusNid, time, authorNid, moduleNid, pathNid, this);

        addRevision(newR);

        return newR;
    }

    @Override
    public boolean promote(PositionBI viewPosition, PathSetReadOnly pomotionPaths, NidSetBI allowedStatus,
            Precedence precedence, int authorNid)
            throws IOException, TerminologyException {
        int viewPathId = viewPosition.getPath().getConceptNid();
        Collection<Version> matchingTuples = computer.getSpecifiedVersions(allowedStatus, viewPosition,
                getVersions(), precedence, null);
        boolean promotedAnything = false;

        for (PathBI promotionPath : pomotionPaths) {
            for (Version v : matchingTuples) {
                if (v.getPathNid() == viewPathId) {
                    RelationshipRevision revision = v.makeAnalog(v.getStatusNid(),
                            Long.MAX_VALUE,
                            authorNid,
                            v.getModuleNid(),
                            promotionPath.getConceptNid());
                    addRevision(revision);
                    promotedAnything = true;
                }
            }
        }

        return promotedAnything;
    }

    @Override
    public void readFromBdb(TupleInput input) {

        // nid, list size, and conceptNid are read already by the binder...
        c2Nid = input.readInt();
        characteristicNid = input.readInt();
        group = input.readSortedPackedInt();
        refinabilityNid = input.readInt();
        typeNid = input.readInt();

        int additionalVersionCount = input.readSortedPackedInt();

        if (additionalVersionCount > 0) {
            revisions = new RevisionSet<>(primordialSapNid);

            for (int i = 0; i < additionalVersionCount; i++) {
                revisions.add(new RelationshipRevision(input, this));
            }
        }
    }

    @Override
    public boolean readyToWriteComponent() {
        assert c2Nid != Integer.MAX_VALUE : assertionString();
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
        ConceptComponent.addNidToBuffer(buf, getEnclosingConcept().getNid());
        buf.append(" t:");
        ConceptComponent.addNidToBuffer(buf, getTypeNid());
        buf.append(" dest:");
        ConceptComponent.addNidToBuffer(buf, c2Nid);
        buf.append(" c:");
        ConceptComponent.addNidToBuffer(buf, getCharacteristicNid());
        buf.append(" g:").append(group);
        buf.append(" r:");
        ConceptComponent.addNidToBuffer(buf, getRefinabilityNid());
        buf.append(" ");
        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    public String toUserString() {
        StringBuffer buf = new StringBuffer();

        ConceptComponent.addTextToBuffer(buf, typeNid);
        buf.append(": ");
        ConceptComponent.addTextToBuffer(buf, c2Nid);

        return buf.toString();
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

        StringBuilder buf = new StringBuilder();

        if (this.c2Nid != another.c2Nid) {
            buf.append("\tRelationship.initialCaseSignificant not equal: \n"
                    + "\t\tthis.c2Nid = ").append(this.c2Nid).append("\n"
                    + "\t\tanother.c2Nid = ").append(another.c2Nid).append("\n");
        }

        if (this.getCharacteristicNid() != another.getCharacteristicNid()) {
            buf.append(
                    "\tRelationship.characteristicNid not equal: \n" + "\t\tthis.characteristicNid = ").append(
                    this.getCharacteristicNid()).append("\n" + "\t\tanother.characteristicNid = ").append(
                    another.getCharacteristicNid()).append("\n");
        }

        if (this.group != another.group) {
            buf.append("\tRelationship.group not equal: \n"
                    + "\t\tthis.group = ").append(this.group).append("\n"
                    + "\t\tanother.group = ").append(another.group).append("\n");
        }

        if (this.getRefinabilityNid() != another.getRefinabilityNid()) {
            buf.append("\tRelationship.refinabilityNid not equal: \n"
                    + "\t\tthis.refinabilityNid = ").append(this.getRefinabilityNid()).append("\n"
                    + "\t\tanother.refinabilityNid = ").append(another.getRefinabilityNid()).append("\n");
        }

        if (this.getTypeNid() != another.getTypeNid()) {
            buf.append("\tRelationship.typeNid not equal: \n"
                    + "\t\tthis.typeNid = ").append(this.getTypeNid()).append("\n"
                    + "\t\tanother.typeNid = ").append(another.getTypeNid()).append("\n");
        }

        // Compare the parents
        buf.append(super.validate(another));

        return buf.toString();
    }

    @Override
    public void writeToBdb(TupleOutput output, int maxReadOnlyStampNid) {
      List<RelationshipRevision> revisionsToWrite = new ArrayList<>();

      if (revisions != null) {
         for (RelationshipRevision p : revisions) {
            if ((p.getStampNid() > maxReadOnlyStampNid) && (p.getTime() != Long.MIN_VALUE)) {
               revisionsToWrite.add(p);
            }
         }
      }

      // Start writing
      // c1Nid is the enclosing concept, does not need to be written.
      output.writeInt(c2Nid);
      output.writeInt(getCharacteristicNid());
      output.writeSortedPackedInt(group);
      output.writeInt(getRefinabilityNid());
      output.writeInt(getTypeNid());
      output.writeSortedPackedInt(revisionsToWrite.size());

      for (RelationshipRevision p : revisionsToWrite) {
         p.writePartToBdb(output);
      }
      
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public int getC1Id() {
        return enclosingConceptNid;
    }

    @Override
    public int getC2Id() {
        return c2Nid;
    }

    @Override
    public int getCharacteristicId() {
        return getCharacteristicNid();
    }

    @Override
    public int getCharacteristicNid() {
        return characteristicNid;
    }

    public static int getClassifierAuthorNid() {
        if (classifierAuthorNid == Integer.MIN_VALUE) {
            try {
                classifierAuthorNid =
                        org.dwfa.cement.ArchitectonicAuxiliary.Concept.SNOROCKET.localize().getNid();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (TerminologyException ex) {
                throw new RuntimeException(ex);
            }
        }

        return classifierAuthorNid;
    }

    @Override
    public int getTargetNid() {
        return c2Nid;
    }

    @Override
    public RelationshipCAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB {
        TkRelationshipType relType = null;
        if (getCharacteristicNid() == SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid() ||
                getCharacteristicNid() == SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid()
                || getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid()) {
            throw new InvalidCAB("Inferred relationships can not be used to make blueprints");
        } else {
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
    public Version getFirstTuple() {
        return getTuples().get(0);
    }

    @Override
    public Relationship getFixedPart() {
        return this;
    }

    @Override
    public int getGroup() {
        return group;
    }

    @Override
    public Version getLastTuple() {
        List<Version> vList = getTuples();

        return vList.get(vList.size() - 1);
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
    public int getSourceNid() {
        return enclosingConceptNid;
    }

    @Override
    public Relationship getPrimordialVersion() {
        return Relationship.this;
    }

    @Override
    public int getRefinabilityId() {
        return getRefinabilityNid();
    }

    @Override
    public int getRefinabilityNid() {
        return refinabilityNid;
    }

    @Override
    public int getRelId() {
        return nid;
    }

    @Override
    public List<? extends I_RelTuple> getSpecifiedVersions(I_ConfigAceFrame frameConfig)
            throws TerminologyException, IOException {
        List<Relationship.Version> specifiedVersions = new ArrayList<Relationship.Version>();

        computer.addSpecifiedVersions(frameConfig.getAllowedStatus(), frameConfig.getViewPositionSetReadOnly(),
                specifiedVersions, getTuples(), frameConfig.getPrecedence(),
                frameConfig.getConflictResolutionStrategy());

        return specifiedVersions;
    }

    @Override
    public List<? extends I_RelTuple> getSpecifiedVersions(NidSetBI allowedStatus, PositionSetBI positions,
            Precedence precedencePolicy, ContradictionManagerBI contradictionManager)
            throws TerminologyException, IOException {
        List<Relationship.Version> specifiedVersions = new ArrayList<Relationship.Version>();

        computer.addSpecifiedVersions(allowedStatus, positions, specifiedVersions, getTuples(),
                precedencePolicy, contradictionManager);

        return specifiedVersions;
    }

    @Override
    public List<Version> getTuples() {
        return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
    }

    @Override
    public List<Version> getTuples(ContradictionManagerBI contradictionManager) {

        // TODO implement conflict resolution
        return getTuples();
    }

    @Override
    public int getTypeId() {
        return this.getTypeNid();
    }

    @Override
    public int getTypeNid() {
        return typeNid;
    }

    @Override
    public UniversalAceRelationship getUniversal() throws IOException, TerminologyException {
        UniversalAceRelationship universal =
                new UniversalAceRelationship(getEnclosingConcept().getUidsForComponent(nid),
                getEnclosingConcept().getUUIDs(),
                Bdb.getConceptDb().getConcept(c2Nid).getUUIDs(), revisions.size());

        for (Version part : getVersions()) {
            UniversalAceRelationshipPart universalPart = new UniversalAceRelationshipPart();

            universalPart.setPathId(Bdb.getConceptDb().getConcept(part.getPathNid()).getUUIDs());
            universalPart.setStatusId(Bdb.getConceptDb().getConcept(part.getStatusNid()).getUUIDs());
            universalPart.setCharacteristicId(
                    Bdb.getConceptDb().getConcept(part.getCharacteristicId()).getUUIDs());
            universalPart.setGroup(part.getGroup());
            universalPart.setRefinabilityId(Bdb.getConceptDb().getConcept(part.getRefinabilityNid()).getUUIDs());
            universalPart.setTypeId(Bdb.getConceptDb().getConcept(part.getTypeNid()).getUUIDs());
            universalPart.setTime(part.getTime());
            universal.addVersion(universalPart);
        }

        return universal;
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
    public Relationship.Version getVersion(ViewCoordinate c) throws ContradictionException {
        List<Relationship.Version> vForC = getVersions(c);

        if (vForC.isEmpty()) {
            return null;
        }

        if (vForC.size() > 1) {
            vForC = c.getContradictionManager().resolveVersions(vForC);
        }

        if (vForC.size() > 1) {
            throw new ContradictionException(vForC.toString());
        }

        if (!vForC.isEmpty()) {
            return vForC.get(0);
        }
        return null;
    }

    @Override
    public List<Version> getVersions() {
        if (versions == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            ArrayList<Version> list = new ArrayList<Version>(count);

            if (getTime() != Long.MIN_VALUE) {
                list.add(new Version(this));
            }

            if (revisions != null) {
                for (RelationshipRevision r : revisions) {
                    if (r.getTime() != Long.MIN_VALUE) {
                        list.add(new Version(r));
                    }
                }
            }

            versions = list;
        }

        return Collections.unmodifiableList(versions);
    }

    @Override
    public List<Version> getVersions(ContradictionManagerBI contradictionManager) {

        // TODO implement conflict resolution
        return getTuples();
    }

    @Override
    public List<Relationship.Version> getVersions(ViewCoordinate c) {
        List<Version> returnValues = new ArrayList<Version>(2);

        computer.addSpecifiedRelVersions(returnValues, getVersions(), c);

        return returnValues;
    }

    public Collection<Relationship.Version> getVersions(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI viewPositions, Precedence precedence, ContradictionManagerBI contradictionMgr) {
        List<Version> returnTuples = new ArrayList<Version>(2);

        computer.addSpecifiedVersions(allowedStatus, allowedTypes, viewPositions, returnTuples, getVersions(),
                precedence, contradictionMgr);

        return returnTuples;
    }

    @Override
    public boolean hasExtensions() throws IOException {
        return ((Concept) getEnclosingConcept()).hasExtensionsForComponent(nid);
    }

    @Override
    public boolean isInferred() throws IOException{
        if(getAuthorNid() == Relationship.getClassifierAuthorNid()){
            return true;
        }else if(BdbTerminologyStore.isIsReleaseFormatSetup()){
            return getCharacteristicNid() == SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID();
        }
        return false;
    }

    @Override
    public boolean isStated() throws IOException{
        return !isInferred();
    }

    //~--- set methods ---------------------------------------------------------
    @Override
    @Deprecated
    public void setC2Id(int destNid) {
        try {
            this.setTargetNid(destNid);
        } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    @Override
    public void setCharacteristicId(int characteristicNid) {
        this.setCharacteristicNid(characteristicNid);
    }

    @Override
    public void setCharacteristicNid(int characteristicNid) {
        if (this.characteristicNid != characteristicNid) {
            this.characteristicNid = characteristicNid;
            modified();
        }
    }

    @Override
    public void setTargetNid(int dNid) throws PropertyVetoException {
        if (this.c2Nid != dNid) {
            // new xref is added on the dbWrite.
            this.c2Nid = dNid;
            modified();
        }
    }

    @Override
    public void setGroup(int group) {
        this.group = group;
        modified();
    }

    @Override
    public void setRefinabilityId(int refinabilityId) {
        this.setRefinabilityNid(refinabilityId);
    }

    @Override
    public void setRefinabilityNid(int refinabilityNid) {
        if (this.refinabilityNid != refinabilityNid) {
            this.refinabilityNid = refinabilityNid;
            modified();
        }
    }

    @Override
    public void setTypeId(int typeNid) {
        this.setTypeNid(typeNid);
    }

    @Override
    public void setTypeNid(int typeNid) {
        if (this.typeNid != typeNid) {
            // new xref is added on the dbWrite.
            this.typeNid = typeNid;
            modified();
        }
    }

    //~--- inner classes -------------------------------------------------------
    public class Version extends ConceptComponent<RelationshipRevision, Relationship>.Version
            implements I_RelTuple<RelationshipRevision>, I_RelPart<RelationshipRevision>,
            RelationshipAnalogBI<RelationshipRevision> {

        public Version(RelationshipAnalogBI cv) {
            super(cv);
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        public RelationshipRevision duplicate() {
            throw new UnsupportedOperationException("Use makeAnalog instead");
        }

        public RelationshipRevision makeAnalog() {
            if (Relationship.this != getCv()) {
                RelationshipRevision rev = (RelationshipRevision) getCv();

                return new RelationshipRevision(rev, Relationship.this);
            }

            return new RelationshipRevision(Relationship.this);
        }

        @Override
        public RelationshipRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
            return (RelationshipRevision) getCv().makeAnalog(statusNid, time, authorNid, moduleNid, pathNid);
        }

        @Override
        public boolean fieldsEqual(ConceptComponent<RelationshipRevision, Relationship>.Version another) {
            Relationship.Version anotherVersion = (Relationship.Version) another;
            if (this.getC2Nid() != anotherVersion.getC2Nid()) {
                return false;
            }

            if (this.getCharacteristicNid() != anotherVersion.getCharacteristicNid()) {
                return false;
            }

            if (this.getGroup() != anotherVersion.getGroup()) {
                return false;
            }

            if (this.getRefinabilityNid() != anotherVersion.getRefinabilityNid()) {
                return false;
            }

            if (this.getTypeNid() != anotherVersion.getTypeNid()) {
                return false;
            }

            return true;
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public int getC1Id() {
            return getEnclosingConcept().getNid();
        }

        public int getC1Nid() {
            return getEnclosingConcept().getNid();
        }

        @Override
        public int getC2Id() {
            return c2Nid;
        }

        public int getC2Nid() {
            return c2Nid;
        }

        @Override
        public int getCharacteristicId() {
            return getCv().getCharacteristicNid();
        }

        @Override
        public int getCharacteristicNid() {
            return getCv().getCharacteristicNid();
        }

        RelationshipAnalogBI getCv() {
            return (RelationshipAnalogBI) cv;
        }

        @Override
        public int getTargetNid() {
            return Relationship.this.c2Nid;
        }

        @Override
        public RelationshipCAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB {
            return getCv().makeBlueprint(vc);
        }

        @Override
        public Relationship getFixedPart() {
            return Relationship.this;
        }

        @Override
        public int getGroup() {
            return getCv().getGroup();
        }

        @Override
        public I_RelPart getMutablePart() {
            return (I_RelPart) super.getMutablePart();
        }

        @Override
        public int getSourceNid() {
            return Relationship.this.enclosingConceptNid;
        }

        @Override
        public Relationship getPrimordialVersion() {
            return Relationship.this;
        }

        @Override
        @Deprecated
        public int getRefinabilityId() {
            return getCv().getRefinabilityNid();
        }

        @Override
        public int getRefinabilityNid() {
            return getCv().getRefinabilityNid();
        }

        @Override
        public int getRelId() {
            return nid;
        }

        @Override
        public I_RelVersioned getRelVersioned() {
            return Relationship.this;
        }

        public Concept getType() throws IOException {
            return Bdb.getConcept(getTypeNid());
        }

        @Override
        @Deprecated
        public int getTypeId() {
            return getTypeNid();
        }

        @Override
        public int getTypeNid() {
            return getCv().getTypeNid();
        }

        @Override
        public ArrayIntList getVariableVersionNids() {
            if (Relationship.this != getCv()) {
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
        public Relationship.Version getVersion(ViewCoordinate c) throws ContradictionException {
            return Relationship.this.getVersion(c);
        }

        @Override
        public List<? extends Version> getVersions() {
            return Relationship.this.getVersions();
        }

        @Override
        public Collection<Relationship.Version> getVersions(ViewCoordinate c) {
            return Relationship.this.getVersions(c);
        }

        @Override
        public boolean isInferred() throws IOException{
            return getCv().isInferred();
        }

        @Override
        public boolean isStated() throws IOException{
            return !isInferred();
        }

        //~--- set methods ------------------------------------------------------
        @Override
        @Deprecated
        public void setCharacteristicId(int characteristicId) throws PropertyVetoException {
            getCv().setCharacteristicNid(characteristicId);
        }

        @Override
        public void setCharacteristicNid(int characteristicNid) throws PropertyVetoException {
            getCv().setCharacteristicNid(characteristicNid);
        }

        @Override
        public void setTargetNid(int destNid) throws PropertyVetoException {
            getCv().setTargetNid(destNid);
        }

        @Override
        public void setGroup(int group) throws PropertyVetoException {
            getCv().setGroup(group);
        }

        @Override
        public void setRefinabilityId(int refinabilityId) throws PropertyVetoException {
            getCv().setRefinabilityNid(refinabilityId);
        }

        @Override
        public void setRefinabilityNid(int refinabilityNid) throws PropertyVetoException {
            getCv().setRefinabilityNid(refinabilityNid);
        }

        @Override
        @Deprecated
        public void setTypeId(int type) throws PropertyVetoException {
            getCv().setTypeNid(type);
        }

        @Override
        public void setTypeNid(int typeNid) throws PropertyVetoException {
            getCv().setTypeNid(typeNid);
        }
    }
}
