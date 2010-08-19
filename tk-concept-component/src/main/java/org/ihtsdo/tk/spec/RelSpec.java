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

import org.ihtsdo.tk.api.constraint.RelConstraintIncoming;
import org.ihtsdo.tk.api.constraint.RelConstraintOutgoing;

public class RelSpec implements SpecBI {

    private ConceptSpec originSpec;
	private ConceptSpec relTypeSpec;
    private ConceptSpec destinationSpec;

    public RelSpec(ConceptSpec originSpec, ConceptSpec relTypeSpec, ConceptSpec destinationSpec) {
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
    
    public RelConstraintOutgoing getOriginatingRelConstraint() {
    	return new RelConstraintOutgoing(originSpec, relTypeSpec, destinationSpec);
    }
    public RelConstraintIncoming getDestinationRelConstraint() {
    	return new RelConstraintIncoming(originSpec, relTypeSpec, destinationSpec);
    }

}
