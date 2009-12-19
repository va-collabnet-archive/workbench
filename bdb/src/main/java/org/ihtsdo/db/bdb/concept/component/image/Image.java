package org.ihtsdo.db.bdb.concept.component.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ImagePart;
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
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.util.TupleComputer;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Image extends ConceptComponent<ImageVariablePart> implements
		I_ImageVersioned<ImageVariablePart, ImageVersion> {

	private static class ImageTupleComputer extends
			TupleComputer<ImageVersion, Image, ImageVariablePart> {

		public ImageVersion makeTuple(ImageVariablePart part, Image core) {
			return new ImageVersion(core, part);
		}
	}

	private static ImageTupleComputer computer = new ImageTupleComputer();
	private String format;
	private byte[] image;
	private int conceptId;

	protected Image(int nid, int listSize, boolean editable) {
		super(nid, listSize, editable);
	}

	@Override
	public void readComponentFromBdb(TupleInput input, int conceptNid) {
		this.conceptId = conceptNid;
		this.format = input.readString();
		int imageBytes = input.readInt();
		image = new byte[imageBytes];
		input.read(image, 0, imageBytes);
		int partsToRead = input.readShort();
		for (int i = 0; i < partsToRead; i++) {
			variableParts.add(new ImageVariablePart(input));
		}
	}

	@Override
	public void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		output.writeString(format);
		output.writeInt(image.length);
		output.write(image);
		List<ImageVariablePart> partsToWrite = new ArrayList<ImageVariablePart>();
		for (ImageVariablePart p: variableParts) {
			if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
				partsToWrite.add(p);
			}
		}
		output.writeShort(partsToWrite.size());
		for (ImageVariablePart p: partsToWrite) {
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
		return conceptId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getLastTuple()
	 */
	public ImageVersion getLastTuple() {
		return new ImageVersion(this, variableParts.get(variableParts.size() - 1));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getTuples()
	 */
	public List<ImageVersion> getTuples() {
		List<ImageVersion> tuples = new ArrayList<ImageVersion>();
		for (ImageVariablePart p : getVersions()) {
			tuples.add(new ImageVersion(this, p));
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
			PositionSetReadOnly positions, List<ImageVersion> matchingTuples) {
		computer.addTuples(allowedStatus, allowedTypes, positions, 
				matchingTuples, true, variableParts, this);
	}

	private static Collection<UUID> getUids(int id) throws IOException,
			TerminologyException {
		return LocalFixedTerminology.getStore().getUids(id);
	}

	public UniversalAceImage getUniversal() throws IOException,
			TerminologyException {
		UniversalAceImage universal = new UniversalAceImage(getUids(nid),
				getImage(), new ArrayList<UniversalAceImagePart>(variableParts
						.size()), getFormat(), getUids(conceptId));
		for (ImageVariablePart part : variableParts) {
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
				variableParts, this);
		boolean promotedAnything = false;
		for (I_Path promotionPath : pomotionPaths) {
			for (ImageVersion it : matchingTuples) {
				if (it.getPathId() == viewPathId) {
					ImageVariablePart promotionPart = it.getPart().makeAnalog(
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
		return variableParts.add((ImageVariablePart) part);
	}

	@Override
	public boolean merge(I_ImageVersioned<ImageVariablePart, ImageVersion> jarImage) {
		throw new UnsupportedOperationException();
	}
}
