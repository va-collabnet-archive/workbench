package org.ihtsdo.db.bdb.concept.component.description;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.util.VersionComputer;
import org.ihtsdo.etypes.EDescription;
import org.ihtsdo.etypes.EDescriptionVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Description extends ConceptComponent<DescriptionRevision, Description> implements I_DescriptionVersioned,
        I_DescriptionPart {

    private static VersionComputer<Description.Version> computer = new VersionComputer<Description.Version>();

    public class Version extends ConceptComponent<DescriptionRevision, Description>.Version implements
            I_DescriptionTuple, I_DescriptionPart {

        public Version() {
            super();
        }

        public Version(int index) {
            super(index);
        }

        @Override
        public int getConceptId() {
            return enclosingConcept.getNid();
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
                return additionalVersions.get(index).getLang();
            }
            return Description.this.lang;
        }

        @Override
        public String getText() {
            if (index >= 0) {
                return additionalVersions.get(index).getText();
            }
            return Description.this.text;
        }

        @Override
        public boolean isInitialCaseSignificant() {
            if (index >= 0) {
                return additionalVersions.get(index).isInitialCaseSignificant();
            }
            return Description.this.initialCaseSignificant;
        }

        @Override
        public void setInitialCaseSignificant(boolean capStatus) {
            if (index >= 0) {
                additionalVersions.get(index).setInitialCaseSignificant(capStatus);
            } else {
                Description.this.initialCaseSignificant = capStatus;
            }
        }

        @Override
        public void setLang(String lang) {
            if (index >= 0) {
                additionalVersions.get(index).setLang(lang);
            } else {
                Description.this.lang = lang;
            }
        }

        @Override
        public void setText(String text) {
            if (index >= 0) {
                additionalVersions.get(index).setText(text);
            } else {
                Description.this.text = text;
            }
        }

        @Override
        public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getTypeId() {
            if (index >= 0) {
                return additionalVersions.get(index).getTypeId();
            } else {
                return Description.this.typeNid;
            }
        }

        @Override
        public void setTypeId(int type) {
            if (index >= 0) {
                additionalVersions.get(index).setTypeId(type);
            } else {
                Description.this.typeNid = type;
            }
        }

        @Override
        public ArrayIntList getPartComponentNids() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public I_DescriptionPart getMutablePart() {
            return (I_DescriptionPart) super.getMutablePart();
        }

        @Override
        public I_DescriptionPart makeAnalog(int statusNid, int pathNid, long time) {
            if (index >= 0) {
                return additionalVersions.get(index).makeAnalog(statusNid, pathNid, time);
            } else {
                return Description.this.makeAnalog(statusNid, pathNid, time);
            }
        }

        @Override
        @Deprecated
        public I_DescriptionPart duplicate() {
            throw new UnsupportedOperationException("Use makeAnalog instead");
        }

    }

    private String text;
    private boolean initialCaseSignificant;
    private int typeNid;
    private String lang;

    public Description(Concept enclosingConcept, TupleInput input) {
        super(enclosingConcept, input);
    }

    public Description(EDescription eDesc, Concept enclosingConcept) {
        super(eDesc, enclosingConcept);
        initialCaseSignificant = eDesc.isInitialCaseSignificant();
        lang = eDesc.getLang();
        text = eDesc.getText();
        typeNid = Bdb.uuidToNid(eDesc.getTypeUuid());
        primordialSapNid = Bdb.getStatusAtPositionNid(eDesc);
        if (eDesc.getExtraVersionsList() != null) {
            additionalVersions = new ArrayList<DescriptionRevision>(eDesc.getExtraVersionsList().size());
            for (EDescriptionVersion edv : eDesc.getExtraVersionsList()) {
                additionalVersions.add(new DescriptionRevision(edv, this));
            }
        }
    }

    public Description() {
        super();
    }


    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName() + ": ");
        buf.append(" computer:" + Description.computer);
        buf.append(" text:" + "'" + this.getText() + "'");
        buf.append(" initialCaseSignificant:" + isInitialCaseSignificant());
        buf.append(" typeNid:");
        ConceptComponent.addNidToBuffer(buf, typeNid);
        buf.append(" lang:" + this.getLang());
        buf.append("; ");
        buf.append(super.toString());
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

    @Override
    public void readFromBdb(TupleInput input) {
        initialCaseSignificant = input.readBoolean();
        lang = input.readString();
        text = input.readString();
        typeNid = input.readInt();
        // nid, list size, and conceptNid are read already by the binder...
        int additionalVersionCount = input.readShort();
        if (additionalVersionCount > 0) {
            additionalVersions = new ArrayList<DescriptionRevision>(additionalVersionCount);
            for (int i = 0; i < additionalVersionCount; i++) {
                additionalVersions.add(new DescriptionRevision(input, this));
            }
        }
    }

    @Override
    public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        List<DescriptionRevision> partsToWrite = new ArrayList<DescriptionRevision>();
        if (additionalVersions != null) {
            for (DescriptionRevision p : additionalVersions) {
                if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
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

    @Override
    public int getConceptId() {
        return enclosingConcept.getNid();
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
        return getTuples().get(getTuples().size() - 1);
    }

    @Override
    public List<Version> getVersions(boolean returnConflictResolvedLatestState) throws TerminologyException,
            IOException {
        return getTuples(returnConflictResolvedLatestState);
    }

    @Override
    public List<Version> getTuples(boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {
        // TODO support returnConflictResolvedLatestState
        List<Version> versions = new ArrayList<Version>();
        versions.add(new Version());
        if (additionalVersions != null) {
            for (int i = 0; i < additionalVersions.size(); i++) {
                versions.add(new Version(i));
            }
        }
        return versions;
    }

    @Override
    public UniversalAceDescription getUniversal() throws IOException, TerminologyException {
        UniversalAceDescription universal = new UniversalAceDescription(getUids(nid), enclosingConcept.getUids(),
            this.versionCount());
        for (DescriptionRevision part : additionalVersions) {
            UniversalAceDescriptionPart universalPart = new UniversalAceDescriptionPart();
            universalPart.setInitialCaseSignificant(part.isInitialCaseSignificant());
            universalPart.setLang(part.getLang());
            universalPart.setPathId(getUids(part.getPathId()));
            universalPart.setStatusId(getUids(part.getStatusId()));
            universalPart.setText(part.getText());
            universalPart.setTypeId(getUids(part.getTypeId()));
            universalPart.setTime(ThinVersionHelper.convert(part.getVersion()));
            universal.addVersion(universalPart);
        }
        return universal;
    }

    @Override
    public boolean matches(Pattern p) {
        String lastText = null;
        for (DescriptionRevision desc : additionalVersions) {
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

    public boolean merge(Description jarDesc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public I_DescribeConceptLocally toLocalFixedDesc() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean promote(I_Position viewPosition, PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
            throws IOException, TerminologyException {
        throw new UnsupportedOperationException();
        /*
         * int viewPathId = viewPosition.getPath().getConceptId();
         * List<DescriptionVersion> matchingTuples = new
         * ArrayList<DescriptionVersion>();
         * addTuples(allowedStatus, viewPosition, matchingTuples);
         * boolean promotedAnything = false;
         * for (I_Path promotionPath: pomotionPaths) {
         * for (DescriptionVersion dt: matchingTuples) {
         * if (dt.getPathId() == viewPathId) {
         * DescriptionMutablePart promotionPart =
         * dt.getPart().makeAnalog(dt.getStatusId(),
         * promotionPath.getConceptId(),
         * Long.MAX_VALUE);
         * // TODO add a check to see if already promoted?
         * dt.getDescVersioned().addVersion(promotionPart);
         * promotedAnything = true;
         * }
         * }
         * }
         * return promotedAnything;
         */
    }

    private static Collection<UUID> getUids(int id) throws IOException, TerminologyException {
        return LocalFixedTerminology.getStore().getUids(id);
    }

    @Override
    public boolean addVersion(I_DescriptionPart newPart) {
        this.versions = null;
        return additionalVersions.add((DescriptionRevision) newPart);
    }

    @Override
    public boolean merge(I_DescriptionVersioned jarDesc) {
        throw new UnsupportedOperationException();
    }

    public int getConceptNid() {
        return enclosingConcept.getNid();
    }

    /*
     * Consider depreciating the below methods...
     */

    List<Version> versions;

    public final List<Version> getTuples() {
        if (versions == null) {
            versions = new ArrayList<Version>();
            versions.add(new Version());
        }
        if (additionalVersions != null) {
            for (int i = 0; i < additionalVersions.size(); i++) {
                versions.add(new Version(i));
            }
        }
        return Collections.unmodifiableList(versions);
    }

    public void addTuples(I_IntSet allowedStatus, I_Position viewPosition, List<Description.Version> matchingTuples) {
        computer.addTuples(allowedStatus, viewPosition, matchingTuples, getTuples());
    }

    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, PositionSetReadOnly positions,
            List<Description.Version> matchingTuples, boolean addUncommitted) {
        computer.addTuples(allowedStatus, allowedTypes, positions, matchingTuples, addUncommitted, getTuples());
    }

    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, PositionSetReadOnly positions,
            List<Version> matchingTuples, boolean addUncommitted, boolean returnConflictResolvedLatestState)
            throws TerminologyException, IOException {
        List<Version> tuples = new ArrayList<Version>();

        computer.addTuples(allowedStatus, allowedTypes, positions, tuples, addUncommitted, getTuples());

        if (returnConflictResolvedLatestState) {
            I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
            I_ManageConflict conflictResolutionStrategy;
            if (config == null) {
                conflictResolutionStrategy = new IdentifyAllConflictStrategy();
            } else {
                conflictResolutionStrategy = config.getConflictResolutionStrategy();
            }

            tuples = conflictResolutionStrategy.resolveTuples(tuples);
        }
        matchingTuples.addAll(tuples);
    }

    @Override
    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positionSet,
            List<I_DescriptionTuple> matchingTuples, boolean addUncommitted) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes, Set<I_Position> positionSet,
            List<I_DescriptionTuple> matchingTuples, boolean addUncommitted, boolean returnConflictResolvedLatestState)
            throws TerminologyException, IOException {
        throw new UnsupportedOperationException();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean isInitialCaseSignificant() {
        return initialCaseSignificant;
    }

    public void setInitialCaseSignificant(boolean initialCaseSignificant) {
        this.initialCaseSignificant = initialCaseSignificant;
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList nidList = new ArrayIntList(3);
        nidList.add(typeNid);
        return nidList;
    }

    @Override
    public int getTypeId() {
        return typeNid;
    }

    @Override
    public void setTypeId(int typeNid) {
        this.typeNid = typeNid;
    }

    @Override
    public I_DescriptionPart makeAnalog(int statusNid, int pathNid, long time) {
        return new DescriptionRevision(this, statusNid, pathNid, time, this);
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
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
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
        return HashFunction.hashCode(new int[] { nid });
    }

}
