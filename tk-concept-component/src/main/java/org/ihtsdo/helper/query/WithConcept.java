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
 * The class <code>WithConcept</code> represents concept type query statements. 
 * These can be used to refine a clause of a query.
 */
public class WithConcept implements Statement {
    private StatementPart s;
    private WithConcept(){
        
    }

    /**
     * Creates a concept statement for refining a query by a type of concept status.
     * @param trueStatement set to <code>false</code> if using negation, <code>true</code> otherwise
     * @param subsumption the <code>Subsumption</code> being used
     * @param status the <code>ConceptSpec</code> representing the desired concept status
     * @return the concept statement representing the desired status
     */
    public static WithConcept status(Boolean trueStatement,
            Subsumption subsumption,
            ConceptSpec status){
            WithConcept d = new WithConcept();
        d.s = new StatementPart(
                    trueStatement,
                    TOKENS.DESC_STATUS,
                    subsumption,
                    status);
        return d;
    }

    /**
     * Gets the statement part of the concept statement.
     * @return the <code>StatementPart</code> representing the concept statement
     */
    public StatementPart getStatementPart() {
        return s;
    }
    
    @Override
    public String toString(){
       return s.toString();
    }

}
