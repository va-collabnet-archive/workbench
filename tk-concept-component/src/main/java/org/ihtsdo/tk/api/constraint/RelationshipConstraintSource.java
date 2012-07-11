package org.ihtsdo.tk.api.constraint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.ihtsdo.tk.spec.ConceptSpec;

public class RelationshipConstraintSource extends RelationshipConstraint {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }
	public RelationshipConstraintSource(ConceptSpec sourceSpec,
			ConceptSpec relationshipTypeSpec, ConceptSpec targetSpec) {
		super(sourceSpec, relationshipTypeSpec, targetSpec);
	}

}
