
package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.io.InputStream;

public interface I_SnorocketFactory {

    void setIsa(int id);
    
    void addConcept(int conceptId, boolean fullyDefined);
    
    void addRelationship(int conceptId1, int roleId, int conceptId2, int group);

    void classify();

    void getResults(I_Callback callback);
    
    interface I_Callback {
        void addRelationship(int conceptId1, int roleId, int conceptId2, int group);
    }

    /*
     * Alternate potential API to allow for batch transfer of data
     * 
    void addIsas(int[] ids);
    void addConcepts(int[] conceptIds, boolean fullyDefined);
    void addRelationships(Rel[] relationships);
    
    final class Rel {
        int conceptId1;
        int roleId;
        int conceptId2;
        int group;
    }
    */

    /**
     * Used for persistence.  This part of the API is not finalised - DO NOT USE.
     * 
     * @return
     * @throws IOException
     */
    InputStream getStream() throws IOException;

    /**
     * Create a new classification engine that can be used to construct and incrementally classify
     * an extension to the base ontology embodied by this classification engine.
     * 
     * @return
     */
    I_SnorocketFactory createExtension();

}
