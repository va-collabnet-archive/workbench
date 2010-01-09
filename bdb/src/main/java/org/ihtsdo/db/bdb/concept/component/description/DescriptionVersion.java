package org.ihtsdo.db.bdb.concept.component.description;

import java.nio.charset.Charset;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.Version;
import org.ihtsdo.etypes.EDescriptionVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class DescriptionVersion 
	extends Version<DescriptionVersion, Description> 
	implements I_DescriptionPart, I_DescriptionTuple {
	
	@SuppressWarnings("unused")
	private static Charset utf8 = Charset.forName("UTF-8");
	
	private transient Description description;
	
	private String text;
	private boolean initialCaseSignificant;
	private int typeNid; 
	private String lang;

	public String toString() {
		return " text: " + text + " cs: " + initialCaseSignificant 
		+ " typeNid: " + typeNid + " lang: " + lang + " " + super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (DescriptionVersion.class.isAssignableFrom(obj.getClass())) {
			DescriptionVersion another = (DescriptionVersion) obj;
			if (this.initialCaseSignificant != another.initialCaseSignificant) {
				return false;
			}
			if (this.typeNid != another.typeNid) {
				return false;
			}
			if (!this.text.equals(another.text)) {
				return false;
			}
			if (!this.lang.equals(another.lang)) {
				return false;
			}
			return super.equals(obj);
		}
		return false;
	}

	public DescriptionVersion(int statusAtPositionNid, 
			Description primoridalMember) {
		super(statusAtPositionNid, primoridalMember);
	}
	
	protected DescriptionVersion(DescriptionVersion another, 
			Description primoridalMember) {
		super(another.getStatusAtPositionNid(), primoridalMember);
		this.text = another.text;
		this.typeNid = another.typeNid;
		this.lang = another.lang;
		this.initialCaseSignificant = another.initialCaseSignificant;
	}

	protected DescriptionVersion(I_DescriptionPart another, 
			int statusNid, int pathNid, long time, 
			Description primoridalMember) {
		super(statusNid, pathNid, time, primoridalMember);
		this.text = another.getText();
		this.typeNid = another.getTypeId();
		this.lang = another.getLang();
		this.initialCaseSignificant = another.isInitialCaseSignificant();
	}

	protected DescriptionVersion(TupleInput input, 
			Description primoridalMember) {
		super(input.readInt(), primoridalMember);
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

	public DescriptionVersion(UniversalAceDescriptionPart umPart, 
			Description primoridalMember) {
		super(Bdb.uuidsToNid(umPart.getStatusId()),
				Bdb.uuidsToNid(umPart.getPathId()),
				umPart.getTime(), primoridalMember);
		text = umPart.getText();
		lang = umPart.getLang();
		initialCaseSignificant = umPart.getInitialCaseSignificant();
		typeNid = Bdb.uuidsToNid(umPart.getTypeId());
	}

	public DescriptionVersion(EDescriptionVersion edv, 
			Description primoridalMember) {
		super(Bdb.uuidToNid(edv.getStatusUuid()),
				Bdb.uuidToNid(edv.getPathUuid()),
				edv.getTime(), primoridalMember);
		initialCaseSignificant = edv.isInitialCaseSignificant();
		lang = edv.getLang();
		text = edv.getText();
		typeNid = Bdb.uuidToNid(edv.getTypeUuid());
		statusAtPositionNid = Bdb.getStatusAtPositionNid(edv);
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
	}

	@Override
	public void setLang(String lang) {
		this.lang = lang;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getTypeId() {
		return typeNid;
	}

	@Override
	public void setTypeId(int typeNid) {
		this.typeNid = typeNid;
	}

	@Override
	public DescriptionVersion duplicate() {
		return new DescriptionVersion(this, this.primordialComponent);
	}

	@Override
	public DescriptionVersion makeAnalog(int statusNid, int pathNid, 
			long time) {
		return new DescriptionVersion(this, statusNid, 
				pathNid, time, this.primordialComponent);
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		ArrayIntList list = new ArrayIntList(3);
		list.add(typeNid);
		return list;
	}

	@Override
	public int getConceptId() {
		return description.getConceptNid();
	}

	@Override
	public int getDescId() {
		return description.nid;
	}

	@Override
	public Description getDescVersioned() {
		return description;
	}

	@Override
	public I_DescriptionPart getMutablePart() {
		return this;
	}

}
