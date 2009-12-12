package org.ihtsdo.db.bdb.concept.component.attributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.util.TupleComputer;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ConceptAttributes 
	extends ConceptComponent<ConceptAttributesPart> 
	implements I_ConceptAttributeVersioned<ConceptAttributesPart, ConceptAttributes, ConceptAttributesTuple> {

	protected ConceptAttributes(int nid, int parts,
			boolean editable) {
		super(nid, parts, editable);
	}

	@Override
	public void readComponentFromBdb(TupleInput input, int conceptNid) {
		// nothing to do...
	}

	@Override
	public void readPartFromBdb(TupleInput input) {
		versions.add(new ConceptAttributesPart(input));
	}

	@Override
	public void writeComponentToBdb(TupleOutput output) {
		// Nothing to do...
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#addVersion(org.dwfa.vodb.types.ThinConPart)
	 */
	public boolean addVersion(ConceptAttributesPart part) {
		return versions.add(part);
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
	public List<ConceptAttributesTuple> getTuples() {
		List<ConceptAttributesTuple> tuples = new ArrayList<ConceptAttributesTuple>();
		for (ConceptAttributesPart p : versions) {
			tuples.add(new ConceptAttributesTuple(this, p));
		}
		return tuples;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#convertIds(org.dwfa.vodb.jar.I_MapNativeToNative)
	 */
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#merge(org.dwfa.vodb.types.ThinConVersioned)
	 */
	public boolean merge(ConceptAttributes jarCon) {
		HashSet<ConceptAttributesPart> versionSet = new HashSet<ConceptAttributesPart>(
				versions);
		boolean changed = false;
		for (ConceptAttributesPart jarPart : jarCon.getVersions()) {
			if (!versionSet.contains(jarPart)) {
				changed = true;
				versions.add((ConceptAttributesPart) jarPart);
			}
		}
		return changed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getTimePathSet()
	 */
	public Set<TimePathId> getTimePathSet() {
		Set<TimePathId> tpSet = new HashSet<TimePathId>();
		for (ConceptAttributesPart p : versions) {
			tpSet.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return tpSet;
	}

	public void addTuples(I_IntSet allowedStatus, PositionSetReadOnly positions,
			List<ConceptAttributesTuple> returnTuples) {
		addTuples(allowedStatus, positions, returnTuples, true);
	}

	
	private static class AttributeTupleComputer extends
			TupleComputer<ConceptAttributesTuple, ConceptAttributes, ConceptAttributesPart> {

		@Override
		public ConceptAttributesTuple makeTuple(ConceptAttributesPart part,
				ConceptAttributes core) {
			return new ConceptAttributesTuple(core, part);
		}
	}

	private static AttributeTupleComputer computer = new AttributeTupleComputer();

	public void addTuples(I_IntSet allowedStatus, PositionSetReadOnly positions,
			List<ConceptAttributesTuple> matchingTuples, boolean addUncommitted) {
		computer.addTuples(allowedStatus, positions, matchingTuples,
				addUncommitted, versions, this);
	}
	
	public void addTuples(I_IntSet allowedStatus, PositionSetReadOnly positionSet,
			List<ConceptAttributesTuple> returnTuples, boolean addUncommitted,
			boolean returnConflictResolvedLatestState) throws TerminologyException, IOException {
	    
		List<ConceptAttributesTuple> tuples = new ArrayList<ConceptAttributesTuple>();
		   
	    addTuples(allowedStatus, positionSet, tuples, addUncommitted);
		
		if (returnConflictResolvedLatestState) {
		    I_ConfigAceFrame config = AceConfig.getVodb().getActiveAceFrameConfig();
			I_ManageConflict conflictResolutionStrategy;
			if (config == null) {
				conflictResolutionStrategy = new IdentifyAllConflictStrategy();
			} else {
				conflictResolutionStrategy = config.getConflictResolutionStrategy();
			}
			
			tuples = conflictResolutionStrategy.resolveTuples(tuples);
		}
		
		returnTuples.addAll(tuples);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getLocalFixedConcept()
	 */
	public I_ConceptualizeLocally getLocalFixedConcept() {
		boolean isDefined = versions.get(versions.size() - 1).isDefined();
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
		for (ConceptAttributesPart part : versions) {
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
		buf.append("NativeId: ");
		buf.append(nid);
		buf.append(" parts: ");
		buf.append(versions.size());
		buf.append("\n  ");
		for (ConceptAttributesPart p : versions) {
			buf.append(p);
			buf.append("\n  ");
		}
		return buf.toString();
	}

	public void setConId(int conId) {
		if (this.nid == Integer.MIN_VALUE) {
			this.nid = conId;
		} else {
			throw new RuntimeException("Cannot change the conId once set");
		}
	}

	public List<ConceptAttributesTuple> getTuples(I_IntSet allowedStatus,
			PositionSetReadOnly viewPositionSet) {
		List<ConceptAttributesTuple> returnList = new ArrayList<ConceptAttributesTuple>();
		
		addTuples(allowedStatus, viewPositionSet, returnList);
		
		return returnList;
	}
	
	public void addTuples(I_IntSet allowedStatus, I_Position viewPosition,
			List<ConceptAttributesTuple> returnTuples) {
		computer.addTuples(allowedStatus, viewPosition, returnTuples, versions, this);
	}

	public List<ConceptAttributesTuple> getTuples(I_IntSet allowedStatus,
			I_Position viewPosition) {
		List<ConceptAttributesTuple> returnList = new ArrayList<ConceptAttributesTuple>();
		
		addTuples(allowedStatus, viewPosition, returnList);
		
		return returnList;
	}

	public boolean promote(I_Position viewPosition, PathSetReadOnly promotionPaths,
			I_IntSet allowedStatus) {
		int viewPathId = viewPosition.getPath().getConceptId();
		boolean promotedAnything = false;
		for (I_Path promotionPath: promotionPaths) {
			for (ConceptAttributesTuple tuple: getTuples(allowedStatus, viewPosition)) {
				if (tuple.getPart().getPathId() == viewPathId) {
					ConceptAttributesPart promotionPart = 
						tuple.getPart().makeAnalog(tuple.getStatusId(), promotionPath.getConceptId(), Long.MAX_VALUE);
					addVersion(promotionPart);
					promotedAnything = true;
				}
			}
		}
		return promotedAnything;
	}
}
