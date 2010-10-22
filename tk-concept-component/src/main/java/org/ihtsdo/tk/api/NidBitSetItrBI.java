package org.ihtsdo.tk.api;

import java.io.IOException;

public interface NidBitSetItrBI {
    /**
     * Returns the current native id.
     * <p>
     * This is invalid until {@link #next()} is called for the first time.
     */
    public int nid();

    /**
     * Moves to the next identifier in the set. Returns true, iff
     * there is such a nid.
     */
    public boolean next() throws IOException;

    /**
     * Skips entries to the first beyond the current whose nid is
     * greater than or equal to <i>target</i>.
     * <p>
     * Returns true iff there is such an entry.
     * <p>
     * Behaves as if written:
     * 
     * <pre>
     * boolean skipTo(int target) {
     *     do {
     *         if (!next())
     *             return false;
     *     } while (target &gt; nid());
     *     return true;
     * }
     * </pre>
     * 
     * Some implementations are considerably more efficient than that.
     */
    public boolean skipTo(int target) throws IOException;

}
