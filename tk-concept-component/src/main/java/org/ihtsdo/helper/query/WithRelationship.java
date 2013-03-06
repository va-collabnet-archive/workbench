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
public class WithRelationship implements Statement {
    private StatementPart s;
    private WithRelationship(){
        
    }

    public static WithRelationship characteristic(
            Boolean trueStatement,
            Subsumption subsumption,
            ConceptSpec characteristic){
        WithRelationship r = new WithRelationship();
        r.s = new StatementPart(
                    trueStatement,
                    TOKENS.REL_CHARACTERISTIC,
                    subsumption,
                    characteristic);
        return r;
    }

    public static WithRelationship status(Boolean trueStatement,
            Subsumption subsumption,
            ConceptSpec status){
            WithRelationship r = new WithRelationship();
        r.s = new StatementPart(
                    trueStatement,
                    TOKENS.REL_STATUS,
                    subsumption,
                    status);
        return r;
    }

    public StatementPart getStatementPart() {
        return s;
    }
    
    @Override
    public String toString(){
       return s.toString();
    }

}