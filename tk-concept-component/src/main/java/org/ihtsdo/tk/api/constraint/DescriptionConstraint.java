package org.ihtsdo.tk.api.constraint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.spec.ConceptSpec;

public class DescriptionConstraint implements ConstraintBI {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(conceptSpec);
        out.writeObject(descTypeSpec);
        out.writeUTF(text);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	conceptSpec = (ConceptSpec) in.readObject();
        	descTypeSpec = (ConceptSpec) in.readObject();
        	text = in.readUTF();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private ConceptSpec conceptSpec;
	private ConceptSpec descTypeSpec;
    private String text;
    
	public DescriptionConstraint(ConceptSpec conceptSpec,
			ConceptSpec descriptionTypeSpec, String text) {
		super();
		this.conceptSpec = conceptSpec;
		this.descTypeSpec = descriptionTypeSpec;
		this.text = text;
	}

	public ConceptSpec getConceptSpec() {
		return conceptSpec;
	}

	public ConceptSpec getDescriptionTypeSpec() {
		return descTypeSpec;
	}

	public String getText() {
		return text;
	}

	public ConceptVersionBI getConcept(ViewCoordinate viewCoordinate) throws IOException {
		return conceptSpec.get(viewCoordinate);
	}

	public ConceptVersionBI getDescType(ViewCoordinate viewCoordinate) throws IOException {
		return descTypeSpec.get(viewCoordinate);
	}


	public int getConceptNid() throws IOException {
		return Ts.get().getNidForUuids(conceptSpec.getUuids());
	}

	public int getDescriptionTypeNid() throws IOException {
		return Ts.get().getNidForUuids(descTypeSpec.getUuids());
	}

    

}
