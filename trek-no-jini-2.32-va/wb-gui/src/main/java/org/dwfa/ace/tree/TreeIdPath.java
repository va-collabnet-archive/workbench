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
package org.dwfa.ace.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.util.HashFunction;

public class TreeIdPath {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    int[] ids;

    public TreeIdPath(Object[] path) {
        ids = new int[path.length];
        for (int i = 0; i < ids.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path[i];
            I_GetConceptData cb = (I_GetConceptData) node.getUserObject();
            if (cb == null) {
                ids[i] = Integer.MIN_VALUE;
            } else {
                ids[i] = cb.getConceptNid();
            }
        }
    }

    public boolean initiallyEqual(TreeIdPath another) {
        if (another.ids.length > ids.length) {
            return false;
        }
        for (int i = 0; i < another.ids.length; i++) {
            if (ids[i] != another.ids[i]) {
                return false;
            }
        }
        return true;
    }

    public TreeIdPath(TreePath path) {
        ids = new int[path.getPathCount()];
        for (int i = 0; i < ids.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getPathComponent(i);
            I_GetConceptData cb = (I_GetConceptData) node.getUserObject();
            if (cb == null) {
                ids[i] = Integer.MIN_VALUE;
            } else {
                ids[i] = cb.getConceptNid();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        TreeIdPath another = (TreeIdPath) obj;
        if (ids.length != another.ids.length) {
            return false;
        }
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] != another.ids[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(ids);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        boolean first = true;
        for (int i : ids) {
            if (!first) {
                buf.append(", ");
            }
            buf.append(i);
            first = false;
        }
        buf.append("]");
        return buf.toString();
    }

}
