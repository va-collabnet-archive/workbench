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
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.description.DescriptionRevision;
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
	
	public ConceptAttributes() {
		super();
	}

	public class Version 
		extends ConceptComponent<ConceptAttributesRevision, ConceptAttributes>.Version 
		implements I_ConceptAttributeTuple, I_ConceptAttributePart {
		
		public Version() {
			super();
		}

		public Version(int index) {
			super(index);
		}

		@Override
		public boolean isDefined() {
			if (index >= 0) {
				return revisions.get(index).isDefined();
			}
			return defined;
		}

		@Override
		public void setDefined(boolean defined) {
			if (index >= 0) {
				revisions.get(index).setDefined(defined);
			} else {
				ConceptAttributes.this.defined = defined;
			}
		}

		@Override
		public ConceptAttributesRevision makeAnalog(int statusNid, int pathNid, long time) {
			if (index >= 0) {
				return revisions.get(index).makeAnalog(statusNid, pathNid, time);
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


		public ArrayIntList getVariableVersionNids() {
			if (index >= 0) {
				ArrayIntList resultList = new ArrayIntList(2);
				return resultList;
			}
			return ConceptAttributes.this.getVariableVersionNids();
		}
		
	}

	@Override
	public void readFromBdb(TupleInput input) {
		try {
			// nid, list size, and conceptNid are read already by the binder...
			defined = input.readBoolean();
			int additionalVersionCount = input.readShort();
			if (additionalVersionCount > 0) {
				if (revisions == null) {
					revisions = new ArrayList<ConceptAttributesRevision>(additionalVersionCount);
				} else {
					revisions.ensureCapacity(revisions.size() + additionalVersionCount);
				}
				for (int i = 0; i < additionalVersionCount; i++) {
					revisions.add(new ConceptAttributesRevision(input, this));
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
		if (revisions != null) {
			for (ConceptAttributesRevision p : revisions) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getTuples()
	 */
	public List<Version> getTuples() {
		return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
	}

	List<Version> versions;
	private List<Version> getVersions() {
		if (versions == null) {
			int count = 1;
			if (revisions != null) {
				count = count + revisions.size();
			}
			ArrayList<Version> list = new ArrayList<Version>(count);
			list.add(new Version());
			if (revisions != null) {
				for (int i = 0; i < revisions.size(); i++) {
					list.add(new Version(i));
				}
			}
			versions = list;
		}
		return versions;
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
			List<Version> returnTuples,
			boolean addUncommitted) {
		computer.addSpecifiedVersions(allowedStatus, positions, returnTuples,
				addUncommitted, getTuples());
	}

	public void addTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positionSet,
			List<Version> returnTuples,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {

		List<Version> matchedTuples = new ArrayList<Version>();

		addTuples(allowedStatus, positionSet, matchedTuples, addUncommitted);

		if (returnConflictResolvedLatestState) {
			I_ConfigAceFrame config = Terms.get()
					.getActiveAceFrameConfig();
			I_ManageConflict conflictResolutionStrategy;
			if (config == null) {
				conflictResolutionStrategy = new IdentifyAllConflictStrategy();
			} else {
				conflictResolutionStrategy = config
						.getConflictResolutionStrategy();
			}
			matchedTuples = conflictResolutionStrategy.resolveTuples(matchedTuples);
		}
		returnTuples.addAll(matchedTuples);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dwfa.vodb.types.I_ConceptAttributeVersioned#getLocalFixedConcept()
	 */
	public I_ConceptualizeLocally getLocalFixedConcept() {
		return LocalFixedConcept.get(nid, !defined);
	}

	@Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
	    if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
	        ConceptAttributes another = (ConceptAttributes) obj;
	        if (this.nid == another.nid) {
	            return true;
	        }
	    }
	    return false;
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
		for (ConceptAttributesRevision part : revisions) {
			UniversalAceConceptAttributesPart universalPart = new UniversalAceConceptAttributesPart();
			universalPart.setStatusId(getUids(part.getStatusId()));
			universalPart.setDefined(part.isDefined());
			universalPart.setPathId(getUids(part.getPathId()));
			universalPart.setTime(part.getTime());
			conceptAttributes.addVersion(universalPart);
		}
		return conceptAttributes;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" defined:" + this.defined);
        buf.append(" }=> ");
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
		computer.addSpecifiedVersions(allowedStatus, viewPosition, returnTuples,
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
		return super.addVersion(new ConceptAttributesRevision(part, this));
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
		if (enclosingConcept.isEditable()) {
			ConceptAttributesRevision newR = new ConceptAttributesRevision(this, statusNid, pathNid, time, this);
			addVersion(newR);
			return newR;
		}
		throw new UnsupportedOperationException("enclosingConcept is not editable");
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

    /**
     * Test method to check to see if two objects are equal in all respects. 
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures. 
     * @throws IOException 
     */
    public String validate(ConceptAttributes another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        // Compare defined
        if (this.defined != another.defined) {
            buf.append("\tConceptAttributes.defined not equal: \n" + 
                "\t\tthis.defined = " + this.defined + "\n" + 
                "\t\tanother.defined = " + another.defined + "\n");
        }       
        // Compare the parents 
        buf.append(super.validate(another));       
        return buf.toString();
    }
        

    @Override
	public List<? extends I_ConceptAttributePart> getMutableParts() {
		return getTuples();
	}
	
}
