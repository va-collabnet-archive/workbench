package org.ihtsdo.db.bdb.concept.component;

public abstract class ComponentFactory<C extends ConceptComponent<P>, P extends Part<P>> {
	
	public abstract C create(int nid, int partCount, boolean editable);

}
