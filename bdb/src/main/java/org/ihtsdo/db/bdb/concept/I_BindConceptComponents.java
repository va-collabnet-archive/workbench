package org.ihtsdo.db.bdb.concept;

public interface I_BindConceptComponents {

	public int getConceptNid();

	public boolean isEditable();

	public void setupBinder(int conceptNid, boolean editable);

}