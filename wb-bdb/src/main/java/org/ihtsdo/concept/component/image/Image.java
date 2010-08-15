package org.ihtsdo.concept.component.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.utypes.UniversalAceImage;
import org.dwfa.ace.utypes.UniversalAceImagePart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.util.HashFunction;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.media.MediaAnalogBI;
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;
import org.ihtsdo.tk.dto.concept.component.media.TkMediaRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Image 
	extends ConceptComponent<ImageRevision, Image> 
	implements I_ImageVersioned, I_ImagePart, MediaAnalogBI {
	
	public class Version 
	extends ConceptComponent<ImageRevision, Image>.Version 
	implements I_ImageTuple, I_ImagePart, MediaAnalogBI {

		public Version() {
			super();
		}

		public Version(int index) {
			super(index);
		}

		@Override
		public int getConceptNid() {
			return enclosingConceptNid;
		}

		@Override
		public String getFormat() {
			return format;
		}

		@Override
		public byte[] getImage() {
			return image;
		}

		@Override
		public byte[] getMedia() {
			return image;
		}

		@Override
		public int getImageId() {
			return nid;
		}

		@Override
		public String getTextDescription() {
			if (index >= 0) {
				return revisions.get(index).getTextDescription();
			}
			return textDescription;
		}

		@Override
		public I_ImageVersioned getVersioned() {
			return Image.this;
		}

		@Override
		public Image.Version getVersion(Coordinate c)
				throws ContraditionException {
			return Image.this.getVersion(c);
		}

		@Override
		public Collection<Image.Version> getVersions(
				Coordinate c) {
			return Image.this.getVersions(c);
		}		
		
	    public List<? extends Version> getVersions() {
	    	return Image.this.getVersions();
	    }

		@Override
		public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
			throw new UnsupportedOperationException();
		}

		@Override
		@Deprecated
		public int getTypeId() {
			if (index >= 0) {
				return revisions.get(index).getTypeId();
			}
			return typeNid;
		}

		@Override
		@Deprecated
		public void setTypeId(int type) {
			if (index >= 0) {
				revisions.get(index).setTypeId(type);
			}
			Image.this.setTypeNid(type);
		}

		@Override
		public int getTypeNid() {
			if (index >= 0) {
				return revisions.get(index).getTypeNid();
			}
			return typeNid;
		}

		@Override
		public void setTypeNid(int type) {
			if (index >= 0) {
				revisions.get(index).setTypeNid(type);
			}
			Image.this.setTypeNid(type);
		}

		public ArrayIntList getVariableVersionNids() {
			if (index >= 0) {
				ArrayIntList resultList = new ArrayIntList(3);
				resultList.add(getTypeId());
				return resultList;
			}
			return Image.this.getVariableVersionNids();
		}

		@Override
		public ImageRevision makeAnalog(int statusNid, int pathNid, long time) {
			if (index >= 0) {
                ImageRevision rev = revisions.get(index);
                if (rev.getTime() == Long.MAX_VALUE && rev.getPathNid() == pathNid) {
			        rev.setStatusNid(statusNid);
                    return rev;
                }
                return rev.makeAnalog(statusNid, pathNid, time);
			}
			return Image.this.makeAnalog(statusNid, pathNid, time);
		}

		@Override
		public ImageRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
			if (index >= 0) {
                ImageRevision rev = revisions.get(index);
                if (rev.getTime() == Long.MAX_VALUE && rev.getPathNid() == pathNid) {
			        rev.setStatusNid(statusNid);
			        rev.setAuthorNid(authorNid);
                    return rev;
                }
                return rev.makeAnalog(statusNid, authorNid, pathNid, time);
			}
			return Image.this.makeAnalog(statusNid, authorNid, pathNid, time);
		}
        @Override
        public ImageRevision makeAnalog() {
            if (index >= 0) {
                ImageRevision rev = revisions.get(index);
                return new ImageRevision(rev, Image.this);
            }
            return new ImageRevision(Image.this);
        }
		@Override
		public I_ImagePart getMutablePart() {
			return (I_ImagePart) super.getMutablePart();
		}
		
		@Override
		@Deprecated
		public I_ImagePart duplicate() {
			throw new UnsupportedOperationException("Use makeAnalog instead");
		}

		@Override
		public void setTextDescription(String name) {
			if (index >= 0) {
				revisions.get(index).setTextDescription(name);
			}
			Image.this.setTextDescription(name);
		}

	}

	private static VersionComputer<Image.Version> computer = 
		new VersionComputer<Image.Version>();

	private String format;
	private byte[] image;
	
	private String textDescription;
	private int typeNid;

	protected Image(Concept enclosingConcept, TupleInput input) throws IOException {
		super(enclosingConcept, input);
	}

	public Image(TkMedia eMedia, Concept enclosingConcept) throws IOException {
		super(eMedia, enclosingConcept);
		image = eMedia.getImage();
		format = eMedia.getFormat();
		textDescription = eMedia.getTextDescription();
		typeNid = Bdb.uuidToNid(eMedia.getTypeUuid());
		primordialSapNid = Bdb.getSapNid(eMedia);
		if (eMedia.getRevisionList() != null) {
			revisions = new CopyOnWriteArrayList<ImageRevision>();
			for (TkMediaRevision eiv: eMedia.getRevisionList()) {
				revisions.add(new ImageRevision(eiv, this));
			}
		}
	}

	public Image() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    StringBuffer buf = new StringBuffer();  
	    buf.append(this.getClass().getSimpleName() + ":{");
	    buf.append("format:" + "'" + this.format + "'");
	    buf.append(" image:" + this.image);
	    buf.append(" textDescription:" + "'" + this.textDescription + "'");
	    buf.append(" typeNid:");
        ConceptComponent.addNidToBuffer(buf, typeNid);
	    buf.append(" ");
	    buf.append(super.toString());
	    return buf.toString();
	}


    //TODO Verify this is a correct implementation 
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (Image.class.isAssignableFrom(obj.getClass())) {
            Image another = (Image) obj;
            if (this.nid == another.nid) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] {this.getNid() });
    }

    @Override
	public boolean fieldsEqual(ConceptComponent<ImageRevision, Image> obj) {
		if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
			Image another = (Image) obj;
			if (!this.format.equals(another.format)) {
				return false;
			}
			if (!Arrays.equals(this.image, another.image)) {
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
    public String validate(Image another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (!this.format.equals(another.format)) {
            buf.append("\tImage.format not equal: \n" + 
                "\t\tthis.format = " + this.format + "\n" + 
                "\t\tanother.format = " + another.format + "\n");
        }
        if (!Arrays.equals(this.image, another.image)) {
            buf.append("\tImage.image not equal: \n" + 
                "\t\tthis.image = " + this.image + "\n" + 
                "\t\tanother.image = " + another.image + "\n");
        }
        if (this.typeNid != another.typeNid) {
            buf.append("\tImage.typeNid not equal: \n" + 
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
		this.format = input.readString();
		int imageBytes = input.readInt();
		image = new byte[imageBytes];
		input.read(image, 0, imageBytes);
		textDescription = input.readString();
		typeNid = input.readInt();
		int additionalVersionCount = input.readShort();
		for (int i = 0; i < additionalVersionCount; i++) {
			ImageRevision ir = new ImageRevision(input, this);
			if (ir.getTime() != Long.MIN_VALUE) {
				revisions.add(ir);
			}
		}
	}

	@Override
	public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		List<ImageRevision> partsToWrite = new ArrayList<ImageRevision>();
		if (revisions != null) {
			for (ImageRevision p: revisions) {
				if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid &&
						p.getTime() != Long.MIN_VALUE) {
					partsToWrite.add(p);
				}
			}
		}
		// Start writing
		// conceptNid is the enclosing concept, does not need to be written. 
		output.writeString(format);
		output.writeInt(image.length);
		output.write(image);
		output.writeString(textDescription);
		output.writeInt(typeNid);
		output.writeShort(partsToWrite.size());
		for (ImageRevision p: partsToWrite) {
			p.writePartToBdb(output);
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getImage()
	 */
	public byte[] getImage() {
		return image;
	}

	public byte[] getMedia() {
		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getImageId()
	 */
	public int getImageId() {
		return nid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getFormat()
	 */
	public String getFormat() {
		return format;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getConceptNid()
	 */
	public int getConceptNid() {
		return enclosingConceptNid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dwfa.vodb.types.I_ImageVersioned#getLastTuple()
	 */
	public Version getLastTuple() {
		List<Version> vList = getTuples();
		return vList.get(vList.size() - 1);
	}

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
			if (getTime() != Long.MIN_VALUE) {
				list.add(new Version());
			}
			if (revisions != null) {
				for (int i = 0; i < revisions.size(); i++) {
					if (revisions.get(i).getTime() != Long.MIN_VALUE) {
						list.add(new Version(i));
					}
				}
			}
			versions = list;
		}
		return versions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.dwfa.vodb.types.I_ImageVersioned#convertIds(org.dwfa.vodb.jar.
	 * I_MapNativeToNative)
	 */
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<I_ImageTuple> matchingTuples, 
			Precedence precedencePolicy, I_ManageContradiction contradictionManager) {
		List<Version> returnTuples = new ArrayList<Version>();
		computer.addSpecifiedVersions(allowedStatus, allowedTypes, positions, 
				returnTuples, getVersions(), precedencePolicy, contradictionManager);
		matchingTuples.addAll(returnTuples);
	}

	public Collection<Image.Version> getVersions(I_IntSet allowedStatus, 
			I_IntSet allowedTypes, PositionSetReadOnly viewPositions,  
			Precedence precedence, I_ManageContradiction contradictionMgr) {
		List<Version> returnTuples = new ArrayList<Version>(2);
		computer.addSpecifiedVersions(allowedStatus, allowedTypes, viewPositions,
				returnTuples, getVersions(), precedence, contradictionMgr);
		return returnTuples;
	}

	private static Collection<UUID> getUids(int id) throws IOException,
			TerminologyException {
		return LocalFixedTerminology.getStore().getUids(id);
	}

	public UniversalAceImage getUniversal() throws IOException,
			TerminologyException {
		UniversalAceImage universal = new UniversalAceImage(getUids(nid),
				getImage(), new ArrayList<UniversalAceImagePart>(revisions
						.size()), getFormat(), getEnclosingConcept().getUids());

		for (org.ihtsdo.concept.component.image.Image.Version part : getVersions()) {
			UniversalAceImagePart universalPart = new UniversalAceImagePart();
			universalPart.setPathId(getUids(part.getPathNid()));
			universalPart.setStatusId(getUids(part.getStatusNid()));
			universalPart.setTextDescription(part.getTextDescription());
			universalPart.setTypeId(getUids(part.getTypeId()));
			universalPart.setTime(part.getTime());
			universal.addVersion(universalPart);
		}
		return universal;
	}

	public boolean promote(PositionBI viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus, Precedence precedence) {
		int viewPathId = viewPosition.getPath().getConceptNid();
		List<Version> matchingTuples = new ArrayList<Version>();
		computer.addSpecifiedVersions(allowedStatus, viewPosition, matchingTuples, 
				getTuples(), precedence, null);
		boolean promotedAnything = false;
		for (PathBI promotionPath : pomotionPaths) {
			for (Version it : matchingTuples) {
				if (it.getPathNid() == viewPathId) {
					ImageRevision promotionPart = (ImageRevision) it.makeAnalog(
							it.getStatusNid(), promotionPath.getConceptNid(),
							Long.MAX_VALUE);
					it.getVersioned().addVersion(promotionPart);
					promotedAnything = true;
				}
			}
		}
		return promotedAnything;
	}

	@Override
	public boolean addVersion(I_ImagePart part) {
		this.versions = null;
		return super.addRevision((ImageRevision) part);
	}

	@Override
	public ArrayIntList getVariableVersionNids() {
		ArrayIntList nidList = new ArrayIntList(3);
		nidList.add(typeNid);
		return nidList;
	}

	@Override
	public String getTextDescription() {
		return textDescription;
	}

	@Override
	public void setTextDescription(String textDescription) {
		this.textDescription = textDescription;
        modified();
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
	public ImageRevision makeAnalog(int statusNid, int pathNid, long time) {
        if (getTime() == time && getPathNid() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
		ImageRevision newR;
			newR = new ImageRevision(this, statusNid,
					Terms.get().getAuthorNid(),
					pathNid, time, this);
		addRevision(newR);
		return newR;
	}

	@Override
	public ImageRevision makeAnalog(int statusNid, int authorNid, int pathNid, long time) {
        if (getTime() == time && getPathNid() == pathNid) {
            throw new UnsupportedOperationException("Cannot make an analog on same time and path...");
        }
		ImageRevision newR;
			newR = new ImageRevision(this, statusNid,
					authorNid,
					pathNid, time, this);
		addRevision(newR);
		return newR;
	}

	@Override
	public Image getMutablePart() {
		return this;
	}

	@Override
	public Image duplicate() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<? extends I_ImagePart> getMutableParts() {
			return getTuples();
	}

	@Override
	protected void clearVersions() {
		versions = null;
	}

	public int getTypeNid() {
		return typeNid;
	}

	public void setTypeNid(int typeNid) {
		this.typeNid = typeNid;
        modified();
	}

	public void setFormat(String format) {
		this.format = format;
        modified();
	}

	public void setImage(byte[] image) {
		this.image = image;
        modified();
	}

    @Override
    public boolean hasExtensions() throws IOException {
        if (getEnclosingConcept().hasMediaExtensions()) {
            return getEnclosingConcept().hasExtensionsForComponent(nid);
        }
        return false;
    }


	@Override
	public Image.Version getVersion(Coordinate c)
			throws ContraditionException {
		List<Image.Version> vForC = getVersions(c);
		if (vForC.size() == 0) {
			return null;
		}
		if (vForC.size() > 1) {
			throw new ContraditionException(vForC.toString());
		}
		return vForC.get(0);
	}

	@Override
	public List<Image.Version> getVersions(Coordinate c) {
		List<Version> returnTuples = new ArrayList<Version>(2);
		computer.addSpecifiedVersions(c.getAllowedStatusNids(), (NidSetBI) null, c.getPositionSet(),
				returnTuples, getVersions(), c.getPrecedence(), c.getContradictionManager());
		return returnTuples;
	}

}
