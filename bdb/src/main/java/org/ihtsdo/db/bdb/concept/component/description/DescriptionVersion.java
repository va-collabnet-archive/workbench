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

	public DescriptionVersion(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}
	
	protected DescriptionVersion(DescriptionVersion another) {
		super(another.getStatusAtPositionNid());
		this.text = another.text;
		this.typeNid = another.typeNid;
		this.lang = another.lang;
		this.initialCaseSignificant = another.initialCaseSignificant;
	}

	protected DescriptionVersion(I_DescriptionPart another, int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
		this.text = another.getText();
		this.typeNid = another.getTypeId();
		this.lang = another.getLang();
		this.initialCaseSignificant = another.isInitialCaseSignificant();
	}

	protected DescriptionVersion(TupleInput input) {
		super(input.readInt());
		text = input.readString();
		lang = input.readString();
		initialCaseSignificant = input.readBoolean();
		typeNid = input.readInt();
	}

	public DescriptionVersion(UniversalAceDescriptionPart umPart) {
		super(Bdb.uuidsToNid(umPart.getStatusId()),
				Bdb.uuidsToNid(umPart.getPathId()),
				umPart.getTime());
		text = umPart.getText();
		lang = umPart.getLang();
		initialCaseSignificant = umPart.getInitialCaseSignificant();
		typeNid = Bdb.uuidsToNid(umPart.getTypeId());
	}

	public DescriptionVersion(EDescriptionVersion edv) {
		super(Bdb.uuidToNid(edv.getStatusUuid()),
				Bdb.uuidToNid(edv.getPathUuid()),
				edv.getTime());
		initialCaseSignificant = edv.isInitialCaseSignificant();
		lang = edv.getLang();
		text = edv.getText();
		typeNid = Bdb.uuidToNid(edv.getTypeUuid());
		statusAtPositionNid = Bdb.getStatusAtPositionNid(edv);
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		output.writeString(text);
		output.writeString(lang);
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
		return new DescriptionVersion(this);
	}

	@Override
	public DescriptionVersion makeAnalog(int statusNid, int pathNid, long time) {
		return new DescriptionVersion(this, statusNid, pathNid, time);
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
