package org.ihtsdo.db.bdb.concept.component.image;

import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;

public class ImageFactory extends ComponentFactory<ImageRevision, Image> {

	@Override
	public Image create(Concept enclosingConcept, 
			TupleInput input) {
		return new Image(enclosingConcept, 
				input);
	}

}