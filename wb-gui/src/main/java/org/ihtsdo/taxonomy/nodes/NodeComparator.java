/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.taxonomy.nodes;

import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author kec
 */
public class NodeComparator implements Comparator<Long> {

    Map<Long, TaxonomyNode>  nodeMap;

    public NodeComparator(Map<Long, TaxonomyNode> nodeMap) {
        this.nodeMap = nodeMap;
    }
    
    @Override
    public int compare(Long nId1, Long nId2) {
        if (nId1.equals(nId2)) {
            return 0;
        }
        TaxonomyNode n1 = nodeMap.get(nId1);
        TaxonomyNode n2 = nodeMap.get(nId2);
        int maxIndex = Math.min(n1.nodesToCompare.length, n2.nodesToCompare.length);
        for (int i = 0; i < maxIndex; i++) {
            if (n1.nodesToCompare[i] == Long.MAX_VALUE && n2.nodesToCompare[i] == Long.MAX_VALUE) {
                // different concepts, but identical text. Return difference of concept ids. 
                return n1.getCnid() - n2.getCnid();
            }
            if (n1.nodesToCompare[i] == Long.MAX_VALUE) {
                return 1;
            }
            if (n2.nodesToCompare[i] == Long.MAX_VALUE) {
                return -1;
            }
            TaxonomyNode nc1 = nodeMap.get(n1.nodesToCompare[i]);
            TaxonomyNode nc2 = nodeMap.get(n2.nodesToCompare[i]);
            int comparison = nc1.sortComparable.compareTo(nc2.sortComparable);
            if (comparison != 0) {
                return comparison;
            }
        }
        if (n1.nodesToCompare.length > n2.nodesToCompare.length) {
            return -1;
        }
        return 1;
    }
    
}
