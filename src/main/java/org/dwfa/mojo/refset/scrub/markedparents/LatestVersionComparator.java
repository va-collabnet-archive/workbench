package org.dwfa.mojo.refset.scrub.markedparents;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;

import java.util.Comparator;
import java.io.Serializable;

/**
 * Used to sort versions by commit time.
 */
public final class LatestVersionComparator implements Comparator<I_ThinExtByRefPart>, Serializable {

    private static final long serialVersionUID = -1882231859707932247L;

    public int compare(final I_ThinExtByRefPart o1, final I_ThinExtByRefPart o2) {
        return o1.getVersion() - o2.getVersion();
    }
}
