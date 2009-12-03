package org.dwfa.ace.task.classify;

import java.util.ArrayList;

public class SnoConGrpList extends ArrayList<SnoConGrp> {
    private static final long serialVersionUID = 1L;

    public SnoConGrpList() {
        super();
    }
    
    /**
     * Counts total concepts in SnoConGrpList
     * @return <code><b>int</b></code> - total concepts
     */
    public int count() {
        int count = 0;
        int max = this.size();
        for (int i = 0; i<max; i++)
            count += this.get(i).size();
        return count;
    }

}
