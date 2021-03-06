package org.ihtsdo.concept.component.image;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.apache.commons.collections.primitives.ArrayIntList;

import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.utypes.UniversalAceImage;
import org.dwfa.ace.utypes.UniversalAceImagePart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
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
import org.ihtsdo.tk.api.media.MediaAnalogBI;
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;
import org.ihtsdo.tk.dto.concept.component.media.TkMediaRevision;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.MediaCAB;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

public class Image extends ConceptComponent<ImageRevision, Image>
        implements I_ImageVersioned<ImageRevision>, I_ImagePart<ImageRevision>, MediaAnalogBI<ImageRevision> {

    private static VersionComputer<Image.Version> computer = new VersionComputer<Image.Version>();
    //~--- fields --------------------------------------------------------------
    private String format;
    private byte[] image;
    private String textDescription;
    private int typeNid;
    List<Version> versions;

    //~--- constructors --------------------------------------------------------
    public Image() {
        super();
    }

    protected Image(Concept enclosingConcept, TupleInput input) throws IOException {
        super(enclosingConcept.getNid(), input);
    }

    public Image(TkMedia eMedia, Concept enclosingConcept) throws IOException {
        super(eMedia, enclosingConcept.getNid());
        image = eMedia.getDataBytes();
        format = eMedia.getFormat();
        textDescription = eMedia.getTextDescription();
        typeNid = Bdb.uuidToNid(eMedia.getTypeUuid());
        primordialSapNid = Bdb.getSapNid(eMedia);

        if (eMedia.getRevisionList() != null) {
            revisions = new RevisionSet<ImageRevision, Image>(primordialSapNid);

            for (TkMediaRevision eiv : eMedia.getRevisionList()) {
                revisions.add(new ImageRevision(eiv, this));
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        allNids.add(typeNid);
    }

    @Override
    public void addTuples(NidSetBI allowedStatus, NidSetBI allowedTypes, PositionSetBI positions,
            List<I_ImageTuple> matchingTuples, Precedence precedencePolicy,
            ContradictionManagerBI contradictionManager) {
        List<Version> returnTuples = new ArrayList<Version>();

        computer.addSpecifiedVersions(allowedStatus, allowedTypes, positions, returnTuples, getVersions(),
                precedencePolicy, contradictionManager);
        matchingTuples.addAll(returnTuples);
    }

    @Override
    public boolean addVersion(I_ImagePart part) {
        this.versions = null;

        return super.addRevision((ImageRevision) part);
    }

    @Override
    public void clearVersions() {
        versions = null;
        clearAnnotationVersions();
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.dwfa.vodb.types.I_ImageVersioned#convertIds(org.dwfa.vodb.jar.
     * I_MapNativeToNative)
     */
    @Override
    public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Image duplicate() {
        throw new UnsupportedOperationException();
    }

    // TODO Verify this is a correct implementation
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (Image.class.isAssignableFrom(obj.getClass())) {
            Image another = (Image) obj;

            if (this.nid == another.nid) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean fieldsEqual(ConceptComponent<ImageRevision, Image> obj) {
        if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            Image another = (Image) obj;

            if (!this.format.equals(another.format)) {
                return false;
            }

            if (!Arrays.equals(this.image, another.image)) {
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
        return Hashcode.compute(new int[]{this.getNid()});
    }
    
    @Override
    public ImageRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
        ImageRevision newR;

        newR = new ImageRevision(this, statusNid, time, authorNid, moduleNid, pathNid, this);
        addRevision(newR);

        return newR;
    }

    @Override
    public boolean promote(PositionBI viewPosition, PathSetReadOnly pomotionPaths, NidSetBI allowedStatus,
            Precedence precedence, int authorNid) {
        int viewPathId = viewPosition.getPath().getConceptNid();
        List<Version> matchingTuples = new ArrayList<Version>();

        computer.addSpecifiedVersions(allowedStatus, viewPosition, matchingTuples, getTuples(), precedence,
                null);

        boolean promotedAnything = false;

        for (PathBI promotionPath : pomotionPaths) {
            for (Version it : matchingTuples) {
                if (it.getPathNid() == viewPathId) {
                    ImageRevision promotionPart = (ImageRevision) it.makeAnalog(it.getStatusNid(),
                            Long.MAX_VALUE,
                            authorNid,
                            it.getModuleNid(),
                            promotionPath.getConceptNid());

                    it.getVersioned().addVersion(promotionPart);
                    promotedAnything = true;
                }
            }
        }

        return promotedAnything;
    }

    @Override
    public void readFromBdb(TupleInput input) {

        // nid, list size, and conceptNid are read already by the binder...
        this.format = input.readString();

        int imageBytes = input.readInt();

        image = new byte[imageBytes];
        input.read(image, 0, imageBytes);
        textDescription = input.readString();
        typeNid = input.readInt();

        int additionalVersionCount = input.readShort();
        revisions = new RevisionSet<>(additionalVersionCount);

        for (int i = 0; i < additionalVersionCount; i++) {
            ImageRevision ir = new ImageRevision(input, this);
            if (ir.getTime() != Long.MIN_VALUE) {
                revisions.add(ir);
            }
        }
    }

    @Override
    public boolean readyToWriteComponent() {
        assert textDescription != null : assertionString();
        assert format != null : assertionString();
        assert typeNid != Integer.MAX_VALUE : assertionString();
        assert image != null : assertionString();

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
        buf.append("format:'").append(this.format).append("'");
        buf.append(" image:").append(this.image);
        buf.append(" textDescription:'").append(this.textDescription).append("'");
        buf.append(" typeNid:");
        ConceptComponent.addNidToBuffer(buf, typeNid);
        buf.append(" ");
        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    public String toUserString() {
        StringBuffer buf = new StringBuffer();

        ConceptComponent.addTextToBuffer(buf, typeNid);
        buf.append("; ");
        buf.append(format);
        buf.append(": ");
        buf.append(textDescription);

        return buf.toString();
    }

    /**
     * Test method to check to see if two objects are equal in all respects.
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures.
     * @throws IOException
     */
    public String validate(Image another) throws IOException {
        assert another != null;

        StringBuilder buf = new StringBuilder();

        if (!this.format.equals(another.format)) {
            buf.append("\tImage.format not equal: \n\t\tthis.format = ").append(this.format).append(
                    "\n\t\tanother.format = ").append(another.format).append("\n");
        }

        if (!Arrays.equals(this.image, another.image)) {
            buf.append("\tImage.image not equal: \n" + "\t\tthis.image = ").append(this.image).append(
                    "\n\t\tanother.image = ").append(another.image).append("\n");
        }

        if (this.typeNid != another.typeNid) {
            buf.append("\tImage.typeNid not equal: \n\t\tthis.typeNid = ").append(this.typeNid).append(
                    "\n\t\tanother.typeNid = ").append(another.typeNid).append("\n");
        }

        // Compare the parents
        buf.append(super.validate(another));

        return buf.toString();
    }

    @Override
    public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        List<ImageRevision> partsToWrite = new ArrayList<ImageRevision>();

        if (revisions != null) {
            for (ImageRevision p : revisions) {
                if ((p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid)
                        && (p.getTime() != Long.MIN_VALUE)) {
                    partsToWrite.add(p);
                }
            }
        }

        // Start writing
        // conceptNid is the enclosing concept, does not need to be written.
        output.writeString(format);
        output.writeInt(image.length);
        output.write(image);
        output.writeString(textDescription);
        output.writeInt(typeNid);
        output.writeShort(partsToWrite.size());

        for (ImageRevision p : partsToWrite) {
            p.writePartToBdb(output);
        }
    }

    //~--- get methods ---------------------------------------------------------

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageVersioned#getConceptNid()
     */
    @Override
    public int getConceptNid() {
        return enclosingConceptNid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageVersioned#getFormat()
     */
    @Override
    public MediaCAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB {
        MediaCAB mediaBp = new MediaCAB(getConceptNid(),
                getTypeNid(),
                getFormat(),
                getTextDescription(),
                getMedia(),
                getVersion(vc),
                vc);
        return mediaBp;
    }

    @Override
    public String getFormat() {
        return format;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageVersioned#getImage()
     */
    @Override
    public byte[] getImage() {
        return image;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageVersioned#getImageId()
     */
    @Override
    public int getImageId() {
        return nid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageVersioned#getLastTuple()
     */
    @Override
    public Version getLastTuple() {
        List<Version> vList = getTuples();

        return vList.get(vList.size() - 1);
    }

    @Override
    public byte[] getMedia() {
        return image;
    }

    @Override
    public Image getMutablePart() {
        return this;
    }

    @Override
    public List<? extends I_ImagePart> getMutableParts() {
        return getTuples();
    }

    @Override
    public Image getPrimordialVersion() {
        return Image.this;
    }

    @Override
    public String getTextDescription() {
        return textDescription;
    }

    @Override
    public List<Version> getTuples() {
        return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
    }

    @Override
    public int getTypeId() {
        return typeNid;
    }

    @Override
    public int getTypeNid() {
        return typeNid;
    }

    private static Collection<UUID> getUids(int id) throws IOException, TerminologyException {
        return LocalFixedTerminology.getStore().getUids(id);
    }

    @Override
    public UniversalAceImage getUniversal() throws IOException, TerminologyException {
        UniversalAceImage universal = new UniversalAceImage(getUids(nid), getImage(),
                new ArrayList<UniversalAceImagePart>(revisions.size()), getFormat(),
                getEnclosingConcept().getUUIDs());

        for (org.ihtsdo.concept.component.image.Image.Version part : getVersions()) {
            UniversalAceImagePart universalPart = new UniversalAceImagePart();

            universalPart.setPathId(getUids(part.getPathNid()));
            universalPart.setStatusId(getUids(part.getStatusNid()));
            universalPart.setTextDescription(part.getTextDescription());
            universalPart.setTypeId(getUids(part.getTypeId()));
            universalPart.setTime(part.getTime());
            universal.addVersion(universalPart);
        }

        return universal;
    }

    @Override
    public ArrayIntList getVariableVersionNids() {
        ArrayIntList nidList = new ArrayIntList(3);

        nidList.add(typeNid);

        return nidList;
    }

    @Override
    public Image.Version getVersion(ViewCoordinate c) throws ContradictionException {
        List<Image.Version> vForC = getVersions(c);

        if (vForC.isEmpty()) {
            return null;
        }

        if (vForC.size() > 1) {
            vForC = c.getContradictionManager().resolveVersions(vForC);
        }

        if (vForC.size() > 1) {
            throw new ContradictionException(vForC.toString());
        }

        return vForC.get(0);
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
                for (ImageRevision ir : revisions) {
                    if (ir.getTime() != Long.MIN_VALUE) {
                        list.add(new Version(ir));
                    }
                }
            }

            versions = list;
        }

        return Collections.unmodifiableList(versions);
    }

    @Override
    public List<Image.Version> getVersions(ViewCoordinate c) {
        List<Version> returnTuples = new ArrayList<Version>(2);

        computer.addSpecifiedVersions(c.getAllowedStatusNids(), (NidSetBI) null, c.getPositionSet(),
                returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager());

        return returnTuples;
    }

    public Collection<Image.Version> getVersions(NidSetBI allowedStatus, NidSetBI allowedTypes,
            PositionSetBI viewPositions, Precedence precedence, ContradictionManagerBI contradictionMgr) {
        List<Version> returnTuples = new ArrayList<Version>(2);

        computer.addSpecifiedVersions(allowedStatus, allowedTypes, viewPositions, returnTuples, getVersions(),
                precedence, contradictionMgr);

        return returnTuples;
    }

    @Override
    public boolean hasExtensions() throws IOException {
        if (getEnclosingConcept().hasMediaExtensions()) {
            return getEnclosingConcept().hasExtensionsForComponent(nid);
        }

        return false;
    }

    //~--- set methods ---------------------------------------------------------
    public void setFormat(String format) {
        this.format = format;
        modified();
    }

    public void setImage(byte[] image) {
        this.image = image;
        modified();
    }

    @Override
    public void setTextDescription(String textDescription) {
        this.textDescription = textDescription;
        modified();
    }

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

    //~--- inner classes -------------------------------------------------------
    public class Version extends ConceptComponent<ImageRevision, Image>.Version
            implements I_ImageTuple<ImageRevision>, I_ImagePart<ImageRevision>, MediaAnalogBI<ImageRevision> {

        public Version(MediaAnalogBI cv) {
            super(cv);
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        public I_ImagePart duplicate() {
            throw new UnsupportedOperationException("Use makeAnalog instead");
        }

        public ImageRevision makeAnalog() {
            if (getCv() != Image.this) {
                return new ImageRevision((ImageRevision) getCv(), Image.this);
            }

            return new ImageRevision(Image.this);
        }
        
        @Override
        public ImageRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
            return (ImageRevision) getCv().makeAnalog(statusNid, time, authorNid, moduleNid, pathNid);
        }

        @Override
        public boolean fieldsEqual(ConceptComponent.Version another) {
            Image.Version anotherVersion = (Image.Version) another;
            if (!this.getFormat().equals(anotherVersion.getFormat())) {
                return false;
            }

            if (!Arrays.equals(this.getImage(), anotherVersion.getImage())) {
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

        MediaAnalogBI getCv() {
            return (MediaAnalogBI) cv;
        }

        @Override
        public MediaCAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB {
            return getCv().makeBlueprint(vc);
        }

        @Override
        public String getFormat() {
            return format;
        }

        @Override
        public byte[] getImage() {
            return image;
        }

        @Override
        public int getImageId() {
            return nid;
        }

        @Override
        public byte[] getMedia() {
            return image;
        }

        @Override
        public I_ImagePart getMutablePart() {
            return (I_ImagePart) super.getMutablePart();
        }

        @Override
        public Image getPrimordialVersion() {
            return Image.this;
        }

        @Override
        public String getTextDescription() {
            return getCv().getTextDescription();
        }

        @Override
        @Deprecated
        public int getTypeId() {
            return getCv().getTypeNid();
        }

        @Override
        public int getTypeNid() {
            return getCv().getTypeNid();
        }

        @Override
        public ArrayIntList getVariableVersionNids() {
            if (Image.this != getCv()) {
                return ((ImageRevision) getCv()).getVariableVersionNids();
            }

            return Image.this.getVariableVersionNids();
        }

        @Override
        public Image.Version getVersion(ViewCoordinate c) throws ContradictionException {
            return Image.this.getVersion(c);
        }

        @Override
        public I_ImageVersioned getVersioned() {
            return Image.this;
        }

        @Override
        public List<? extends Version> getVersions() {
            return Image.this.getVersions();
        }

        @Override
        public Collection<Image.Version> getVersions(ViewCoordinate c) {
            return Image.this.getVersions(c);
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setTextDescription(String name) throws PropertyVetoException {
            getCv().setTextDescription(name);
        }

        @Override
        @Deprecated
        public void setTypeId(int type) throws PropertyVetoException {
            getCv().setTypeNid(type);
        }

        @Override
        public void setTypeNid(int type) throws PropertyVetoException {
            getCv().setTypeNid(type);
        }
    }
}
