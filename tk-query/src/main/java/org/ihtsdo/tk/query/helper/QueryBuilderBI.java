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
package org.ihtsdo.tk.query.helper;

import java.io.IOException;
import java.util.ArrayList;
import org.ihtsdo.tk.query.parts.Query;
import org.ihtsdo.tk.query.parts.QueryResultBinder;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;

/**
 * The interface <code>QueryBuilderBI</code> represents methods for getting results
 * from one or more <code>Queries</code>. Use TerminologyStoreDI.getQueryBuilder method
 * to get a <code>QueryBuilderBI</code> object.
 * @see TerminologyStoreDI
 */
public interface QueryBuilderBI {
    
    /**
     * Gets a list of result concept nids that meet the specified queries. The list of result sets are ordered to 
     * according to the order of the queries.
     * @param queries one or more <code>Queries</code> to find results for
     * @return an <code>ArrayList</code> of result sets containing the matching concept nids
     * @throws IOException signals that an I/O exception has occurred
     * @throws Exception indicates an exception has occurred
     */
    public ArrayList<QueryResultBinder> getResults(Query... queries) throws IOException, Exception;
    
}
