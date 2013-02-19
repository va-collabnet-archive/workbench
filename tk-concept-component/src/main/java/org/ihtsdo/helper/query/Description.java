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

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;


/**
 * Null clauses for difference report: any
 * @author aimeefurber
 */
public class Description implements Branch{
    private Clause[] clauses;
    private Boolean trueStatement;
    private TOKENS token;
    private ViewCoordinate v1;
    private ViewCoordinate v2;
    
    private Description() {
        
    }

    public static Description added(Boolean trueStatement, 
            ViewCoordinate v1, ViewCoordinate v2) {
        Description r = new Description();
        r.v1 = v1;
        r.v2 = v2;
        r.trueStatement = trueStatement;
        r.token = TOKENS.ADDED_DESCRIPTION;
        return r;
    }
    
    public static Description added(Boolean trueStatement, 
            ViewCoordinate v1, ViewCoordinate v2,
            Clause... clauses) {
        Description r = new Description();
        r.v1 = v1;
        r.v2 = v2;
        r.trueStatement = trueStatement;
        r.token = TOKENS.ADDED_DESCRIPTION;
        r.clauses = clauses;
        return r;
    }

    public static Description changed(Boolean trueStatement,
            ViewCoordinate v1, ViewCoordinate v2) {
        Description r = new Description();
        r.v1 = v1;
        r.v2 = v2;
        r.trueStatement = trueStatement;
        r.token = TOKENS.CHANGED_DESCRIPTION_STATUS;
        return r;
    }
    
    public static Description changed(Boolean trueStatement,
            ViewCoordinate v1, ViewCoordinate v2,
            Clause... clauses) {
        Description r = new Description();
        r.v1 = v1;
        r.v2 = v2;
        r.trueStatement = trueStatement;
        r.token = TOKENS.CHANGED_DESCRIPTION_STATUS;
        r.clauses = clauses;
        return r;
    }

    public Clause[] getClauses() {
        return clauses;
    }

    public Boolean getTrueStatement() {
        return trueStatement;
    }

    public TOKENS getToken() {
        return token;
    }

    public ViewCoordinate getV1() {
        return v1;
    }

    public ViewCoordinate getV2() {
        return v2;
    }
    
    @Override
    public String toString(){
       StringBuilder sb = new StringBuilder();
       sb.append(trueStatement.toString()).append("\n    ");
       sb.append(v1.getPositionSet().toString()).append("\n    ");
       sb.append(v2.getPositionSet().toString()).append("\n    ");
       sb.append(token.toString()).append("\n    ");
       if(clauses != null){
           for(Clause c : clauses){
            sb.append(c.toString());
            sb.append("\n    ");
           }
       }
       return sb.toString();
    }
}
