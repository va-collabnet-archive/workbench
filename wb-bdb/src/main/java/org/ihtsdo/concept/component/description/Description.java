package org.ihtsdo.concept.component.description;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Description
        extends ConceptComponent<DescriptionRevision, Description>
        implements I_DescriptionVersioned<DescriptionRevision>,
        I_DescriptionPart<DescriptionRevision>,
        DescriptionAnalogBI<DescriptionRevision> {

    private static VersionComputer<Description.Version> computer =
            new VersionComputer<Description.Version>();

    public class Version
            extends ConceptComponent<DescriptionRevision, Description>.Version
            implements I_DescriptionTuple<DescriptionRevision>,
            I_DescriptionPart<DescriptionRevision>,
            DescriptionAnalogBI<DescriptionRevision> {

        public Version() {
            super();
        }

        public Version(int index) {
            super(index);
        }

        @Override
        public Description getPrimordialVersion() {
            return Description.this;
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
        public I_DescriptionVersioned getDescVersioned() {
            return Description.this;
        }

        @Override
        public String getLang() {
            if (index >= 0) {
                return revisions.get(index).getLang();
            }
            return Description.this.lang;
        }

        @Override
        public String getText() {
            if (index >= 0) {
                return revisions.get(index).getText();
            }
            return Description.this.text;
        }

        @Override
        public boolean isInitialCaseSignificant() {
            if (index >= 0) {
                return revisions.get(index).isInitialCaseSignificant();
            }
            return Description.this.initialCaseSignificant;
        }

        @Override
        public void setInitialCaseSignificant(boolean capStatus) {
            if (index >= 0) {
                revisions.get(index).setInitialCaseSignificant(capStatus);
            } else {
                Description.this.setInitialCaseSignificant(capStatus);
            }
        }

        @Override
        public void setLang(String lang) {
            if (index >= 0) {
                revisions.get(index).setLang(lang);
            } else {
                Description.this.setLang(lang);
            }
        }

        @Override
        public void setText(String text) {
            if (index >= 0) {
                revisions.get(index).setText(text);
            } else {
                Description.this.setText(text);
            }
        }

        @Override
        @Deprecated
        public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
            // TODO Auto-generated method stub
        }

        @Override
        public int getTypeId() {
            return getTypeNid();
        }

        @Override
        public int getTypeNid() {
            if (index >= 0) {
                return revisions.get(index).getTypeNid();
            }
            return Description.this.typeNid;
        }

        @Override
        public List<? extends Version> getVersions() {
            return Description.this.getVersions();
        }

        @Override
        @Deprecated
        public void setTypeId(int type) {
            if (index >= 0) {
                revisions.get(index).setTypeId(type);
            } else {
                Description.this.setTypeId(type);
            }
        }

        @Override
        public void setTypeNid(int typeNid) {
            if (index >= 0) {
                revisions.get(index).setTypeNid(typeNid);
            } else {
                Description.this.setTypeNid(typeNid);
            }
        }

        @Override
        public ArrayIntList getVariableVersionNids() {
            if (index >= 0) {
                ArrayIntList resultList = new ArrayIntList(3);
                resultList.add(getTypeId());
                return resultList;
            }
            return Description.this.getVariableVersionNids();
        }

        @Override
        public I_DescriptionPart getMutablePart() {
            return (I_DescriptionPart) super.getMutablePart();
        }

        @Override
        public DescriptionRevision makeAnalog(int statusNid, int pathNid, long time) {
            if (index >= 0) {
                DescriptionRevision rev = revisions.get(index);
                if (rev.getTime() == Long.MAX_VALUE && rev.getPathNid() == pathNid) {
                    rev.setStatusNid(statusNid);
                    return rev;
                }
                return revisions.get(index).makeAnalog(statusNid, pathNid, time);
            } else {
                return Description.this.makeAnalog(statusNid, pathNid, time);
            }
        }

        public DescriptionRevision makeAnalog() {
            if (index >= 0) {
                DescriptionRevision rev = revisions.get(index);
                return new DescriptionRevision(rev, Description.this);
            }
            return new DescriptionRevision(Description.this);
        }

        @Override
        @Deprecated
        public I_DescriptionPart duplicate() {
            throw new UnsupportedOperationException("Use makeAnalog instead");
        }

        @Override
        public DescriptionRevision makeAnalog(int statusNid, int authorNid, int pathNid,
                long time) {
            if (index >= 0) {
                DescriptionRevision rev = revisions.get(index);
                if (rev.getTime() == Long.MAX_VALUE && rev.getPathNid() == pathNid) {
                    rev.setStatusNid(statusNid);
                    rev.setAuthorNid(authorNid);
                    return rev;
                }
                return revisions.get(index).makeAnalog(statusNid, authorNid, pathNid, time);
            } else {
                return Description.this.makeAnalog(statusNid, authorNid, pathNid, time);
            }
        }

        @Override
        public Description.Version getVersion(ViewCoordinate c)
                throws ContraditionException {
            return Description.this.getVersion(c);
        }

        @Override
        public Collection<Description.Version> getVersions(
                ViewCoordinate c) {
            return Description.this.getVersions(c);
        }
    }
    private String text;
    private boolean initialCaseSignificant;
    int typeNid;
    private String lang;

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
            revisions = new CopyOnWriteArrayList<DescriptionRevision>();
            for (TkDescriptionRevision edv : eDesc.getRevisionList()) {
                try {
                    revisions.add(new DescriptionRevision(edv, this));
                } catch (TerminologyException e) {
                    throw new IOException(e);
                }
            }
        }
    }

    public Description() {
        super();
    }


    /* (non-Javadoc)
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

    /**
     * Test method to check to see if two objects are equal in all respects. 
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures. 
     * @throws IOException 
     */
    public String validate(Description another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();

        if (this.initialCaseSignificant != another.initialCaseSignificant) {
            buf.append("\tDescription.initialCaseSignificant not equal: \n"
                    + "\t\tthis.initialCaseSignificant = " + this.initialCaseSignificant + "\n"
                    + "\t\tanother.initialCaseSignificant = " + another.initialCaseSignificant + "\n");
        }
        if (!this.text.equals(another.text)) {
            buf.append("\tDescription.text not equal: \n"
                    + "\t\tthis.text = " + this.text + "\n"
                    + "\t\tanother.text = " + another.text + "\n");
        }
        if (!this.lang.equals(another.lang)) {
            buf.append("\tDescription.lang not equal: \n"
                    + "\t\tthis.lang = " + this.lang + "\n"
                    + "\t\tanother.lang = " + another.lang + "\n");
        }
        if (this.typeNid != another.typeNid) {
            buf.append("\tDescription.typeNid not equal: \n"
                    + "\t\tthis.typeNid = " + this.typeNid + "\n"
                    + "\t\tanother.typeNid = " + another.typeNid + "\n");
        }

        // Compare the parents 
        buf.append(super.validate(another));

        return buf.toString();
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
            revisions = new CopyOnWriteArrayList<DescriptionRevision>();
            for (int i = 0; i < additionalVersionCount; i++) {
                DescriptionRevision dr = new DescriptionRevision(input, this);
                if (dr.getTime() != Long.MIN_VALUE) {
                    revisions.add(dr);
                }
            }
        }
    }

    @Override
    public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        List<DescriptionRevision> partsToWrite = new ArrayList<DescriptionRevision>();
        if (revisions != null) {
            for (DescriptionRevision p : revisions) {
                if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid
                        && p.getTime() != Long.MIN_VALUE) {
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

    @Override
    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        throw new UnsupportedOperationException();
    }

    public Concept getConcept() {
        return getEnclosingConcept();
    }

    @Override
    public int getDescId() {
        return nid;
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
    public List<Version> getVersions(ContradictionManagerBI contradictionMgr)
            throws TerminologyException, IOException {
        return getTuples(contradictionMgr);
    }

    @Override
    public List<Version> getTuples(ContradictionManagerBI contradictionMgr)
            throws TerminologyException, IOException {
        // TODO implement ContradictionManagerBI contradictionMgr
        return getVersions();
    }

    @Override
    public UniversalAceDescription getUniversal() throws IOException,
            TerminologyException {
        UniversalAceDescription universal = new UniversalAceDescription(this);
        return universal;
    }

    @Override
    public boolean matches(Pattern p) {
        String lastText = null;
        for (Description.Version desc : getVersions()) {
            if (desc.getText() != lastText) {
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
    public I_DescribeConceptLocally toLocalFixedDesc() {
        return new LocalFixedDesc(nid, getStatusNid(), getConceptNid(), isInitialCaseSignificant(),
                getTypeId(), getText(), getLang());
    }

    @Override
    public boolean addVersion(I_DescriptionPart newPart) {
        this.versions = null;
        BdbCommitManager.addUncommittedDescNid(nid);
        return super.addRevision((DescriptionRevision) newPart);
    }

    public int getConceptNid() {
        return enclosingConceptNid;
    }

    /*
     * Consider depreciating the below methods...
     */
    List<Version> versions;

    public List<Version> getTuples() {
        return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
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

    public void addTuples(NidSetBI allowedStatus, I_Position viewPosition,
            List<Description.Version> matchingTuples, Precedence precedence,
            ContradictionManagerBI contradictionMgr) {
        computer.addSpecifiedVersions(allowedStatus, viewPosition,
                matchingTuples, getVersions(), precedence, contradictionMgr);
    }

    @Override
    public void addTuples(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI positions,
            List<I_DescriptionTuple<DescriptionRevision>> matchingTuples,
            Precedence precedence, ContradictionManagerBI contradictionManager) {
        List<Version> returnTuples = new ArrayList<Version>();
        computer.addSpecifiedVersions(allowedStatus, allowedTypes, positions,
                returnTuples, getVersions(), precedence, contradictionManager);
        matchingTuples.addAll(returnTuples);
    }

    public List<Description.Version> getVersions(NidSetBI allowedStatus,
            NidSetBI allowedTypes, PositionSetBI viewPositions,
            Precedence precedence, ContradictionManagerBI contradictionMgr) {
        List<Version> returnTuples = new ArrayList<Version>(2);
        computer.addSpecifiedVersions(allowedStatus, allowedTypes, viewPositions,
                returnTuples, getVersions(), precedence, contradictionMgr);
        return returnTuples;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        modified();
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
        modified();
    }

    public boolean isInitialCaseSignificant() {
        return initialCaseSignificant;
    }

    public void setInitialCaseSignificant(boolean initialCaseSignificant) {
        this.initialCaseSignificant = initialCaseSignificant;
        modified();
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList nidList = new ArrayIntList(3);
        nidList.add(typeNid);
        return nidList;
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

    @Override
    public DescriptionRevision makeAnalog(int statusNid, int pathNid, long time) {
        DescriptionRevision newR;
        newR = new DescriptionRevision(this, statusNid,
                Terms.get().getAuthorNid(),
                pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public DescriptionRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        DescriptionRevision newR;
        newR = new DescriptionRevision(this, statusNid,
                authorNid,
                pathNid, time, this);
        addRevision(newR);
        return newR;
    }

    @Override
    public I_DescriptionPart duplicate() {
        throw new UnsupportedOperationException();
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
    public int hashCode() {
        return HashFunction.hashCode(new int[]{nid});
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

                    DescriptionRevision revision = v.makeAnalog(v.getStatusNid(),
                            promotionPath.getConceptNid(), Long.MAX_VALUE);
                    addRevision(revision);
                    promotedAnything = true;
                }
            }
        }
        return promotedAnything;
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
    public Description.Version getVersion(ViewCoordinate c)
            throws ContraditionException {
        List<Description.Version> vForC = getVersions(c);
        if (vForC.size() == 0) {
            return null;
        }
        if (vForC.size() > 1) {
            throw new ContraditionException(vForC.toString());
        }
        return vForC.get(0);
    }

    @Override
    public List<Description.Version> getVersions(ViewCoordinate c) {
        List<Version> returnTuples = new ArrayList<Version>(2);
        computer.addSpecifiedVersions(c.getAllowedStatusNids(), (NidSetBI) null, c.getPositionSet(),
                returnTuples, getVersions(), c.getPrecedence(), c.getContradictionManager());
        return returnTuples;
    }

    @Override
    public Description getPrimordialVersion() {
        return Description.this;
    }
}
