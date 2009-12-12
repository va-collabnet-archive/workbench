package org.ihtsdo.db.bdb.concept.component.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.utypes.UniversalAceImage;
import org.dwfa.ace.utypes.UniversalAceImagePart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.util.TupleComputer;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Image extends ConceptComponent<ImagePart> implements
		I_ImageVersioned<ImagePart, Image, ImageTuple> {

	private static class ImageTupleComputer extends
			TupleComputer<ImageTuple, Image, ImagePart> {

		public ImageTuple makeTuple(ImagePart part, Image core) {
			return new ImageTuple(core, part);
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
	}

	@Override
	public void writeComponentToBdb(TupleOutput output) {
		output.writeString(format);
		output.writeInt(image.length);
		output.write(image);
	}

	@Override
	public void readPartFromBdb(TupleInput input) {
		versions.add(new ImagePart(input));
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
	public ImageTuple getLastTuple() {
		return new ImageTuple(this, versions.get(versions.size() - 1));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getTuples()
	 */
	public List<ImageTuple> getTuples() {
		List<ImageTuple> tuples = new ArrayList<ImageTuple>();
		for (ImagePart p : getVersions()) {
			tuples.add(new ImageTuple(this, p));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.dwfa.vodb.types.I_ImageVersioned#merge(org.dwfa.vodb.types.
	 * ThinImageVersioned)
	 */
	public boolean merge(Image jarImage) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getTimePathSet()
	 */
	public Set<TimePathId> getTimePathSet() {
		Set<TimePathId> tpSet = new HashSet<TimePathId>();
		for (ImagePart p : versions) {
			tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return tpSet;
	}

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<ImageTuple> matchingTuples) {
		computer.addTuples(allowedStatus, allowedTypes, positions, 
				matchingTuples, true, versions, this);
	}

	private static Collection<UUID> getUids(int id) throws IOException,
			TerminologyException {
		return LocalFixedTerminology.getStore().getUids(id);
	}

	public UniversalAceImage getUniversal() throws IOException,
			TerminologyException {
		UniversalAceImage universal = new UniversalAceImage(getUids(nid),
				getImage(), new ArrayList<UniversalAceImagePart>(versions
						.size()), getFormat(), getUids(conceptId));
		for (ImagePart part : versions) {
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
		List<ImageTuple> matchingTuples = new ArrayList<ImageTuple>();
		computer.addTuples(allowedStatus, viewPosition, matchingTuples, 
				versions, this);
		boolean promotedAnything = false;
		for (I_Path promotionPath : pomotionPaths) {
			for (ImageTuple it : matchingTuples) {
				if (it.getPathId() == viewPathId) {
					ImagePart promotionPart = it.getPart().makeAnalog(
							it.getStatusId(), promotionPath.getConceptId(),
							Long.MAX_VALUE);
					it.getVersioned().addVersion(promotionPart);
					promotedAnything = true;
				}
			}
		}
		return promotedAnything;
	}
}
