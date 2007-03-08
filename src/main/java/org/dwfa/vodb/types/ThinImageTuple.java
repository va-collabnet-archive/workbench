package org.dwfa.vodb.types;


public class ThinImageTuple {
	private ThinImageVersioned core ;
	private ThinImagePart part;
	public ThinImageTuple(ThinImageVersioned core, ThinImagePart part) {
		super();
		this.core = core;
		this.part = part;
	}
	public byte[] getImage() {
		return core.getImage();
	}
	public int getImageId() {
		return core.getImageId();
	}
	public int getPathId() {
		return part.getPathId();
	}
	public int getStatusId() {
		return part.getStatusId();
	}
	public int getVersion() {
		return part.getVersion();
	}
	public String getTextDescription() {
		return part.getTextDescription();
	}
	public int getTypeId() {
		return part.getTypeId();
	}
	public String getFormat() {
		return core.getFormat();
	}
	public int getConceptId() {
		return core.getConceptId();
	}

}
