package org.dwfa.ace.task.classify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;

public class SnoConGrp extends ArrayList<SnoCon> {
    private static final long serialVersionUID = 1L;

    public SnoConGrp(List<SnoCon> conList, boolean needsToBeSorted) {
        super();
        // set doSort = true if list not pre-sorted to C1-Group-Type-C2 order
        if (needsToBeSorted)
            Collections.sort(conList);
        this.addAll(conList);
    }

    public SnoConGrp(Collection<String> concepts) {
        super();
        // :NYI: defined or not_defined is indeterminate coming from classifier callback.
        for (String cStr : concepts)
            this.add(new SnoCon(unwrap(cStr), false));
        Collections.sort(this);
    }

    public SnoConGrp(SnoCon o) {
        super();
        this.add(o); // 
    }

    public SnoConGrp() {
        super();
    }
    
    static private int unwrap(final String id) {
        return Integer.parseInt(String.valueOf(id));
    }

} // class SnoConGrp
