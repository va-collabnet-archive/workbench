package org.ihtsdo.concept.component.relationship;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.PRECEDENCE;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.ERelationship;
import org.ihtsdo.etypes.ERelationshipRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Relationship extends ConceptComponent<RelationshipRevision, Relationship> 
	implements I_RelVersioned, I_RelPart {

	public class Version 
	extends ConceptComponent<RelationshipRevision, Relationship>.Version 
	implements I_RelTuple, I_RelPart {
	
	public Version() {
		super();
	}

	public Version(int index) {
		super(index);
	}

	@Override
	public int getC1Id() {
		return getEnclosingConcept().getNid();
	}

	@Override
	public int getC2Id() {
		return c2Nid;
	}

	@Override
	public int getCharacteristicId() {
		if (index >= 0) {
			return revisions.get(index).getCharacteristicId();
		}
		return characteristicNid;
	}

	@Override
	public int getGroup() {
		if (index >= 0) {
			return revisions.get(index).getGroup();
		}
		return group;
	}

	@Override
	public int getRefinabilityId() {
		if (index >= 0) {
			return revisions.get(index).getRefinabilityId();
		}
		return refinabilityNid;
	}

	@Override
	public int getRelId() {
		return nid;
	}

	@Override
	public I_RelVersioned getRelVersioned() {
		return Relationship.this;
	}

	@Override
	public void setCharacteristicId(int characteristicId) {
		if (index >= 0) {
			revisions.get(index).setCharacteristicId(characteristicId);
		} else {
			Relationship.this.setCharacteristicId(characteristicId);
		}
	}

	@Override
	public void setGroup(int group) {
		if (index >= 0) {
			revisions.get(index).setGroup(group);
		} else {
			Relationship.this.setGroup(group);
		}
	}

	@Override
	public void setRefinabilityId(int refinabilityId) {
		if (index >= 0) {
			revisions.get(index).setRefinabilityId(refinabilityId);
		} else {
			Relationship.this.setRefinabilityId(refinabilityId);
		}
	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTypeId() {
		if (index >= 0) {
			return revisions.get(index).getTypeId();
		} else {
			return Relationship.this.typeNid;
		}
	}

	@Override
	public void setTypeId(int type) {
		if (index >= 0) {
			revisions.get(index).setTypeId(type);
		} else {
			Relationship.this.setTypeId(type);
		}		
	}
	
	public Relationship getFixedPart() {
		return Relationship.this;
	}

	public ArrayIntList getVariableVersionNids() {
		if (index >= 0) {
			ArrayIntList resultList = new ArrayIntList(7);
			resultList.add(getCharacteristicId());
			resultList.add(getRefinabilityId());
			resultList.add(getTypeId());
			resultList.add(getC1Id());
			resultList.add(getC2Id());			
			return resultList;
		}
		return Relationship.this.getVariableVersionNids();
	}

	@Override
	public RelationshipRevision makeAnalog(int statusNid, int pathNid, long time) {
		if (index >= 0) {
		    RelationshipRevision rev = revisions.get(index);
            if (rev.getTime() == Long.MAX_VALUE && rev.getPathId() == pathNid) {
			    rev.setStatusId(statusNid);
                return rev;
            }
            return rev.makeAnalog(statusNid, pathNid, time);
		} else {
			return Relationship.this.makeAnalog(statusNid, pathNid, time);
		}
	}

	@Override
	public I_RelPart getMutablePart() {
		return (I_RelPart) super.getMutablePart();
	}
	
	@Override
	@Deprecated
	public RelationshipRevision duplicate() {
		throw new UnsupportedOperationException("Use makeAnalog instead");
	}

	}
	
	private static VersionComputer<Relationship.Version> computer = 
		new VersionComputer<Relationship.Version>();

	private int c2Nid;

	private int characteristicNid;
	private int group;
	private int refinabilityNid;
	private int typeNid;

	public Relationship(Concept enclosingConcept, 
			TupleInput input) throws IOException {
		super(enclosingConcept, 
				input);
	}
	

	public Relationship(ERelationship eRel, Concept enclosingConcept) throws IOException {
		super(eRel, enclosingConcept);
		c2Nid = Bdb.uuidToNid(eRel.getC2Uuid());
		characteristicNid = Bdb.uuidToNid(eRel.getCharacteristicUuid());
		group = eRel.getRelGroup();
		refinabilityNid = Bdb.uuidToNid(eRel.getRefinabilityUuid());
		typeNid = Bdb.uuidToNid(eRel.getTypeUuid());
		primordialSapNid = Bdb.getSapNid(eRel);
		if (eRel.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<RelationshipRevision>();
			for (ERelationshipRevision erv: eRel.getRevisionList()) {
				revisions.add(new RelationshipRevision(erv, this));
			}
		}
	}

    public Relationship() {
        super();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName() + ":{");
		buf.append("src:");
		ConceptComponent.addNidToBuffer(buf, getEnclosingConcept().getNid());
		buf.append(" t:");
		ConceptComponent.addNidToBuffer(buf, typeNid);
		buf.append(" dest:");
		ConceptComponent.addNidToBuffer(buf, c2Nid);
        buf.append(" c:");
        ConceptComponent.addNidToBuffer(buf, characteristicNid);
		buf.append(" g:" + group);
		buf.append(" r:");
		ConceptComponent.addNidToBuffer(buf, refinabilityNid);
		buf.append(" ");
		buf.append(super.toString());
		return buf.toString();
	}

	@Override
	public boolean fieldsEqual(ConceptComponent<RelationshipRevision, Relationship> obj) {
		if (Relationship.class.isAssignableFrom(obj.getClass())) {
			Relationship another = (Relationship) obj;
			if (this.c2Nid != another.c2Nid) {
				return false;
			}
			if (this.characteristicNid != another.characteristicNid) {
				return false;
			}
			if (this.group != another.group) {
				return false;
			}
			if (this.refinabilityNid != another.refinabilityNid) {
				return false;
			}
			if (this.typeNid != another.typeNid) {
				return false;
			}
			return conceptComponentFieldsEqual(another);
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
    public String validate(Relationship another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (this.c2Nid != another.c2Nid) {
            buf.append("\tRelationship.initialCaseSignificant not equal: \n" + 
                "\t\tthis.c2Nid = " + this.c2Nid + "\n" + 
                "\t\tanother.c2Nid = " + another.c2Nid + "\n");
        }
        if (this.characteristicNid != another.characteristicNid) {
            buf.append("\tRelationship.characteristicNid not equal: \n" + 
                "\t\tthis.characteristicNid = " + this.characteristicNid + "\n" + 
                "\t\tanother.characteristicNid = " + another.characteristicNid + "\n");
        }
        if (this.group != another.group) {
            buf.append("\tRelationship.group not equal: \n" + 
                "\t\tthis.group = " + this.group + "\n" + 
                "\t\tanother.group = " + another.group + "\n");
        }
        if (this.refinabilityNid != another.refinabilityNid) {
            buf.append("\tRelationship.refinabilityNid not equal: \n" + 
                "\t\tthis.refinabilityNid = " + this.refinabilityNid + "\n" + 
                "\t\tanother.refinabilityNid = " + another.refinabilityNid + "\n");
        }
        if (this.typeNid != another.typeNid) {
            buf.append("\tRelationship.typeNid not equal: \n" + 
                "\t\tthis.typeNid = " + this.typeNid + "\n" + 
                "\t\tanother.typeNid = " + another.typeNid + "\n");
        }

        // Compare the parents 
        buf.append(super.validate(another));
        
        return buf.toString();
    }

	@Override
	public void readFromBdb(TupleInput input) {
		// nid, list size, and conceptNid are read already by the binder...
		c2Nid = input.readInt();
		characteristicNid = input.readInt();
		group = input.readInt();
		refinabilityNid = input.readInt();
		typeNid = input.readInt();
		int additionalVersionCount = input.readShort();
		if (additionalVersionCount > 0) {
			revisions = new CopyOnWriteArrayList<RelationshipRevision>();
			for (int i = 0; i < additionalVersionCount; i++) {
				revisions.add(new RelationshipRevision(input, this));
			}
		}
	}

	@Override
	public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		//
		List<RelationshipRevision> partsToWrite = new ArrayList<RelationshipRevision>();
		if (revisions != null) {
			for (RelationshipRevision p : revisions) {
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid &&
						p.getTime() != Long.MIN_VALUE) {
					partsToWrite.add(p);
				}
			}
		}
		// Start writing
		// c1Nid is the enclosing concept, does not need to be written. 
		output.writeInt(c2Nid);
		output.writeInt(characteristicNid);
		output.writeInt(group);
		output.writeInt(refinabilityNid);
		output.writeInt(typeNid);
		output.writeShort(partsToWrite.size());
		for (RelationshipRevision p : partsToWrite) {
			p.writePartToBdb(output);
		}
	}


	@Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
		if (Relationship.class.isAssignableFrom(obj.getClass())) {
			Relationship another = (Relationship) obj;
			return nid == another.nid;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] { nid, c2Nid, enclosingConceptNid });
	}


	@Override
	public boolean addRetiredRec(int[] releases, int retiredStatusId) {
		throw new UnsupportedOperationException();
	}

	private void addTuples(I_IntSet allowedStatus, I_Position viewPosition,
			List<Version> matchingTuples, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) {
		computer.addSpecifiedVersions(allowedStatus, viewPosition, matchingTuples,
				getTuples(), precedencePolicy, contradictionManager);
	}

    public List<? extends I_RelTuple> getSpecifiedVersions(I_ConfigAceFrame frameConfig) throws TerminologyException, IOException {
        List<Relationship.Version> specifiedVersions = new ArrayList<Relationship.Version>();
        computer.addSpecifiedVersions(frameConfig.getAllowedStatus(), frameConfig.getViewPositionSetReadOnly(), specifiedVersions,
            getTuples(), frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy());
        return specifiedVersions;
    }

    public List<? extends I_RelTuple> getSpecifiedVersions(I_IntSet allowedStatus, PositionSetReadOnly positions, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) throws TerminologyException, IOException {
        List<Relationship.Version> specifiedVersions = new ArrayList<Relationship.Version>();
        computer.addSpecifiedVersions(allowedStatus, positions, specifiedVersions,
            getTuples(), precedencePolicy, contradictionManager);
        return specifiedVersions;
    }

    @Deprecated
	public void addTuples(I_IntSet allowedTypes, 
						  List<I_RelTuple> returnRels,
						  boolean addUncommitted, 
						  boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException("Use a method that specified the config");
	}

	public boolean addVersion(I_RelPart part) {
		this.versions = null;
		return super.addRevision((RelationshipRevision) part);
	}

	public boolean addPart(RelationshipRevision part) {
		return revisions.add(part);
	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getC1Id() {
		return enclosingConceptNid;
	}

	@Override
	public int getC2Id() {
		return c2Nid;
	}

	@Override
	public Version getFirstTuple() {
		return getTuples().get(0);
	}

	@Override
	public Version getLastTuple() {
		List<Version> vList = getTuples();
		return vList.get(vList.size() - 1);
	}

	@Override
	public int getRelId() {
		return nid;
	}
	List<Version> versions;

	public List<Version> getTuples() {
		return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
	}

	protected List<? extends Version> getVersions() {
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
	
	public List<Version> getVersions(I_ManageContradiction contradictionManager) {
		// TODO implement conflict resolution
		return getTuples();
	}
	
	public List<Version> getTuples(I_ManageContradiction contradictionManager) {
		// TODO implement conflict resolution
		return getTuples();
	}
	
	
	
	@Override
	public UniversalAceRelationship getUniversal() throws IOException,
			TerminologyException {
		UniversalAceRelationship universal = new UniversalAceRelationship(
				getEnclosingConcept().getUidsForComponent(nid), 
				getEnclosingConcept().getUids(), 
				Bdb.getConceptDb().getConcept(c2Nid).getUids(),
				revisions.size());
		for (Version part : getVersions()) {
			UniversalAceRelationshipPart universalPart = new UniversalAceRelationshipPart();
			universalPart.setPathId(Bdb.getConceptDb().getConcept(part.getPathId()).getUids());
			universalPart.setStatusId(Bdb.getConceptDb().getConcept(part.getStatusId()).getUids());
			universalPart.setCharacteristicId(Bdb.getConceptDb().getConcept(part.getCharacteristicId()).getUids());
			universalPart.setGroup(part.getGroup());
			universalPart.setRefinabilityId(Bdb.getConceptDb().getConcept(part.getRefinabilityId()).getUids());
			universalPart.setTypeId(Bdb.getConceptDb().getConcept(part.getTypeId()).getUids());
			universalPart.setTime(part.getTime());
			universal.addVersion(universalPart);
		}
		return universal;
	}

	@Override
	public void setC2Id(int destNid) {
		this.c2Nid = destNid;
        modified();
	}

	@Override
	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus, PRECEDENCE precedence)
			throws IOException, TerminologyException {
        int viewPathId = viewPosition.getPath().getConceptId();
        Collection<Version> matchingTuples = computer.
        	getSpecifiedVersions(allowedStatus, 
        			viewPosition, 
        			getVersions(), precedence, null);
        boolean promotedAnything = false;
        for (I_Path promotionPath : pomotionPaths) {
            for (Version v : matchingTuples) {
                if (v.getPathId() == viewPathId) {
                	
                    RelationshipRevision revision =  v.makeAnalog(v.getStatusId(), 
							promotionPath.getConceptId(), Long.MAX_VALUE);
                    addRevision(revision);
                    promotedAnything = true;
                }
            }
        }
        return promotedAnything;
	}


	@Override
	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<I_RelTuple> relTupleList,
			PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) {
		List<Version> tuplesToReturn = new ArrayList<Version>();
		computer.addSpecifiedVersions(allowedStatus, allowedTypes, positions, tuplesToReturn,
					getVersions(), precedencePolicy, contradictionManager);
		relTupleList.addAll(tuplesToReturn);
	}


	@Override
	public boolean addVersionNoRedundancyCheck(I_RelPart rel) {
		throw new UnsupportedOperationException();
	}


	@Override
	public int getCharacteristicId() {
		return characteristicNid;
	}


	@Override
	public int getGroup() {
		return group;
	}


	@Override
	public int getRefinabilityId() {
		return refinabilityNid;
	}


	@Override
	public void setCharacteristicId(int characteristicNid) {
		this.characteristicNid = characteristicNid;
        modified();
	}


	@Override
	public void setGroup(int group) {
		this.group = group;
        modified();
	}


	@Override
	public void setRefinabilityId(int refinabilityId) {
		this.refinabilityNid = refinabilityId;
        modified();
	}

	@Override
	public int getTypeId() {
		return this.typeNid;
	}


	@Override
	public void setTypeId(int typeNid) {
		this.typeNid = typeNid;
        modified();
	}


	@Override
	public ArrayIntList getVariableVersionNids() {
		ArrayIntList nidList = new ArrayIntList(7);
		nidList.add(enclosingConceptNid);
		nidList.add(c2Nid);
		nidList.add(characteristicNid);
		nidList.add(refinabilityNid);
		nidList.add(typeNid);
		return nidList;
	}


	@Override
	public RelationshipRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (getTime() == time && getPathId() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
		RelationshipRevision newR = new RelationshipRevision(this, statusNid, pathNid, time, this);
		addRevision(newR);
		return newR;
	}

	@Override
	public I_RelPart duplicate() {
		throw new UnsupportedOperationException("Use makeAnalog instead");
	}
	
	public Relationship getFixedPart() {
		return this;
	}


	@Override
	public Relationship getMutablePart() {
		return this;
	}


	@Override
	public List<? extends I_RelPart> getMutableParts() {
		return getVersions();
	}

	@Override
	protected void clearVersions() {
		versions = null;
	}

}
