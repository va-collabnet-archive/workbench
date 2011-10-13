/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.taxonomy;

/**
 *
 * @author kec
 */
public interface CancelableBI {

    boolean isCanceled();

    void setCanceled(boolean canceled);
    
}
