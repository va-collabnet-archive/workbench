package org.ihtsdo.db.bdb.concept.component.image;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.concept.component.Part;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ImagePart extends Part<ImagePart> 
		implements I_ImagePart<ImagePart>{

	private String textDescription;
	private int typeNid;
	
	public ArrayIntList getPartComponentNids() {
		ArrayIntList partComponentNids = new ArrayIntList(3);
		partComponentNids.add(getPathId());
		partComponentNids.add(getStatusId());
		partComponentNids.add(typeNid);
		return partComponentNids;
	}

	protected ImagePart(TupleInput input) {
		super(input.readInt());
		this.textDescription = input.readString();
		this.typeNid = input.readInt();
	}

	private ImagePart(ImagePart another) {
		super(another.statusAtPositionNid);
		this.textDescription = another.textDescription;
		this.typeNid = another.typeNid;
	}

	private ImagePart(ImagePart another, int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
		this.textDescription = another.textDescription;
		this.typeNid = another.typeNid;
	}

	@Override
	public ImagePart makeAnalog(int statusNid, int pathNid, long time) {
		return new ImagePart(this, statusNid, pathNid, time);
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		output.writeString(textDescription);
		output.writeInt(typeNid);
	}

	private ImagePart(int statusAtPositionNid) {
		super(statusAtPositionNid);
		// TODO Auto-generated constructor stub
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
	public boolean hasNewData(ImagePart another) {
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
		ImagePart another = (ImagePart) obj;
		return ((getPathId() == another.getPathId()) &&
				(getStatusId() == another.getStatusId()) && 
				(textDescription.equals(another.textDescription)) &&
				(typeNid == another.typeNid) &&
				(getTime() == another.getTime()));
	}
	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] {getPathId(), getStatusId(), 
				textDescription.hashCode(), typeNid});
	}
	
	public ImagePart duplicate() {
		return new ImagePart(this);
	}
	
}
