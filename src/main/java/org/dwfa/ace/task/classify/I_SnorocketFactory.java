
package org.dwfa.ace.task.classify;

public interface I_SnorocketFactory {

    void addIsa(int id);

    void classify();
    
    void addConcept(int conceptId, boolean fullyDefined);
    
    void addRelationship(int c1, int rel, int c2, int group);

    void getResults(I_Callback callback);
    
    interface I_Callback {
        void addRelationship(int c1, int rel, int c2, int group);
    }

    /*
     * Alternate potential API to allow for batch transfer of data
     * 
    void addIsas(int[] ids);
    void addConcepts(int[] concepts, boolean fullyDefined);
    void addRelationships(Rel[] relationships);
    
    final class Rel {
        int c1;
        int rel;
        int c2;
        int group;
    }
    */
    
    /**
     *  not a public API -- just for development
     */
    int getRowCount();

}
