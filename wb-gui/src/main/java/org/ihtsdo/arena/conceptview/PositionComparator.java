/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.util.Comparator;
import org.ihtsdo.tk.api.PositionBI;

/**
 *
 * @author kec
 */
class PositionComparator implements Comparator<PositionBI> {

    public PositionComparator() {
    }

    @Override
    public int compare(PositionBI t, PositionBI t1) {
        if (t.getTime() != t1.getTime()) {
            if (t.getTime() > t1.getTime()) {
                return 1;
            }
            return -1;
        }
        return t.getPath().getConceptNid() - t1.getPath().getConceptNid();
    }
    
}
