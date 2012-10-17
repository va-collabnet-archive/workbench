package org.dwfa.ace.refset;

import java.util.Vector;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.dwfa.ace.api.ebr.I_ExtendByRef;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static com.google.common.base.Preconditions.*;

/**
 * A Refex tree model that we can instruct to hide certain entries
 *
 * @author adrian
 *
 */
public class RefexTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = -6498357151441239744L;

    private Predicate<I_ExtendByRef> clauseFilter;

    /** Allow all nodes that pass the clauseFilter, or have a descendant that does */
    private Predicate<RefsetSpecTreeNode> treeNodeFilter = new Predicate<RefsetSpecTreeNode>() {
        public boolean apply(RefsetSpecTreeNode input) {
            if(input.getUserObject() instanceof I_ExtendByRef) {
                I_ExtendByRef ext = (I_ExtendByRef)input.getUserObject();
                if(clauseFilter.apply(ext)) {
                    return true;
                } else {
                    Vector<RefsetSpecTreeNode> children = input.getChildren();
                    if(children != null) {
                        for(RefsetSpecTreeNode treeNode : input.getChildren()) {
                            if(apply(treeNode)) {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        };
    };

    public RefexTreeModel(TreeNode root, Predicate<I_ExtendByRef> clauseFilter) {
        super(root);
        this.clauseFilter = clauseFilter;
    }

    /**
     * Filter the children of each node
     *
     * @param parentNode
     * @return
     */
    private Iterable<RefsetSpecTreeNode> filteredChildren(RefsetSpecTreeNode parentNode) {
        return Iterables.filter(parentNode.getChildren(), treeNodeFilter);
    }

    @Override
    public Object getChild(Object parent, int index) {
        checkArgument(parent instanceof RefsetSpecTreeNode, "RefexTreeModel only works on RefsetSpecTreeNode");

        RefsetSpecTreeNode parentNode = (RefsetSpecTreeNode) parent;
        return Iterables.get(filteredChildren(parentNode), index);
    }

    @Override
    public int getChildCount(Object parent) {
        checkArgument(parent instanceof RefsetSpecTreeNode, "RefexTreeModel only works on RefsetSpecTreeNode");

        RefsetSpecTreeNode parentNode = (RefsetSpecTreeNode) parent;
        return Iterables.size(filteredChildren(parentNode));

    }

}
