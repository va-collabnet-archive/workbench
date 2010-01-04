package org.ihtsdo.db.bdb.concept.component.image;

import java.util.UUID;

import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;

public class ImageFactory extends ComponentFactory<ImageVersion, Image> {

	@Override
	public Image create(int nid, int partCount, Concept enclosingConcept, 
			TupleInput input, 
			UUID primordialUuid) {
		return new Image(nid, partCount, enclosingConcept, 
				primordialUuid);
	}

}