package org.dwfa.tapi.spec;

public class RelSpec {

    private ConceptSpec relType;
    private ConceptSpec destination;
    private boolean transitive;
    
    public RelSpec(ConceptSpec relType, ConceptSpec destination, boolean transitive) {
        super();
        this.relType = relType;
        this.destination = destination;
        this.transitive = transitive;
    }

    public ConceptSpec getRelType() {
        return relType;
    }

    public ConceptSpec getDestination() {
        return destination;
    }

    public boolean isTransitive() {
        return transitive;
    }
}
