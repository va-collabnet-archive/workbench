package org.ihtsdo.concept.component.description;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class DescriptionRevision 
	extends Revision<DescriptionRevision, Description> 
	implements I_DescriptionPart, DescriptionAnalogBI {
	
	@SuppressWarnings("unused")
	private static Charset utf8 = Charset.forName("UTF-8");
	
	private String text;
	private boolean initialCaseSignificant;
	private int typeNid; 
	private String lang;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        
        buf.append(this.getClass().getSimpleName() + ":{");
        buf.append(" text:" + "'" + this.getText() + "'");
        buf.append(" initialCaseSignificant:" + isInitialCaseSignificant());
        buf.append(" typeNid:");
        ConceptComponent.addNidToBuffer(buf, typeNid);
        buf.append(" lang:" + this.getLang());
        buf.append(super.toString());
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (DescriptionRevision.class.isAssignableFrom(obj.getClass())) {
            DescriptionRevision another = (DescriptionRevision) obj;
            return this.sapNid == another.sapNid;
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
    public String validate(DescriptionRevision another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        
        if (this.initialCaseSignificant != another.initialCaseSignificant) {
            buf.append("\tDescriptionRevision.initialCaseSignificant not equal: \n" + 
                "\t\tthis.initialCaseSignificant = " + this.initialCaseSignificant + "\n" + 
                "\t\tanother.initialCaseSignificant = " + another.initialCaseSignificant + "\n");
        }
        if (!this.text.equals(another.text)) {
            buf.append("\tDescriptionRevision.text not equal: \n" + 
                "\t\tthis.text = " + this.text + "\n" + 
                "\t\tanother.text = " + another.text + "\n");
        }
        if (!this.lang.equals(another.lang)) {
            buf.append("\tDescriptionRevision.lang not equal: \n" + 
                "\t\tthis.lang = " + this.lang + "\n" + 
                "\t\tanother.lang = " + another.lang + "\n");
        }
        if (this.typeNid != another.typeNid) {
            buf.append("\tDescriptionRevision.typeNid not equal: \n" + 
                "\t\tthis.typeNid = " + this.typeNid + "\n" + 
                "\t\tanother.typeNid = " + another.typeNid + "\n");
        }

        // Compare the parents 
        buf.append(super.validate(another));
        
        return buf.toString();
    }
    
	public DescriptionRevision(int statusAtPositionNid, 
			Description primoridalMember) {
		super(statusAtPositionNid, primoridalMember);
	}
    protected DescriptionRevision(Description primoridalMember) {
        super(primoridalMember.primordialSapNid, primoridalMember);
        this.text = primoridalMember.getText();
        this.typeNid = primoridalMember.typeNid;
        this.lang = primoridalMember.getLang();
        this.initialCaseSignificant = primoridalMember.isInitialCaseSignificant();
    }
	
	protected DescriptionRevision(DescriptionRevision another, 
			Description primoridalMember) {
		super(another.getStatusAtPositionNid(), primoridalMember);
		this.text = another.text;
		this.typeNid = another.typeNid;
		this.lang = another.lang;
		this.initialCaseSignificant = another.initialCaseSignificant;
	}

	protected DescriptionRevision(I_DescriptionPart another, 
			int statusNid, int authorNid, int pathNid, long time, 
			Description primoridalMember) {
		super(statusNid, authorNid, pathNid, time, primoridalMember);
		this.text = another.getText();
		this.typeNid = another.getTypeNid();
		this.lang = another.getLang();
		this.initialCaseSignificant = another.isInitialCaseSignificant();
	}

	protected DescriptionRevision(TupleInput input, 
			Description primoridalMember) {
		super(input, primoridalMember);
		text = input.readString();
		if (text == null) {
			text = primoridalMember.getText();
		}
		lang = input.readString();
		if (lang == null) {
			lang = primoridalMember.getLang();
		}
		initialCaseSignificant = input.readBoolean();
		typeNid = input.readInt();
	}

	public DescriptionRevision(UniversalAceDescriptionPart umPart, 
			Description primoridalMember) throws TerminologyException, IOException {
		super(Bdb.uuidsToNid(umPart.getStatusId()),
				Terms.get().getAuthorNid(),
				Bdb.uuidsToNid(umPart.getPathId()),
				umPart.getTime(), primoridalMember);
		text = umPart.getText();
		lang = umPart.getLang();
		initialCaseSignificant = umPart.getInitialCaseSignificant();
		typeNid = Bdb.uuidsToNid(umPart.getTypeId());
	}

	public DescriptionRevision(TkDescriptionRevision edv, 
			Description primoridalMember) throws TerminologyException, IOException {
		super(Bdb.uuidToNid(edv.getStatusUuid()),
				Terms.get().getAuthorNid(),
				Bdb.uuidToNid(edv.getPathUuid()),
				edv.getTime(), primoridalMember);
		initialCaseSignificant = edv.isInitialCaseSignificant();
		lang = edv.getLang();
		text = edv.getText();
		typeNid = Bdb.uuidToNid(edv.getTypeUuid());
		sapNid = Bdb.getSapNid(edv);
	}

    public DescriptionRevision() {
        super();
    }
    
    @Override
	protected void writeFieldsToBdb(TupleOutput output) {
		if (text.equals(primordialComponent.getText())) {
			output.writeString((String) null);
		} else {
			output.writeString(text);
		}
		if (lang.equals(primordialComponent.getLang())) {
			output.writeString((String) null);
		} else {
			output.writeString(lang);
		}
		output.writeBoolean(initialCaseSignificant);
		output.writeInt(typeNid);
	}

	@Override
	public boolean isInitialCaseSignificant() {
		return initialCaseSignificant;
	}

	@Override
	public String getLang() {
		return lang;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setInitialCaseSignificant(boolean capStatus) {
		initialCaseSignificant = capStatus;
        modified();
	}

	@Override
	public void setLang(String lang) {
		this.lang = lang;
        modified();
	}

	@Override
	public void setText(String text) {
		this.text = text;
        modified();
	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public int getTypeId() {
		return typeNid;
	}
	@Override
	public int getTypeNid() {
		return typeNid;
	}

	@Override
	@Deprecated
	public void setTypeId(int typeNid) {
		this.typeNid = typeNid;
        modified();
	}
	public void setTypeNid(int typeNid) {
		this.typeNid = typeNid;
        modified();
	}

	@Override
	public DescriptionRevision duplicate() {
		return new DescriptionRevision(this, this.primordialComponent);
	}

	@Override
	public DescriptionRevision makeAnalog(int statusNid, int pathNid, 
			long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            return this;
        }
		try {
			return new DescriptionRevision(this, statusNid, 
					Terms.get().getAuthorNid(),
					pathNid, time, this.primordialComponent);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public DescriptionRevision makeAnalog(int statusNid, int authorNid, int pathNid, 
			long time) {
        if (this.getTime() == time && this.getPathNid() == pathNid) {
            this.setStatusNid(statusNid);
            this.setAuthorNid(authorNid);
            return this;
        }
		try {
			return new DescriptionRevision(this, statusNid, 
					authorNid,
					pathNid, time, this.primordialComponent);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getConceptNid() {
		return primordialComponent.getConceptNid();
	}

	@Override
    public ArrayIntList getVariableVersionNids() {
		ArrayIntList list = new ArrayIntList(3);
		list.add(typeNid);
		return list;
	}
	
	@Override
	public Description.Version getVersion(Coordinate c)
			throws ContraditionException {
		return primordialComponent.getVersion(c);
	}

	@Override
	public Collection<Description.Version> getVersions(
			Coordinate c) {
		return primordialComponent.getVersions(c);
	}		

    @Override
	public String toUserString() {
        StringBuffer buf = new StringBuffer();
        ConceptComponent.addTextToBuffer(buf, typeNid);
        buf.append(": ");
        buf.append("'" + this.getText() + "'");
        return buf.toString();
	}
}
