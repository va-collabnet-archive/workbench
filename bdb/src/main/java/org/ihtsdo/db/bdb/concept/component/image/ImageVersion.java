package org.ihtsdo.db.bdb.concept.component.image;

import java.util.Arrays;

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
	
	public String toString() {
		return " textDescription: " + textDescription + " typeNid: " + typeNid + " " + super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (ImageVersion.class.isAssignableFrom(obj.getClass())) {
			ImageVersion another = (ImageVersion) obj;
			if (this.typeNid != another.typeNid) {
				return false;
			}
			if (!this.textDescription.equals(another.textDescription)) {
				return false;
			}
			if (!Arrays.equals(this.getImage(), another.getImage())) {
				return false;
			}
			return super.equals(obj);
		}
		return false;
	}

	protected ArrayIntList getVariableVersionNids() {
		ArrayIntList partComponentNids = new ArrayIntList(3);
		partComponentNids.add(typeNid);
		return partComponentNids;
	}

	protected ImageVersion(TupleInput input, 
			Image primoridalMember) {
		super(input.readInt(), primoridalMember);
		this.textDescription = input.readString();
		this.typeNid = input.readInt();
	}

	private ImageVersion(ImageVersion another, 
			Image primoridalMember) {
		super(another.statusAtPositionNid, 
				primoridalMember);
		this.textDescription = another.textDescription;
		this.typeNid = another.typeNid;
	}

	protected ImageVersion(I_ImagePart another, int statusNid, 
			int pathNid, long time, 
			Image primoridalMember) {
		super(statusNid, pathNid, time, primoridalMember);
		this.textDescription = another.getTextDescription();
		this.typeNid = another.getTypeId();
	}

	@Override
	public ImageVersion makeAnalog(int statusNid, int pathNid, long time) {
		return new ImageVersion(this, statusNid, pathNid, time, this.primordialComponent);
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		output.writeString(textDescription);
		output.writeInt(typeNid);
	}

	private ImageVersion(int statusAtPositionNid, 
			Image primoridalMember) {
		super(statusAtPositionNid, primoridalMember);
	}


	public ImageVersion(EImageVersion eiv, 
			Image primoridalMember) {
		super(Bdb.uuidToNid(eiv.getStatusUuid()), 
			  Bdb.uuidToNid(eiv.getPathUuid()), 
			  eiv.getTime(), primoridalMember);
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
