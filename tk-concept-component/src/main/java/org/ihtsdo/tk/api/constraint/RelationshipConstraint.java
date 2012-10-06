/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.api.constraint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.spec.ConceptSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class RelationshipConstraint.
 */
public class RelationshipConstraint implements ConstraintBI {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant dataVersion. */
    private static final int dataVersion = 1;

    /**
     * Write object.
     *
     * @param out the out
     * @throws IOException signals that an I/O exception has occurred
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(originSpec);
        out.writeObject(relTypeSpec);
        out.writeObject(destinationSpec);
    }

    /**
     * Read object.
     *
     * @param in the in
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException the class not found exception
     */
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
    
    /** The origin spec. */
    private ConceptSpec originSpec;
	
	/** The rel type spec. */
	private ConceptSpec relTypeSpec;
    
    /** The destination spec. */
    private ConceptSpec destinationSpec;
    
	/**
	 * Instantiates a new relationship constraint.
	 *
	 * @param sourceSpec the source spec
	 * @param relationshipTypeSpec the relationship type spec
	 * @param targetSpec the target spec
	 */
	public RelationshipConstraint(ConceptSpec sourceSpec, 
			ConceptSpec relationshipTypeSpec,
			ConceptSpec targetSpec) {
		super();
		this.originSpec = sourceSpec;
		this.relTypeSpec = relationshipTypeSpec;
		this.destinationSpec = targetSpec;
	}

	/**
	 * Gets the source spec.
	 *
	 * @return the source spec
	 */
	public ConceptSpec getSourceSpec() {
		return originSpec;
	}

	/**
	 * Gets the relationship type spec.
	 *
	 * @return the relationship type spec
	 */
	public ConceptSpec getRelationshipTypeSpec() {
		return relTypeSpec;
	}

	/**
	 * Gets the target spec.
	 *
	 * @return the target spec
	 */
	public ConceptSpec getTargetSpec() {
		return destinationSpec;
	}

	/**
	 * Gets the source.
	 *
	 * @param viewCoordinate the view coordinate
	 * @return the source
	 * @throws IOException signals that an I/O exception has occurred
	 */
	public ConceptVersionBI getSource(ViewCoordinate viewCoordinate) throws IOException {
		return originSpec.get(viewCoordinate);
	}

	/**
	 * Gets the relationship type.
	 *
	 * @param viewCoordinate the view coordinate
	 * @return the relationship type
	 * @throws IOException signals that an I/O exception has occurred
	 */
	public ConceptVersionBI getRelationshipType(ViewCoordinate viewCoordinate) throws IOException {
		return relTypeSpec.get(viewCoordinate);
	}

	/**
	 * Gets the target.
	 *
	 * @param viewCoordinate the view coordinate
	 * @return the target
	 * @throws IOException signals that an I/O exception has occurred
	 */
	public ConceptVersionBI getTarget(ViewCoordinate viewCoordinate) throws IOException {
		return destinationSpec.get(viewCoordinate);
	}

	/**
	 * Gets the source nid.
	 *
	 * @return the source nid
	 * @throws IOException signals that an I/O exception has occurred
	 */
	public int getSourceNid() throws IOException {
		return Ts.get().getNidForUuids(originSpec.getUuids());
	}

	/**
	 * Gets the relationship type nid.
	 *
	 * @return the relationship type nid
	 * @throws IOException signals that an I/O exception has occurred
	 */
	public int getRelationshipTypeNid() throws IOException {
		return Ts.get().getNidForUuids(relTypeSpec.getUuids());
	}

	/**
	 * Gets the target nid.
	 *
	 * @return the target nid
	 * @throws IOException signals that an I/O exception has occurred
	 */
	public int getTargetNid() throws IOException {
		return Ts.get().getNidForUuids(destinationSpec.getUuids());
	}

}
