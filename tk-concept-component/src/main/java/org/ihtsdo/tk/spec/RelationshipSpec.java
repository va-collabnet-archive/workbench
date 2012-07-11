/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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

public class RelationshipSpec implements SpecBI {

    /**
	 * 
	 */
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

    public RelationshipSpec(ConceptSpec sourceSpec, ConceptSpec relationshipTypeSpec, ConceptSpec targetSpec) {
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
    
    public RelationshipConstraintSource getSourceRelationshipConstraint() {
    	return new RelationshipConstraintSource(originSpec, relTypeSpec, destinationSpec);
    }
    public RelationshipConstraintTarget getTargetRelationshipConstraint() {
    	return new RelationshipConstraintTarget(originSpec, relTypeSpec, destinationSpec);
    }

	public void setSourceSpec(ConceptSpec sourceSpec) {
		this.originSpec = sourceSpec;
	}

	public void setRelationshipTypeSpec(ConceptSpec relationshipTypeSpec) {
		this.relTypeSpec = relationshipTypeSpec;
	}

	public void setTargetSpec(ConceptSpec targetSpec) {
		this.destinationSpec = targetSpec;
	}

}
