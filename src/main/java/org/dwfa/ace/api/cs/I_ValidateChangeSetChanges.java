package org.dwfa.ace.api.cs;

import java.io.File;
import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.utypes.I_AmChangeSetObject;
import org.dwfa.tapi.TerminologyException;

/**
 * This interface provides the methods necessary to validate
 * a change set, and to allow selective import of change set
 * contents according to aribtrary criterion.
 * 
 * @author kec
 *
 */
public interface I_ValidateChangeSetChanges {
   
    /**
     * Determine if a change set is validated for input. This method provides
     * the opportunity for a validator class to open the file, examine the contents, and compare it 
     * with the content within the provided <code>I_TermFactory</code> object to determine if a change set is 
     * validated for processing. If validated, individual changes in the change set will be processed via
     * the <code>validateChange</code> method, where individual items can be filtered for processing. 
     * 
     * @return true if the change set is validated for processing. 

     * @param tf the
     * @return true if change set is validated for processing. 
     * @throws IOException
     */
   public boolean validateFile(File csFile, I_TermFactory tf) throws IOException, TerminologyException;
   
   /**
    * Determine if an individual component within a change set is validated for input. 
    * @param csObj The change set object to validate. 
    * @return true if the <code>csObj</code> is validated for import into the <code>I_TermFactory</code>
    * @throws IOException
    * @throws TerminologyException 
    */
   public boolean validateChange(I_AmChangeSetObject csObj, I_TermFactory tf) throws IOException, TerminologyException;
   
}
