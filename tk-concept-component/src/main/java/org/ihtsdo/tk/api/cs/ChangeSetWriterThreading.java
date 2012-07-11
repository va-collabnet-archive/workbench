package org.ihtsdo.tk.api.cs;

import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;

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
  
   public static ChangeSetWriterThreading
           get(ChangeSetGenerationThreadingPolicy changeSetGenerationThreadingPolicy) {
    	switch (changeSetGenerationThreadingPolicy) {
		case SINGLE_THREAD:
			return SINGLE_THREAD;
		case MULTI_THREAD:
			return MULTI_THREAD;
		default:
			throw new UnsupportedOperationException("Can't handle csgtp: " + changeSetGenerationThreadingPolicy);
		}
    }

    public ChangeSetGenerationThreadingPolicy convert() {
    	switch (this) {
		case SINGLE_THREAD:
			return ChangeSetGenerationThreadingPolicy.SINGLE_THREAD;
		case MULTI_THREAD:
			return ChangeSetGenerationThreadingPolicy.MULTI_THREAD;
		default:
			throw new UnsupportedOperationException("Can't handle csgtp: " + this);
		}
    }

}
