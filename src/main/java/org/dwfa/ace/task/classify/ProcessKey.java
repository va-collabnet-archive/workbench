package org.dwfa.ace.task.classify;

public enum ProcessKey {

    SNOROCKET,ClassificationResultString;
    
    public String getAttachmentKey() {
        return "A: " + this.name();
    }

}
