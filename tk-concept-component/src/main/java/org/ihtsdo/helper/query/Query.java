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
 *
 * @author aimeefurber
 */
public class Query implements Branch{
    private Branch[] branches;
    private Grouping grouping;
    
    private Query(){
        
    }
    
    public static Query or(Branch... branches){
        Query q = new Query();
        q.grouping = Grouping.OR;
        q.branches = branches;
        return q;
    }
    
    public static Query and(Branch... branches){
        Query q = new Query();
        q.grouping = Grouping.AND;
        q.branches = branches;
        return q;
    }

    public Branch[] getBranches() {
        return branches;
    }

    public Grouping getGrouping() {
        return grouping;
    }
    
    @Override
    public String toString(){
       StringBuilder sb = new StringBuilder();
       for(Branch b : branches){
           sb.append(b.toString());
           sb.append("\n    ");
       }
       return sb.toString();
    }

}

