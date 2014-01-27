package org.ihtsdo.mojo.maven.classifier;

import org.dwfa.ace.task.classify.SnoCon;

public class SnoConStringID extends SnoCon {

	private String stringId;
	public SnoConStringID() {
	}

	public String getStringId() {
		return stringId;
	}

	public void setStringId(String stringId) {
		this.stringId = stringId;
	}

	public SnoConStringID(int id,String stringId, boolean isDefined) {
		super(id, isDefined);
		
		this.stringId=stringId;
	}

}
