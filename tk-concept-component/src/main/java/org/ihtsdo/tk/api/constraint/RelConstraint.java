package org.ihtsdo.tk.api.constraint;

import java.io.IOException;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.spec.ConceptSpec;

public class RelConstraint implements ConstraintBI {

    private ConceptSpec originSpec;
	private ConceptSpec relTypeSpec;
    private ConceptSpec destinationSpec;
    
	public RelConstraint(ConceptSpec originSpec, 
			ConceptSpec relTypeSpec,
			ConceptSpec destinationSpec) {
		super();
		this.originSpec = originSpec;
		this.relTypeSpec = relTypeSpec;
		this.destinationSpec = destinationSpec;
	}

	public ConceptSpec getOriginSpec() {
		return originSpec;
	}

	public ConceptSpec getRelTypeSpec() {
		return relTypeSpec;
	}

	public ConceptSpec getDestinationSpec() {
		return destinationSpec;
	}

	public ConceptVersionBI getOrigin(Coordinate c) throws IOException {
		return originSpec.get(c);
	}

	public ConceptVersionBI getRelType(Coordinate c) throws IOException {
		return relTypeSpec.get(c);
	}

	public ConceptVersionBI getDestination(Coordinate c) throws IOException {
		return destinationSpec.get(c);
	}

	public int getOriginNid() throws IOException {
		return Ts.get().uuidsToNid(originSpec.getUuids());
	}

	public int getRelTypeNid() throws IOException {
		return Ts.get().uuidsToNid(relTypeSpec.getUuids());
	}

	public int getDestinationNid() throws IOException {
		return Ts.get().uuidsToNid(destinationSpec.getUuids());
	}

}
