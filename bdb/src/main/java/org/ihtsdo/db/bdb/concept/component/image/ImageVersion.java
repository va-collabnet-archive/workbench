package org.ihtsdo.db.bdb.concept.component.image;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.Version;
import org.ihtsdo.etypes.EImageVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ImageVersion extends Version<ImageVersion, Image> 
		implements I_ImagePart, I_ImageTuple {

	private transient Image image;
	private String textDescription;
	private int typeNid;
	
	protected ArrayIntList getVariableVersionNids() {
		ArrayIntList partComponentNids = new ArrayIntList(3);
		partComponentNids.add(typeNid);
		return partComponentNids;
	}

	protected ImageVersion(TupleInput input) {
		super(input.readInt());
		this.textDescription = input.readString();
		this.typeNid = input.readInt();
	}

	private ImageVersion(ImageVersion another) {
		super(another.statusAtPositionNid);
		this.textDescription = another.textDescription;
		this.typeNid = another.typeNid;
	}

	protected ImageVersion(I_ImagePart another, int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
		this.textDescription = another.getTextDescription();
		this.typeNid = another.getTypeId();
	}

	@Override
	public ImageVersion makeAnalog(int statusNid, int pathNid, long time) {
		return new ImageVersion(this, statusNid, pathNid, time);
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		output.writeString(textDescription);
		output.writeInt(typeNid);
	}

	private ImageVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
		// TODO Auto-generated constructor stub
	}


	public ImageVersion(EImageVersion eiv) {
		super(Bdb.uuidToNid(eiv.getStatusUuid()), 
			  Bdb.uuidToNid(eiv.getPathUuid()), 
			  eiv.getTime());
		this.textDescription = eiv.getTextDescription();
		this.typeNid = Bdb.uuidToNid(eiv.getTypeUuid());
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#getTextDescription()
	 */
	public String getTextDescription() {
		return textDescription;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#setTextDescription(java.lang.String)
	 */
	public void setTextDescription(String name) {
		this.textDescription = name;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#getTypeId()
	 */
	public int getTypeId() {
		return typeNid;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#setTypeId(int)
	 */
	public void setTypeId(int type) {
		this.typeNid = type;
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#hasNewData(org.dwfa.vodb.types.ThinImagePart)
	 */
	public boolean hasNewData(ImageVersion another) {
		return ((this.getPathId() != another.getPathId()) ||
				(this.getStatusId() != another.getStatusId()) ||
				((this.textDescription.equals(another.getTextDescription()) == false) ||
				(this.typeNid != another.getTypeId())));
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#convertIds(org.dwfa.vodb.jar.I_MapNativeToNative)
	 */
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean equals(Object obj) {
		ImageVersion another = (ImageVersion) obj;
		return ((getPathId() == another.getPathId()) &&
				(getStatusId() == another.getStatusId()) && 
				(textDescription.equals(another.textDescription)) &&
				(typeNid == another.typeNid) &&
				(getTime() == another.getTime()));
	}
	
	public ImageVersion duplicate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getConceptId() {
		return image.getConceptId();
	}

	@Override
	public String getFormat() {
		return image.getFormat();
	}

	@Override
	public byte[] getImage() {
		return image.getImage();
	}

	@Override
	public int getImageId() {
		return image.nid;
	}

	@Override
	public I_ImagePart getMutablePart() {
		return this;
	}
	
}
