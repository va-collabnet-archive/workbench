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
package org.dwfa.ace.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dwfa.ace.task.search.I_TestSearchResults;

public class QueryBean implements Serializable {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String queryString;
    private List<I_TestSearchResults> extraCriterion;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.queryString);
        out.writeObject(this.extraCriterion);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.queryString = (String) in.readObject();
            this.extraCriterion = (List<I_TestSearchResults>) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    QueryBean(String queryString, List<I_TestSearchResults> extraCriterion) {
        super();
        this.queryString = queryString;
        if (extraCriterion != null) {
            this.extraCriterion = Collections.unmodifiableList(extraCriterion);
        } else {
            this.extraCriterion = Collections.unmodifiableList(new ArrayList<I_TestSearchResults>());
        }
    }

    public String getQueryString() {
        return queryString;
    }

    public List<I_TestSearchResults> getExtraCriterion() {
        return extraCriterion;
    }

}
