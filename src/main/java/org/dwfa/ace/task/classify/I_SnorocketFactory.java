
package org.dwfa.ace.task.classify;

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
    
}
