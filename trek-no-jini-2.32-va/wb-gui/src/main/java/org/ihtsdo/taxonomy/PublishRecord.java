
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.taxonomy.nodes.TaxonomyNode;

/**
 *
 * @author kec
 */
public class PublishRecord {
   TaxonomyNode publishedNode;
   UpdateType   updateType; 

   //~--- constant enums ------------------------------------------------------

   public enum UpdateType { NO_TAXONOMY_CHANGE, CHILD_CHANGE, EXTRA_PARENT_CHANGE,
                            EXTRA_PARENT_AND_CHILD_CHANGE }

   ;

   //~--- constructors --------------------------------------------------------

   public PublishRecord(TaxonomyNode publishedNode, UpdateType updateType) {
      this.publishedNode = publishedNode;
      this.updateType    = updateType;
   }

   //~--- get methods ---------------------------------------------------------

   public TaxonomyNode getPublishedNode() {
      return publishedNode;
   }

   public UpdateType getUpdateType() {
      return updateType;
   }
}
