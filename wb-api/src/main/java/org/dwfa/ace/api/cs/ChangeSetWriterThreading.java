package org.dwfa.ace.api.cs;

public enum ChangeSetWriterThreading {
    SINGLE_THREAD("single threaded"), MULTI_THREAD("multi-threaded");

    String displayString;

    private ChangeSetWriterThreading(String displayString) {
        this.displayString = displayString;
    }

    @Override
    public String toString() {
        return displayString;
    }

}
