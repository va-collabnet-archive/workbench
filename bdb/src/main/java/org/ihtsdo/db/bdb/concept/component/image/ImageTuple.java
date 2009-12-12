package org.ihtsdo.db.bdb.concept.component.image;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.ihtsdo.db.bdb.concept.component.Tuple;

public class ImageTuple 
	extends Tuple<ImagePart, Image> 
	implements I_ImageTuple<ImagePart, ImageTuple, Image> {

	private Image fixed;
	private ImagePart part;
	
	protected ImageTuple(Image fixed, ImagePart part) {
		super();
		this.fixed = fixed;
		this.part = part;
	}

	public ArrayIntList getPartComponentNids() {
		return part.getPartComponentNids();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageTuple#getImage()
	 */
	public byte[] getImage() {
		return fixed.getImage();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageTuple#getImageId()
	 */
	public int getImageId() {
		return fixed.getImageId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageTuple#getPathId()
	 */
	public int getPathId() {
		return part.getPathId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageTuple#getStatusId()
	 */
	public int getStatusId() {
		return part.getStatusId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageTuple#getVersion()
	 */
	public int getVersion() {
		return part.getVersion();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageTuple#getTextDescription()
	 */
	public String getTextDescription() {
		return part.getTextDescription();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageTuple#getTypeId()
	 */
	public int getTypeId() {
		return part.getTypeId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageTuple#getFormat()
	 */
	public String getFormat() {
		return fixed.getFormat();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageTuple#getConceptId()
	 */
	public int getConceptId() {
		return fixed.getConceptId();
	}
	
	public Image getVersioned() {
		return fixed;
	}
	public ImagePart getPart() {
		return part;
	}
	public void setPathId(int pathId) {
		throw new UnsupportedOperationException();
	}
	public void setStatusId(int idStatus) {
		throw new UnsupportedOperationException();
	}
	public void setVersion(int version) {
		throw new UnsupportedOperationException();
	}
	public Image getFixedPart() {
		return fixed;
	}

	public ImagePart duplicate() {
		return duplicate();
	}
	
	public void setTypeId(int type) {
		part.setTypeId(type);
	}
	
	public int getFixedPartId() {
		return fixed.getNid();
	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getStatusAtPositionNid() {
		return part.statusAtPositionNid;
	}

	@Override
	public long getTime() {
		return part.getTime();
	}

	@Override
	public ImagePart makeAnalog(int statusNid, int pathNid, long time) {
		return part.makeAnalog(statusNid, pathNid, time);
	}
	

}
