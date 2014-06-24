
/*
* To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.ihtsdo.taxonomy.model;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 * This is an "idea" interface for future use.
 * @author kec
 */
public interface ChildNodeGeneratorBI {
   List<? extends ChildNodeFilterBI> getChildFilterList();
}
