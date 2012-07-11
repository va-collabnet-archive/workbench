package org.ihtsdo.tk.api.constraint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.spec.ConceptSpec;

public class RelationshipConstraint implements ConstraintBI {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(originSpec);
        out.writeObject(relTypeSpec);
        out.writeObject(destinationSpec);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	originSpec = (ConceptSpec) in.readObject();
        	relTypeSpec = (ConceptSpec) in.readObject();
        	destinationSpec = (ConceptSpec) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }
    private ConceptSpec originSpec;
	private ConceptSpec relTypeSpec;
    private ConceptSpec destinationSpec;
    
	public RelationshipConstraint(ConceptSpec sourceSpec, 
			ConceptSpec relationshipTypeSpec,
			ConceptSpec targetSpec) {
		super();
		this.originSpec = sourceSpec;
		this.relTypeSpec = relationshipTypeSpec;
		this.destinationSpec = targetSpec;
	}

	public ConceptSpec getSourceSpec() {
		return originSpec;
	}

	public ConceptSpec getRelationshipTypeSpec() {
		return relTypeSpec;
	}

	public ConceptSpec getTargetSpec() {
		return destinationSpec;
	}

	public ConceptVersionBI getSource(ViewCoordinate viewCoordinate) throws IOException {
		return originSpec.get(viewCoordinate);
	}

	public ConceptVersionBI getRelationshipType(ViewCoordinate viewCoordinate) throws IOException {
		return relTypeSpec.get(viewCoordinate);
	}

	public ConceptVersionBI getTarget(ViewCoordinate viewCoordinate) throws IOException {
		return destinationSpec.get(viewCoordinate);
	}

	public int getSourceNid() throws IOException {
		return Ts.get().getNidForUuids(originSpec.getUuids());
	}

	public int getRelationshipTypeNid() throws IOException {
		return Ts.get().getNidForUuids(relTypeSpec.getUuids());
	}

	public int getTargetNid() throws IOException {
		return Ts.get().getNidForUuids(destinationSpec.getUuids());
	}

}
