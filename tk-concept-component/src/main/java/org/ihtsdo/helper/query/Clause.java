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

import java.util.ArrayList;

/**
 * The class <code>Clause</code> represents the clause of a query. A clause can contain another clause.
 */
public class Clause implements Statement{
    private Statement[] statements;
    private Grouping grouping;
    
    private Clause(){
        
    }
    
    /**
     * Creates a clause with an OR grouping.
     * @param clauses the statements of the clause
     * @return a clause with the specified grouping and statements
     */
    public static Clause or(Statement... clauses){
        Clause c = new Clause();
        c.grouping = Grouping.OR;
        c.statements = clauses;
        return c;
    }
    
    /**
     * Creates a clause with an AND grouping.
     * @param clauses the statements of the clause
     * @return a clause with the specified grouping and statements
     */
    public static Clause and(Statement... clauses){
        Clause c = new Clause();
        c.grouping = Grouping.AND;
        c.statements = clauses;
        return c;
    }

    /**
     * Gets the statements contained in the clause.
     * @return the statements contained in the clause
     */
    public Statement[] getStatements() {
        return statements;
    }

    /**
     * Get the grouping type of the clause. OR or AND.
     * @return the grouping type of the clause
     */
    public Grouping getGrouping() {
        return grouping;
    }
    
    @Override
    public String toString(){
       StringBuilder sb = new StringBuilder();
       sb.append(grouping.toString()).append("\n    ");
       for(Statement s : statements){
           sb.append(s.toString());
           sb.append("\n    ");
       }
       return sb.toString();
    }
}
