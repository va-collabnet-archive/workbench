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
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
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
