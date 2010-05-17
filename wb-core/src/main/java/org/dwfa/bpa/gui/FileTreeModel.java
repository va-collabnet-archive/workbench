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
/*
 * Created on Apr 19, 2006
 */
package org.dwfa.bpa.gui;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class FileTreeModel implements TreeModel {
    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new FileWrapper(new File(
        System.getProperty("user.dir"), "tasks")));
    private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
    private DefaultMutableTreeNode processesNode = new DefaultMutableTreeNode(new FileWrapper(new File(
        System.getProperty("user.dir"), "processes")));

    public FileTreeModel() {
        super();
    }

    public Object getRoot() {
        return rootNode;
    }

    public Object getChild(Object parent, int index) {
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parent;
        File root = ((FileWrapper) parentNode.getUserObject()).getFile();
        File[] children = root.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if ((parent == rootNode) && (index == children.length)) {
            return processesNode;
        }
        return new DefaultMutableTreeNode(new FileWrapper(children[index]));
    }

    public int getChildCount(Object parent) {
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parent;
        File root = ((FileWrapper) parentNode.getUserObject()).getFile();
        File[] children = root.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if (parent == rootNode) {
            return children.length + 1;
        }
        return children.length;
    }

    public boolean isLeaf(Object node) {
        DefaultMutableTreeNode thisNode = (DefaultMutableTreeNode) node;
        File root = ((FileWrapper) thisNode.getUserObject()).getFile();
        File[] children = root.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        return children == null || children.length == 0;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        // TODO Auto-generated method stub

    }

    public int getIndexOfChild(Object parent, Object child) {
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parent;
        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) child;
        File rootFile = ((FileWrapper) parentNode.getUserObject()).getFile();
        File[] children = rootFile.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        File childFile = ((FileWrapper) childNode.getUserObject()).getFile();
        for (int i = 0; i < children.length; i++) {
            if (children[i].equals(childFile)) {
                return i;
            }
        }
        if ((parent == rootNode) && (child == processesNode)) {
            return children.length;
        }
        return -1;
    }

    public void addTreeModelListener(TreeModelListener l) {
        this.listeners.add(l);

    }

    public void removeTreeModelListener(TreeModelListener l) {
        this.listeners.remove(l);
    }

    /**
     * The only event raised by this model is TreeStructureChanged with the
     * root as path, i.e. the whole tree has changed.
     */
    public void fireTreeStructureChanged() {
        TreeModelEvent e = new TreeModelEvent(this, new Object[] { rootNode });
        for (TreeModelListener l : listeners) {
            l.treeStructureChanged(e);
        }
    }

}
