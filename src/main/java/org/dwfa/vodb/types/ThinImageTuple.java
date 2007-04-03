package org.dwfa.vodb.types;

import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;


public class ThinImageTuple implements I_ImageTuple {
	private I_ImageVersioned core ;
	private I_ImagePart part;
	public ThinImageTuple(I_ImageVersioned core, I_ImagePart part) {
		super();
		this.core = core;
		this.part = part;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageTuple#getImage()
	 */
	public byte[] getImage() {
		return core.getImage();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageTuple#getImageId()
	 */
	public int getImageId() {
		return core.getImageId();
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
		return core.getFormat();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageTuple#getConceptId()
	 */
	public int getConceptId() {
		return core.getConceptId();
	}

}
