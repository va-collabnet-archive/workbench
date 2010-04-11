package org.ihtsdo.concept.component.image;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.EImageRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ImageRevision extends Revision<ImageRevision, Image> 
		implements I_ImagePart {

	private String textDescription;
	private int typeNid;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    StringBuffer buf = new StringBuffer();  
	    buf.append(this.getClass().getSimpleName() + ":{");
	    buf.append(" textDescription:" + "'" + this.textDescription + "'");
	    buf.append(" typeNid:" + this.typeNid);
	    buf.append(super.toString());
	    return buf.toString();
	}

	@Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
	    if (ImageRevision.class.isAssignableFrom(obj.getClass())) {
	        ImageRevision another = (ImageRevision) obj;
	        if (this.sapNid == another.sapNid) {
	            return true;
	        }
	    }
	    return false;
	}


	public ArrayIntList getVariableVersionNids() {
		ArrayIntList partComponentNids = new ArrayIntList(3);
		partComponentNids.add(typeNid);
		return partComponentNids;
	}

	protected ImageRevision(TupleInput input, 
			Image primoridalMember) {
		super(input.readInt(), primoridalMember);
		this.textDescription = input.readString();
		this.typeNid = input.readInt();
	}

	private ImageRevision(ImageRevision another, 
			Image primoridalMember) {
		super(another.sapNid, 
				primoridalMember);
		this.textDescription = another.textDescription;
		this.typeNid = another.typeNid;
	}

	protected ImageRevision(I_ImagePart another, int statusNid, 
			int pathNid, long time, 
			Image primoridalMember) {
		super(statusNid, pathNid, time, primoridalMember);
		this.textDescription = another.getTextDescription();
		this.typeNid = another.getTypeId();
	}

	protected ImageRevision() {
        super();
    }
    
    @Override
	public ImageRevision makeAnalog(int statusNid, int pathNid, long time) {
		return new ImageRevision(this, statusNid, pathNid, time, this.primordialComponent);
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		output.writeString(textDescription);
		output.writeInt(typeNid);
	}

	private ImageRevision(int statusAtPositionNid, 
			Image primoridalMember) {
		super(statusAtPositionNid, primoridalMember);
	}


	public ImageRevision(EImageRevision eiv, 
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
        modified();
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
        modified();
	}
	
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#hasNewData(org.dwfa.vodb.types.ThinImagePart)
	 */
	public boolean hasNewData(ImageRevision another) {
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
	
	public ImageRevision duplicate() {
		throw new UnsupportedOperationException();
	}
}
