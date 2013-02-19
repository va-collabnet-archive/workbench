/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.helper.query;

import org.ihtsdo.tk.spec.ConceptSpec;

/**
 *
 * @author aimeefurber
 */
public class StatementPart {
    private Boolean trueStatement;
    private TOKENS token;
    private Subsumption subsumption;
    private ConceptSpec concept;
    
    public StatementPart(Boolean trueStatement,
            TOKENS token, 
            Subsumption subsumption, 
            ConceptSpec concept) {
        this.trueStatement = trueStatement;
        this.token = token;
        this.subsumption = subsumption;
        this.concept = concept;
    }

    public Boolean getTrueStatement() {
        return trueStatement;
    }

    public TOKENS getToken() {
        return token;
    }

    public Subsumption getSubsumption() {
        return subsumption;
    }

    public ConceptSpec getConcept() {
        return concept;
    }
    
    @Override
    public String toString(){
       StringBuilder sb = new StringBuilder();
       sb.append(trueStatement.toString()).append("\n    ");
       sb.append(token.toString()).append("\n    ");
       sb.append(subsumption.toString()).append("\n    ");
       sb.append(concept.toString()).append("\n    ");
       return sb.toString();
    }
}
