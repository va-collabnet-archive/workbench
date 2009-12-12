package org.ihtsdo.db.bdb.concept.component.description;

import java.nio.charset.Charset;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.ihtsdo.db.bdb.concept.component.Part;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class DescriptionPart extends Part<DescriptionPart> 
	implements I_DescriptionPart<DescriptionPart> {
	
	@SuppressWarnings("unused")
	private static Charset utf8 = Charset.forName("UTF-8");
	
	private String text;
	private boolean initialCaseSignificant;
	private int typeNid; 
	private String lang;

	public DescriptionPart(int statusAtPositionNid) {
		super(statusAtPositionNid);
	}
	
	protected DescriptionPart(DescriptionPart another) {
		super(another.getStatusAtPositionNid());
		this.text = another.text;
		this.typeNid = another.typeNid;
		this.lang = another.lang;
		this.initialCaseSignificant = another.initialCaseSignificant;
	}

	protected DescriptionPart(DescriptionPart another, int statusNid, int pathNid, long time) {
		super(statusNid, pathNid, time);
		this.text = another.text;
		this.typeNid = another.typeNid;
		this.lang = another.lang;
		this.initialCaseSignificant = another.initialCaseSignificant;
	}

	protected DescriptionPart(TupleInput input) {
		super(input.readInt());
		text = input.readString();
		lang = input.readString();
		initialCaseSignificant = input.readBoolean();
		typeNid = input.readInt();
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
	public boolean hasNewData(DescriptionPart another) {
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
	public DescriptionPart duplicate() {
		return new DescriptionPart(this);
	}

	@Override
	public DescriptionPart makeAnalog(int statusNid, int pathNid, long time) {
		return new DescriptionPart(this, statusNid, pathNid, time);
	}

	@Override
	public ArrayIntList getPartComponentNids() {
		ArrayIntList list = super.getComponentNids();
		list.add(typeNid);
		return list;
	}

}
