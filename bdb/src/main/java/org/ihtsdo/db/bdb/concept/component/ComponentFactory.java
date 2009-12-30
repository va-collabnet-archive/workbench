package org.ihtsdo.db.bdb.concept.component;

public abstract class ComponentFactory<C extends ConceptComponent<P, C>, P extends Version<P, C>> {
	
	public abstract C create(int nid, int partCount, boolean editable);

}
