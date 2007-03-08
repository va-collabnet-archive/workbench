package org.dwfa.vodb.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.IntSet;
import org.dwfa.vodb.jar.I_MapNativeToNative;

public class ThinImageVersioned {
	public ThinImageVersioned() {
		super();
	}
	private int imageId;
	private String format;
	private byte[] image;
	private int conceptId;
	private List<ThinImagePart> versions;
	public ThinImageVersioned(int nativeId, byte[] image, List<ThinImagePart> versions, String format, 
			int conceptId) {
		super();
		this.imageId = nativeId;
		this.image = image;
		this.versions = versions;
		this.format = format;
		this.conceptId = conceptId;
	}
	public byte[] getImage() {
		return image;
	}
	public int getImageId() {
		return imageId;
	}
	public List<ThinImagePart> getVersions() {
		return versions;
	}

	public boolean addVersion(ThinImagePart part) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(part);
		} else if (index >= 0) {
			ThinImagePart prevPart = versions.get(index);
			if (prevPart.hasNewData(part)) {
				if (prevPart.getTextDescription().equals(part.getTextDescription())) {
					part.setTextDescription(prevPart.getTextDescription());
				}
				return versions.add(part);
			}
		}
		return false;
	}
	public String getFormat() {
		return format;
	}
	public int getConceptId() {
		return conceptId;
	}
	public ThinImageTuple getLastTuple() {
		return new ThinImageTuple(this, versions.get(versions.size() -1));
	}
	public Collection<ThinImageTuple> getTuples() {
		List<ThinImageTuple> tuples = new ArrayList<ThinImageTuple>();
		for (ThinImagePart p: getVersions()) {
			tuples.add(new ThinImageTuple(this, p));
		}
		return tuples;
	}
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		conceptId = jarToDbNativeMap.get(conceptId);
		imageId = jarToDbNativeMap.get(imageId);
		for (ThinImagePart p: versions) {
			p.convertIds(jarToDbNativeMap);
		}
		
	}
	public boolean merge(ThinImageVersioned jarImage) {
		HashSet<ThinImagePart> versionSet = new HashSet<ThinImagePart>(versions);
		boolean changed = false;
		for (ThinImagePart jarPart: jarImage.versions) {
			if (!versionSet.contains(jarPart)) {
				changed = true;
				versions.add(jarPart);
			}
		}
		return changed;
	}
	public Set<TimePathId> getTimePathSet() {
		Set<TimePathId> tpSet = new HashSet<TimePathId>(); 
		for (ThinImagePart p: versions) {
			tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return tpSet;
	}

	public void addTuples(IntSet allowedStatus, IntSet allowedTypes,
			Set<Position> positions, List<ThinImageTuple> returnImages) {
		Set<ThinImagePart> uncommittedParts = new HashSet<ThinImagePart>();
		if (positions == null) {
			List<ThinImagePart> addedParts = new ArrayList<ThinImagePart>();
			Set<ThinImagePart> rejectedParts = new HashSet<ThinImagePart>();
			for (ThinImagePart part : versions) {
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
			for (ThinImagePart part : addedParts) {
				boolean addPart = true;
				for (ThinImagePart reject : rejectedParts) {
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

			Set<ThinImagePart> addedParts = new HashSet<ThinImagePart>();
			for (Position position : positions) {
				Set<ThinImagePart> rejectedParts = new HashSet<ThinImagePart>();
				ThinImageTuple possible = null;
				for (ThinImagePart part : versions) {
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
					for (ThinImagePart reject : rejectedParts) {
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
		for (ThinImagePart p: uncommittedParts) {
			returnImages.add(new ThinImageTuple(this, p));
		}
	}

}
