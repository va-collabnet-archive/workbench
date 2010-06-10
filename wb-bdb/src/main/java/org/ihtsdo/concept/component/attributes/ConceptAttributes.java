package org.ihtsdo.concept.component.attributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PRECEDENCE;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EConceptAttributesRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ConceptAttributes 
		extends ConceptComponent<ConceptAttributesRevision, ConceptAttributes>
		implements I_ConceptAttributeVersioned,
				   I_ConceptAttributePart {

	private boolean defined;
		
	public ConceptAttributes(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}

	public ConceptAttributes(EConceptAttributes eAttr, Concept c) throws IOException {
		super(eAttr, c);
		defined = eAttr.isDefined();
		if (eAttr.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<ConceptAttributesRevision>();
			for (EConceptAttributesRevision ear: eAttr.getRevisionList()) {
				revisions.add(new ConceptAttributesRevision(ear, this));
			}
		}
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
				ConceptAttributes.this.setDefined(defined);
			}
		}

		@Override
		public ConceptAttributesRevision makeAnalog(int statusNid, int pathNid, long time) {
			if (index >= 0) {
			    ConceptAttributesRevision rev = revisions.get(index);
			    if (rev.getTime() == Long.MAX_VALUE && rev.getPathId() == pathNid) {
			        rev.setStatusId(statusNid);
			        return rev;
			    }
				return rev.makeAnalog(statusNid, pathNid, time);
			}
			return new ConceptAttributesRevision(ConceptAttributes.this, 
					statusNid, pathNid, time, ConceptAttributes.this);
		}
        @Override
        public ConceptAttributesRevision makeAnalog() {
            if (index >= 0) {
                ConceptAttributesRevision rev = revisions.get(index);
                return new ConceptAttributesRevision(rev, ConceptAttributes.this);
            }
            return new ConceptAttributesRevision(ConceptAttributes.this, ConceptAttributes.this);
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
					revisions = new CopyOnWriteArrayList<ConceptAttributesRevision>();
				}
				for (int i = 0; i < additionalVersionCount; i++) {
					ConceptAttributesRevision car = new ConceptAttributesRevision(input, this);
					if (car.getTime() != Long.MIN_VALUE) {
						revisions.add(car);
					}
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(" Processing nid: " + enclosingConceptNid, e);
		}
	}

	@Override
	public void writeToBdb(TupleOutput output,
			int maxReadOnlyStatusAtPositionNid) {
		AceLog.getAppLog().info("ConceptAttributes writeToBdb called for CA of "+this.toString());
		List<ConceptAttributesRevision> partsToWrite = new ArrayList<ConceptAttributesRevision>();
		AceLog.getAppLog().info("ConceptAttributes writeToBdb partsToWrite size = "+partsToWrite.size());
		if (revisions != null) {
			AceLog.getAppLog().info("ConceptAttributes writeToBdb revisions != null");
			for (ConceptAttributesRevision p : revisions) {
				AceLog.getAppLog().info("ConceptAttributes writeToBdb ConceptAttributesRevision p = "+p.toString());
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid &&
						p.getTime() != Long.MIN_VALUE) {
					AceLog.getAppLog().info("ConceptAttributes writeToBdb partsToWrite.add(p)");
					partsToWrite.add(p);
				}
			}
		}
		// Start writing
		output.writeBoolean(defined);
		output.writeShort(partsToWrite.size());
		for (ConceptAttributesRevision p : partsToWrite) {
			AceLog.getAppLog().info("ConceptAttributes writeToBdb writing parts)");
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
	
	
	
	@Override
	protected List<Version> getVersions() {
		List<Version> list = versions;
		if (list == null) {
			int count = 1;
			if (revisions != null) {
				count = count + revisions.size();
			}
			list = new ArrayList<Version>(count);
			list.add(new Version());
			if (revisions != null) {
				for (int i = 0; i < revisions.size(); i++) {
					list.add(new Version(i));
				}
			}
			versions = list;
		}
		return list;
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

	private static VersionComputer<ConceptAttributes.Version> computer = 
		new VersionComputer<ConceptAttributes.Version>();


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
		for (Version part : getVersions()) {
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
        buf.append("defined:" + this.defined);
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

    @Override
    public void addTuples(I_IntSet allowedStatus, PositionSetReadOnly positionSet,
            List<I_ConceptAttributeTuple> returnTuples, PRECEDENCE precedencePolicy,
            I_ManageContradiction contradictionManager) throws TerminologyException, IOException {
        List<Version> returnList = new ArrayList<Version>();
        computer.addSpecifiedVersions(allowedStatus, positionSet, returnList, 
            getVersions(), precedencePolicy, contradictionManager);
        returnTuples.addAll(returnList);
    }

	public List<Version> getTuples(I_IntSet allowedStatus,
			PositionSetReadOnly viewPositionSet, PRECEDENCE precedencePolicy, 
			I_ManageContradiction contradictionManager) {
		List<Version> returnList = new ArrayList<Version>();
        computer.addSpecifiedVersions(allowedStatus, viewPositionSet, returnList, 
            getVersions(), precedencePolicy, contradictionManager);
		return returnList;
	}


	public void addTuples(I_IntSet allowedStatus, I_Position viewPosition,
			List<Version> returnTuples, PRECEDENCE precedencePolicy, 
			I_ManageContradiction contradictionManager) {
		computer.addSpecifiedVersions(allowedStatus, viewPosition, returnTuples,
				getVersions(), precedencePolicy, contradictionManager);
	}

	public List<Version> getTuples(I_IntSet allowedStatus,
			I_Position viewPosition, PRECEDENCE precedencePolicy, I_ManageContradiction contradictionManager) {
		List<Version> returnList = new ArrayList<Version>();

		addTuples(allowedStatus, viewPosition, returnList, precedencePolicy,
		    contradictionManager);

		return returnList;
	}

	public boolean promote(I_Position viewPosition,
			PathSetReadOnly promotionPaths, I_IntSet allowedStatus, PRECEDENCE precedence) {
		int viewPathId = viewPosition.getPath().getConceptId();
		boolean promotedAnything = false;
		for (I_Path promotionPath : promotionPaths) {
			for (Version version : getTuples(allowedStatus,
					viewPosition, precedence, null)) {
				if (version.getPathId() == viewPathId) {
					ConceptAttributesRevision promotionPart = 
						version.makeAnalog(version.getStatusId(),
									promotionPath.getConceptId(),
									Long.MAX_VALUE);
					addRevision(promotionPart);
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
		return super.addRevision(new ConceptAttributesRevision(part, this));
	}

	@Override
	@Deprecated
	public List<Version> getTuples(I_IntSet allowedStatus,
			Set<I_Position> viewPositionSet) throws TerminologyException, IOException {
		return getTuples(allowedStatus, new PositionSetReadOnly(viewPositionSet), Terms.get().getActiveAceFrameConfig().getPrecedence(),
		    Terms.get().getActiveAceFrameConfig().getConflictResolutionStrategy());
	}

	@Override
	public boolean isDefined() {
		return defined;
	}

	@Override
	public void setDefined(boolean defined) {
		this.defined = defined;
        modified();
	}

	@Override
	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
	    if (getTime() == time && getPathId() == pathNid) {
	        throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
	    }
		ConceptAttributesRevision newR = new ConceptAttributesRevision(this, statusNid, pathNid, time, this);
		addRevision(newR);
		return newR;
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

	@Override
	protected void clearVersions() {
		versions = null;
	}

    @Override
    public boolean hasExtensions() throws IOException {
        if (getEnclosingConcept().hasAttributeExtensions()) {
            return getEnclosingConcept().hasExtensionsForComponent(nid);
        }
        return false;
    }


 	
}
