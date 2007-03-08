package org.dwfa.vodb.types;

public class ThinIdPart {
	private int pathId;
	private int version;
	private int idStatus;
	private int source;
	private Object sourceId;
	
	public int getPathId() {
		return pathId;
	}
	public void setPathId(int pathId) {
		this.pathId = pathId;
	}
	public int getIdStatus() {
		return idStatus;
	}
	public void setIdStatus(int idStatus) {
		this.idStatus = idStatus;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public Object getSourceId() {
		return sourceId;
	}
	public void setSourceId(Object sourceId) {
		this.sourceId = sourceId;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public boolean hasNewData(ThinIdPart another) {
		return ((this.pathId != another.pathId) ||
				(this.idStatus != another.idStatus) ||
				(this.source != another.source) ||
				sourceId.equals(another.sourceId) == false);
	}
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Source: ");
		buf.append(source);
		buf.append(" SourceId: ");
		buf.append(sourceId);
		buf.append(" StatusId: ");
		buf.append(idStatus);
		buf.append(" pathId: ");
		buf.append(pathId);
		buf.append(" version: ");
		buf.append(version);
		return buf.toString();
	}
	@Override
	public boolean equals(Object obj) {
		ThinIdPart another = (ThinIdPart) obj;
		return ((pathId == another.pathId) &&
				(version == another.version) &&
				(idStatus == another.idStatus) &&
				(source == another.source) &&
				(sourceId.equals(another.sourceId)));
	}
	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] {pathId, version, idStatus, source, sourceId.hashCode()});
	}

}
