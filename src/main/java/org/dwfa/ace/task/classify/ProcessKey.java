package org.dwfa.ace.task.classify;

public enum ProcessKey {

    SNOROCKET;
    
    public String getAttachmentKey() {
        return "A: " + this.name();
    }

}
