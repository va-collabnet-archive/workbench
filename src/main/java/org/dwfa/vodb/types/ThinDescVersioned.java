package org.dwfa.vodb.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dwfa.ace.IntSet;
import org.dwfa.vodb.jar.I_MapNativeToNative;

public class ThinDescVersioned {
	private int descId;

	private int conceptId;

	private List<ThinDescPart> versions;

	public ThinDescVersioned(int descId, int conceptId, int count) {
		super();
		this.descId = descId;
		this.conceptId = conceptId;
		this.versions = new ArrayList<ThinDescPart>(count);
	}

	public boolean addVersion(ThinDescPart desc) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(desc);
		} else if (index >= 0) {
			ThinDescPart prevDesc = versions.get(index);
			if (prevDesc.hasNewData(desc)) {
				if (prevDesc.getText().equals(desc.getText())) {
					desc.setText(prevDesc.getText());
				}
				if (prevDesc.getLang().equals(desc.getLang())) {
					desc.setLang(prevDesc.getLang());
				}
				return versions.add(desc);
			}
		}
		return false;
	}

	public List<ThinDescPart> getVersions() {
		return versions;
	}

	public int versionCount() {
		return versions.size();
	}

	public boolean matches(Pattern p) {
		String lastText = null;
		for (ThinDescPart desc : versions) {
			if (desc.getText() != lastText) {
				lastText = desc.getText();
				Matcher m = p.matcher(lastText);
				if (m.find()) {
					return true;
				}
			}
		}
		return false;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("ThinDescVersioned: relId: ");
		buff.append(descId);
		buff.append("\n     ConceptId: ");
		buff.append(conceptId);
		buff.append("\n");
		for (ThinDescPart desl : versions) {
			buff.append("     ");
			buff.append(desl.toString());
			buff.append("\n");
		}

		return buff.toString();
	}

	public int getConceptId() {
		return conceptId;
	}

	public int getDescId() {
		return descId;
	}

	public List<ThinDescTuple> getTuples() {
		List<ThinDescTuple> tuples = new ArrayList<ThinDescTuple>();
		for (ThinDescPart p : getVersions()) {
			tuples.add(new ThinDescTuple(this, p));
		}
		return tuples;
	}

	public ThinDescTuple getFirstTuple() {
		return new ThinDescTuple(this, versions.get(0));
	}

	public ThinDescTuple getLastTuple() {
		return new ThinDescTuple(this, versions.get(versions.size() - 1));
	}

	public void addTuples(IntSet allowedStatus, IntSet allowedTypes,
			Set<Position> positions, List<ThinDescTuple> returnRels) {
		Set<ThinDescPart> uncommittedParts = new HashSet<ThinDescPart>();
		if (positions == null) {
			List<ThinDescPart> addedParts = new ArrayList<ThinDescPart>();
			Set<ThinDescPart> rejectedParts = new HashSet<ThinDescPart>();
			for (ThinDescPart part : versions) {
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
			for (ThinDescPart part : addedParts) {
				boolean addPart = true;
				for (ThinDescPart reject : rejectedParts) {
					if ((part.getVersion() <= reject.getVersion())
							&& (part.getPathId() == reject.getPathId())) {
						addPart = false;
						continue;
					}
				}
				if (addPart) {
					returnRels.add(new ThinDescTuple(this, part));
				}
			}
		} else {
			Set<ThinDescPart> addedParts = new HashSet<ThinDescPart>();
			for (Position position : positions) {
				Set<ThinDescPart> rejectedParts = new HashSet<ThinDescPart>();
				ThinDescTuple possible = null;
				for (ThinDescPart part : versions) {
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
							if (rejectedStatusPosition
									.isSubsequentOrEqualTo(possibleStatusPosition)) {
								possible = null;
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
								possible = new ThinDescTuple(this, part);
								addedParts.add(part);
							}
						} else {
							if (possible.getPathId() == part.getPathId()) {
								if (part.getVersion() > possible.getVersion()) {
									if (!addedParts.contains(part)) {
										possible = new ThinDescTuple(this, part);
										addedParts.add(part);
									}
								}
							} else {
								if (position.getDepth(part.getPathId()) < position
										.getDepth(possible.getPathId())) {
									if (!addedParts.contains(part)) {
										possible = new ThinDescTuple(this, part);
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
					for (ThinDescPart reject : rejectedParts) {
						Position rejectedStatusPosition = new Position(reject
								.getVersion(), position.getPath()
								.getMatchingPath(reject.getPathId()));
						if (rejectedStatusPosition
								.isSubsequentOrEqualTo(possibleStatusPosition)) {
							addPart = false;
							continue;
						}
					}
					if (addPart) {
						returnRels.add(possible);
					}
				}
			}
		}
		for (ThinDescPart p: uncommittedParts) {
			returnRels.add(new ThinDescTuple(this, p));
		}
	}

	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		conceptId = jarToDbNativeMap.get(conceptId);
		descId = jarToDbNativeMap.get(descId);
		for (ThinDescPart p : versions) {
			p.convertIds(jarToDbNativeMap);
		}

	}

	public boolean merge(ThinDescVersioned jarDesc) {
		HashSet<ThinDescPart> versionSet = new HashSet<ThinDescPart>(versions);
		boolean changed = false;
		for (ThinDescPart jarPart : jarDesc.versions) {
			if (!versionSet.contains(jarPart)) {
				changed = true;
				versions.add(jarPart);
			}
		}
		return changed;

	}

	public Set<TimePathId> getTimePathSet() {
		Set<TimePathId> tpSet = new HashSet<TimePathId>();
		for (ThinDescPart p : versions) {
			tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return tpSet;
	}

	@Override
	public boolean equals(Object obj) {
		ThinDescVersioned another = (ThinDescVersioned) obj;
		return descId == another.descId;
	}

	@Override
	public int hashCode() {
		return descId;
	}

}
