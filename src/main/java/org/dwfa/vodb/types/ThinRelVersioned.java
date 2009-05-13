package org.dwfa.vodb.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.TupleAdder;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinRelVersioned implements I_RelVersioned {
	private int relId;

	private int componentOneId;

	private int componentTwoId;

	private List<I_RelPart> versions;

	public ThinRelVersioned(int componentOneId, int componentTwoId,
			int count) {
		this(Integer.MIN_VALUE, componentOneId, componentTwoId,
				count);
	}
	public ThinRelVersioned(int relId, int componentOneId, int componentTwoId,
			int count) {
		super();
		this.relId = relId;
		this.componentOneId = componentOneId;
		this.componentTwoId = componentTwoId;
		this.versions = new ArrayList<I_RelPart>(count);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#addVersion(org.dwfa.vodb.types.I_RelPart)
	 */
	public boolean addVersion(I_RelPart rel) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(rel);
		} else if ((index >= 0) && (versions.get(index).hasNewData(rel))) {
			return versions.add(rel);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#addVersionNoRedundancyCheck(org.dwfa.vodb.types.ThinRelPart)
	 */
	public boolean addVersionNoRedundancyCheck(I_RelPart rel) {
		return versions.add(rel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#getVersions()
	 */
	public List<I_RelPart> getVersions() {
		return versions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#versionCount()
	 */
	public int versionCount() {
		return versions.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#addRetiredRec(int[], int)
	 */
	public boolean addRetiredRec(int[] releases, int retiredStatusId) {
		I_RelPart lastRelVersion = versions.get(versions.size() - 1);
		if (lastRelVersion.getVersion() != releases[releases.length - 1]) {
			// last version is not from the last release,
			// so we need to inactivate the relationship.
			int lastMention = -1;
			for (int i = 0; i < releases.length - 1; i++) {
				if (releases[i] == lastRelVersion.getVersion()) {
					lastMention = i;
					break;
				}
			}
			

			if (lastMention != -1) {
				int retiredVersion = releases[lastMention + 1];
				ThinRelPart retiredRel = new ThinRelPart();
				retiredRel.setPathId(lastRelVersion.getPathId());
				retiredRel.setVersion(retiredVersion);
				retiredRel.setStatusId(retiredStatusId);
				retiredRel
						.setCharacteristicId(lastRelVersion.getCharacteristicId());
				retiredRel.setGroup(lastRelVersion.getGroup());
				retiredRel.setRefinabilityId(lastRelVersion.getRefinabilityId());
				retiredRel.setTypeId(lastRelVersion.getTypeId());
				versions.add(retiredRel);
				return true;
			} else {
				if (AceLog.getAppLog().isLoggable(Level.FINE)) {
					StringBuffer b = new StringBuffer();
					b.append("[");
					for (int releaseVersion: releases) {
						b.append(ThinVersionHelper.format(releaseVersion));
						b.append("(");
						b.append(releaseVersion);
						b.append("), ");
					}
					b.append("]");
					AceLog.getAppLog().fine("\nUnable to add retired record for: " + this + 
							"\nreleases: " + b.toString() + 
							"\nc1 " + ConceptBean.get(componentOneId).toString() + 
							"\nc2 " + ConceptBean.get(componentTwoId).toString());
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#removeRedundantRecs()
	 */
	public boolean removeRedundantRecs() {
		I_RelVersioned compact = new ThinRelVersioned(relId, componentOneId,
				componentTwoId, versions.size());
		TreeMap<Integer, I_RelPart> partMap = new TreeMap<Integer, I_RelPart>();
		for (I_RelPart v : versions) {
			partMap.put(v.getVersion(), v);
		}
		for (Integer key : partMap.keySet()) {
			compact.addVersion(partMap.get(key));
		}
		if (versions.size() == compact.getVersions().size()) {
			return false;
		}
		versions = compact.getVersions();
		return true;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("ThinRelVersioned: relId: ");
		buff.append(relId);
		buff.append(" c1id: ");
		buff.append(componentOneId);
		buff.append(" c2id: ");
		buff.append(componentTwoId);
		buff.append("\n");
		for (I_RelPart rel : versions) {
			buff.append("     ");
			buff.append(rel.toString());
			buff.append("\n");
		}

		return buff.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#getC1Id()
	 */
	public int getC1Id() {
		return componentOneId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#getC2Id()
	 */
	public int getC2Id() {
		return componentTwoId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#getRelId()
	 */
	public int getRelId() {
		return relId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#getTuples()
	 */
	public List<I_RelTuple> getTuples() {
		List<I_RelTuple> tuples = new ArrayList<I_RelTuple>();
		for (I_RelPart p : versions) {
			tuples.add(new ThinRelTuple(this, p));
		}
		return tuples;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#getFirstTuple()
	 */
	public I_RelTuple getFirstTuple() {
		return new ThinRelTuple(this, versions.get(0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#getLastTuple()
	 */
	public I_RelTuple getLastTuple() {
		return new ThinRelTuple(this, versions.get(versions.size() - 1));
	}

	private class RelTupleAdder extends
			TupleAdder<I_RelTuple, ThinRelVersioned> {

		@Override
		public I_RelTuple makeTuple(I_AmPart part, ThinRelVersioned core) {
			return new ThinRelTuple(core, (I_RelPart) part);
		}

	}

	RelTupleAdder adder = new RelTupleAdder();

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			Set<I_Position> positions, List<I_RelTuple> matchingTuples,
			boolean addUncommitted) {
		adder.addTuples(allowedStatus, allowedTypes, positions, matchingTuples,
				addUncommitted, versions, this);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#convertIds(org.dwfa.vodb.jar.I_MapNativeToNative)
	 */
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		componentOneId = jarToDbNativeMap.get(componentOneId);
		componentTwoId = jarToDbNativeMap.get(componentTwoId);
		relId = jarToDbNativeMap.get(relId);
		for (I_RelPart p : versions) {
			p.convertIds(jarToDbNativeMap);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#merge(org.dwfa.vodb.types.ThinRelVersioned)
	 */
	public boolean merge(I_RelVersioned jarRel) {
		HashSet<I_RelPart> versionSet = new HashSet<I_RelPart>(versions);
		boolean changed = false;
		for (I_RelPart jarPart : jarRel.getVersions()) {
			if (!versionSet.contains(jarPart)) {
				changed = true;
				versions.add(jarPart);
			}
		}
		return changed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#getTimePathSet()
	 */
	public Set<TimePathId> getTimePathSet() {
		Set<TimePathId> tpSet = new HashSet<TimePathId>();
		for (I_RelPart p : versions) {
			tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return tpSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_RelVersioned#setC2Id(int)
	 */
	public void setC2Id(int destId) {
		componentTwoId = destId;

	}

	private static Collection<UUID> getUids(int id) throws IOException,
			TerminologyException {
		return LocalFixedTerminology.getStore().getUids(id);
	}

	public UniversalAceRelationship getUniversal() throws IOException,
			TerminologyException {
		UniversalAceRelationship universal = new UniversalAceRelationship(getUids(relId), getUids(componentOneId), getUids(componentTwoId),
				versions.size());
		for (I_RelPart part : versions) {
			UniversalAceRelationshipPart universalPart = new UniversalAceRelationshipPart();
			universalPart.setPathId(getUids(part.getPathId()));
			universalPart.setStatusId(getUids(part.getStatusId()));
			universalPart.setCharacteristicId(getUids(part.getCharacteristicId()));
			universalPart.setGroup(part.getGroup());
			universalPart.setRefinabilityId(getUids(part.getRefinabilityId()));
			universalPart.setRelTypeId(getUids(part.getTypeId()));
			universalPart.setTime(ThinVersionHelper.convert(part.getVersion()));
			universal.addVersion(universalPart);
		}
		return universal;
	}

	@Override
	public boolean equals(Object obj) {
		if (ThinRelVersioned.class.isAssignableFrom(obj.getClass())) {
			ThinRelVersioned another = (ThinRelVersioned) obj;
			return relId == another.relId;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return relId;
	}
	public void setRelId(int relId) {
		if (this.relId == Integer.MIN_VALUE) {
			this.relId = relId;
		} else {
			throw new RuntimeException("Cannot change the relid once set");
		}
	}

}
