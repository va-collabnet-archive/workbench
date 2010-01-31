package org.ihtsdo.db.bdb.concept.component.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.concept.component.description.Description.Version;
import org.ihtsdo.db.util.VersionComputer;
import org.ihtsdo.etypes.EImage;
import org.ihtsdo.etypes.EImageVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Image 
	extends ConceptComponent<ImageRevision, Image> 
	implements I_ImageVersioned, I_ImagePart {
	
	public class Version 
	extends ConceptComponent<ImageRevision, Image>.Version 
	implements I_ImageTuple, I_ImagePart {

		public Version() {
			super();
		}

		public Version(int index) {
			super(index);
		}

		@Override
		public int getConceptId() {
			return enclosingConcept.getConceptId();
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
		public String getTextDescription() {
			if (index >= 0) {
				return revisions.get(index).getTextDescription();
			}
			return textDescription;
		}

		@Override
		public I_ImageVersioned getVersioned() {
			return Image.this;
		}

		@Override
		public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getTypeId() {
			if (index >= 0) {
				return revisions.get(index).getTypeId();
			}
			return typeNid;
		}

		@Override
		public void setTypeId(int type) {
			if (index >= 0) {
				revisions.get(index).setTypeId(type);
			}
			typeNid = type;
		}

		public ArrayIntList getVariableVersionNids() {
			if (index >= 0) {
				ArrayIntList resultList = new ArrayIntList(3);
				resultList.add(getTypeId());
				return resultList;
			}
			return Image.this.getVariableVersionNids();
		}

		@Override
		public I_ImagePart makeAnalog(int statusNid, int pathNid, long time) {
			if (index >= 0) {
				return revisions.get(index).makeAnalog(statusNid, pathNid, time);
			}
			return Image.this.makeAnalog(statusNid, pathNid, time);
		}
		@Override
		public I_ImagePart getMutablePart() {
			return (I_ImagePart) super.getMutablePart();
		}
		
		@Override
		@Deprecated
		public I_ImagePart duplicate() {
			throw new UnsupportedOperationException("Use makeAnalog instead");
		}

		@Override
		public void setTextDescription(String name) {
			if (index >= 0) {
				revisions.get(index).setTextDescription(name);
			}
			textDescription = name;
		}

	}

	private static VersionComputer<Image.Version> computer = 
		new VersionComputer<Image.Version>();

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
			revisions = new ArrayList<ImageRevision>(eImage.getExtraVersionsList().size());
			for (EImageVersion eiv: eImage.getExtraVersionsList()) {
				revisions.add(new ImageRevision(eiv, this));
			}
		}
	}

	public Image() {
		super();
	}

	/**
	 * Returns a string representation of the object.
	 */
	@Override
	public String toString() {
	    StringBuffer buf = new StringBuffer();  
	    buf.append(this.getClass().getSimpleName() + ": ");
	    buf.append(" computer:" + Image.computer);
	    buf.append(" format:" + "'" + this.format + "'");
	    buf.append(" image:" + this.image);
	    buf.append(" textDescription:" + "'" + this.textDescription + "'");
	    buf.append(" typeNid:");
        ConceptComponent.addNidToBuffer(buf, typeNid);
	    buf.append("; ");
	    buf.append(super.toString());
	    return buf.toString();
	}


    //TODO Verify this is a correct implementation 
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (Image.class.isAssignableFrom(obj.getClass())) {
            Image another = (Image) obj;
            if (this.nid == another.nid) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] {this.getNid() });
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
			revisions.add(new ImageRevision(input, this));
		}
	}

	@Override
	public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		List<ImageRevision> partsToWrite = new ArrayList<ImageRevision>();
		if (revisions != null) {
			for (ImageRevision p: revisions) {
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
		for (ImageRevision p: partsToWrite) {
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
	public Version getLastTuple() {
		List<Version> vList = getTuples();
		return vList.get(vList.size() - 1);
	}

	List<Version> versions;
	public List<Version> getTuples() {
		return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
	}

	private List<Version> getVersions() {
		if (versions == null) {
			int count = 1;
			if (revisions != null) {
				count = count + revisions.size();
			}
			ArrayList<Version> list = new ArrayList<Version>(count);
			list.add(new Version());
			if (revisions != null) {
				for (int i = 0; i < revisions.size(); i++) {
					list.add(new Version(i));
				}
			}
			versions = list;
		}
		return versions;
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
				getImage(), new ArrayList<UniversalAceImagePart>(revisions
						.size()), getFormat(), enclosingConcept.getUids());
		for (ImageRevision part : revisions) {
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
		List<Version> matchingTuples = new ArrayList<Version>();
		computer.addTuples(allowedStatus, viewPosition, matchingTuples, 
				getTuples());
		boolean promotedAnything = false;
		for (I_Path promotionPath : pomotionPaths) {
			for (Version it : matchingTuples) {
				if (it.getPathId() == viewPathId) {
					ImageRevision promotionPart = (ImageRevision) it.makeAnalog(
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
		this.versions = null;
		return revisions.add((ImageRevision) part);
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
	public ImageRevision makeAnalog(int statusNid, int pathNid, long time) {
		return new ImageRevision(this, statusNid, pathNid, time, this);
	}

	@Override
	public Image getMutablePart() {
		return this;
	}

	@Override
	public Image duplicate() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<? extends I_ImagePart> getMutableParts() {
			return getTuples();
	}

}
