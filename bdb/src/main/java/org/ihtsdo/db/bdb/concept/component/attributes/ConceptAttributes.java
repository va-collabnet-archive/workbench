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
		extends ConceptComponent<ConceptAttributesVersion, ConceptAttributes>
		implements I_ConceptAttributeVersioned,
				   I_ConceptAttributePart, 
				   I_ConceptAttributeTuple {

	private boolean defined;
	
	public ConceptAttributes(Concept enclosingConcept, TupleInput input) {
		super(enclosingConcept, input);
	}

	public ConceptAttributes(EConceptAttributes eAttr, Concept c) {
		super(eAttr, c);
		defined = eAttr.isDefined();
	}

	@Override
	public void readFromBdb(TupleInput input) {
		try {
			// nid, list size, and conceptNid are read already by the binder...
			defined = input.readBoolean();
			int additionalVersionCount = input.readShort();
			if (additionalVersionCount > 0) {
				if (additionalVersions == null) {
					additionalVersions = new ArrayList<ConceptAttributesVersion>(additionalVersionCount);
				} else {
					additionalVersions.ensureCapacity(additionalVersions.size() + additionalVersionCount);
				}
				for (int i = 0; i < additionalVersionCount; i++) {
					additionalVersions.add(new ConceptAttributesVersion(input, this));
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(" Processing nid: " + this.enclosingConcept.getNid(), e);
		}
	}

	@Override
	public void writeToBdb(TupleOutput output,
			int maxReadOnlyStatusAtPositionNid) {
		List<ConceptAttributesVersion> partsToWrite = new ArrayList<ConceptAttributesVersion>();
		if (additionalVersions != null) {
			for (ConceptAttributesVersion p : additionalVersions) {
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
					partsToWrite.add(p);
				}
			}
		}
		// Start writing
		output.writeBoolean(defined);
		output.writeShort(partsToWrite.size());
		for (ConceptAttributesVersion p : partsToWrite) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getTuples()
	 */
	public List<ConceptAttributesVersion> getTuples() {
		return Collections.unmodifiableList(additionalVersions);
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
			List<ConceptAttributesVersion> returnTuples) {
		addTuples(allowedStatus, positions, returnTuples, true);
	}

	private static class AttributeTupleComputer
			extends
			VersionComputer<ConceptAttributes, ConceptAttributesVersion> {
	}

	private static AttributeTupleComputer computer = new AttributeTupleComputer();

	public void addTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positions,
			List<ConceptAttributesVersion> matchingTuples,
			boolean addUncommitted) {
		computer.addTuples(allowedStatus, positions, matchingTuples,
				addUncommitted, additionalVersions, this);
	}

	public void addTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positionSet,
			List<ConceptAttributesVersion> returnTuples,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {

		List<ConceptAttributesVersion> tuples = new ArrayList<ConceptAttributesVersion>();

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
		for (ConceptAttributesVersion part : additionalVersions) {
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

	public List<ConceptAttributesVersion> getTuples(I_IntSet allowedStatus,
			PositionSetReadOnly viewPositionSet) {
		List<ConceptAttributesVersion> returnList = new ArrayList<ConceptAttributesVersion>();

		addTuples(allowedStatus, viewPositionSet, returnList);

		return returnList;
	}

	public void addTuples(I_IntSet allowedStatus, I_Position viewPosition,
			List<ConceptAttributesVersion> returnTuples) {
		computer.addTuples(allowedStatus, viewPosition, returnTuples,
				additionalVersions, this);
	}

	public List<ConceptAttributesVersion> getTuples(I_IntSet allowedStatus,
			I_Position viewPosition) {
		List<ConceptAttributesVersion> returnList = new ArrayList<ConceptAttributesVersion>();

		addTuples(allowedStatus, viewPosition, returnList);

		return returnList;
	}

	public boolean promote(I_Position viewPosition,
			PathSetReadOnly promotionPaths, I_IntSet allowedStatus) {
		int viewPathId = viewPosition.getPath().getConceptId();
		boolean promotedAnything = false;
		for (I_Path promotionPath : promotionPaths) {
			for (ConceptAttributesVersion tuple : getTuples(allowedStatus,
					viewPosition)) {
				if (tuple.getMutablePart().getPathId() == viewPathId) {
					ConceptAttributesVersion promotionPart = tuple
							.getMutablePart().makeAnalog(tuple.getStatusId(),
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
		return additionalVersions.add(new ConceptAttributesVersion(part, this));
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
	public List<I_ConceptAttributeTuple> getTuples(I_IntSet allowedStatus,
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
		return new ConceptAttributesVersion(this, statusNid, pathNid, time, this);
	}

	
	@Override
	public I_ConceptAttributeVersioned getConVersioned() {
		return this;
	}

	@Override
	public int getConceptStatus() {
		return getStatusId();
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
	public boolean fieldsEqual(ConceptComponent<ConceptAttributesVersion, ConceptAttributes> obj) {
		if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
			ConceptAttributes another = (ConceptAttributes) obj;
			if (this.defined == another.defined) {
				return conceptComponentFieldsEqual(another);
			}
		}
		return false;
	}
	
}
