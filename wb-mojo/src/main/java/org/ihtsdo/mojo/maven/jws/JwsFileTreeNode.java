/*
 * Copyright 2014 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.mojo.maven.jws;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author marc
 */
public class JwsFileTreeNode implements Iterable<JwsFileTreeNode> {

    public String nodePath;
    public String nodeType; // "root" "folder" "file"
    public JwsFileTreeNode parent;
    public List<JwsFileTreeNode> children;

    public JwsFileTreeNode() {
        this.nodePath = "";
        this.nodeType = "";
        this.children = new LinkedList<>();
    }

    public JwsFileTreeNode(String dataPath /* ..data.. */) {
        this.nodePath = dataPath;
        this.nodeType = "";
        this.children = new LinkedList<>();
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public JwsFileTreeNode addChild(String childPath) {
        JwsFileTreeNode childNode = new JwsFileTreeNode(childPath);
        childNode.nodeType = "file";
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }

    public int getLevel() {
        if (this.isRoot()) {
            return 0;
        } else {
            return parent.getLevel() + 1;
        }
    }

    @Override
    public String toString() {
        if (nodePath != null) {
            return createIndent(getLevel()) + nodePath + " (" + nodeType + ")";
        } else {
            return "[path null]";
        }
    }

    public String toStringWithParentPaths() {
        if (nodePath != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(nodeType).append(": ");
            sb.append(nodePath);
            JwsFileTreeNode pathParent = parent;
            while (pathParent != null) {
                sb.append(" > ").append(pathParent.nodePath);
                pathParent = pathParent.parent;
            }
            return sb.toString();
        } else {
            return "[path null]";
        }
    }

    private static String createIndent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append('.');
        }
        return sb.toString();
    }

    @Override
    public Iterator<JwsFileTreeNode> iterator() {
        JwsFileTreeNodeIter iter = new JwsFileTreeNodeIter(this);
        return iter;
    }
}
