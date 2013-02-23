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
 *
 * @author aimeefurber
 */
public class QueryTest{
        ViewCoordinate v1;
        ViewCoordinate v2;
   
    public void doSomething(){
        Query description = new DescriptionChangedQuery(v1, v2).getQuery();
        Query inferred = new RelationshipInferredChangedQuery(v1, v2).getQuery();
        Query stated = new RelationshipStatedChangedQuery(v1, v2).getQuery();
    }
    
}
