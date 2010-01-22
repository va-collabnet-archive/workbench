package org.ihtsdo.db.bdb.concept.component.attributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.util.VersionComputer;
import org.ihtsdo.etypes.EConceptAttributes;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ConceptAttributes 
		extends ConceptComponent<ConceptAttributesRevision, ConceptAttributes>
		implements I_ConceptAttributeVersioned,
				   I_ConceptAttributePart {

	private boolean defined;
		
	public ConceptAttributes(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public ConceptAttributes(EConceptAttributes eAttr, Concept c) {
		super(eAttr, c);
		defined = eAttr.isDefined();
	}
	
	public class Version 
		extends ConceptComponent<ConceptAttributesRevision, ConceptAttributes>.Version 
		implements I_ConceptAttributeTuple {
		
		public Version() {
			super();
		}

		public Version(int index) {
			super(index);
		}

		@Override
		public boolean isDefined() {
			if (index >= 0) {
				return additionalVersions.get(index).isDefined();
			}
			return defined;
		}

		@Override
		public void setDefined(boolean defined) {
			if (index >= 0) {
				additionalVersions.get(index).setDefined(defined);
			} else {
				ConceptAttributes.this.defined = defined;
			}
		}

		@Override
		public ConceptAttributesRevision makeAnalog(int statusNid, int pathNid, long time) {
			if (index >= 0) {
				return additionalVersions.get(index).makeAnalog(statusNid, pathNid, time);
			}
			return new ConceptAttributesRevision(ConceptAttributes.this, 
					statusNid, pathNid, time, ConceptAttributes.this);
		}

		@Override
		public int getConId() {
			return nid;
		}

		@Override
		public I_ConceptAttributePart getMutablePart() {
			return (I_ConceptAttributePart) super.getMutablePart();
		}
		
		@Override
		@Deprecated
		public I_ConceptAttributePart duplicate() {
			throw new UnsupportedOperationException("Use makeAnalog instead");
		}
		@Override
		public I_ConceptAttributeVersioned getConVersioned() {
			return ConceptAttributes.this;
		}

		@Override
		@Deprecated
		public int getConceptStatus() {
			return getStatusId();
		}


		@Override
		public ArrayIntList getPartComponentNids() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

	@Override
	public void readFromBdb(TupleInput input) {
		try {
			// nid, list size, and conceptNid are read already by the binder...
			defined = input.readBoolean();
			int additionalVersionCount = input.readShort();
			if (additionalVersionCount > 0) {
				if (additionalVersions == null) {
					additionalVersions = new ArrayList<ConceptAttributesRevision>(additionalVersionCount);
				} else {
					additionalVersions.ensureCapacity(additionalVersions.size() + additionalVersionCount);
				}
				for (int i = 0; i < additionalVersionCount; i++) {
					additionalVersions.add(new ConceptAttributesRevision(input, this));
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(" Processing nid: " + this.enclosingConcept.getNid(), e);
		}
	}

	@Override
	public void writeToBdb(TupleOutput output,
			int maxReadOnlyStatusAtPositionNid) {
		List<ConceptAttributesRevision> partsToWrite = new ArrayList<ConceptAttributesRevision>();
		if (additionalVersions != null) {
			for (ConceptAttributesRevision p : additionalVersions) {
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
					partsToWrite.add(p);
				}
			}
		}
		// Start writing
		output.writeBoolean(defined);
		output.writeShort(partsToWrite.size());
		for (ConceptAttributesRevision p : partsToWrite) {
			p.writePartToBdb(output);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getConId()
	 */
	public int getConId() {
		return nid;
	}

	List<Version> versions;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getTuples()
	 */
	public List<Version> getTuples() {
		if (versions == null) {
			versions = new ArrayList<Version>();
			versions.add(new Version());
		}
		if (additionalVersions != null) {
			for (int i = 0; i < additionalVersions.size(); i++) {
				versions.add(new Version(i));
			}
		}
		return Collections.unmodifiableList(versions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dwfa.vodb.types.I_ConceptAttributeVersioned#convertIds(org.dwfa.vodb
	 * .jar.I_MapNativeToNative)
	 */
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dwfa.vodb.types.I_ConceptAttributeVersioned#merge(org.dwfa.vodb.types
	 * .ThinConVersioned)
	 */
	public boolean merge(ConceptAttributes jarCon) {
		throw new UnsupportedOperationException();
	}

	public void addTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positions,
			List<Version> returnTuples) {
		addTuples(allowedStatus, positions, returnTuples, true);
	}

	private static VersionComputer<ConceptAttributes.Version> computer = 
		new VersionComputer<ConceptAttributes.Version>();

	public void addTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positions,
			List<Version> tuples,
			boolean addUncommitted) {
		computer.addTuples(allowedStatus, positions, tuples,
				addUncommitted, getTuples());
	}

	public void addTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positionSet,
			List<Version> returnTuples,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {

		List<Version> tuples = new ArrayList<Version>();

		addTuples(allowedStatus, positionSet, tuples, addUncommitted);

		if (returnConflictResolvedLatestState) {
			I_ConfigAceFrame config = AceConfig.getVodb()
					.getActiveAceFrameConfig();
			I_ManageConflict conflictResolutionStrategy;
			if (config == null) {
				conflictResolutionStrategy = new IdentifyAllConflictStrategy();
			} else {
				conflictResolutionStrategy = config
						.getConflictResolutionStrategy();
			}
			tuples = conflictResolutionStrategy.resolveTuples(tuples);
		}
		returnTuples.addAll(tuples);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dwfa.vodb.types.I_ConceptAttributeVersioned#getLocalFixedConcept()
	 */
	public I_ConceptualizeLocally getLocalFixedConcept() {
		boolean isDefined = additionalVersions.get(additionalVersions.size() - 1)
				.isDefined();
		boolean isPrimitive = !isDefined;
		return LocalFixedConcept.get(nid, isPrimitive);
	}

	@Override
	public boolean equals(Object obj) {
		ConceptAttributes another = (ConceptAttributes) obj;
		return nid == another.nid;
	}

	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] { nid });
	}

	private static Collection<UUID> getUids(int id) throws IOException,
			TerminologyException {
		return LocalFixedTerminology.getStore().getUids(id);
	}

	public UniversalAceConceptAttributes getUniversal() throws IOException,
			TerminologyException {
		UniversalAceConceptAttributes conceptAttributes = new UniversalAceConceptAttributes(
				getUids(nid), this.versionCount());
		for (ConceptAttributesRevision part : additionalVersions) {
			UniversalAceConceptAttributesPart universalPart = new UniversalAceConceptAttributesPart();
			universalPart.setStatusId(getUids(part.getStatusId()));
			universalPart.setDefined(part.isDefined());
			universalPart.setPathId(getUids(part.getPathId()));
			universalPart.setTime(part.getTime());
			conceptAttributes.addVersion(universalPart);
		}
		return conceptAttributes;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("nid: ");
		buf.append(nid);
		buf.append(" def: ");
		buf.append(defined);
		buf.append(" ");
		buf.append(super.toString());
		return buf.toString();
	}

	public void setConId(int cNid) {
		if (this.nid == Integer.MIN_VALUE) {
			this.nid = cNid;
		} else {
			throw new RuntimeException("Cannot change the cNid once set");
		}
	}

	public List<Version> getTuples(I_IntSet allowedStatus,
			PositionSetReadOnly viewPositionSet) {
		List<Version> returnList = new ArrayList<Version>();

		addTuples(allowedStatus, viewPositionSet, returnList);

		return returnList;
	}

	public void addTuples(I_IntSet allowedStatus, I_Position viewPosition,
			List<Version> returnTuples) {
		computer.addTuples(allowedStatus, viewPosition, returnTuples,
				getTuples());
	}

	public List<Version> getTuples(I_IntSet allowedStatus,
			I_Position viewPosition) {
		List<Version> returnList = new ArrayList<Version>();

		addTuples(allowedStatus, viewPosition, returnList);

		return returnList;
	}

	public boolean promote(I_Position viewPosition,
			PathSetReadOnly promotionPaths, I_IntSet allowedStatus) {
		int viewPathId = viewPosition.getPath().getConceptId();
		boolean promotedAnything = false;
		for (I_Path promotionPath : promotionPaths) {
			for (Version version : getTuples(allowedStatus,
					viewPosition)) {
				if (version.getPathId() == viewPathId) {
					ConceptAttributesRevision promotionPart = 
						version.makeAnalog(version.getStatusId(),
									promotionPath.getConceptId(),
									Long.MAX_VALUE);
					addVersion(promotionPart);
					promotedAnything = true;
				}
			}
		}
		return promotedAnything;
	}

	/*
	 * Below methods should be considered for deprecation...
	 */

	@Override
	public boolean addVersion(I_ConceptAttributePart part) {
		this.versions = null;
		return additionalVersions.add(new ConceptAttributesRevision(part, this));
	}

	@Override
	public boolean merge(I_ConceptAttributeVersioned jarCon) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addTuples(I_IntSet allowedStatus, Set<I_Position> positionSet,
			List<I_ConceptAttributeTuple> returnTuples) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void addTuples(I_IntSet allowedStatus, Set<I_Position> positionSet,
			List<I_ConceptAttributeTuple> returnTuples, boolean addUncommitted) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void addTuples(I_IntSet allowedStatus, Set<I_Position> positionSet,
			List<I_ConceptAttributeTuple> returnTuples, boolean addUncommitted,
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Version> getTuples(I_IntSet allowedStatus,
			Set<I_Position> viewPositionSet) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDefined() {
		return defined;
	}

	@Override
	public void setDefined(boolean defined) {
		this.defined = defined;
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
		return new ConceptAttributesRevision(this, statusNid, pathNid, time, this);
	}

	@Override
	public ArrayIntList getVariableVersionNids() {
		return new ArrayIntList(2);
	}

	@Override
	public I_ConceptAttributePart duplicate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ConceptAttributes getMutablePart() {
		return this;
	}

	@Override
	public boolean fieldsEqual(ConceptComponent<ConceptAttributesRevision, ConceptAttributes> obj) {
		if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
			ConceptAttributes another = (ConceptAttributes) obj;
			if (this.defined == another.defined) {
				return conceptComponentFieldsEqual(another);
			}
		}
		return false;
	}
	
}
