package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UniversalAceImage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Collection<UUID> imageId;
	private String format;
	private byte[] image;
	private Collection<UUID> conceptId;
	private List<UniversalAceImagePart> versions;
	
	public UniversalAceImage() {
		super();
	}
	
	public UniversalAceImage(Collection<UUID> nativeId, byte[] image, List<UniversalAceImagePart> versions, String format, 
			Collection<UUID> conceptId) {
		super();
		this.imageId = nativeId;
		this.image = image;
		this.versions = versions;
		this.format = format;
		this.conceptId = conceptId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getImage()
	 */
	public byte[] getImage() {
		return image;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getImageId()
	 */
	public Collection<UUID> getImageId() {
		return imageId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getVersions()
	 */
	public List<UniversalAceImagePart> getVersions() {
		return versions;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#addVersion(org.dwfa.vodb.types.ThinImagePart)
	 */
	public boolean addVersion(UniversalAceImagePart part) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(part);
		} else if (index >= 0) {
			return versions.add(part);
			
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getFormat()
	 */
	public String getFormat() {
		return format;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getConceptId()
	 */
	public Collection<UUID> getConceptId() {
		return conceptId;
	}

}
