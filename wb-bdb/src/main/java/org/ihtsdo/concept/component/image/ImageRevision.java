package org.ihtsdo.concept.component.image;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.dto.concept.component.media.TkMediaRevision;

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

	ImageRevision(ImageRevision another, 
			Image primoridalMember) {
		super(another.sapNid, 
				primoridalMember);
		this.textDescription = another.textDescription;
		this.typeNid = another.typeNid;
	}
    ImageRevision(Image primoridalMember) {
    super(primoridalMember.primordialSapNid, 
            primoridalMember);
    this.textDescription = primoridalMember.getTextDescription();
    this.typeNid = primoridalMember.getTypeNid();
}

	protected ImageRevision(I_ImagePart another, int statusNid, 
			int authorNid,
			int pathNid, long time, 
			Image primoridalMember) {
		super(statusNid, 
				authorNid, 
				pathNid, time, primoridalMember);
		this.textDescription = another.getTextDescription();
		this.typeNid = another.getTypeNid();
	}

	protected ImageRevision() {
        super();
    }
    
    @Override
	public ImageRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
			return new ImageRevision(this, statusNid, 
					Terms.get().getAuthorNid(), 
					pathNid, time, this.primordialComponent);
	}

    @Override
	public ImageRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            this.setAuthorNid(authorNid);
            return this;
        }
			return new ImageRevision(this, statusNid, 
					authorNid, 
					pathNid, time, 
					this.primordialComponent);
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		output.writeString(textDescription);
		output.writeInt(typeNid);
	}


	public ImageRevision(TkMediaRevision eiv, 
			Image primoridalMember) {
		super(Bdb.uuidToNid(eiv.getStatusUuid()), 
				  Bdb.uuidToNid(eiv.getAuthorUuid()), 
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
	@Deprecated
	public int getTypeId() {
		return typeNid;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#setTypeId(int)
	 */
	@Deprecated
	public void setTypeId(int type) {
		this.typeNid = type;
        modified();
	}
	public int getTypeNid() {
		return typeNid;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImagePart#setTypeId(int)
	 */
	public void setTypeNid(int type) {
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
