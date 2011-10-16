
/*
* To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.ihtsdo.taxonomy.model;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 *
 * @author kec
 */
public interface ChildNodeFilterBI {
   boolean pass(ConceptVersionBI parent, ConceptVersionBI possibleChild) throws Exception;
}
