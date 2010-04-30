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
package org.dwfa.ace.api;

import java.io.IOException;

public interface I_IterateIds {

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
    public abstract boolean next() throws IOException;

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
    public abstract boolean skipTo(int target) throws IOException;

}
