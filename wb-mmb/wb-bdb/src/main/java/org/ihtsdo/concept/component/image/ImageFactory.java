package org.ihtsdo.concept.component.image;

import java.io.IOException;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ComponentFactory;

import com.sleepycat.bind.tuple.TupleInput;

public class ImageFactory extends ComponentFactory<ImageRevision, Image> {

	@Override
	public Image create(Concept enclosingConcept, 
			TupleInput input) throws IOException {
		return new Image(enclosingConcept, 
				input);
	}

}