package org.dwfa.vodb.types;

import org.dwfa.ace.api.I_IdPart;

public class ThinIdIntegerPartWithCoreDelegate implements I_IdPart {
	private ThinIdPartCore core;
	private int sourceId;
	
	public ThinIdIntegerPartWithCoreDelegate() {
		super();
	}
	public ThinIdIntegerPartWithCoreDelegate(int sourceId, ThinIdPartCore core) {
		super();
		this.core = core;
		this.sourceId = sourceId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#getPathId()
	 */
	public int getPathId() {
		return core.getPathId();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#setPathId(int)
	 */
	public void setPathId(int pathId) {
		throw new UnsupportedOperationException("Create a duplicate, then set values on the duplicate. ");
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#getIdStatus()
	 */
	public int getIdStatus() {
		return core.getIdStatus();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#setIdStatus(int)
	 */
	public void setIdStatus(int idStatus) {
		throw new UnsupportedOperationException("Create a duplicate, then set values on the duplicate. ");
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#getSource()
	 */
	public int getSource() {
		return core.getSource();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#setSource(int)
	 */
	public void setSource(int source) {
		throw new UnsupportedOperationException("Create a duplicate, then set values on the duplicate. ");
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#getSourceId()
	 */
	public Object getSourceId() {
		return sourceId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#setSourceId(java.lang.Object)
	 */
	public void setSourceId(Object sourceId) {
		this.sourceId = (Integer) sourceId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#getVersion()
	 */
	public int getVersion() {
		return core.getVersion();
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#setVersion(int)
	 */
	public void setVersion(int version) {
		throw new UnsupportedOperationException("Create a duplicate, then set values on the duplicate. ");
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_IdPart#hasNewData(org.dwfa.vodb.types.ThinIdPart)
	 */
	public boolean hasNewData(I_IdPart another) {
		return ((this.getPathId() != another.getPathId()) ||
				(this.getIdStatus() != another.getIdStatus()) ||
				(this.getSource() != another.getSource()) ||
				getSourceId().equals(another.getSourceId()) == false);
	}
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Source: ");
		buf.append(getSource());
		buf.append(" SourceId: ");
		buf.append(getSourceId().toString());
		buf.append(" StatusId: ");
		buf.append(getIdStatus());
		buf.append(" pathId: ");
		buf.append(getPathId());
		buf.append(" version: ");
		buf.append(getVersion());
		return buf.toString();
	}
	@Override
	public boolean equals(Object obj) {
		I_IdPart another = (I_IdPart) obj;
		return ((getPathId() == another.getPathId()) &&
				(getVersion() == another.getVersion()) &&
				(getIdStatus() == another.getIdStatus()) &&
				(getSource() == another.getSource()) &&
				(getSourceId().equals(another.getSourceId())));
	}
	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] {core.hashCode(), getSourceId().hashCode()});
	}
	public I_IdPart duplicate() {
		return new ThinIdPart(this);
	}
}
