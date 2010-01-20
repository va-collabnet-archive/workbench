package org.ihtsdo.db.bdb.concept.component.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.utypes.UniversalAceImage;
import org.dwfa.ace.utypes.UniversalAceImagePart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.util.VersionComputer;
import org.ihtsdo.etypes.EImage;
import org.ihtsdo.etypes.EImageVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Image 
	extends ConceptComponent<ImageVersion, Image> 
	implements I_ImageVersioned, I_ImagePart, I_ImageTuple {

	private static class ImageTupleComputer extends
			VersionComputer<Image, ImageVersion> {

	}

	private static ImageTupleComputer computer = new ImageTupleComputer();
	private String format;
	private byte[] image;
	
	private String textDescription;
	private int typeNid;

	protected Image(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public Image(EImage eImage, Concept enclosingConcept) {
		super(eImage, enclosingConcept);
		image = eImage.getImage();
		format = eImage.getFormat();
		textDescription = eImage.getTextDescription();
		typeNid = Bdb.uuidToNid(eImage.getPrimordialComponentUuid());
		primordialSapNid = Bdb.getStatusAtPositionNid(eImage);
		if (eImage.getExtraVersionsList() != null) {
			additionalVersions = new ArrayList<ImageVersion>(eImage.getExtraVersionsList().size());
			for (EImageVersion eiv: eImage.getExtraVersionsList()) {
				additionalVersions.add(new ImageVersion(eiv, this));
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("nid: ");
		buf.append(nid);
		buf.append(" format: ");
		buf.append(format);
		buf.append(" textDescription: ");
		buf.append(textDescription);
		buf.append(" typeNid: ");
		buf.append(typeNid);
		buf.append(" ");
		buf.append(super.toString());
		return buf.toString();
	}


	@Override
	public boolean fieldsEqual(ConceptComponent<ImageVersion, Image> obj) {
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
	public void readFromBdb(TupleInput input) {		
		// nid, list size, and conceptNid are read already by the binder...
		this.format = input.readString();
		int imageBytes = input.readInt();
		image = new byte[imageBytes];
		input.read(image, 0, imageBytes);
		textDescription = input.readString();
		typeNid = input.readInt();
		int additionalVersionCount = input.readShort();
		for (int i = 0; i < additionalVersionCount; i++) {
			additionalVersions.add(new ImageVersion(input, this));
		}
	}

	@Override
	public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		List<ImageVersion> partsToWrite = new ArrayList<ImageVersion>();
		if (additionalVersions != null) {
			for (ImageVersion p: additionalVersions) {
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
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
		for (ImageVersion p: partsToWrite) {
			p.writePartToBdb(output);
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getImage()
	 */
	public byte[] getImage() {
		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getImageId()
	 */
	public int getImageId() {
		return nid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getFormat()
	 */
	public String getFormat() {
		return format;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getConceptId()
	 */
	public int getConceptId() {
		return enclosingConcept.getNid();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getLastTuple()
	 */
	public ImageVersion getLastTuple() {
		return additionalVersions.get(additionalVersions.size() - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getTuples()
	 */
	public List<I_ImageTuple> getTuples() {
		List<I_ImageTuple> tuples = new ArrayList<I_ImageTuple>();
		for (ImageVersion p : additionalVersions) {
			tuples.add(p);
		}
		return tuples;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.dwfa.vodb.types.I_ImageVersioned#convertIds(org.dwfa.vodb.jar.
	 * I_MapNativeToNative)
	 */
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<I_ImageTuple> matchingTuples) {
		throw new UnsupportedOperationException();
		/*
		computer.addTuples(allowedStatus, allowedTypes, positions, 
				matchingTuples, true, mutableParts, this);
				*/
	}

	private static Collection<UUID> getUids(int id) throws IOException,
			TerminologyException {
		return LocalFixedTerminology.getStore().getUids(id);
	}

	public UniversalAceImage getUniversal() throws IOException,
			TerminologyException {
		UniversalAceImage universal = new UniversalAceImage(getUids(nid),
				getImage(), new ArrayList<UniversalAceImagePart>(additionalVersions
						.size()), getFormat(), enclosingConcept.getUids());
		for (ImageVersion part : additionalVersions) {
			UniversalAceImagePart universalPart = new UniversalAceImagePart();
			universalPart.setPathId(getUids(part.getPathId()));
			universalPart.setStatusId(getUids(part.getStatusId()));
			universalPart.setTextDescription(part.getTextDescription());
			universalPart.setTypeId(getUids(part.getTypeId()));
			universalPart.setTime(part.getTime());
			universal.addVersion(universalPart);
		}
		return universal;
	}

	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus) {
		int viewPathId = viewPosition.getPath().getConceptId();
		List<ImageVersion> matchingTuples = new ArrayList<ImageVersion>();
		computer.addTuples(allowedStatus, viewPosition, matchingTuples, 
				additionalVersions, this);
		boolean promotedAnything = false;
		for (I_Path promotionPath : pomotionPaths) {
			for (ImageVersion it : matchingTuples) {
				if (it.getPathId() == viewPathId) {
					ImageVersion promotionPart = it.makeAnalog(
							it.getStatusId(), promotionPath.getConceptId(),
							Long.MAX_VALUE);
					it.getVersioned().addVersion(promotionPart);
					promotedAnything = true;
				}
			}
		}
		return promotedAnything;
	}

	@Override
	public boolean addVersion(I_ImagePart part) {
		return additionalVersions.add((ImageVersion) part);
	}

	@Override
	public boolean merge(I_ImageVersioned jarImage) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ArrayIntList getVariableVersionNids() {
		ArrayIntList nidList = new ArrayIntList(3);
		nidList.add(typeNid);
		return nidList;
	}

	@Override
	public String getTextDescription() {
		return textDescription;
	}

	@Override
	public void setTextDescription(String textDescription) {
		this.textDescription = textDescription;
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
	public ImageVersion makeAnalog(int statusNid, int pathNid, long time) {
		return new ImageVersion(this, statusNid, pathNid, time, this);
	}

	@Override
	public I_ImageVersioned getVersioned() {
		return this;
	}

	@Override
	public Image getMutablePart() {
		return this;
	}

	@Override
	public Image duplicate() {
		throw new UnsupportedOperationException();
	}
	
}
