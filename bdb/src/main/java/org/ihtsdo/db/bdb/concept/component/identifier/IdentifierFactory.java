package org.ihtsdo.db.bdb.concept.component.identifier;

import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

public class IdentifierFactory extends ComponentFactory<Identifier, IdentifierMutablePart>{

	@Override
	public Identifier create(int nid, int partCount, boolean editable) {
		return new Identifier(nid, partCount, editable);
	}

}
