/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.api;

import java.io.IOException;


/**
 * The Interface NidBitSetItrBI.
 */
public interface NidBitSetItrBI {
    
    /**
     * Returns the current nid.
     * <p>
     * This is invalid until {@link #next()} is called for the first time.
     *
     * @return the nid
     */
    public int nid();

    /**
     * Moves to the next identifier in the set. Returns true, if
     * there is such a nid.
     *
     * @return <code>true</code>, if there is another identifier in the set
     * @throws IOException signals that an I/O exception has occurred
     */
    public boolean next() throws IOException;

    /**
     * Skips entries to the first beyond the current whose nid is
     * greater than or equal to <i>targetNid</i>.
     * <p>
     * Returns true if there is such an entry.
     * <p>
     * Behaves as if written:
     * 
     * <pre>
     * boolean skipTo(int targetNid) {
     * do {
     * if (!next())
     * return false;
     * } while (targetNid &gt; nid());
     * return true;
     * }
     * </pre>
     * 
     * Some implementations are considerably more efficient than that.
     *
     * @param targetNid the target nid
     * @return <code>true</code>, if successful
     * @throws IOException signals that an I/O exception has occurred
     */
    public boolean skipTo(int targetNid) throws IOException;

}
