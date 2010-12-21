package org.ihtsdo.concept.component.attributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.conattr.ConAttrAnalogBI;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;

public class ConceptAttributes
        extends ConceptComponent<ConceptAttributesRevision, ConceptAttributes>
        implements I_ConceptAttributeVersioned,
        I_ConceptAttributePart,
        ConAttrAnalogBI {

    private boolean defined;

    public ConceptAttributes(Concept enclosingConcept, TupleInput input) throws IOException {
        super(enclosingConcept.getNid(), input);
    }

    public ConceptAttributes(TkConceptAttributes eAttr, Concept c) throws IOException {
        super(eAttr, c.getNid());
        System.out.println("ConceptAttributes TkConceptAttributes eAttr= "+eAttr);
        defined = eAttr.isDefined();
        if (eAttr.getRevisionList() != null) {
            revisions = new CopyOnWriteArrayList<ConceptAttributesRevision>();
            for (TkConceptAttributesRevision ear : eAttr.getRevisionList()) {
                revisions.add(new ConceptAttributesRevision(ear, this));
            }
        }
    }

    public ConceptAttributes() {
        super();
    }


       public class Version
            extends ConceptComponent<ConceptAttributesRevision, ConceptAttributes>.Version
            implements I_ConceptAttributeTuple, I_ConceptAttributePart {

        public Version() {
            super();
        }

        public Version(int index) {
            super(index);
        }

            
        @Override
        public boolean isDefined() {
            if (index >= 0) {
                return revisions.get(index).isDefined();
            }
            return defined;
        }

        @Override
        public void setDefined(boolean defined) {
            if (index >= 0) {
                revisions.get(index).setDefined(defined);
            } else {
                ConceptAttributes.this.setDefined(defined);
            }
        }

        @Override
        public ConceptAttributes.Version getVersion(Coordinate c)
                throws ContraditionException {
            return ConceptAttributes.this.getVersion(c);
        }

        @Override
        public Collection<ConceptAttributes.Version> getVersions(
                Coordinate c) {
            return ConceptAttributes.this.getVersions(c);
        }

        public List<? extends Version> getVersions() {
            return ConceptAttributes.this.getVersions();
        }

        @Override
        public ConceptAttributesRevision makeAnalog(int statusNid, int pathNid, long time) {
            if (index >= 0) {
                ConceptAttributesRevision rev = revisions.get(index);
                if (rev.getTime() == Long.MAX_VALUE && rev.getPathNid() == pathNid) {
                    rev.setStatusNid(statusNid);
                    return rev;
                }
                return rev.makeAnalog(statusNid, pathNid, time);
            }
            try {
                return new ConceptAttributesRevision(ConceptAttributes.this,
                        statusNid,
                        Terms.get().getAuthorNid(),
                        pathNid, time, ConceptAttributes.this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public ConceptAttributesRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
            if (index >= 0) {
                ConceptAttributesRevision rev = revisions.get(index);
                if (rev.getTime() == Long.MAX_VALUE && rev.getPathNid() == pathNid) {
                    rev.setStatusNid(statusNid);
                    return rev;
                }
                return rev.makeAnalog(statusNid, authorNid, pathNid, time);
            }
            try {
                return new ConceptAttributesRevision(ConceptAttributes.this,
                        statusNid,
                        authorNid,
                        pathNid, time, ConceptAttributes.this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public ConceptAttributesRevision makeAnalog() {
            if (index >= 0) {
                ConceptAttributesRevision rev = revisions.get(index);
                return new ConceptAttributesRevision(rev, ConceptAttributes.this);
            }
            return new ConceptAttributesRevision(ConceptAttributes.this, ConceptAttributes.this);
        }

        @Override
        public int getConId() {
            return nid;
        }

        @Override
        public I_ConceptAttributePart getMutablePart() {
            return (I_ConceptAttributePart) super.getMutablePart();
        }

        @Override
        @Deprecated
        public I_ConceptAttributePart duplicate() {
            throw new UnsupportedOperationException("Use makeAnalog instead");
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

        public ArrayIntList getVariableVersionNids() {
            if (index >= 0) {
                ArrayIntList resultList = new ArrayIntList(2);
                return resultList;
            }
            return ConceptAttributes.this.getVariableVersionNids();
        }
    }

    @Override
    public void readFromBdb(TupleInput input) {
        try {
            // nid, list size, and conceptNid are read already by the binder...
            defined = input.readBoolean();
            int additionalVersionCount = input.readShort();
            if (additionalVersionCount > 0) {
                if (revisions == null) {
                    revisions = new CopyOnWriteArrayList<ConceptAttributesRevision>();
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
    public void writeToBdb(TupleOutput output,
            int maxReadOnlyStatusAtPositionNid) {
        List<ConceptAttributesRevision> partsToWrite = new ArrayList<ConceptAttributesRevision>();
        if (revisions != null) {
            for (ConceptAttributesRevision p : revisions) {
                if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid
                        && p.getTime() != Long.MIN_VALUE) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getConId()
     */
    public int getConId() {
        return nid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getTuples()
     */
    public List<Version> getTuples() {
        return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
    }
    List<Version> versions;

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
        return list;
    }
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ConceptAttributeVersioned#convertIds(org.dwfa.vodb
     * .jar.I_MapNativeToNative)
     */

    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        throw new UnsupportedOperationException();
    }
    private static VersionComputer<ConceptAttributes.Version> computer =
            new VersionComputer<ConceptAttributes.Version>();


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ConceptAttributeVersioned#getLocalFixedConcept()
     */
    public I_ConceptualizeLocally getLocalFixedConcept() {
        return LocalFixedConcept.get(nid, !defined);
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
    public int hashCode() {
        return HashFunction.hashCode(new int[]{nid});
    }

    private static Collection<UUID> getUids(int id) throws IOException,
            TerminologyException {
        return LocalFixedTerminology.getStore().getUids(id);
    }

    public UniversalAceConceptAttributes getUniversal() throws IOException,
            TerminologyException {
        UniversalAceConceptAttributes conceptAttributes = new UniversalAceConceptAttributes(
                getUids(nid), this.versionCount());
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append("defined:" + this.defined);
        buf.append(" ");
        buf.append(super.toString());
        return buf.toString();
    }

    public void setConId(int cNid) {
        if (this.nid == Integer.MIN_VALUE) {
            this.nid = cNid;
        } else {
            throw new RuntimeException("Cannot change the cNid once set");
        }
    }

    @Override
    public void addTuples(NidSetBI allowedStatus, PositionSetBI positionSet,
            List<I_ConceptAttributeTuple> returnTuples, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager) throws TerminologyException, IOException {
        List<Version> returnList = new ArrayList<Version>();
        computer.addSpecifiedVersions(allowedStatus, positionSet, returnList,
                getVersions(), precedencePolicy, contradictionManager);
        returnTuples.addAll(returnList);
    }

    public List<Version> getTuples(NidSetBI allowedStatus,
            PositionSetBI viewPositionSet, Precedence precedencePolicy,
            I_ManageContradiction contradictionManager) {
        List<Version> returnList = new ArrayList<Version>();
        computer.addSpecifiedVersions(allowedStatus, viewPositionSet, returnList,
                getVersions(), precedencePolicy, contradictionManager);
        return returnList;
    }

    public void addTuples(NidSetBI allowedStatus, PositionBI viewPosition,
            List<Version> returnTuples, Precedence precedencePolicy,
            I_ManageContradiction contradictionManager) {
        computer.addSpecifiedVersions(allowedStatus, viewPosition, returnTuples,
                getVersions(), precedencePolicy, contradictionManager);
    }

    public Collection<Version> getVersions(NidSetBI allowedStatus,
            PositionSetBI viewPositions,
            Precedence precedence, ContradictionManagerBI contradictionMgr) {
        List<Version> returnTuples = new ArrayList<Version>(2);
        computer.addSpecifiedVersions(allowedStatus, viewPositions,
                returnTuples, getVersions(), precedence, contradictionMgr);
        return returnTuples;
    }

    @Override
    public List<? extends I_ConceptAttributeTuple> getTuples(
            NidSetBI allowedStatus, PositionSetBI viewPositionSet)
            throws TerminologyException, IOException {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
        return getTuples(allowedStatus,
                viewPositionSet, config.getPrecedence(), config.getConflictResolutionStrategy());
    }

    public List<Version> getTuples(NidSetBI allowedStatus,
            PositionBI viewPosition, Precedence precedencePolicy, I_ManageContradiction contradictionManager) {
        List<Version> returnList = new ArrayList<Version>();

        addTuples(allowedStatus, viewPosition, returnList, precedencePolicy,
                contradictionManager);

        return returnList;
    }

    public boolean promote(PositionBI viewPosition,
            PathSetReadOnly promotionPaths, NidSetBI allowedStatus, Precedence precedence) {
        int viewPathId = viewPosition.getPath().getConceptNid();
        boolean promotedAnything = false;
        for (PathBI promotionPath : promotionPaths) {
            for (Version version : getTuples(allowedStatus,
                    viewPosition, precedence, null)) {
                if (version.getPathNid() == viewPathId) {
                    ConceptAttributesRevision promotionPart =
                            version.makeAnalog(version.getStatusNid(),
                            promotionPath.getConceptNid(),
                            Long.MAX_VALUE);
                    addRevision(promotionPart);
                    promotedAnything = true;
                }
            }
        }
        return promotedAnything;
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
    public boolean isDefined() {
        return defined;
    }

    @Override
    public void setDefined(boolean defined) {
        this.defined = defined;
        modified();
    }

    @Override
    public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
        if (getTime() == time && getPathNid() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
        ConceptAttributesRevision newR;
        newR = new ConceptAttributesRevision(this, statusNid,
                Terms.get().getAuthorNid(),
                pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public ConceptAttributesRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (getTime() == time && getPathNid() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
        ConceptAttributesRevision newR;
        newR = new ConceptAttributesRevision(this, statusNid,
                Terms.get().getAuthorNid(),
                pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        return new ArrayIntList(2);
    }

    @Override
    public I_ConceptAttributePart duplicate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConceptAttributes getMutablePart() {
        return this;
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

    /**
     * Test method to check to see if two objects are equal in all respects. 
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures. 
     * @throws IOException 
     */
    public String validate(ConceptAttributes another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();

        // Compare defined
        if (this.defined != another.defined) {
            buf.append("\tConceptAttributes.defined not equal: \n"
                    + "\t\tthis.defined = " + this.defined + "\n"
                    + "\t\tanother.defined = " + another.defined + "\n");
        }

        // Compare the parents 
        buf.append(super.validate(another));

        return buf.toString();
    }

    @Override
    public List<? extends I_ConceptAttributePart> getMutableParts() {
        return getTuples();
    }

    @Override
    protected void clearVersions() {
        versions = null;
    }

    @Override
    public boolean hasExtensions() throws IOException {
        return getEnclosingConcept().hasExtensionsForComponent(nid);
    }

    @Override
    public ConceptAttributes.Version getVersion(Coordinate c)
            throws ContraditionException {
        List<ConceptAttributes.Version> vForC = getVersions(c);
        if (vForC.size() == 0) {
            return null;
        }
        if (vForC.size() > 1) {
            throw new ContraditionException(vForC.toString());
        }
        return vForC.get(0);
    }

    @Override
    public List<ConceptAttributes.Version> getVersions(Coordinate c) {
        List<Version> returnTuples = new ArrayList<Version>(2);
        computer.addSpecifiedVersions(c.getAllowedStatusNids(), (NidSetBI) null, c.getPositionSet(),
                returnTuples, getVersions(), c.getPrecedence(), c.getContradictionManager());
        return returnTuples;
    }

    @Override
    public String toUserString() {
        StringBuffer buf = new StringBuffer();
        buf.append("concept ");
        if (defined) {
            buf.append("is fully defined");
        }
        if (defined == false) {
            buf.append("is primitive");
        }
        return buf.toString();
    }
}
