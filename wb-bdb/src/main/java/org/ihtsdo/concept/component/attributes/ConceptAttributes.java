package org.ihtsdo.concept.component.attributes;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.LocalFixedTerminology;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeAnalogBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

public class ConceptAttributes extends ConceptComponent<ConceptAttributesRevision, ConceptAttributes>
        implements I_ConceptAttributeVersioned<ConceptAttributesRevision>,
        I_ConceptAttributePart<ConceptAttributesRevision>,
        ConceptAttributeAnalogBI<ConceptAttributesRevision> {

    private static VersionComputer<ConceptAttributes.Version> computer =
            new VersionComputer<ConceptAttributes.Version>();
    //~--- fields --------------------------------------------------------------
    private boolean defined;
    List<Version> versions;

    //~--- constructors --------------------------------------------------------
    public ConceptAttributes() {
        super();
    }

    public ConceptAttributes(Concept enclosingConcept, TupleInput input) throws IOException {
        super(enclosingConcept.getNid(), input);
    }

    public ConceptAttributes(TkConceptAttributes eAttr, Concept c) throws IOException {
        super(eAttr, c.getNid());
        defined = eAttr.isDefined();

        if (eAttr.getRevisionList() != null) {
            revisions = new RevisionSet(primordialSapNid);

            for (TkConceptAttributesRevision ear : eAttr.getRevisionList()) {
                revisions.add(new ConceptAttributesRevision(ear, this));
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        // nothing to add
    }

    public void addTuples(NidSetBI allowedStatus, PositionBI viewPosition, List<Version> returnTuples,
            Precedence precedencePolicy, ContradictionManagerBI contradictionManager) {
        computer.addSpecifiedVersions(allowedStatus, viewPosition, returnTuples, getVersions(),
                precedencePolicy, contradictionManager);
    }

    @Override
    public void addTuples(NidSetBI allowedStatus, PositionSetBI positionSet,
            List<I_ConceptAttributeTuple> returnTuples, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager)
            throws TerminologyException, IOException {
        List<Version> returnList = new ArrayList<Version>();

        computer.addSpecifiedVersions(allowedStatus, positionSet, returnList, getVersions(), precedencePolicy,
                contradictionManager);
        returnTuples.addAll(returnList);
    }

    public void addTuples(NidSetBI allowedStatus, PositionSetBI positionSet,
            List<I_ConceptAttributeTuple> returnTuples, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager, long time)
            throws TerminologyException, IOException {
        List<Version> returnList = new ArrayList<Version>();

        computer.addSpecifiedVersions(allowedStatus, null, positionSet, returnList, getVersions(),
                precedencePolicy, contradictionManager, time);
        returnTuples.addAll(returnList);
    }

    /*
     * Below methods should be considered for deprecation...
     */
    @Override
    public boolean addVersion(I_ConceptAttributePart part) {
        this.versions = null;

        return super.addRevision(new ConceptAttributesRevision(part, this));
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
    public I_ConceptAttributePart duplicate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            ConceptAttributes another = (ConceptAttributes) obj;

            if (this.nid == another.nid) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean fieldsEqual(ConceptComponent<ConceptAttributesRevision, ConceptAttributes> obj) {
        if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            ConceptAttributes another = (ConceptAttributes) obj;

            if (this.defined == another.defined) {
                return conceptComponentFieldsEqual(another);
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{nid});
    }
    
    @Override
    public ConceptAttributesRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
        ConceptAttributesRevision newR;

        newR = new ConceptAttributesRevision(this, statusNid, time, authorNid, moduleNid, pathNid, this);
        addRevision(newR);

        return newR;
    }

    @Override
    public boolean promote(PositionBI viewPosition, PathSetReadOnly promotionPaths, NidSetBI allowedStatus,
            Precedence precedence, int authorNid) {
        int viewPathId = viewPosition.getPath().getConceptNid();
        boolean promotedAnything = false;

        for (PathBI promotionPath : promotionPaths) {
            for (Version version : getTuples(allowedStatus, viewPosition, precedence, null)) {
                if (version.getPathNid() == viewPathId) {
                    ConceptAttributesRevision promotionPart = version.makeAnalog(version.getStatusNid(),
                            Long.MAX_VALUE,
                            authorNid,
                            version.getModuleNid(),
                            promotionPath.getConceptNid());

                    addRevision(promotionPart);
                    promotedAnything = true;
                }
            }
        }

        return promotedAnything;
    }

    @Override
    public void readFromBdb(TupleInput input) {
        try {

            // nid, list size, and conceptNid are read already by the binder...
            defined = input.readBoolean();

            int additionalVersionCount = input.readShort();

            if (additionalVersionCount > 0) {
                if (revisions == null) {
                    revisions = new RevisionSet(primordialSapNid);
                }

                for (int i = 0; i < additionalVersionCount; i++) {
                    ConceptAttributesRevision car = new ConceptAttributesRevision(input, this);

                    if (car.getTime() != Long.MIN_VALUE) {
                        revisions.add(car);
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(" Processing nid: " + enclosingConceptNid, e);
        }
    }

    @Override
    public boolean readyToWriteComponent() {
        return true;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(this.getClass().getSimpleName()).append(":{");
        buf.append("defined:").append(this.defined);
        buf.append(" ");
        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    public String toUserString() {
        StringBuilder buf = new StringBuilder();

        buf.append("concept ");

        if (defined) {
            buf.append("is fully defined");
        } else {
            buf.append("is primitive");
        }

        return buf.toString();
    }

    /**
     * Test method to check to see if two objects are equal in all respects.
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures.
     * @throws IOException
     */
    public String validate(ConceptAttributes another) throws IOException {
        assert another != null;

        StringBuilder buf = new StringBuilder();

        // Compare defined
        if (this.defined != another.defined) {
            buf.append("\tConceptAttributes.defined not equal: "
                    + "\n\t\tthis.defined = ").append(this.defined).append("\n"
                    + "\t\tanother.defined = ").append(another.defined).append("\n");
        }

        // Compare the parents
        buf.append(super.validate(another));

        return buf.toString();
    }

    @Override
    public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        List<ConceptAttributesRevision> partsToWrite = new ArrayList<ConceptAttributesRevision>();

        if (revisions != null) {
            for (ConceptAttributesRevision p : revisions) {
                if ((p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid)
                        && (p.getTime() != Long.MIN_VALUE)) {
                    partsToWrite.add(p);
                }
            }
        }

        // Start writing
        output.writeBoolean(defined);
        output.writeShort(partsToWrite.size());

        for (ConceptAttributesRevision p : partsToWrite) {
            p.writePartToBdb(output);
        }
    }

    //~--- get methods ---------------------------------------------------------

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getConId()
     */
    @Override
    public int getConId() {
        return nid;
    }

    @Override
    public ConceptAttributeAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB {
        ConceptAttributeAB conAttrBp = new ConceptAttributeAB(getConId(), defined,
                getVersion(vc), vc);
        return conAttrBp;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_ConceptAttributeVersioned#getLocalFixedConcept()
     */
    @Override
    public I_ConceptualizeLocally getLocalFixedConcept() {
        return LocalFixedConcept.get(nid, !defined);
    }

    @Override
    public ConceptAttributes getMutablePart() {
        return this;
    }

    @Override
    public List<? extends I_ConceptAttributePart> getMutableParts() {
        return getTuples();
    }

    @Override
    public ConceptAttributes getPrimordialVersion() {
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getTuples()
     */
    @Override
    public List<Version> getTuples() {
        return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
    }

    @Override
    public List<? extends I_ConceptAttributeTuple> getTuples(NidSetBI allowedStatus,
            PositionSetBI viewPositionSet)
            throws TerminologyException, IOException {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        return getTuples(allowedStatus, viewPositionSet, config.getPrecedence(),
                config.getConflictResolutionStrategy());
    }

    public List<Version> getTuples(NidSetBI allowedStatus, PositionBI viewPosition,
            Precedence precedencePolicy, ContradictionManagerBI contradictionManager) {
        List<Version> returnList = new ArrayList<Version>();

        addTuples(allowedStatus, viewPosition, returnList, precedencePolicy, contradictionManager);

        return returnList;
    }

    public List<Version> getTuples(NidSetBI allowedStatus, PositionSetBI viewPositionSet,
            Precedence precedencePolicy, ContradictionManagerBI contradictionManager) {
        List<Version> returnList = new ArrayList<Version>();

        computer.addSpecifiedVersions(allowedStatus, viewPositionSet, returnList, getVersions(),
                precedencePolicy, contradictionManager);

        return returnList;
    }

    private static Collection<UUID> getUids(int id) throws IOException, TerminologyException {
        return LocalFixedTerminology.getStore().getUids(id);
    }

    @Override
    public UniversalAceConceptAttributes getUniversal() throws IOException, TerminologyException {
        UniversalAceConceptAttributes conceptAttributes = new UniversalAceConceptAttributes(getUids(nid),
                this.versionCount());

        for (Version part : getVersions()) {
            UniversalAceConceptAttributesPart universalPart = new UniversalAceConceptAttributesPart();

            universalPart.setStatusId(getUids(part.getStatusNid()));
            universalPart.setDefined(part.isDefined());
            universalPart.setPathId(getUids(part.getPathNid()));
            universalPart.setTime(part.getTime());
            conceptAttributes.addVersion(universalPart);
        }

        return conceptAttributes;
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        return new ArrayIntList(2);
    }

    @Override
    public ConceptAttributes.Version getVersion(ViewCoordinate c) throws ContradictionException {
        List<ConceptAttributes.Version> vForC = getVersions(c);

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
        List<Version> list = versions;

        if (list == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            list = new ArrayList<Version>(count);

            if (getTime() != Long.MIN_VALUE) {
                list.add(new Version(this));
            }

            if (revisions != null) {
                for (ConceptAttributesRevision r : revisions) {
                    if (r.getTime() != Long.MIN_VALUE) {
                        list.add(new Version(r));
                    }
                }
            }

            versions = list;
        }

        return Collections.unmodifiableList(list);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_ConceptAttributeVersioned#convertIds(org.dwfa.vodb
     * .jar.I_MapNativeToNative)
     */
    @Override
    public List<ConceptAttributes.Version> getVersions(ViewCoordinate c) {
        List<Version> returnTuples = new ArrayList<Version>(2);

        computer.addSpecifiedVersions(c.getAllowedStatusNids(), (NidSetBI) null, c.getPositionSet(),
                returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager());

        return returnTuples;
    }

    public Collection<Version> getVersions(NidSetBI allowedStatus, PositionSetBI viewPositions,
            Precedence precedence, ContradictionManagerBI contradictionMgr) {
        List<Version> returnTuples = new ArrayList<Version>(2);

        computer.addSpecifiedVersions(allowedStatus, viewPositions, returnTuples, getVersions(), precedence,
                contradictionMgr);

        return returnTuples;
    }

    @Override
    public boolean hasExtensions() throws IOException {
        return getEnclosingConcept().hasExtensionsForComponent(nid);
    }

    @Override
    public boolean isDefined() {
        return defined;
    }

    //~--- set methods ---------------------------------------------------------
    public void setConId(int cNid) {
        if (this.nid == Integer.MIN_VALUE) {
            this.nid = cNid;
        } else {
            throw new RuntimeException("Cannot change the cNid once set");
        }
    }

    @Override
    public void setDefined(boolean defined) {
        this.defined = defined;
        modified();
    }

    //~--- inner classes -------------------------------------------------------
    public class Version extends ConceptComponent<ConceptAttributesRevision, ConceptAttributes>.Version
            implements I_ConceptAttributeTuple<ConceptAttributesRevision>,
            I_ConceptAttributePart<ConceptAttributesRevision>,
            ConceptAttributeAnalogBI<ConceptAttributesRevision> {

        public Version(ConceptAttributeAnalogBI<ConceptAttributesRevision> cv) {
            super(cv);
        }

        //~--- methods ----------------------------------------------------------
        @Override
        @Deprecated
        public I_ConceptAttributePart duplicate() {
            throw new UnsupportedOperationException("Use makeAnalog instead");
        }

        public ConceptAttributesRevision makeAnalog() {
            if (cv == ConceptAttributes.this) {
                return new ConceptAttributesRevision(ConceptAttributes.this, ConceptAttributes.this);
            }

            return new ConceptAttributesRevision((ConceptAttributesRevision) getCv(), ConceptAttributes.this);
        }

        @Override
        public ConceptAttributesRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
            return getCv().makeAnalog(statusNid, time, authorNid, moduleNid, pathNid);
        }

        @Override
        public boolean fieldsEqual(ConceptComponent<ConceptAttributesRevision, ConceptAttributes>.Version another) {
            ConceptAttributes.Version anotherVersion = (ConceptAttributes.Version) another;
            if (this.isDefined() == anotherVersion.isDefined()) {
                return true;
            }
            return false;
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public int getConId() {
            return nid;
        }

        @Override
        public I_ConceptAttributeVersioned getConVersioned() {
            return ConceptAttributes.this;
        }

        @Override
        @Deprecated
        public int getConceptStatus() {
            return getStatusNid();
        }

        @Override
        public Collection<? extends RefexVersionBI<?>> getActiveRefexes(ViewCoordinate xyz, int refsetNid)
                throws IOException {
            return ConceptAttributes.this.getRefexMembersActive(xyz, refsetNid);
        }

        public ConceptAttributeAnalogBI<ConceptAttributesRevision> getCv() {
            return (ConceptAttributeAnalogBI<ConceptAttributesRevision>) cv;
        }

        @Override
        public ConceptAttributeAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB{
            return getCv().makeBlueprint(vc);
        }

        @Override
        public I_ConceptAttributePart getMutablePart() {
            return (I_ConceptAttributePart) super.getMutablePart();
        }

        @Override
        public ConceptAttributes getPrimordialVersion() {
            return ConceptAttributes.this;
        }

        @Override
        public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ArrayIntList getVariableVersionNids() {
            return new ArrayIntList(2);
        }

        @Override
        public ConceptAttributes.Version getVersion(ViewCoordinate c) throws ContradictionException {
            return ConceptAttributes.this.getVersion(c);
        }

        @Override
        public List<? extends Version> getVersions() {
            return ConceptAttributes.this.getVersions();
        }

        @Override
        public Collection<ConceptAttributes.Version> getVersions(ViewCoordinate c) {
            return ConceptAttributes.this.getVersions(c);
        }

        @Override
        public boolean hasRefexMemberActive(ViewCoordinate xyz, int refsetNid) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isDefined() {
            return getCv().isDefined();
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setDefined(boolean defined) throws PropertyVetoException {
            getCv().setDefined(defined);
        }
    }
}
