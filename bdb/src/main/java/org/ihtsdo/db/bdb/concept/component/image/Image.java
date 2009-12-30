package org.ihtsdo.db.bdb.concept.component.image;

import java.io.IOException;
import java.util.ArrayList;
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
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.util.VersionComputer;

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
	private int conceptNid;
	
	private String textDescription;
	private int typeNid;

	protected Image(int nid, int listSize, boolean editable) {
		super(nid, listSize, editable);
	}

	public Image(UniversalAceImage uImage, boolean editable) {
		super(Bdb.uuidsToNid(uImage.getImageId()), uImage.getVersions().size(), editable);
		conceptNid = Bdb.uuidsToNid(uImage.getConceptId());
		image = uImage.getImage();
		format = uImage.getFormat();
		for (UniversalAceImagePart uPart: uImage.getVersions()) {
			componentVersion.add(new ImageVersion(uPart));
		}
	}

	@Override
	public void readComponentFromBdb(TupleInput input, int conceptNid, int listSize) {
		this.conceptNid = conceptNid;
		
		// nid, list size, and conceptNid are read already by the binder...
		this.format = input.readString();
		int imageBytes = input.readInt();
		image = new byte[imageBytes];
		input.read(image, 0, imageBytes);
		for (int i = 0; i < listSize; i++) {
			componentVersion.add(new ImageVersion(input));
		}
	}

	@Override
	public void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		List<ImageVersion> partsToWrite = new ArrayList<ImageVersion>();
		for (ImageVersion p: componentVersion) {
			if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
				partsToWrite.add(p);
			}
		}
		// Start writing
		output.writeInt(nid);
		output.writeShort(partsToWrite.size());
		// conceptNid is the enclosing concept, does not need to be written. 
		output.writeString(format);
		output.writeInt(image.length);
		output.write(image);
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
		return conceptNid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getLastTuple()
	 */
	public ImageVersion getLastTuple() {
		return componentVersion.get(componentVersion.size() - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getTuples()
	 */
	public List<I_ImageTuple> getTuples() {
		List<I_ImageTuple> tuples = new ArrayList<I_ImageTuple>();
		for (ImageVersion p : componentVersion) {
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
				getImage(), new ArrayList<UniversalAceImagePart>(componentVersion
						.size()), getFormat(), getUids(conceptNid));
		for (ImageVersion part : componentVersion) {
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
				componentVersion, this);
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
		return componentVersion.add((ImageVersion) part);
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
		return new ImageVersion(this, statusNid, pathNid, time);
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
