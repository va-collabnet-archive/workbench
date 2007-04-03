package org.dwfa.vodb.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.IntSet;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.vodb.jar.I_MapNativeToNative;

public class ThinImageVersioned implements I_ImageVersioned {
	public ThinImageVersioned() {
		super();
	}
	private int imageId;
	private String format;
	private byte[] image;
	private int conceptId;
	private List<I_ImagePart> versions;
	public ThinImageVersioned(int nativeId, byte[] image, List<I_ImagePart> versions, String format, 
			int conceptId) {
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
	public int getImageId() {
		return imageId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getVersions()
	 */
	public List<I_ImagePart> getVersions() {
		return versions;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#addVersion(org.dwfa.vodb.types.ThinImagePart)
	 */
	public boolean addVersion(ThinImagePart part) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(part);
		} else if (index >= 0) {
			I_ImagePart prevPart = versions.get(index);
			if (prevPart.hasNewData(part)) {
				if (prevPart.getTextDescription().equals(part.getTextDescription())) {
					part.setTextDescription(prevPart.getTextDescription());
				}
				return versions.add(part);
			}
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
	public int getConceptId() {
		return conceptId;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getLastTuple()
	 */
	public I_ImageTuple getLastTuple() {
		return new ThinImageTuple(this, versions.get(versions.size() -1));
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getTuples()
	 */
	public Collection<ThinImageTuple> getTuples() {
		List<ThinImageTuple> tuples = new ArrayList<ThinImageTuple>();
		for (I_ImagePart p: getVersions()) {
			tuples.add(new ThinImageTuple(this, p));
		}
		return tuples;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#convertIds(org.dwfa.vodb.jar.I_MapNativeToNative)
	 */
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		conceptId = jarToDbNativeMap.get(conceptId);
		imageId = jarToDbNativeMap.get(imageId);
		for (I_ImagePart p: versions) {
			p.convertIds(jarToDbNativeMap);
		}
		
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#merge(org.dwfa.vodb.types.ThinImageVersioned)
	 */
	public boolean merge(ThinImageVersioned jarImage) {
		HashSet<I_ImagePart> versionSet = new HashSet<I_ImagePart>(versions);
		boolean changed = false;
		for (I_ImagePart jarPart: jarImage.versions) {
			if (!versionSet.contains(jarPart)) {
				changed = true;
				versions.add(jarPart);
			}
		}
		return changed;
	}
	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getTimePathSet()
	 */
	public Set<TimePathId> getTimePathSet() {
		Set<TimePathId> tpSet = new HashSet<TimePathId>(); 
		for (I_ImagePart p: versions) {
			tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return tpSet;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_ImageVersioned#addTuples(org.dwfa.ace.IntSet, org.dwfa.ace.IntSet, java.util.Set, java.util.List)
	 */
	public void addTuples(IntSet allowedStatus, IntSet allowedTypes,
			Set<Position> positions, List<ThinImageTuple> returnImages) {
		Set<I_ImagePart> uncommittedParts = new HashSet<I_ImagePart>();
		if (positions == null) {
			List<I_ImagePart> addedParts = new ArrayList<I_ImagePart>();
			Set<I_ImagePart> rejectedParts = new HashSet<I_ImagePart>();
			for (I_ImagePart part : versions) {
				if (part.getVersion() == Integer.MAX_VALUE) {
					uncommittedParts.add(part);
				} else {
					if ((allowedStatus != null)
							&& (!allowedStatus.contains(part.getStatusId()))) {
						rejectedParts.add(part);
						continue;
					}
					if ((allowedTypes != null)
							&& (!allowedTypes.contains(part.getTypeId()))) {
						rejectedParts.add(part);
						continue;
					}
					addedParts.add(part);
				}
			}
			for (I_ImagePart part : addedParts) {
				boolean addPart = true;
				for (I_ImagePart reject : rejectedParts) {
					if ((part.getVersion() <= reject.getVersion())
							&& (part.getPathId() == reject.getPathId())) {
						addPart = false;
						continue;
					}
				}
				if (addPart) {
					returnImages.add(new ThinImageTuple(this, part));
				}
			}
		} else {

			Set<I_ImagePart> addedParts = new HashSet<I_ImagePart>();
			for (Position position : positions) {
				Set<I_ImagePart> rejectedParts = new HashSet<I_ImagePart>();
				ThinImageTuple possible = null;
				for (I_ImagePart part : versions) {
					if (part.getVersion() == Integer.MAX_VALUE) {
						uncommittedParts.add(part);
						continue;
					} else if ((allowedStatus != null)
							&& (!allowedStatus.contains(part.getStatusId()))) {
						if (possible != null) {
							Position rejectedStatusPosition = new Position(part
									.getVersion(), position.getPath()
									.getMatchingPath(part.getPathId()));
							Path possiblePath = position.getPath()
									.getMatchingPath(possible.getPathId());
							Position possibleStatusPosition = new Position(
									possible.getVersion(), possiblePath);
							if (position
									.isSubsequentOrEqualTo(rejectedStatusPosition)) {
								if (rejectedStatusPosition
										.isSubsequentOrEqualTo(possibleStatusPosition)) {
									possible = null;
								}
							}
						}
						rejectedParts.add(part);
						continue;
					}
					if ((allowedTypes != null)
							&& (!allowedTypes.contains(part.getTypeId()))) {
						rejectedParts.add(part);
						continue;
					}
					if (position.isSubsequentOrEqualTo(part.getVersion(), part
							.getPathId())) {
						if (possible == null) {
							if (!addedParts.contains(part)) {
								possible = new ThinImageTuple(this, part);
								addedParts.add(part);
							}
						} else {
							if (possible.getPathId() == part.getPathId()) {
								if (part.getVersion() > possible.getVersion()) {
									if (!addedParts.contains(part)) {
										possible = new ThinImageTuple(this, part);
										addedParts.add(part);
									}
								}
							} else {
								if (position.getDepth(part.getPathId()) < position
										.getDepth(possible.getPathId())) {
									if (!addedParts.contains(part)) {
										possible = new ThinImageTuple(this, part);
										addedParts.add(part);
									}
								}
							}
						}
					}

				}
				if (possible != null) {
					Path possiblePath = position.getPath().getMatchingPath(
							possible.getPathId());
					Position possibleStatusPosition = new Position(possible
							.getVersion(), possiblePath);
					boolean addPart = true;
					for (I_ImagePart reject : rejectedParts) {
						Position rejectedStatusPosition = new Position(reject
								.getVersion(), position.getPath()
								.getMatchingPath(reject.getPathId()));
						if ((rejectedStatusPosition
								.isSubsequentOrEqualTo(possibleStatusPosition))
								&& (position
										.isSubsequentOrEqualTo(rejectedStatusPosition))) {
							addPart = false;
							continue;
						}
					}
					if (addPart) {
						returnImages.add(possible);
					}
				}
			}
		}
		for (I_ImagePart p: uncommittedParts) {
			returnImages.add(new ThinImageTuple(this, p));
		}
	}

}
