/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.api.coordinate;

import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;


/**
 * The Class EditCoordinate represents metadata needed to make an edit. It
 * stores the author of the edit, the module the edit will occur in, and the
 * path the edit will occur on.
 */
public class EditCoordinate {

    /**
     * The author nid.
     */
    private int authorNid;
    /**
     * The module nid.
     */
    private int moduleNid;
    /**
     * The edit paths.
     */
    private NidSetBI editPaths;

    /**
     * Instantiates a new <code>EditCoordinate</code> based on the given author, module, and paths.
     *
     * @param authorNid the author nid
     * @param moduleNid the module nid
     * @param editPathNids the edit path nids
     */
    public EditCoordinate(int authorNid, int moduleNid, NidSetBI editPathNids) {
        super();
        assert editPathNids != null;
        assert authorNid != Integer.MIN_VALUE;
        this.authorNid = authorNid;
        this.moduleNid = moduleNid;
        this.editPaths = editPathNids;
    }

    /**
     * Instantiates a new <code>EditCoordinate</code> based on the given author, module, and paths.
     *
     * @param authorNid the author nid
     * @param moduleNid the module nid
     * @param editPathNids the edit path nids
     */
    public EditCoordinate(int authorNid, int moduleNid, int... editPathNids) {
        super();
        assert editPathNids != null;
        assert authorNid != Integer.MIN_VALUE;
        this.authorNid = authorNid;
        this.moduleNid = moduleNid;
        this.editPaths = new NidSet(editPathNids);
    }

    /**
     * Gets the author nid.
     *
     * @return the author nid
     */
    public int getAuthorNid() {
        return authorNid;
    }

    /**
     * Gets the module nid.
     *
     * @return the module nid
     */
    public int getModuleNid() {
        return moduleNid;
    }

    /**
     * Gets the edit paths.
     *
     * @return the edit paths
     */
    public int[] getEditPaths() {
        return editPaths.getSetValues();
    }

    /**
     * Gets the edit paths set.
     *
     * @return the edit paths set
     */
    public NidSetBI getEditPathsSet() {
        return editPaths;
    }

    
   /**
    * @see java.lang.Object#toString()
    */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("authorNid: ").append(authorNid);
        sb.append("moduleNid: ").append(moduleNid);
        sb.append("editPaths: ").append(editPaths);
        return sb.toString();
    }
}
