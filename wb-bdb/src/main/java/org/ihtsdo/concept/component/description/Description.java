package org.ihtsdo.concept.component.description;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedDesc;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

public class Description extends ConceptComponent<DescriptionRevision, Description>
        implements I_DescriptionVersioned<DescriptionRevision>, I_DescriptionPart<DescriptionRevision>,
        DescriptionAnalogBI<DescriptionRevision> {

    private static VersionComputer<Description.Version> computer = new VersionComputer<Description.Version>();
    //~--- fields --------------------------------------------------------------
    private boolean initialCaseSignificant;
    private String lang;
    private String text;
    int typeNid;

    /*
     * Consider depreciating the below methods...
     */
    List<Version> versions;

    //~--- constructors --------------------------------------------------------
    public Description() {
        super();
    }

    public Description(Concept enclosingConcept, TupleInput input) throws IOException {
        super(enclosingConcept.getNid(), input);
    }

    public Description(TkDescription eDesc, Concept enclosingConcept) throws IOException {
        super(eDesc, enclosingConcept.getNid());
        initialCaseSignificant = eDesc.isInitialCaseSignificant();
        lang = eDesc.getLang();
        text = eDesc.getText();
        typeNid = Bdb.uuidToNid(eDesc.getTypeUuid());
        primordialSapNid = Bdb.getSapNid(eDesc);

        if (eDesc.getRevisionList() != null) {
            revisions = new RevisionSet<DescriptionRevision, Description>(primordialSapNid);

            for (TkDescriptionRevision edv : eDesc.getRevisionList()) {
                try {
                    revisions.add(new DescriptionRevision(edv, this));
                } catch (TerminologyException e) {
                    throw new IOException(e);
                }
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        allNids.add(typeNid);
    }

    public void addTuples(NidSetBI allowedStatus, I_Position viewPosition,
            List<Description.Version> matchingTuples, Precedence precedence,
            ContradictionManagerBI contradictionMgr) {
        computer.addSpecifiedVersions(allowedStatus, viewPosition, matchingTuples, getVersions(), precedence,
                contradictionMgr);
    }

    @Override
    public void addTuples(NidSetBI allowedStatus, NidSetBI allowedTypes, PositionSetBI positions,
            List<I_DescriptionTuple<DescriptionRevision>> matchingTuples, Precedence precedence,
            ContradictionManagerBI contradictionManager) {
        List<Version> returnTuples = new ArrayList<Version>();

        computer.addSpecifiedVersions(allowedStatus, allowedTypes, positions, returnTuples, getVersions(),
                precedence, contradictionManager);
        matchingTuples.addAll(returnTuples);
    }

    public void addTuples(NidSetBI allowedStatus, NidSetBI allowedTypes, PositionSetBI positions,
            List<I_DescriptionTuple<DescriptionRevision>> matchingTuples, Precedence precedence,
            ContradictionManagerBI contradictionManager, Long time) {
        List<Version> returnTuples = new ArrayList<Version>();

        computer.addSpecifiedVersions(allowedStatus, allowedTypes, positions, returnTuples, getVersions(),
                precedence, contradictionManager, time);
        matchingTuples.addAll(returnTuples);
    }

    @Override
    public boolean addVersion(I_DescriptionPart newPart) {
        this.versions = null;
        BdbCommitManager.addUncommittedDescNid(nid);

        return super.addRevision((DescriptionRevision) newPart);
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
    public I_DescriptionPart duplicate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (Description.class.isAssignableFrom(obj.getClass())) {
            Description another = (Description) obj;

            if (this.nid == another.nid) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean fieldsEqual(ConceptComponent<DescriptionRevision, Description> obj) {
        if (Description.class.isAssignableFrom(obj.getClass())) {
            Description another = (Description) obj;

            if (this.initialCaseSignificant != another.initialCaseSignificant) {
                return false;
            }

            if (!this.text.equals(another.text)) {
                return false;
            }

            if (!this.lang.equals(another.lang)) {
                return false;
            }

            if (this.typeNid != another.typeNid) {
                return false;
            }

            return conceptComponentFieldsEqual(another);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{nid});
    }
    
    @Override
    public DescriptionRevision makeAnalog(int statusNid,long time, int authorNid, int moduleNid, int pathNid) {
        DescriptionRevision newR;

        newR = new DescriptionRevision(this, statusNid, time, authorNid,
                moduleNid, pathNid, this);
        addRevision(newR);

        return newR;
    }

    @Override
    public boolean matches(Pattern p) {
        String lastText = null;

        for (Description.Version desc : getVersions()) {
            if (!desc.getText().equals(lastText)) {
                lastText = desc.getText();

                Matcher m = p.matcher(lastText);

                if (m.find()) {
                    return true;
                }
            }
        }

        return false;
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
                    DescriptionRevision revision = v.makeAnalog(v.getStatusNid(), Long.MAX_VALUE,
                            authorNid, v.getModuleNid(), promotionPath.getConceptNid());

                    addRevision(revision);
                    promotedAnything = true;
                }
            }
        }

        return promotedAnything;
    }

    @Override
    public void readFromBdb(TupleInput input) {
        initialCaseSignificant = input.readBoolean();
        lang = input.readString();
        text = input.readString();
        typeNid = input.readInt();

        // nid, list size, and conceptNid are read already by the binder...
        int additionalVersionCount = input.readShort();

        if (additionalVersionCount > 0) {
            revisions = new RevisionSet<>(primordialSapNid);

            for (int i = 0; i < additionalVersionCount; i++) {
                DescriptionRevision dr = new DescriptionRevision(input, this);

                if (dr.getTime() != Long.MIN_VALUE) {
                    revisions.add(dr);
                }
            }
        }
    }

    @Override
    public boolean readyToWriteComponent() {
        assert text != null : assertionString();
        assert typeNid != Integer.MAX_VALUE : assertionString();
        assert lang != null : assertionString();

        return true;
    }

    @Override
    public I_DescribeConceptLocally toLocalFixedDesc() {
        return new LocalFixedDesc(nid, getStatusNid(), getConceptNid(), isInitialCaseSignificant(),
                getTypeId(), getText(), getLang());
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(this.getClass().getSimpleName()).append(":{");
        buf.append("cNid: ").append(this.enclosingConceptNid).append(" ");
        buf.append("text: '").append(this.getText()).append("'");
        buf.append(" caseSig: ").append(isInitialCaseSignificant());
        buf.append(" type:");
        ConceptComponent.addNidToBuffer(buf, typeNid);
        buf.append(" lang:").append(this.getLang());
        buf.append(" ");
        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    public String toUserString() {
        StringBuilder buf = new StringBuilder();

        ConceptComponent.addTextToBuffer(buf, typeNid);
        buf.append(": ");
        buf.append("'").append(this.getText()).append("'");

        return buf.toString();
    }

    /**
     * Test method to check to see if two objects are equal in all respects.
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures.
     * @throws IOException
     */
    public String validate(Description another) throws IOException {
        assert another != null;

        StringBuilder buf = new StringBuilder();

        if (this.initialCaseSignificant != another.initialCaseSignificant) {
            buf.append(
                    "\tDescription.initialCaseSignificant not equal: \n"
                    + "\t\tthis.initialCaseSignificant = ").append(this.initialCaseSignificant).append(
                    "\n" + "\t\tanother.initialCaseSignificant = ").append(
                    another.initialCaseSignificant).append("\n");
        }

        if (!this.text.equals(another.text)) {
            buf.append("\tDescription.text not equal: \n" + "\t\tthis.text = ").append(this.text).append("\n"
                    + "\t\tanother.text = ").append(another.text).append("\n");
        }

        if (!this.lang.equals(another.lang)) {
            buf.append("\tDescription.lang not equal: \n" + "\t\tthis.lang = ").append(this.lang).append("\n"
                    + "\t\tanother.lang = ").append(another.lang).append("\n");
        }

        if (this.typeNid != another.typeNid) {
            buf.append("\tDescription.typeNid not equal: \n"
                    + "\t\tthis.typeNid = ").append(this.typeNid).append("\n"
                    + "\t\tanother.typeNid = ").append(another.typeNid).append("\n");
        }

        // Compare the parents
        buf.append(super.validate(another));

        return buf.toString();
    }

    @Override
    public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        List<DescriptionRevision> partsToWrite = new ArrayList<DescriptionRevision>();

        if (revisions != null) {
            for (DescriptionRevision p : revisions) {
                if ((p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid)
                        && (p.getTime() != Long.MIN_VALUE)) {
                    partsToWrite.add(p);
                }
            }
        }

        output.writeBoolean(initialCaseSignificant);
        output.writeString(lang);
        output.writeString(text);
        output.writeInt(typeNid);
        output.writeShort(partsToWrite.size());

        // conceptNid is the enclosing concept, does not need to be written.
        for (DescriptionRevision p : partsToWrite) {
            p.writePartToBdb(output);
        }
    }

    //~--- get methods ---------------------------------------------------------
    public Concept getConcept() {
        return getEnclosingConcept();
    }

    @Override
    public int getConceptNid() {
        return enclosingConceptNid;
    }

    @Override
    public int getDescId() {
        return nid;
    }

    @Override
    public DescriptionCAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB {
        DescriptionCAB descBp = new DescriptionCAB(getConceptNid(), getTypeNid(),
                LANG_CODE.getLangCode(lang), getText(), initialCaseSignificant,
                getVersion(vc), vc);
        return descBp;
    }

    @Override
    public Version getFirstTuple() {
        return getTuples().get(0);
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public Version getLastTuple() {
        List<Version> vList = getTuples();

        return vList.get(vList.size() - 1);
    }

    @Override
    public I_DescriptionPart getMutablePart() {
        return this;
    }

    @Override
    public List<? extends I_DescriptionPart<DescriptionRevision>> getMutableParts() {
        return getTuples();
    }

    @Override
    public Description getPrimordialVersion() {
        return Description.this;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public List<Version> getTuples() {
        return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
    }

    @Override
    public List<Version> getTuples(ContradictionManagerBI contradictionMgr)
            throws TerminologyException, IOException {

        // TODO implement ContradictionManagerBI contradictionMgr
        return getVersions();
    }

    @Override
    @Deprecated
    public int getTypeId() {
        return typeNid;
    }

    @Override
    public int getTypeNid() {
        return typeNid;
    }

    @Override
    public UniversalAceDescription getUniversal() throws IOException, TerminologyException {
        UniversalAceDescription universal = new UniversalAceDescription(this);

        return universal;
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList nidList = new ArrayIntList(3);

        nidList.add(typeNid);

        return nidList;
    }

    @Override
    public Description.Version getVersion(ViewCoordinate c) throws ContradictionException {
        List<Description.Version> vForC = getVersions(c);

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
                for (DescriptionRevision rev : revisions) {
                    if (rev.getTime() != Long.MIN_VALUE) {
                        list.add(new Version(rev));
                    }
                }
            }

            versions = list;
        }

        return Collections.unmodifiableList(versions);
    }

    @Override
    public List<Version> getVersions(ContradictionManagerBI contradictionMgr)
            throws TerminologyException, IOException {
        return getTuples(contradictionMgr);
    }

    @Override
    public List<Description.Version> getVersions(ViewCoordinate c) {
        List<Version> returnTuples = new ArrayList<Version>(2);

        computer.addSpecifiedVersions(c.getAllowedStatusNids(), (NidSetBI) null, c.getPositionSet(),
                returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager());

        return returnTuples;
    }

    public List<Description.Version> getVersions(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI viewPositions, Precedence precedence, ContradictionManagerBI contradictionMgr) {
        List<Version> returnTuples = new ArrayList<Version>(2);

        computer.addSpecifiedVersions(allowedStatus, allowedTypes, viewPositions, returnTuples, getVersions(),
                precedence, contradictionMgr);

        return returnTuples;
    }

    @Override
    public boolean hasExtensions() throws IOException {
        return getEnclosingConcept().hasExtensionsForComponent(nid);
    }

    @Override
    public boolean isInitialCaseSignificant() {
        return initialCaseSignificant;
    }

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setInitialCaseSignificant(boolean initialCaseSignificant) {
        this.initialCaseSignificant = initialCaseSignificant;
        modified();
    }

    @Override
    public void setLang(String lang) {
        this.lang = lang;
        modified();
    }

    @Override
    public void setText(String text) {
        this.text = StringEscapeUtils.unescapeHtml4(text);
        modified();
    }

    @Override
    @Deprecated
    public void setTypeId(int typeNid) {
        this.typeNid = typeNid;
        modified();
    }

    @Override
    public void setTypeNid(int typeNid) {
        this.typeNid = typeNid;
        modified();
    }

    //~--- inner classes -------------------------------------------------------
    public class Version extends ConceptComponent<DescriptionRevision, Description>.Version
            implements I_DescriptionTuple<DescriptionRevision>, I_DescriptionPart<DescriptionRevision>,
            DescriptionAnalogBI<DescriptionRevision> {

        public Version(DescriptionAnalogBI<DescriptionRevision> cv) {
            super(cv);
        }

        //~--- methods ----------------------------------------------------------
        @Override
        @Deprecated
        public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
            // TODO Auto-generated method stub
        }

        @Override
        @Deprecated
        public I_DescriptionPart duplicate() {
            throw new UnsupportedOperationException("Use makeAnalog instead");
        }

        public DescriptionRevision makeAnalog() {
            if (Description.this == cv) {
                return new DescriptionRevision(Description.this);
            }

            return new DescriptionRevision((DescriptionRevision) cv, Description.this);
        }
        
        @Override
        public DescriptionRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
            return getCv().makeAnalog(statusNid, time, authorNid, moduleNid, pathNid);
        }

        @Override
        public boolean fieldsEqual(ConceptComponent<DescriptionRevision, Description>.Version another) {
            Description.Version anotherVersion = (Description.Version) another;
            if (this.isInitialCaseSignificant() != anotherVersion.isInitialCaseSignificant()) {
                return false;
            }

            if (!this.getText().equals(anotherVersion.getText())) {
                return false;
            }

            if (!this.getLang().equals(anotherVersion.getLang())) {
                return false;
            }

            if (this.getTypeNid() != anotherVersion.getTypeNid()) {
                return false;
            }

            return true;
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public int getConceptNid() {
            return enclosingConceptNid;
        }

        public DescriptionAnalogBI<DescriptionRevision> getCv() {
            return (DescriptionAnalogBI<DescriptionRevision>) cv;
        }

        @Override
        public int getDescId() {
            return nid;
        }

        @Override
        public I_DescriptionVersioned getDescVersioned() {
            return Description.this;
        }

        @Override
        public DescriptionCAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB {
            return getCv().makeBlueprint(vc);
        }

        @Override
        public String getLang() {
            return getCv().getLang();
        }

        @Override
        public I_DescriptionPart getMutablePart() {
            return (I_DescriptionPart) super.getMutablePart();
        }

        @Override
        public Description getPrimordialVersion() {
            return Description.this;
        }

        @Override
        public String getText() {
            return getCv().getText();
        }

        @Override
        public int getTypeId() {
            return getTypeNid();
        }

        @Override
        public int getTypeNid() {
            return getCv().getTypeNid();
        }

        @Override
        public ArrayIntList getVariableVersionNids() {
            if (getCv() == Description.this.getVariableVersionNids()) {
                return Description.this.getVariableVersionNids();
            }

            return ((DescriptionRevision) getCv()).getVariableVersionNids();
        }

        @Override
        public Description.Version getVersion(ViewCoordinate c) throws ContradictionException {
            return Description.this.getVersion(c);
        }

        @Override
        public List<? extends Version> getVersions() {
            return Description.this.getVersions();
        }

        @Override
        public Collection<Description.Version> getVersions(ViewCoordinate c) {
            return Description.this.getVersions(c);
        }

        @Override
        public boolean isInitialCaseSignificant() {
            return getCv().isInitialCaseSignificant();
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setInitialCaseSignificant(boolean capStatus) throws PropertyVetoException {
            getCv().setInitialCaseSignificant(capStatus);
        }

        @Override
        public void setLang(String lang) throws PropertyVetoException {
            getCv().setLang(lang);
        }

        @Override
        public void setText(String text) throws PropertyVetoException {
            getCv().setText(text);
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
        
        @Override
        public boolean matches(Pattern p) {
            return getCv().matches(p);
        }
    }
}
