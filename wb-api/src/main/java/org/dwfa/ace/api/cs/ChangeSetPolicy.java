package org.dwfa.ace.api.cs;

public enum ChangeSetPolicy {
    /**
     * Don't generate change sets. 
     */
    OFF("no changeset"), 
    /**
     * Only include changes that represent the sapNids from the current commit. 
     */
    INCREMENTAL("incremental changeset"),
    /**
     * Only include sapNids that are written to the mutable database. 
     */
    MUTABLE_ONLY("mutable-only changeset"),
    /**
     * Include all changes. 
     */
    COMPREHENSIVE("comprehensive changeset");
    ;

    String displayString;

    private ChangeSetPolicy(String displayString) {
        this.displayString = displayString;
    }

    @Override
    public String toString() {
        return displayString;
    }

}
