package org.ihtsdo.concept.component.description;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedDesc;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EDescription;
import org.ihtsdo.etypes.EDescriptionRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Description 
	extends ConceptComponent<DescriptionRevision, Description> 
	implements I_DescriptionVersioned, I_DescriptionPart {

	private static VersionComputer<Description.Version> computer = 
		new VersionComputer<Description.Version>();
	
	public class Version 
		extends ConceptComponent<DescriptionRevision, Description>.Version 
		implements I_DescriptionTuple, I_DescriptionPart {

		public Version() {
			super();
		}

		public Version(int index) {
			super(index);
		}

		@Override
		public int getConceptId() {
			return enclosingConceptNid;
		}

		@Override
		public int getDescId() {
			return nid;
		}

		@Override
		public I_DescriptionVersioned getDescVersioned() {
			return Description.this;
		}

		@Override
		public String getLang() {
			if (index >= 0) {
				return revisions.get(index).getLang();
			}
			return Description.this.lang;
		}

		@Override
		public String getText() {
			if (index >= 0) {
				return revisions.get(index).getText();
			}
			return Description.this.text;
		}

		@Override
		public boolean isInitialCaseSignificant() {
			if (index >= 0) {
				return revisions.get(index).isInitialCaseSignificant();
			}
			return Description.this.initialCaseSignificant;
		}

		@Override
		public void setInitialCaseSignificant(boolean capStatus) {
			if (index >= 0) {
				revisions.get(index).setInitialCaseSignificant(capStatus);
			} else {
				Description.this.setInitialCaseSignificant(capStatus);
			}
		}

		@Override
		public void setLang(String lang) {
			if (index >= 0) {
				revisions.get(index).setLang(lang);
			} else {
				Description.this.setLang(lang);
			}
		}

		@Override
		public void setText(String text) {
			if (index >= 0) {
				revisions.get(index).setText(text);
			} else {
				Description.this.setText(text);
			}
		}

		@Override
		public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getTypeId() {
			if (index >= 0) {
				assert revisions.get(index).getTypeId() != Integer.MAX_VALUE: Description.this;
				return revisions.get(index).getTypeId();
			} else {
				assert Description.this.typeNid != Integer.MAX_VALUE: Description.this;
				return Description.this.typeNid;
			}
		}

		@Override
		public void setTypeId(int type) {
			if (index >= 0) {
				revisions.get(index).setTypeId(type);
			} else {
				Description.this.setTypeId(type);
			}
		}

		public ArrayIntList getVariableVersionNids() {
			if (index >= 0) {
				ArrayIntList resultList = new ArrayIntList(3);
				resultList.add(getTypeId());
				return resultList;
			}
			return Description.this.getVariableVersionNids();
		}

		@Override
		public I_DescriptionPart getMutablePart() {
			return (I_DescriptionPart) super.getMutablePart();
		}
		@Override
		public DescriptionRevision makeAnalog(int statusNid, int pathNid, long time) {
			if (index >= 0) {
				return revisions.get(index).makeAnalog(statusNid, pathNid, time);
			} else {
				return Description.this.makeAnalog(statusNid, pathNid, time);
			}
		}
		@Override
		@Deprecated
		public I_DescriptionPart duplicate() {
			throw new UnsupportedOperationException("Use makeAnalog instead");
		}
		
	}
	
	private String text;
	private boolean initialCaseSignificant;
	private int typeNid; 
	private String lang;

	
	public Description(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}
	
	public Description(EDescription eDesc, Concept enclosingConcept) throws IOException {
		super(eDesc, enclosingConcept);
		initialCaseSignificant = eDesc.isInitialCaseSignificant();
		lang = eDesc.getLang();
		text = eDesc.getText();
		typeNid = Bdb.uuidToNid(eDesc.getTypeUuid());
		primordialSapNid = Bdb.getSapNid(eDesc);
		if (eDesc.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<DescriptionRevision>();
			for (EDescriptionRevision edv: eDesc.getRevisionList()) {
				revisions.add(new DescriptionRevision(edv, this));
			}
		}
	}

    public Description() {
		super();
	}

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append("text: '" + this.getText() + "'");
        buf.append(" caseSig: " + isInitialCaseSignificant());
        buf.append(" type:");
        ConceptComponent.addNidToBuffer(buf, typeNid);
        buf.append(" lang:" + this.getLang());
        buf.append(" ");
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean fieldsEqual(ConceptComponent<DescriptionRevision, Description> obj) {
        if (Description.class.isAssignableFrom(obj.getClass())) {
            Description another = (Description) obj;
            if (this.initialCaseSignificant != another.initialCaseSignificant) {
                return false;
            }
            if (!this.text.equals(another.text)) {
                return false;
            }
            if (!this.lang.equals(another.lang)) {
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
    public String validate(Description another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (this.initialCaseSignificant != another.initialCaseSignificant) {
            buf.append("\tDescription.initialCaseSignificant not equal: \n" + 
                "\t\tthis.initialCaseSignificant = " + this.initialCaseSignificant + "\n" + 
                "\t\tanother.initialCaseSignificant = " + another.initialCaseSignificant + "\n");
        }
        if (!this.text.equals(another.text)) {
            buf.append("\tDescription.text not equal: \n" + 
                "\t\tthis.text = " + this.text + "\n" + 
                "\t\tanother.text = " + another.text + "\n");
        }
        if (!this.lang.equals(another.lang)) {
            buf.append("\tDescription.lang not equal: \n" + 
                "\t\tthis.lang = " + this.lang + "\n" + 
                "\t\tanother.lang = " + another.lang + "\n");
        }
        if (this.typeNid != another.typeNid) {
            buf.append("\tDescription.typeNid not equal: \n" + 
                "\t\tthis.typeNid = " + this.typeNid + "\n" + 
                "\t\tanother.typeNid = " + another.typeNid + "\n");
        }

        // Compare the parents 
        buf.append(super.validate(another));
        
        return buf.toString();
    }

    
    @Override
	public void readFromBdb(TupleInput input) {
		initialCaseSignificant = input.readBoolean();
		lang = input.readString();
		text = input.readString();
		typeNid = input.readInt();
		// nid, list size, and conceptNid are read already by the binder...
		int additionalVersionCount = input.readShort();
		if (additionalVersionCount > 0) {
			revisions = new CopyOnWriteArrayList<DescriptionRevision>();
			for (int i = 0; i < additionalVersionCount; i++) {
				DescriptionRevision dr = new DescriptionRevision(input, this);
				if (dr.getTime() != Long.MIN_VALUE) {
					revisions.add(dr);
				}
			}
		}
	}

	@Override
	public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		List<DescriptionRevision> partsToWrite = new ArrayList<DescriptionRevision>();
		if (revisions != null) {
			for (DescriptionRevision p: revisions) {
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid &&
						p.getTime() != Long.MIN_VALUE) {
					partsToWrite.add(p);
				}
			}
		}
		
		output.writeBoolean(initialCaseSignificant);
		output.writeString(lang);
		output.writeString(text);
		output.writeInt(typeNid);
		output.writeShort(partsToWrite.size());
		// conceptNid is the enclosing concept, does not need to be written. 
		for (DescriptionRevision p: partsToWrite) {
			p.writePartToBdb(output);
		}

	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getConceptId() {
		return enclosingConceptNid;
	}

	@Override
	public int getDescId() {
		return nid;
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
	public List<Version> getVersions(
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		return getTuples(returnConflictResolvedLatestState);
	}

	@Override
	public List<Version> getTuples(
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		return getVersions();
	}

	@Override
	public UniversalAceDescription getUniversal() throws IOException,
			TerminologyException {
		UniversalAceDescription universal = new UniversalAceDescription(this);
		return universal;
	}

	@Override
	public boolean matches(Pattern p) {
		String lastText = null;
		for (Description.Version desc : getVersions()) {
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

	@Override
	public I_DescribeConceptLocally toLocalFixedDesc() {
        return new LocalFixedDesc(nid, getStatusId(), getConceptId(), isInitialCaseSignificant(),
            getTypeId(), getText(), getLang());
	}

	@Override
	public boolean addVersion(I_DescriptionPart newPart) {
		this.versions = null;
		BdbCommitManager.addUncommittedDescNid(nid);
		return super.addRevision((DescriptionRevision) newPart);
	}

	public int getConceptNid() {
		return enclosingConceptNid;
	}

	/*
	 * Consider depreciating the below methods...
	 */
	
	List<Version> versions;
	

	public List<Version> getTuples() {
		return Collections.unmodifiableList(new ArrayList<Version>(getVersions()));
	}

	@Override
    public List<Version> getVersions() {
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

	public void addTuples(I_IntSet allowedStatus, I_Position viewPosition,
			List<Description.Version> matchingTuples) {
		computer.addSpecifiedVersions(allowedStatus, viewPosition,
				matchingTuples, getVersions());
	}

	@Override
	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<I_DescriptionTuple> matchingTuples,
			boolean addUncommitted) {
		List<Version> returnTuples = new ArrayList<Version>();
		computer.addSpecifiedVersions(allowedStatus, allowedTypes, positions,
				returnTuples, addUncommitted, getVersions());
		matchingTuples.addAll(returnTuples);
	}

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<I_DescriptionTuple> matchingTuples,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<Version> returnTuples = new ArrayList<Version>();

		computer.addSpecifiedVersions(allowedStatus, allowedTypes, positions,
				returnTuples, addUncommitted, getVersions());

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

			returnTuples = conflictResolutionStrategy.resolveTuples(returnTuples);
		}
		matchingTuples.addAll(returnTuples);
	}


	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
        modified();
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
        modified();
	}

	public boolean isInitialCaseSignificant() {
		return initialCaseSignificant;
	}

	public void setInitialCaseSignificant(boolean initialCaseSignificant) {
		this.initialCaseSignificant = initialCaseSignificant;
        modified();
	}

	@Override
	public ArrayIntList getVariableVersionNids() {
		ArrayIntList nidList = new ArrayIntList(3);
		nidList.add(typeNid);
		return nidList;
	}

	@Override
	public int getTypeId() {
		return typeNid;
	}

	@Override
	public void setTypeId(int typeNid) {
		this.typeNid = typeNid;
        modified();
	}

	@Override
	public DescriptionRevision makeAnalog(int statusNid, int pathNid, long time) {
		DescriptionRevision newR = new DescriptionRevision(this, statusNid, pathNid, time, this);
		addRevision(newR);
		return newR;
	}
	
	@Override
	public I_DescriptionPart duplicate() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public I_DescriptionPart getMutablePart() {
		return this;
	}

	@Override
	public List<? extends I_DescriptionPart> getMutableParts() {
		return new EditableVersionList(getVersions());
	}
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (Description.class.isAssignableFrom(obj.getClass())) {
            Description another = (Description) obj;
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

	@Override
	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
			throws IOException, TerminologyException {
        int viewPathId = viewPosition.getPath().getConceptId();
        Collection<Version> matchingTuples = computer.
        	getSpecifiedVersions(allowedStatus, 
        			viewPosition, 
        			getVersions());
        boolean promotedAnything = false;
        for (I_Path promotionPath : pomotionPaths) {
            for (Version v : matchingTuples) {
                if (v.getPathId() == viewPathId) {
                	
                    DescriptionRevision revision =  v.makeAnalog(v.getStatusId(), 
							promotionPath.getConceptId(), Long.MAX_VALUE);
                    addRevision(revision);
                    promotedAnything = true;
                }
            }
        }
        return promotedAnything;
	}

	@Override
	protected void clearVersions() {
		versions = null;
	}

}
