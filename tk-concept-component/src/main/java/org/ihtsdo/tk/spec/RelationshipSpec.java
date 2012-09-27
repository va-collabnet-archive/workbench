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
package org.ihtsdo.tk.spec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.ihtsdo.tk.api.constraint.RelationshipConstraintTarget;
import org.ihtsdo.tk.api.constraint.RelationshipConstraintSource;

// TODO: Auto-generated Javadoc
/**
 * The Class RelationshipSpec.
 */
public class RelationshipSpec implements SpecBI {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant dataVersion. */
    private static final int dataVersion = 1;

    /**
     * Write object.
     *
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @throws IOException Signals that an I/O exception has occurred.
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
     * Instantiates a new relationship spec.
     *
     * @param sourceSpec the source spec
     * @param relationshipTypeSpec the relationship type spec
     * @param targetSpec the target spec
     */
    public RelationshipSpec(ConceptSpec sourceSpec, ConceptSpec relationshipTypeSpec, ConceptSpec targetSpec) {
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
     * Gets the source relationship constraint.
     *
     * @return the source relationship constraint
     */
    public RelationshipConstraintSource getSourceRelationshipConstraint() {
    	return new RelationshipConstraintSource(originSpec, relTypeSpec, destinationSpec);
    }
    
    /**
     * Gets the target relationship constraint.
     *
     * @return the target relationship constraint
     */
    public RelationshipConstraintTarget getTargetRelationshipConstraint() {
    	return new RelationshipConstraintTarget(originSpec, relTypeSpec, destinationSpec);
    }

	/**
	 * Sets the source spec.
	 *
	 * @param sourceSpec the new source spec
	 */
	public void setSourceSpec(ConceptSpec sourceSpec) {
		this.originSpec = sourceSpec;
	}

	/**
	 * Sets the relationship type spec.
	 *
	 * @param relationshipTypeSpec the new relationship type spec
	 */
	public void setRelationshipTypeSpec(ConceptSpec relationshipTypeSpec) {
		this.relTypeSpec = relationshipTypeSpec;
	}

	/**
	 * Sets the target spec.
	 *
	 * @param targetSpec the new target spec
	 */
	public void setTargetSpec(ConceptSpec targetSpec) {
		this.destinationSpec = targetSpec;
	}

}
