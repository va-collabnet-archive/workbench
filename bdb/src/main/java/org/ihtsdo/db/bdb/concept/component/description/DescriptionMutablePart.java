package org.ihtsdo.db.bdb.concept.component.description;

import java.nio.charset.Charset;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.MutablePart;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class DescriptionMutablePart extends MutablePart<DescriptionMutablePart> 
	implements I_DescriptionPart {
	
	@SuppressWarnings("unused")
	private static Charset utf8 = Charset.forName("UTF-8");
	
	private String text;
	private boolean initialCaseSignificant;
	private int typeNid; 
	private String lang;

	public DescriptionMutablePart(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}
	
	protected DescriptionMutablePart(DescriptionMutablePart another) {
		super(another.getStatusAtPositionNid());
		this.text = another.text;
		this.typeNid = another.typeNid;
		this.lang = another.lang;
		this.initialCaseSignificant = another.initialCaseSignificant;
	}

	protected DescriptionMutablePart(DescriptionMutablePart another, int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
		this.text = another.text;
		this.typeNid = another.typeNid;
		this.lang = another.lang;
		this.initialCaseSignificant = another.initialCaseSignificant;
	}

	protected DescriptionMutablePart(TupleInput input) {
		super(input.readInt());
		text = input.readString();
		lang = input.readString();
		initialCaseSignificant = input.readBoolean();
		typeNid = input.readInt();
	}

	public DescriptionMutablePart(UniversalAceDescriptionPart umPart) {
		super(Bdb.uuidsToNid(umPart.getStatusId()),
				Bdb.uuidsToNid(umPart.getPathId()),
				umPart.getTime());
		text = umPart.getText();
		lang = umPart.getLang();
		initialCaseSignificant = umPart.getInitialCaseSignificant();
		typeNid = Bdb.uuidsToNid(umPart.getTypeId());
	}

	@Override
	protected void writeFieldsToBdb(TupleOutput output) {
		output.writeString(text);
		output.writeString(lang);
		output.writeBoolean(initialCaseSignificant);
		output.writeInt(typeNid);
	}

	@Override
	public boolean getInitialCaseSignificant() {
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
	public boolean hasNewData(I_DescriptionPart another) {
		throw new UnsupportedOperationException();
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
	public DescriptionMutablePart duplicate() {
		return new DescriptionMutablePart(this);
	}

	@Override
	public DescriptionMutablePart makeAnalog(int statusNid, int pathNid, long time) {
		return new DescriptionMutablePart(this, statusNid, pathNid, time);
	}

	@Override
	protected ArrayIntList getVariableVersionNids() {
		ArrayIntList list = new ArrayIntList(3);
		list.add(typeNid);
		return list;
	}

}
